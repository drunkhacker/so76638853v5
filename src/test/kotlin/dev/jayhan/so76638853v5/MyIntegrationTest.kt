package dev.jayhan.so76638853v5

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.integration.endpoint.MessageProducerSupport
import org.springframework.integration.test.context.SpringIntegrationTest
import org.springframework.messaging.support.GenericMessage
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*


@TestConfiguration
class MyTestConfig {
    @Bean
    fun mqttConfig(): MqttConfig {
        return MqttConfig(server = "tcp://non.existing.host:1883", clientId = UUID.randomUUID().toString())
    }
}


@ExtendWith(SpringExtension::class)
@DirtiesContext
@SpringIntegrationTest(noAutoStartup = ["mqttChannelAdapter"])
@ContextConfiguration(classes = [MqttIntegrationConfig::class, MyTestConfig::class])
class MyIntegrationTest {

    @Autowired
    @Qualifier("mqttChannelAdapter")
    private lateinit var mqttChannelAdapter: MessageProducerSupport

    @Test
    fun mytest(context: ApplicationContext) {
        mqttChannelAdapter.outputChannel?.send(GenericMessage("1"))
    }
}