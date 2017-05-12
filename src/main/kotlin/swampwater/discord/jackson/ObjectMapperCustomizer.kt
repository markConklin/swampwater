package swampwater.discord.jackson

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Component

@Component
open class ObjectMapperCustomizer : Jackson2ObjectMapperBuilderCustomizer {
    override fun customize(builder: Jackson2ObjectMapperBuilder) {
        builder
                .propertyNamingStrategy(SNAKE_CASE)
                .featuresToEnable(WRITE_DATES_AS_TIMESTAMPS)
                .findModulesViaServiceLoader(true)
    }
}