package org.springframework.integration.discord.support

import org.springframework.messaging.support.MessageHeaderAccessor


operator fun MessageHeaderAccessor.set(name: String, value: Any?) = this.setHeader(name, value)

operator fun MessageHeaderAccessor.get(name: String): Any? = this.getHeader(name)

