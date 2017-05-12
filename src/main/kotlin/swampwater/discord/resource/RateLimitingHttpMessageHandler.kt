package swampwater.discord.resource

import org.springframework.expression.Expression
import org.springframework.expression.common.TemplateParserContext
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus.TOO_MANY_REQUESTS
import org.springframework.http.RequestEntity
import org.springframework.integration.expression.ExpressionUtils.createStandardEvaluationContext
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler
import org.springframework.integration.http.support.DefaultHttpHeaderMapper
import org.springframework.messaging.Message
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.lang.Long.max
import java.time.Clock
import java.time.Instant.now
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS


open class RateLimitingHttpMessageHandler(val restTemplate: RestTemplate, var clock: Clock = Clock.systemUTC()) : AbstractReplyProducingMessageHandler() {
    var url: String? = null
        set(value) {
            field = value
            urlExpression = parser.parseExpression(value, context)
        }
    var urlExpression: Expression? = null

    val method = POST

    private var global = now(clock).toEpochMilli()
    private val limits = ConcurrentHashMap<String, Long>()

    private lateinit var evaluationContext: StandardEvaluationContext
    private val parser = SpelExpressionParser()
    private val context = TemplateParserContext()
    private val headerMapper = DefaultHttpHeaderMapper.outboundMapper()

    override fun doInit() {
        evaluationContext = createStandardEvaluationContext(beanFactory)
        requiresReply = false
    }

    override fun handleRequestMessage(message: Message<*>): Any? {
        val requestHeaders = HttpHeaders()
        headerMapper.fromHeaders(message.headers, requestHeaders)

        val destination: String = urlExpression!!.getValue(evaluationContext, message, String::class.java)
        val uri = restTemplate.uriTemplateHandler.expand(destination)
        val request = RequestEntity<Any>(message.payload, requestHeaders, method, uri)

        var result: Any? = null
        while (result == null) {
            (max(global, limits.getOrPut(destination, { now(clock).toEpochMilli() })) - now(clock).toEpochMilli()).let {
                if (it > 0) MILLISECONDS.sleep(it)
            }
            try {
                val response = restTemplate.exchange(request, String::class.java)
                result = response.headers.apply {
                    if (rateLimitRemaining == 0) {
                        limits[destination] = rateLimitReset!!
                    }
                    messageBuilderFactory
                            .withPayload(response.body)
                            .copyHeaders(headerMapper.toHeaders(this))
                            .build()
                }
            } catch(e: HttpClientErrorException) {
                if (e.statusCode != TOO_MANY_REQUESTS) throw e
                with(e.responseHeaders) {
                    val retry: Long = rateLimitReset!!
                    if (isRateLimitGlobal) {
                        global = retry
                    } else {
                        limits[destination] = retry
                    }
                }
            }
        }
        return if (requiresReply) result else null
    }
}

val HttpHeaders.rateLimitReset: Long?
    get() = SECONDS.toMillis(this.getFirst("X-RateLimit-Reset")?.toLong() ?: 0)

val HttpHeaders.rateLimitRemaining: Int?
    get() = this.getFirst("X-RateLimit-Remaining")?.toInt()

val HttpHeaders.isRateLimitGlobal: Boolean
    get() = this.getFirst("X-RateLimit-Global")?.toBoolean() != null
