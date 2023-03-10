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
import org.springframework.context.lifecycle.InitializingBean

@ComponentScan
class ConfigWithDefaultScanRoot

@Component
class InjectMe

@Component
class FooService : InitializingBean {
    @Autowired("injectMe")
    lateinit var dep: InjectMe

    @Autowired
    var nonExistingDep: InjectMe? = null
    var hadDependenciesInjected = false

    override fun afterPropertiesSet() {
        this.hadDependenciesInjected = true
    }
}

@Component("barlarbarbarbar")
@Scope(strategy = ScopeStrategy.Prototype)
class BarService {
    @Autowired
    lateinit var injectMe: InjectMe
    var hadInitializing = false
}

@ComponentScan("dev.claycheng")
class ConfigWithSpecificScanRoot

class ApplicationContextSpec : DescribeSpec({

    val context = ApplicationContext(ConfigWithDefaultScanRoot::class)
    describe("should scan bean candidates with @ComponentScan annotation") {
        it("with specific path") {
            val contextWithSpecificRoot = ApplicationContext(ConfigWithSpecificScanRoot::class)
            contextWithSpecificRoot.scanRoot shouldBe "dev.claycheng"
        }

        it("without path, config class as the root to scan") {
            context.scanRoot shouldBe "org.springframework.context"
        }
    }

    it("should cache the bean definition of bean candidates to keep about the metadata of bean") {
        context.cachedBeanDefinitions["fooService"]?.let {
            it.beanName shouldBe "fooService"
            it.scope shouldBe ScopeStrategy.Singleton
        }

        context.cachedBeanDefinitions["barlarbarbarbar"]?.let {
            it.beanName shouldBe "barlarbarbarbar"
            it.scope shouldBe ScopeStrategy.Prototype
        }
    }

    describe("should get the singleton or prototype bean") {
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
                fooServiceBean.nonExistingDep shouldBe null
            }

            it("prototype bean") {
                val barServiceBean = context.getBean("barlarbarbarbar") as BarService
                barServiceBean.injectMe shouldNotBe null
            }
        }
    }

    describe("life cycle") {
        it("InitializingBean#afterPropertiesSet") {
            val fooService = context.getBean("fooService") as FooService
            fooService.hadDependenciesInjected shouldBe true

            val barService = context.getBean("barlarbarbarbar") as BarService
            barService.hadInitializing shouldBe false
        }
    }
})
