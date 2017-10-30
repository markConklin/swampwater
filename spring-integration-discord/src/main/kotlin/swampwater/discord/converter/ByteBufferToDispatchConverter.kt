package swampwater.discord.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream
import org.springframework.core.convert.converter.Converter
import swampwater.discord.Dispatch
import java.nio.ByteBuffer
import java.util.zip.InflaterInputStream

class ByteBufferToDispatchConverter(val objectMapper: ObjectMapper) : Converter<ByteBuffer, Dispatch> {
    override fun convert(source: ByteBuffer): Dispatch = InflaterInputStream(ByteBufferBackedInputStream(source)).use { s -> objectMapper.readValue(s, Dispatch::class.java) }
}
