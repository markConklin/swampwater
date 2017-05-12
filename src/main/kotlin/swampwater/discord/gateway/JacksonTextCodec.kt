package swampwater.discord.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import swampwater.discord.Dispatch
import javax.websocket.Decoder
import javax.websocket.Encoder
import javax.websocket.EndpointConfig

open class JacksonTextCodec : Encoder.Text<Dispatch>, Decoder.Text<Dispatch> {
    private lateinit var objectMapper: ObjectMapper

    override fun init(config: EndpointConfig) {
        objectMapper = config.userProperties.applicationContext!!.getBean(ObjectMapper::class.java)
    }

    override fun willDecode(s: String?): Boolean {
        return true
    }

    override fun decode(s: String?): Dispatch {
        return objectMapper.readValue(s, Dispatch::class.java)
    }

    override fun encode(`object`: Dispatch?): String {
        return objectMapper.writeValueAsString(`object`)
    }

    override fun destroy() {
    }
}
