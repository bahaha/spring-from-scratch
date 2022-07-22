package com.claytw.context

import com.claytw.context.annotations.ComponentScan
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ApplicationContextSpec: DescribeSpec({
    @ComponentScan class ConfigWithDefaultScanPath
    @ComponentScan("com.claytw") class ConfigWithSpecificScanPath

    describe("should scan bean candidates with @ComponentScan annotation") {
        it("with specific path") {
            val context = ApplicationContext(ConfigWithSpecificScanPath::class)
            context.scanRoot shouldBe "com.claytw"
        }

        it("without path, should scan from classpath root of config class") {
            val context = ApplicationContext(ConfigWithDefaultScanPath::class)
            context.scanRoot shouldBe "com.claytw.context"
        }
    }
})
