/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package dev.claycheng

import dev.claycheng.config.AppConfig
import org.springframework.context.ApplicationContext

class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}

fun main() {
    ApplicationContext(AppConfig::class)
}