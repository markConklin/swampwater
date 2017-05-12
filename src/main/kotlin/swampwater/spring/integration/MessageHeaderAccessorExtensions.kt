package swampwater.spring.integration

import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.support.MessageHeaderAccessor


operator fun MessageHeaderAccessor.set(name: String, value: Any?) = this.setHeader(name, value)

operator fun SimpMessageHeaderAccessor.get(name: String): Any? = this.getHeader(name)

