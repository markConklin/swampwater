package swampwater.discord.converter

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.convert.converter.Converter
import swampwater.discord.Dispatch

class StringToDispatchConverter(val objectMapper: ObjectMapper) : Converter<String, Dispatch> {
    override fun convert(source: String): Dispatch = objectMapper.readValue(source, Dispatch::class.java)
}
