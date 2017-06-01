package swampwater

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication.run
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows.from
import org.springframework.integration.dsl.channel.MessageChannels.queue
import org.springframework.integration.dsl.http.Http
import org.springframework.integration.scheduling.PollerMetadata
import org.springframework.integration.scheduling.PollerMetadata.DEFAULT_POLLER
import org.springframework.messaging.MessageChannel
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.scheduling.support.PeriodicTrigger
import org.springframework.web.util.UriComponentsBuilder.fromUriString
import swampwater.discord.CreateMessage
import swampwater.discord.GameStatusUpdate
import swampwater.discord.Gateway
import swampwater.discord.Op
import swampwater.discord.gateway.DiscordGatewayContainer
import swampwater.discord.gateway.DiscordGatewayMessageHandler
import swampwater.discord.gateway.DiscordGatewayMessageProducer
import swampwater.discord.gateway.DiscordMessageHeaderAccessor
import swampwater.discord.resource.RateLimitingInterceptor
import java.util.concurrent.Executors.newSingleThreadScheduledExecutor
import java.util.concurrent.TimeUnit.MILLISECONDS


@SpringBootApplication
@EnableIntegration
open class Application(
        builder: RestTemplateBuilder,
        val objectMapper: ObjectMapper,
        @Value("\${discord.baseUrl}") val baseUrl: String,
        @Value("Bot \${discord.authorization}") val authorization: String,
        @Value("\${discord.gateway.version}") version: String
) {

    private val restTemplate = builder
            .additionalInterceptors(ClientHttpRequestInterceptor { request, body, execution ->
                request.headers[AUTHORIZATION] = authorization
                execution.execute(request, body)
            }, RateLimitingInterceptor())
            .build()

    private val gatewayUrl: java.net.URI by lazy {
        fromUriString(restTemplate.getForObject("$baseUrl/gateway/bot", Gateway::class.java).url)
                .queryParam("encoding", "json")
                .queryParam("v", version)
                .build()
                .toUri()
    }

    @Bean
    open fun scheduler() = ConcurrentTaskScheduler(newSingleThreadScheduledExecutor())

    @Bean(DEFAULT_POLLER)
    open fun defaultPoller() = PollerMetadata().apply { trigger = PeriodicTrigger(10, MILLISECONDS) }

    @Bean
    open fun gatewayContainer() = DiscordGatewayContainer(gatewayUrl, authorization, scheduler())

    @Bean
    open fun outboundGatewayHandler() = DiscordGatewayMessageHandler(gatewayContainer())

    @Bean
    open fun inboundGatewayProducer() = DiscordGatewayMessageProducer().apply { gatewayContainer().eventHandler = this }

    @Bean
    open fun gatewayInboundFlow(): IntegrationFlow = from(inboundGatewayProducer())
            .route<Map<String, Any>, String>(
                    {
                        (it[DiscordMessageHeaderAccessor.EventType] ?: it[DiscordMessageHeaderAccessor.Op])?.toString()?.toCamelCase()
                    },
                    {
                        it
                                .channelMapping("ready", "discord.ready.inbound")
                                .subFlowMapping("messageCreate", { sf ->
                                    sf
                                            .enrichHeaders { he -> he.headerExpression("discord-channel", "payload.channelId") }
                                            .channel("discord.messageCreate.inbound")
                                })
                                .resolutionRequired(false)
                                .defaultOutputChannel("nullChannel")
                    }
            )
            .get()

    @Bean("discord.message.outbound")
    open fun discordMessageOutbound(): MessageChannel = queue().get()

    @Bean
    open fun messageOutboundFlow(): IntegrationFlow = from(discordMessageOutbound())
            .split()
            .transform(String::class.java, { CreateMessage(it) })
            .enrichHeaders(mutableMapOf(CONTENT_TYPE to APPLICATION_JSON_VALUE as Any))
            .handle(Http
                    .outboundChannelAdapter<CreateMessage>({ "$baseUrl/channels/${it.headers["discord-channel"]}/messages" }, restTemplate)
                    .get())
            .get()

    @Bean
    open fun statusUpdateFlow(): IntegrationFlow = from(Http
            .inboundGateway("/status")
            .requestPayloadType(SetStatusRequest::class.java)
            .requestMapping { it.methods(POST) }
            .get())
            .transform { it: SetStatusRequest -> GameStatusUpdate(it.idle, it.game) }
            .enrichHeaders(mutableMapOf(DiscordMessageHeaderAccessor.Op to Op.StatusUpdate as Any))
            .handle(outboundGatewayHandler())
            .get()

    @Bean
    open fun jokes(): List<Joke> = objectMapper.readValue(javaClass.getResourceAsStream("/jokes.json"), objectMapper.typeFactory.constructCollectionType(List::class.java, Joke::class.java))
}

fun String.beginWithUpperCase(): String = when (this.length) {
    0 -> ""
    1 -> this.toUpperCase()
    else -> this[0].toUpperCase() + this.substring(1)
}

fun String.toCamelCase() = this.toLowerCase().split('_').map { it.beginWithUpperCase() }.joinToString("").decapitalize()

fun main(vararg args: String) {
    run(Application::class.java, *args)
}
