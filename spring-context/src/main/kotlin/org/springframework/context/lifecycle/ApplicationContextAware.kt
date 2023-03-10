package org.springframework.context.lifecycle

import org.springframework.context.ApplicationContext

interface ApplicationContextAware {

    fun setApplicationContext(context: ApplicationContext)
}
