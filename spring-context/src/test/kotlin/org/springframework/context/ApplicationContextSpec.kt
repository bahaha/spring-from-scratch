package org.springframework.context

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import org.springframework.context.annotations.Autowired
import org.springframework.context.annotations.Component
import org.springframework.context.annotations.ComponentScan
import org.springframework.context.annotations.Scope
import org.springframework.context.core.ScopeStrategy

@ComponentScan
class ConfigWithDefaultScanRoot

@Component
class InjectMe

@Component
class FooService {
    @Autowired("injectMe")
    var dep: InjectMe? = null
}

@Component("barlarbarbarbar")
@Scope(strategy = ScopeStrategy.Prototype)
class BarService {
    @Autowired
    var injectMe: InjectMe? = null
}

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
        context.cachedBeanDefinitions["fooService"]?.apply {
            beanName shouldBe "fooService"
            scope shouldBe ScopeStrategy.Singleton
        }

        context.cachedBeanDefinitions["barlarbarbarbar"]?.apply {
            beanName shouldBe "barlarbarbarbar"
            scope shouldBe ScopeStrategy.Prototype
        }
    }

    describe("should get the singleton or prototype bean") {
        val context = ApplicationContext(ConfigWithDefaultScanRoot::class)
        it("non existing bean") {
            shouldThrow<NullPointerException> {
                context.getBean("non-existing-bean")
            }
        }

        describe("without dependencies") {
            it("singleton bean") {
                val fooServiceBean = context.getBean("fooService")
                fooServiceBean.javaClass.canonicalName shouldBe "org.springframework.context.FooService"

                val singletonFooService = context.getBean("fooService")
                fooServiceBean shouldBeSameInstanceAs singletonFooService
            }

            it("prototype bean") {
                val barServiceBean = context.getBean("barlarbarbarbar")
                barServiceBean.javaClass.canonicalName shouldBe "org.springframework.context.BarService"

                val prototypeBean = context.getBean("barlarbarbarbar")
                barServiceBean shouldNotBeSameInstanceAs prototypeBean
            }
        }

        describe("with dependencies") {
            it("singleton bean") {
                val fooServiceBean = context.getBean("fooService") as FooService
                fooServiceBean.dep shouldNotBe null
            }

            it("prototype bean") {
                val barServiceBean = context.getBean("barlarbarbarbar") as BarService
                barServiceBean.injectMe shouldNotBe null
            }
        }
    }
})
