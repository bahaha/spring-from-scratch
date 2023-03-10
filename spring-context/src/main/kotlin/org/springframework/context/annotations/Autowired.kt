package org.springframework.context.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Autowired(val beanName: String = "")
