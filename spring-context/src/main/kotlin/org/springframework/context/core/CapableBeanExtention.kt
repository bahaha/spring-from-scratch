package org.springframework.context.core

import org.springframework.context.lifecycle.ApplicationContextAware
import org.springframework.context.lifecycle.BeanPostProcessor
import org.springframework.context.lifecycle.InitializingBean
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

fun KClass<*>.hasAwareApplicationContext(): Boolean = isSubclassOf(ApplicationContextAware::class)
fun KClass<*>.isInitializingBean(): Boolean = isSubclassOf(InitializingBean::class)
fun KClass<*>.isPostProcessor(): Boolean = isSubclassOf(BeanPostProcessor::class)
