package org.springframework.context
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.context.annotations.ComponentScan

@ComponentScan class ConfigWithDefaultScanRoot
@ComponentScan("dev.claycheng") class ConfigWithSpecificScanRoot

class ApplicationContextSpec: DescribeSpec({

    describe("should scan bean candidates with @ComponentScan annotation") {
        it ("with specific path") {
            val context = ApplicationContext(ConfigWithSpecificScanRoot::class)
            context.scanRoot shouldBe "dev.claycheng"
        }

        it ("without path, config class as the root to scan") {
            val context = ApplicationContext(ConfigWithDefaultScanRoot::class)
            context.scanRoot shouldBe  "org.springframework.context"
        }
    }
})
