package org.springframework.context
import org.springframework.context.annotations.ComponentScan
import kotlin.reflect.KClass

class ApplicationContext <T: Any> (clazz: KClass<T>) {
    var scanRoot: String

    init {
        val scanAnnotation = clazz.annotations.find { it.annotationClass == ComponentScan::class } as ComponentScan
        scanRoot = scanAnnotation.path.ifBlank { clazz.java.`package`.name }
    }
}