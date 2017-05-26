package swampwater

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import swampwater.discord.Channel
import swampwater.discord.DMChannel
import swampwater.discord.TextGuildChannel
import swampwater.discord.VoiceGuildChannel
import swampwater.discord.jackson.ObjectMapperCustomizer

@RunWith(JUnitPlatform::class)
class ChannelDeserializerSpek : Spek({
    given("Object Mapper with custom deserializer configuration") {
        val objectMapper: ObjectMapper = Jackson2ObjectMapperBuilder().apply {
            ObjectMapperCustomizer().customize(this)
        }.build()
        on("deserializing a DMChannel") {
            val channel: Channel = objectMapper.readValue("""
            {
                "id": "19238980",
                "recipient":{
                    "id": "9859034"
                    },
                "last_message_id": "124545",
                "is_private": true
            }""", Channel::class.java)
            it("should be of type DMChannel") {
                assertThat(channel).isInstanceOf(DMChannel::class.java)
            }
        }
        on("deserializing a TextGuildChannel") {
            val channel: Channel = objectMapper.readValue("""
            {
                "id": "19238980",
                "guild_id": "1029098",
                "name": "buffalo",
                "type": 0,
                "position": 2,
                "permission_overwrites": [],
                "last_message_id": "124545",
                "is_private": false
            }""", Channel::class.java)
            it("should be of type TextGuildChannel") {
                assertThat(channel).isInstanceOf(TextGuildChannel::class.java)
            }
        }
        on("deserializing a VoiceGuildChannel") {
            val channel: Channel = objectMapper.readValue("""
            {
                "id": "19238980",
                "guild_id": "1029098",
                "name": "buffalo",
                "type": 2,
                "position": 2,
                "permission_overwrites": [],
                "bit_rate": 124545,
                "user_limit": 13,
                "is_private": false
            }""", Channel::class.java)
            it("should be of type VoiceGuildChannel") {
                assertThat(channel).isInstanceOf(VoiceGuildChannel::class.java)
            }
        }
    }
})