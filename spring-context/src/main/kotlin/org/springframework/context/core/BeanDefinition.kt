package org.springframework.context.core

data class BeanDefinition(
    val clazz: Class<*>,
    val scope: ScopeStrategy,
    val beanName: String,
)
