package org.springframework.context.core

import kotlin.reflect.KClass

data class BeanDefinition(
    val clazz: KClass<*>,
    val scope: ScopeStrategy,
    val beanName: String,
    val isInitializingBean: Boolean,
)
