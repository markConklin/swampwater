package swampwater.discord.gateway

import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import swampwater.discord.Dispatch


open class DiscordGatewayMessageHandler(private val container: DiscordGatewayContainer) : MessageHandler {
    override fun handleMessage(message: Message<*>) {
        container.send(Dispatch(DiscordMessageHeaderAccessor(message).op!!, message.payload))
    }
}