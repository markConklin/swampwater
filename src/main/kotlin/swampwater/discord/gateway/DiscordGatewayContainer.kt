package swampwater.discord.gateway

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory.getLog
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.SmartLifecycle
import org.springframework.scheduling.TaskScheduler
import swampwater.discord.*
import java.net.URI
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS
import javax.websocket.*
import javax.websocket.ContainerProvider.getWebSocketContainer

open class DiscordGatewayContainer(val gatewayUrl: URI, val authorization: String, val scheduler: TaskScheduler) : SmartLifecycle, ApplicationContextAware, Endpoint() {
    companion object {
        val missingHeartbeatACK = CloseReason(CloseReason.CloseCodes.getCloseCode(4000), "Missing Heartbeat ACK")
        val logger: Log = getLog(DiscordGatewayContainer::class.java)
    }

    private val sendLimiter = Limiter(120L, 1L, MINUTES)
    private var sequence: Int? = null
    private var sessionId: String? = null
    private var heartbeat: ScheduledFuture<*>? = null
    private var heartbeatAck: Boolean = true
    private var restarting: Boolean = false
    private var running: Boolean = false

    private lateinit var latch: CountDownLatch

    private val config = ClientEndpointConfig.Builder.create()
            .decoders(mutableListOf(JacksonBinaryDecoder::class.java, JacksonTextCodec::class.java))
            .encoders(mutableListOf(JacksonTextCodec::class.java as Class<out Encoder>))
            .build()

    var session: Session? = null
        get() {
            latch.await(10, SECONDS)
            return field
        }

    var eventHandler: DiscordGatewayMessageProducer? = null

    override fun onOpen(session: Session, config: EndpointConfig) {
        this.session = session
        latch.countDown()
        session.addMessageHandler(MessageHandler.Whole<Dispatch> { dispatch ->
            sequence = dispatch.s
            val (op, event) = dispatch
            when (op) {
                Op.Dispatch -> {
                    when (event) {
                        is Ready -> sessionId = event.sessionId
                    }
                    eventHandler?.onEvent(dispatch)
                }
                Op.Reconnect -> session.close()
                Op.InvalidSession -> {
                    if (!(event as Boolean)) {
                        sequence = null
                        sessionId = null
                    }
                    session.close()
                }
                Op.Hello -> {
                    heartbeat = scheduler.scheduleAtFixedRate({
                        if (heartbeatAck) {
                            heartbeatAck = false
                            send(Dispatch(Op.Heartbeat, sequence))
                        } else {
                            session.close(missingHeartbeatACK)
                        }
                    }, Date(), (event as Hello).heartbeatInterval)
                    val request = if (sessionId == null) {
                        Dispatch(Op.Identity, Identity(authorization))
                    } else {
                        Dispatch(Op.Resume, Resume(authorization, sessionId!!, sequence))
                    }
                    send(request)
                }
                Op.HeartbeatAck -> heartbeatAck = true
                else -> logger.warn("Unrecognized Op code $op")
            }
        })
    }

    override fun onError(session: Session?, throwable: Throwable?) {
        logger.error("error", throwable)
        if (restarting) scheduler.schedule(this::start, Date())
    }

    override fun onClose(session: Session?, closeReason: CloseReason?) {
        logger.info("closing $closeReason")
        heartbeat?.cancel(true)
        restarting = true
        scheduler.schedule(this::start, Date())
    }

    override fun start() {
        running = true
        latch = CountDownLatch(1)
        getWebSocketContainer().connectToServer(this, config, gatewayUrl)
    }

    override fun isRunning(): Boolean = running

    override fun isAutoStartup(): Boolean = true

    override fun stop(callback: Runnable) {
        stop()
        callback.run()
    }

    override fun stop() {
        session?.close()
        running = false
    }

    override fun getPhase(): Int = Int.MAX_VALUE

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        config.userProperties.applicationContext = applicationContext
    }

    fun send(dispatch: Dispatch) {
        sendLimiter.submit { session!!.basicRemote.sendObject(dispatch) }
    }
}
