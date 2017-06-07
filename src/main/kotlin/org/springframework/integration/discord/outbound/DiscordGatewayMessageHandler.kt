package org.springframework.integration.discord.outbound

import org.springframework.integration.discord.common.DiscordGatewayContainer
import org.springframework.integration.discord.support.DiscordMessageHeaderAccessor
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import swampwater.discord.Dispatch


open class DiscordGatewayMessageHandler(private val container: DiscordGatewayContainer) : MessageHandler {
    override fun handleMessage(message: Message<*>) {
        container.session!!.basicRemote.sendObject(Dispatch(DiscordMessageHeaderAccessor(message).op!!, message.payload))
    }
}