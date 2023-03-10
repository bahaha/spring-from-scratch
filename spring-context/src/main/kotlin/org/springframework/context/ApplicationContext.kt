package org.springframework.context

import org.springframework.context.annotations.Component
import org.springframework.context.annotations.ComponentScan
import org.springframework.context.annotations.Scope
import org.springframework.context.core.BeanDefinition
import org.springframework.context.core.ScopeStrategy
import java.io.File
import java.net.URLDecoder
import kotlin.reflect.KClass
import kotlin.streams.asStream

class ApplicationContext<T : Any>(clazz: KClass<T>) {
    lateinit var scanRoot: String
    var cachedBeanDefinitions: Map<String, BeanDefinition> = emptyMap()
    private val singletonDependenciesPool: MutableMap<String, Any> = mutableMapOf()

    private val classLoader = clazz.java.classLoader

    init {
        scan(clazz)
        val beans = cachedBeanDefinitions.values
            .filter { it.scope == ScopeStrategy.Singleton }
            .associate { it.beanName to createBean(it) }
        singletonDependenciesPool.putAll(beans)
    }

    private fun scan(clazz: KClass<T>) {
        val scanAnnotation =
            clazz.annotations.find { it.annotationClass == ComponentScan::class } as ComponentScan
        scanRoot = scanAnnotation.path.ifBlank { clazz.java.`package`.name }

        val rootDirectoryToScan = scanRoot.replace(".", "/")
        val resources = classLoader.resources(rootDirectoryToScan)
        val classFiles = resources
            .map { URLDecoder.decode(it.file, Charsets.UTF_8) }
            .map { File(it) }
            .filter { it.isDirectory }
            .flatMap { (it.listFiles()?.asSequence() ?: emptySequence()).asStream() }
            .filter { it.isFile && it.extension == "class" }

        val classNames = classFiles
            .map { it.absolutePath }
            .map { it.substringAfter(rootDirectoryToScan).substringBeforeLast(".") }
            .map { it.replace("/", ".") }
            .map { "$scanRoot$it" }

        val beanCandidates = classNames
            .map { classLoader.loadClass(it) }
            .filter { it.isAnnotationPresent(Component::class.java) }
            .toList()

        val beanDefinitions = beanCandidates
            .map { componentBean ->
                val scope = componentBean?.takeIf { it.isAnnotationPresent(Scope::class.java) }
                    ?.getAnnotation(Scope::class.java)?.strategy
                    ?: ScopeStrategy.Singleton
                val beanName =
                    componentBean.getAnnotation(Component::class.java)?.beanName?.takeIf { it.isNotBlank() }
                        ?: componentBean.simpleName.replaceFirstChar { name -> name.lowercase() }

                BeanDefinition(componentBean.kotlin, scope, beanName)
            }
            .associateBy(BeanDefinition::beanName)

        this.cachedBeanDefinitions = beanDefinitions
    }

    private fun createBean(beanDefinition: BeanDefinition): Any {
        val clazz = beanDefinition.clazz
        return clazz.constructors.first().call()
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
