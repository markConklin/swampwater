package swampwater

import org.springframework.integration.annotation.Router
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.stereotype.Component
import swampwater.discord.Message
import swampwater.discord.Ready
import swampwater.discord.User
import java.util.concurrent.ThreadLocalRandom

@Component
open class MessageListener(val gateway: DiscordGateway, val jokes: MutableList<Joke>) {
    private lateinit var self: User

    @Router(inputChannel = "discord.gateway.inbound", suffix = ".inbound")
    fun route(message: org.springframework.messaging.Message<*>): String {
        val payload: Any = message.payload
        return when (payload) {
            is Message -> if (Regex("^tell me a joke").matches(payload.content)) "joke" else "ack"
            is Ready -> "ready"
            else -> "nullChannel"
        }
    }

    @ServiceActivator(inputChannel = "joke.inbound")
    fun joke(message: Message) {
        if (message.author.id == self.id) return
        jokes[ThreadLocalRandom.current().nextInt(jokes.size)].apply {
            gateway.sendMessage(message.channelId, setup, punchline)
        }
    }

    @ServiceActivator(inputChannel = "ack.inbound")
    fun ack(message: Message) {
        if (message.author.id == self.id) return
        gateway.sendMessage(message.channelId, "\"${message.content}\" received\n")
    }

    @ServiceActivator(inputChannel = "ready.inbound")
    fun ready(ready: Ready) {
        self = ready.user
    }
}
