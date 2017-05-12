package swampwater

import org.springframework.integration.annotation.Gateway
import org.springframework.integration.annotation.GatewayHeader
import org.springframework.integration.annotation.MessagingGateway
import org.springframework.messaging.handler.annotation.Header
import swampwater.discord.CreateMessage
import swampwater.discord.GameStatusUpdate

@MessagingGateway
interface DiscordGateway {
    @Gateway(requestChannel = "discord.message.outbound")
    fun sendMessage(@Header("channel") channel: String, content: CreateMessage)

    @Gateway(requestChannel = "discord.message.outbound")
    fun sendMessage(@Header("channel") channel: String, vararg content: String)

    @Gateway(requestChannel = "discord.gateway.outbound", headers = arrayOf(GatewayHeader(name = "discord-op", expression = "T(swampwater.discord.Op).StatusUpdate")))
    fun setStatus(update: GameStatusUpdate)
}