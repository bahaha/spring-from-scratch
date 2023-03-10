package org.springframework.context

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.context.annotations.Component
import org.springframework.context.annotations.ComponentScan
import org.springframework.context.annotations.Scope
import org.springframework.context.core.ScopeStrategy

@ComponentScan
class ConfigWithDefaultScanRoot

@Component
class FooService

@Component("barlarbarbarbar")
@Scope(strategy = ScopeStrategy.Prototype)
class BarService

@ComponentScan("dev.claycheng")
class ConfigWithSpecificScanRoot

class ApplicationContextSpec : DescribeSpec({

    describe("should scan bean candidates with @ComponentScan annotation") {
        it("with specific path") {
            val context = ApplicationContext(ConfigWithSpecificScanRoot::class)
            context.scanRoot shouldBe "dev.claycheng"
        }

        it("without path, config class as the root to scan") {
            val context = ApplicationContext(ConfigWithDefaultScanRoot::class)
            context.scanRoot shouldBe "org.springframework.context"
        }
    }

    it("should cache the bean definition of bean candidates to keep about the metadata of bean") {
        val context = ApplicationContext(ConfigWithDefaultScanRoot::class)
        context.cachedBeanDefinitions["fooService"]?.beanName shouldBe "fooService"
        context.cachedBeanDefinitions["fooService"]?.scope shouldBe ScopeStrategy.Singleton

        context.cachedBeanDefinitions["barlarbarbarbar"]?.beanName shouldBe "barlarbarbarbar"
        context.cachedBeanDefinitions["barlarbarbarbar"]?.scope shouldBe ScopeStrategy.Prototype
    }
})
