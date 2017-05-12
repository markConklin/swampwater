package swampwater.discord.jackson

import com.fasterxml.jackson.databind.module.SimpleModule
import swampwater.discord.Dispatch
import swampwater.discord.jackson.deserializer.DispatchDeserializer
import swampwater.discord.jackson.mixin.DispatchMixin

class DiscordJacksonModule : SimpleModule() {
    init {
        addDeserializer(Dispatch::class.java, DispatchDeserializer)
        setMixInAnnotation(Dispatch::class.java, DispatchMixin::class.java)
    }
}