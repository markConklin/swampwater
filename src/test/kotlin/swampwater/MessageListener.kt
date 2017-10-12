package swampwater

import org.springframework.integration.annotation.Filter
import org.springframework.integration.annotation.Router
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
open class MessageListener(private val jokes: List<Joke>) {
    var self: String? = null
        @ServiceActivator(inputChannel = "discord.ready.inbound")
        set(@Payload("user.id") value) {
            field = value
        }

    @Filter(inputChannel = "discord.messageCreate.inbound", outputChannel = "event.inbound")
    fun filter(@Payload("author.id") author: String) = author != self!!

    @Router(inputChannel = "event.inbound", suffix = ".inbound")
    fun route(@Payload("content") content: String) = if (Regex("^tell me a joke").matches(content)) "joke" else "ack"

    @ServiceActivator(inputChannel = "joke.inbound", outputChannel = "discord.message.outbound")
    fun joke() = jokes.random().toList()

    @ServiceActivator(inputChannel = "ack.inbound", outputChannel = "discord.message.outbound")
    fun ack(@Payload("content") content: String) = "\"$content\" received\n"
}
