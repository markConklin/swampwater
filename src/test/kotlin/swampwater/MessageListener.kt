package swampwater

import org.springframework.integration.annotation.Filter
import org.springframework.integration.annotation.Router
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.stereotype.Component
import swampwater.discord.Message
import swampwater.discord.Ready
import swampwater.discord.User
import java.util.concurrent.ThreadLocalRandom

@Component
open class MessageListener(val jokes: List<Joke>) {
    private lateinit var self: User

    @Filter(inputChannel = "discord.message.inbound", outputChannel = "event.inbound")
    fun filter(message: Message) = message.author.id != self.id

    @Router(inputChannel = "event.inbound")
    fun route(message: Message) = if (Regex("^tell me a joke").matches(message.content)) "joke.inbound" else "ack.inbound"

    @ServiceActivator(inputChannel = "joke.inbound", outputChannel = "discord.message.outbound")
    fun joke(message: Message) = jokes[ThreadLocalRandom.current().nextInt(jokes.size)].let { listOf(it.setup, it.punchline) }

    @ServiceActivator(inputChannel = "ack.inbound", outputChannel = "discord.message.outbound")
    fun ack(message: Message) = "\"${message.content}\" received\n"

    @ServiceActivator(inputChannel = "discord.ready.inbound")
    fun ready(ready: Ready) {
        self = ready.user
    }
}
