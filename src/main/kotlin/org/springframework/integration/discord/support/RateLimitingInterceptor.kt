package org.springframework.integration.discord.support

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus.TOO_MANY_REQUESTS
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.lang.Long.max
import java.time.Clock
import java.time.Instant.now
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

class RateLimitingInterceptor(private var clock: Clock = Clock.systemUTC()) : ClientHttpRequestInterceptor {
    private var global = now(clock).toEpochMilli()
    private val limits = ConcurrentHashMap<String, Long>()

    override fun intercept(request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution): ClientHttpResponse {
        val destination = request.uri.toString()
        while (true) {
            val milli = now(clock).toEpochMilli()
            (max(global, limits.getOrPut(destination, { milli })) - milli).let {
                if (it > 0) MILLISECONDS.sleep(it)
            }
            val response = execution.execute(request, body)
            if (response.statusCode == TOO_MANY_REQUESTS) {
                with(response.headers) {
                    val retry: Long = rateLimitReset!!
                    if (isRateLimitGlobal) {
                        global = retry
                    } else {
                        limits[destination] = retry
                    }
                }
            } else {
                if (response.headers.rateLimitRemaining == 0) {
                    limits[destination] = response.headers.rateLimitReset!!
                }
                return response
            }
        }
    }
}

val HttpHeaders.rateLimitReset: Long?
    get() = SECONDS.toMillis(this.getFirst("X-RateLimit-Reset")?.toLong() ?: 0)

val HttpHeaders.rateLimitRemaining: Int?
    get() = this.getFirst("X-RateLimit-Remaining")?.toInt()

val HttpHeaders.isRateLimitGlobal: Boolean
    get() = this.getFirst("X-RateLimit-Global")?.toBoolean() != null
