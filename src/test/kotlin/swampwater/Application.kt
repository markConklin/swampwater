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
import java.util.concurrent.Executors.newSingleThreadScheduledExecutor
import java.util.concurrent.TimeUnit.MILLISECONDS


@SpringBootApplication
@EnableIntegration
open class Application(
        builder: RestTemplateBuilder,
        val objectMapper: ObjectMapper,
        @Value("\${discord.baseUrl}") baseUrl: String,
        @Value("Bot \${discord.authorization}") val authorization: String,
        @Value("\${discord.gateway.version}") version: String
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
            .route<Any, String>({ p -> p.javaClass.name },
                    { m ->
                        m
                                .channelMapping(Ready::class.java.name, "discord.${Ready::class.java.simpleName.decapitalize()}.inbound")
                                .subFlowMapping(Message::class.java.name, { sf ->
                                    sf
                                            .enrichHeaders { he -> he.headerExpression("discord-channel", "payload.channelId") }
                                            .channel("discord.${Message::class.java.simpleName.decapitalize()}.inbound")
                                })
                                .resolutionRequired(false)
                                .defaultOutputChannel("nullChannel")
                    })
            .get()

    @Bean
    open fun outboundMessageHandler() = RateLimitingHttpMessageHandler(restTemplate).apply { url = "/channels/#{headers['discord-channel']}/messages" }

    @Bean("discord.message.outbound")
    open fun discordMessageOutbound(): MessageChannel = MessageChannels.queue().get()

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
        requestMapping = RequestMapping().apply {
            setMethods(POST)
            setPathPatterns("/status")
        }
    }

    @Bean
    open fun statusUpdateFlow(): IntegrationFlow = IntegrationFlows.from(httpStatusProducer())
            .transform { it: SetStatusRequest -> GameStatusUpdate(it.idle, it.game) }
            .enrichHeaders(mutableMapOf(DiscordMessageHeaderAccessor.Op to Op.StatusUpdate as Any))
            .handle(outboundGatewayHandler())
            .get()

    @Bean
    open fun jokes(): List<Joke> = objectMapper.readValue(javaClass.getResourceAsStream("/jokes.json"), objectMapper.typeFactory.constructCollectionType(List::class.java, Joke::class.java))

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            run(Application::class.java, *args)
        }
    }
}
