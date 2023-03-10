package org.springframework.context.annotations

@Retention
@Target(AnnotationTarget.FIELD)
annotation class Autowired(val beanName: String = "")
