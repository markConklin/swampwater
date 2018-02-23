package org.springframework.integration.discord.common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory.getLog
import org.springframework.context.SmartLifecycle
import org.springframework.integration.discord.inbound.DiscordGatewayMessageProducer
import org.springframework.integration.discord.support.ApplicationContextProvider
import org.springframework.scheduling.TaskScheduler
import org.springframework.web.socket.adapter.standard.ConvertingEncoderDecoderSupport
import swampwater.discord.*
import java.net.URI
import java.time.Clock
import java.time.Instant.now
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.SECONDS
import javax.websocket.*
import javax.websocket.ContainerProvider.getWebSocketContainer

@ClientEndpoint(
        decoders = [(DispatchBinaryDecoder::class), (DispatchTextDecoder::class)],
        encoders = [(DispatchTextEncoder::class)]
)
open class DiscordGatewayContainer(
        private val gatewayUrl: URI,
        private val authorization: String,
        private val scheduler: TaskScheduler,
        private val clock: Clock = Clock.systemUTC()
) : SmartLifecycle {

    companion object {
        val logger: Log = getLog(DiscordGatewayContainer::class.java)
    }

    private var heartbeat: ScheduledFuture<*>? = null
    private var restarting: Boolean = false
    private var running: Boolean = false

    private lateinit var latch: CountDownLatch

    var session: Session? = null
        get() {
            latch.await(10, SECONDS)
            return field
        }
        set(value) {
            field = value
            latch.countDown()
        }

    var eventHandler: DiscordGatewayMessageProducer? = null

    @OnOpen
    fun onOpen(session: Session) {
        this.session = RateLimitingSession(session)
    }

    private var sequence: Int? = null
    private var sessionId: String? = null
    private var heartbeatAck: Boolean = true

    @OnMessage
    fun apply(dispatch: Dispatch) {
        sequence = dispatch.s
        val (op, event) = dispatch
        when (op) {
            Op.Dispatch -> {
                when (event) {
                    is Ready -> sessionId = event.sessionId
                }
                eventHandler?.onEvent(dispatch)
            }
            Op.Reconnect -> session!!.close()
            Op.InvalidSession -> {
                if (!(event as Boolean)) {
                    sequence = null
                    sessionId = null
                }
                session!!.close()
            }
            Op.Hello -> {
                heartbeat = scheduler.scheduleAtFixedRate({
                    if (heartbeatAck) {
                        heartbeatAck = false
                        session!!.basicRemote.sendObject(Dispatch(Op.Heartbeat, sequence))
                    } else {
                        session!!.close(missingHeartbeatACK)
                    }
                }, Date.from(now(clock)), (event as Hello).heartbeatInterval)
                val request = if (sessionId == null) {
                    Dispatch(Op.Identity, Identity(authorization))
                } else {
                    Dispatch(Op.Resume, Resume(authorization, sessionId!!, sequence))
                }
                session!!.basicRemote.sendObject(request)
            }
            Op.HeartbeatAck -> heartbeatAck = true
            else -> logger.warn("Unrecognized Op code $op")
        }
    }

    @OnError
    fun onError(throwable: Throwable?) {
        logger.error("error", throwable)
        if (restarting) scheduler.schedule(this::start, Date.from(now(clock)))
    }

    @OnClose
    fun onClose(closeReason: CloseReason?) {
        logger.info("closing $closeReason")
        heartbeat?.cancel(true)
        restarting = true
        scheduler.schedule(this::start, Date.from(now(clock)))
    }

    override fun start() {
        running = true
        latch = CountDownLatch(1)
        getWebSocketContainer().connectToServer(this, gatewayUrl)
    }

    override fun isRunning() = running

    override fun isAutoStartup() = true

    override fun stop(callback: Runnable) {
        stop()
        callback.run()
    }

    override fun stop() {
        session?.close()
        running = false
    }

    override fun getPhase() = Int.MAX_VALUE
}

class DispatchBinaryDecoder : ConvertingEncoderDecoderSupport.BinaryDecoder<Dispatch>() {
    override fun getApplicationContext() = ApplicationContextProvider.context
}

class DispatchTextDecoder : ConvertingEncoderDecoderSupport.TextDecoder<Dispatch>() {
    override fun getApplicationContext() = ApplicationContextProvider.context
}

class DispatchTextEncoder : ConvertingEncoderDecoderSupport.TextEncoder<Dispatch>() {
    override fun getApplicationContext() = ApplicationContextProvider.context
}

val missingHeartbeatACK = CloseReason(CloseReason.CloseCodes.getCloseCode(4000), "Missing Heartbeat ACK")
