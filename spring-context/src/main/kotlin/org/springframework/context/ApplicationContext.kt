package org.springframework.context

import org.springframework.context.annotations.Component
import org.springframework.context.annotations.ComponentScan
import org.springframework.context.annotations.Scope
import org.springframework.context.core.*
import org.springframework.context.lifecycle.ApplicationContextAware
import org.springframework.context.lifecycle.BeanPostProcessor
import org.springframework.context.lifecycle.InitializingBean
import java.io.File
import java.net.URLDecoder
import java.util.*
import kotlin.reflect.KClass
import kotlin.streams.asStream

class ApplicationContext(clazz: KClass<*>) {
    lateinit var scanRoot: String
    var cachedBeanDefinitions = mutableMapOf<String, BeanDefinition>()
    private val singletonDependenciesPool = mutableMapOf<String, Any>()
    private val beanPostProcessors = LinkedList<BeanPostProcessor>()
    private val classLoader = clazz.java.classLoader

    init {
        scan(clazz)
        cachedBeanDefinitions.values
            .filter { it.scope == ScopeStrategy.Singleton }
            .associate { it.beanName to createBean(it) }
            .let { singletonDependenciesPool.putAll(it) }
    }

    private fun scan(clazz: KClass<*>) {
        val scanAnnotation =
            clazz.annotations.find { it.annotationClass == ComponentScan::class } as ComponentScan
        scanRoot = scanAnnotation.path.ifBlank { clazz.java.`package`.name }

        val rootDirectoryToScan = scanRoot.replace(".", "/")
        val resources = classLoader.resources(rootDirectoryToScan)
        val classFiles = resources
            .map { URLDecoder.decode(it.file, Charsets.UTF_8) }
            .map { File(it) }
            .filter { it.isDirectory }
            .flatMap { root ->
                root.walkBottomUp()
                    .filter { file -> file.isFile && file.extension == "class" }
                    .asStream()
            }

        val classNames = classFiles
            .map { it.absolutePath }
            .map { it.substringAfter(rootDirectoryToScan).substringBeforeLast(".") }
            .map { it.replace("/", ".") }
            .map { "$scanRoot$it" }

        val beanCandidates = classNames
            .map { classLoader.loadClass(it) }
            .filter { it.isAnnotationPresent(Component::class.java) }
            .toList()

        beanCandidates.map { componentBean ->
            val beanClazz = componentBean.kotlin
            val scope = componentBean?.takeIf { it.isAnnotationPresent(Scope::class.java) }
                ?.getAnnotation(Scope::class.java)?.strategy
                ?: ScopeStrategy.Singleton
            val beanName =
                componentBean.getAnnotation(Component::class.java)?.beanName?.takeIf { it.isNotBlank() }
                    ?: componentBean.simpleName.replaceFirstChar { it.lowercase() }
            BeanDefinition(
                clazz = componentBean.kotlin,
                scope = scope,
                beanName = beanName,
                hasAwareApplicationContext = beanClazz.hasAwareApplicationContext(),
                isInitializingBean = beanClazz.isInitializingBean(),
                isPostProcessor = beanClazz.isPostProcessor(),
            )
        }
            .associateBy(BeanDefinition::beanName)
            .let { cachedBeanDefinitions.putAll(it) }

        cachedBeanDefinitions.values
            .filter { it.isPostProcessor }
            .mapNotNull { createBean(it) as? BeanPostProcessor }
            .let { beanPostProcessors.addAll(it) }
    }

    private fun createBean(beanDefinition: BeanDefinition): Any {
        val clazz = beanDefinition.clazz
        val instance = clazz.constructors.first().call()
        val beanName = beanDefinition.beanName

        (instance as? ApplicationContextAware)?.setApplicationContext(this)
        beanPostProcessors.forEach { it.postProcessBeforeInitialization(instance, beanName) }
        beanPostProcessors.forEach { it.postProcessAfterInitialization(instance, beanName) }
        (instance as? InitializingBean)?.afterPropertiesSet()
        return instance
    }

    fun getBean(beanName: String): Any {
        val beanDefinition = cachedBeanDefinitions[beanName]
            ?: throw NullPointerException("No bean definition found for bean name: $beanName")
        return when (beanDefinition.scope) {
            ScopeStrategy.Prototype -> createBean(beanDefinition)
            ScopeStrategy.Singleton ->
                singletonDependenciesPool.getOrPut(beanName) { createBean(beanDefinition) }
        }
    }
}
