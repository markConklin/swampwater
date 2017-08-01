package org.springframework.integration.discord.common

import com.google.common.util.concurrent.RateLimiter
import javax.websocket.RemoteEndpoint
import javax.websocket.Session

class RateLimitingSession(session: Session) : Session by session {
    companion object {
        val rateLimiter: RateLimiter = RateLimiter.create(2.0)
    }

    private val basicRemote = RateLimitingBasicRemote(rateLimiter, session.basicRemote)

    override fun getBasicRemote(): RemoteEndpoint.Basic = basicRemote

    class RateLimitingBasicRemote(val limiter: RateLimiter, val remote: RemoteEndpoint.Basic) : RemoteEndpoint.Basic by remote {
        @Synchronized
        override fun sendObject(data: Any?) {
            limiter.acquire()
            remote.sendObject(data)
        }
    }
}
