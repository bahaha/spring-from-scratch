package org.springframework.context.factory

import org.springframework.context.ApplicationContext
import org.springframework.context.annotations.Autowired
import org.springframework.context.annotations.Component
import org.springframework.context.lifecycle.ApplicationContextAware
import org.springframework.context.lifecycle.BeanPostProcessor
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

@Component
class AutowiredBeanPostProcessor : BeanPostProcessor, ApplicationContextAware {
    var context: ApplicationContext? = null

    override fun setApplicationContext(context: ApplicationContext) {
        this.context = context
    }

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        check(context != null) { "No application context found." }

        val dependencies = bean::class.declaredMemberProperties
            .filterIsInstance<KMutableProperty1<Any, Any>>()
            .filter { it.javaField?.annotations?.any { annotation -> annotation is Autowired } == true }

        dependencies.forEach { dependency ->
            val annotation = dependency.javaField?.getAnnotation(Autowired::class.java)
            val dependencyBeanName =
                annotation?.beanName?.takeIf { it.isNotBlank() } ?: dependency.name
            try {
                dependency.setter.call(bean, context?.getBean(dependencyBeanName))
            } catch (_: NullPointerException) {
            }
        }

        return bean
    }
}
