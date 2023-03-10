package org.springframework.context.annotations

import org.springframework.context.core.ScopeStrategy

@Retention
@Target(AnnotationTarget.CLASS)
annotation class Scope(val strategy: ScopeStrategy = ScopeStrategy.Singleton)
