package org.springframework.context.annotations

@Retention
@Target(AnnotationTarget.CLASS)
annotation class ComponentScan(val path: String = "")
