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

open class DiscordGatewayContainer(private val gatewayUrl: URI, val authorization: String, val scheduler: TaskScheduler, val clock: Clock = Clock.systemUTC()) : SmartLifecycle, Endpoint() {
    companion object {
        val logger: Log = getLog(DiscordGatewayContainer::class.java)
    }

    private var heartbeat: ScheduledFuture<*>? = null
    private var restarting: Boolean = false
    private var running: Boolean = false

    private lateinit var latch: CountDownLatch

    private val config = ClientEndpointConfig.Builder.create()
            .decoders(mutableListOf(DispatchBinaryDecoder::class.java, DispatchTextDecoder::class.java))
            .encoders(mutableListOf(DispatchTextEncoder::class.java as Class<out Encoder>))
            .build()

    var session: Session? = null
        get() {
            latch.await(10, SECONDS)
            return field
        }

    var eventHandler: DiscordGatewayMessageProducer? = null

    override fun onOpen(session: Session, config: EndpointConfig) {
        this.session = RateLimitingSession(session).apply {
            addMessageHandler(object : MessageHandler.Whole<Dispatch> {
                private var sequence: Int? = null
                private var sessionId: String? = null
                private var heartbeatAck: Boolean = true

                override fun onMessage(dispatch: Dispatch) {
                    sequence = dispatch.s
                    val (op, event) = dispatch
                    when (op) {
                        Op.Dispatch -> {
                            when (event) {
                                is Ready -> sessionId = event.sessionId
                            }
                            eventHandler?.onEvent(dispatch)
                        }
                        Op.Reconnect -> close()
                        Op.InvalidSession -> {
                            if (!(event as Boolean)) {
                                sequence = null
                                sessionId = null
                            }
                            close()
                        }
                        Op.Hello -> {
                            heartbeat = scheduler.scheduleAtFixedRate({
                                if (heartbeatAck) {
                                    heartbeatAck = false
                                    basicRemote.sendObject(Dispatch(Op.Heartbeat, sequence))
                                } else {
                                    close(missingHeartbeatACK)
                                }
                            }, Date.from(now(clock)), (event as Hello).heartbeatInterval)
                            val request = if (sessionId == null) {
                                Dispatch(Op.Identity, Identity(authorization))
                            } else {
                                Dispatch(Op.Resume, Resume(authorization, sessionId!!, sequence))
                            }
                            basicRemote.sendObject(request)
                        }
                        Op.HeartbeatAck -> heartbeatAck = true
                        else -> logger.warn("Unrecognized Op code $op")
                    }
                }
            })
        }
        latch.countDown()
    }

    override fun onError(session: Session?, throwable: Throwable?) {
        logger.error("error", throwable)
        if (restarting) scheduler.schedule(this::start, Date.from(now(clock)))
    }

    override fun onClose(session: Session?, closeReason: CloseReason?) {
        logger.info("closing $closeReason")
        heartbeat?.cancel(true)
        restarting = true
        scheduler.schedule(this::start, Date.from(now(clock)))
    }

    override fun start() {
        running = true
        latch = CountDownLatch(1)
        getWebSocketContainer().connectToServer(this, config, gatewayUrl)
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
