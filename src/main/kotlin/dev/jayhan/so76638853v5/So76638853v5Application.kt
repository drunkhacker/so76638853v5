package dev.jayhan.so76638853v5

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.integration.config.EnableIntegration

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableIntegration
class So76638853v5Application

fun main(args: Array<String>) {
    runApplication<So76638853v5Application>(*args)
}
