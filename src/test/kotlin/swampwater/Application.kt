package swampwater

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication.run
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod.POST
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.IntegrationFlows.from
import org.springframework.integration.dsl.channel.MessageChannels
import org.springframework.integration.dsl.support.Transformers.toJson
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway
import org.springframework.integration.http.inbound.RequestMapping
import org.springframework.integration.scheduling.PollerMetadata
import org.springframework.integration.scheduling.PollerMetadata.DEFAULT_POLLER
import org.springframework.messaging.MessageChannel
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.scheduling.support.PeriodicTrigger
import org.springframework.web.util.UriComponentsBuilder.fromUriString
import swampwater.discord.*
import swampwater.discord.gateway.DiscordGatewayContainer
import swampwater.discord.gateway.DiscordGatewayMessageHandler
import swampwater.discord.gateway.DiscordGatewayMessageProducer
import swampwater.discord.gateway.DiscordMessageHeaderAccessor
import swampwater.discord.resource.RateLimitingHttpMessageHandler
import java.time.Clock
import java.util.concurrent.Executors.newScheduledThreadPool


@SpringBootApplication
@EnableIntegration
open class Application(
        builder: RestTemplateBuilder,
        val objectMapper: ObjectMapper,
        @Value("\${discord.baseUrl}") val baseUrl: String,
        @Value("Bot \${discord.authorization}") val authorization: String,
        @Value("\${discord.gateway.version}") val version: String
) {

    private val restTemplate = builder
            .rootUri(baseUrl)
            .interceptors(ClientHttpRequestInterceptor { request, body, execution ->
                request.headers[AUTHORIZATION] = authorization
                execution.execute(request, body)
            })
            .build()

    private val gatewayUrl: java.net.URI by lazy {
        fromUriString(restTemplate.getForObject("/gateway/bot", Gateway::class.java).url)
                .queryParam("encoding", "json")
                .queryParam("v", version)
                .build()
                .toUri()
    }

    @Bean
    open fun clock(): Clock = Clock.systemUTC()

    @Bean
    open fun scheduler() = ConcurrentTaskScheduler(newScheduledThreadPool(1))

    @Bean(DEFAULT_POLLER)
    open fun defaultPoller() = PollerMetadata().apply { trigger = PeriodicTrigger(10) }

    @Bean
    open fun gatewayContainer() = DiscordGatewayContainer(gatewayUrl, authorization, scheduler())

    @Bean
    open fun outboundGatewayHandler() = DiscordGatewayMessageHandler(gatewayContainer())

    @Bean
    open fun gatewayOutboundFlow(): IntegrationFlow = from("discord.gateway.outbound")
            .handle(outboundGatewayHandler())
            .get()

    @Bean
    open fun inboundMessageProducer() = DiscordGatewayMessageProducer().apply { gatewayContainer().eventHandler = this }

    @Bean
    open fun gatewayInboundFlow(): IntegrationFlow = from(inboundMessageProducer())
            .route<Any, String>({ p -> p.javaClass.name },
                    { m ->
                        m
                                .channelMapping(Ready::class.java.name, "discord.${Ready::class.java.simpleName.decapitalize()}.inbound")
                                .subFlowMapping(Message::class.java.name, { sf ->
                                    sf
                                            .enrichHeaders { he -> he.headerExpression("channel", "payload.channelId") }
                                            .channel("discord.${Message::class.java.simpleName.decapitalize()}.inbound")
                                })
                                .resolutionRequired(false)
                                .defaultOutputChannel("nullChannel")
                    })
            .get()

    @Bean
    open fun outboundMessageHandler() = RateLimitingHttpMessageHandler(restTemplate).apply { url = "/channels/#{headers['channel']}/messages" }

    @Bean("discord.message.outbound")
    open fun discordMessageOutbound() = QueueChannel()

    @Bean
    open fun messageOutboundFlow(): IntegrationFlow = from(discordMessageOutbound())
            .split()
            .transform(String::class.java, { CreateMessage(it) })
            .transform(toJson())
            .handle(outboundMessageHandler())
            .get()

    @Bean
    open fun httpStatusProducer() = HttpRequestHandlingMessagingGateway(false).apply {
        setRequestPayloadType(SetStatusRequest::class.java)
        requestChannel = statusChannel()
        requestMapping = RequestMapping().apply {
            setMethods(POST)
            setPathPatterns("/status")
        }
    }

    @Bean
    open fun statusChannel(): MessageChannel = MessageChannels.direct().get()

    @Bean
    open fun statusUpdateFlow(): IntegrationFlow = IntegrationFlows.from(statusChannel())
            .transform { it: SetStatusRequest -> GameStatusUpdate(it.idle, it.game) }
            .enrichHeaders(mutableMapOf(DiscordMessageHeaderAccessor.Op to Op.StatusUpdate as Any))
            .handle(outboundGatewayHandler())
            .get()

    @Bean
    open fun jokes(): MutableList<Joke> = objectMapper.readValue(javaClass.getResourceAsStream("/jokes.json"), objectMapper.typeFactory.constructCollectionType(MutableList::class.java, Joke::class.java))
}

fun main(vararg args: String) {
    run(Application::class.java, *args)
}