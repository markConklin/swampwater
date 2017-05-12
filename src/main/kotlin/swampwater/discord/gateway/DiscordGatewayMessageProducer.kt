package swampwater.discord.gateway

import org.springframework.integration.endpoint.MessageProducerSupport
import swampwater.discord.Dispatch


open class DiscordGatewayMessageProducer : MessageProducerSupport() {
    open fun onEvent(dispatch: Dispatch) {
        sendMessage(messageBuilderFactory.withPayload(dispatch.d).copyHeaders(DiscordMessageHeaderAccessor(dispatch).toMessageHeaders()).build())
    }
}