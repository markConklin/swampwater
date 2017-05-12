package swampwater.discord.gateway

import org.springframework.context.ApplicationContext


var MutableMap<String, Any>.applicationContext: ApplicationContext?
    get() = this["applicationContext"] as ApplicationContext
    set(value) {
        this["applicationContext"] = value as Any
    }
