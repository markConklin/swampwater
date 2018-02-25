package swampwater.discord.jackson.mixin

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import swampwater.discord.TextGuildChannel
import swampwater.discord.VoiceGuildChannel

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = TextGuildChannel::class, name = "0"),
        JsonSubTypes.Type(value = VoiceGuildChannel::class, name = "2")
)
interface GuildChannelMixin
