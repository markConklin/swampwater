package org.springframework.integration.discord.support

import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageHeaderAccessor
import swampwater.discord.Dispatch
import swampwater.discord.Op


class DiscordMessageHeaderAccessor : MessageHeaderAccessor {
    companion object {
        val Op = "discord-op"
        val EventType = "discord-type"
        val Sequence = "discord-sequence"
        val SessionId = "discord-session-id"
    }

    constructor(dispatch: Dispatch) {
        op = dispatch.op
        type = dispatch.t
        sequence = dispatch.s
    }

    constructor(message: Message<*>) : super(message)

    var op: Op?
        get() = getHeader(Op) as Op?
        set(value) {
            setHeader(Op, value)
        }

    var type: String?
        get() = getHeader(EventType) as String?
        set(value) {
            setHeader(EventType, value)
        }

    var sequence: Int?
        get() = getHeader(Sequence) as Int?
        set(value) {
            setHeader(Sequence, value)
        }

    var session: String?
        get() = getHeader(SessionId) as String?
        set(value) {
            setHeader(SessionId, value)
        }
}