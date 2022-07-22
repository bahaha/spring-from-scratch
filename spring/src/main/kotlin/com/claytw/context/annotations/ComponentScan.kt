package com.claytw.context.annotations

@Target(AnnotationTarget.CLASS)
@Retention
annotation class ComponentScan(val path: String = "")
