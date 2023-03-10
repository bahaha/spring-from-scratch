package org.springframework.context.lifecycle

interface BeanPostProcessor {

    fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        return bean
    }

    fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        return bean
    }
}
