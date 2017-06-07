package org.springframework.integration.discord.inbound

import org.springframework.integration.endpoint.MessageProducerSupport
import swampwater.discord.Dispatch
import org.springframework.integration.discord.support.DiscordMessageHeaderAccessor


open class DiscordGatewayMessageProducer : MessageProducerSupport() {
    open fun onEvent(dispatch: Dispatch) {
        sendMessage(
                messageBuilderFactory
                        .withPayload(dispatch.d)
                        .copyHeaders(DiscordMessageHeaderAccessor(dispatch).toMessageHeaders())
                        .build()
        )
    }
}