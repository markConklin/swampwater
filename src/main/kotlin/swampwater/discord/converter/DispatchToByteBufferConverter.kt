package swampwater.discord.converter

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.convert.converter.Converter
import swampwater.discord.Dispatch
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.DeflaterOutputStream

class DispatchToByteBufferConverter(val objectMapper: ObjectMapper) : Converter<Dispatch, ByteBuffer> {
    override fun convert(source: Dispatch): ByteBuffer {
        val result = ByteArrayOutputStream()
        DeflaterOutputStream(result).use { s -> objectMapper.writeValue(s, source) }
        return ByteBuffer.wrap(result.toByteArray())
    }
}
