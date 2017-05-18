package swampwater

import org.springframework.integration.annotation.Filter
import org.springframework.integration.annotation.Router
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import swampwater.discord.User
import java.util.concurrent.ThreadLocalRandom

@Component
open class MessageListener(val jokes: List<Joke>) {
    var self: User? = null
        @ServiceActivator(inputChannel = "discord.ready.inbound")
        set(@Payload("user") value) {
            field = value
        }

    @Filter(inputChannel = "discord.message.inbound", outputChannel = "event.inbound")
    fun filter(@Payload("author") author: User) = author.id != self?.id

    @Router(inputChannel = "event.inbound", suffix = ".inbound")
    fun route(@Payload("content") content: String) = if (Regex("^tell me a joke").matches(content)) "joke" else "ack"

    @ServiceActivator(inputChannel = "joke.inbound", outputChannel = "discord.message.outbound")
    fun joke() = jokes[ThreadLocalRandom.current().nextInt(jokes.size)].toList()

    @ServiceActivator(inputChannel = "ack.inbound", outputChannel = "discord.message.outbound")
    fun ack(@Payload("content") content: String) = "\"$content\" received\n"
}
