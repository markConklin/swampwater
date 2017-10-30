package org.springframework.integration.discord.support

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component("applicationContextProvider")
open class ApplicationContextProvider : ApplicationContextAware {
    companion object {
        lateinit var context: ApplicationContext
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }
}
