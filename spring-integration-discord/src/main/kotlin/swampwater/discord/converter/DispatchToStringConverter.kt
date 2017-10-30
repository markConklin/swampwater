package swampwater.discord.converter

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.convert.converter.Converter
import swampwater.discord.Dispatch


class DispatchToStringConverter(val objectMapper: ObjectMapper) : Converter<Dispatch, String> {
    override fun convert(source: Dispatch): String = objectMapper.writeValueAsString(source)
}
