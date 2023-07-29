package dev.jayhan.so76638853v5

import com.ninjasquad.springmockk.SpykBean
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.integration.channel.AbstractMessageChannel
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.channel.interceptor.WireTap
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.endpoint.MessageProducerSupport
import org.springframework.integration.mqtt.support.MqttHeaders
import org.springframework.integration.test.context.MockIntegrationContext
import org.springframework.integration.test.context.SpringIntegrationTest
import org.springframework.integration.test.mock.MockIntegration
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.support.GenericMessage
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID

@ExtendWith(SpringExtension::class)
@SpringIntegrationTest(noAutoStartup = ["mqttChannelAdapter"])
@Import(MqttIntegrationConfig::class)
@ContextConfiguration
class MyIntegrationTest {

    @Autowired
    @Qualifier("mqttChannelAdapter")
    private lateinit var mqttChannelAdapter: MessageProducerSupport

    @Autowired
    @Qualifier("mqttOutboundChannel")
    private lateinit var mqttOutboundChannel: AbstractMessageChannel

    @Autowired
    private lateinit var mockIntegrationContext: MockIntegrationContext

    @SpykBean
    private lateinit var myMessageHandler: MyMessageHandler

    @Test
    fun mytest() {
        val qchann = QueueChannel()
        mqttOutboundChannel.addInterceptor(WireTap(qchann))

        val messageArgumentCaptor = MockIntegration.messageArgumentCaptor()
        val mockHandler = MockIntegration.mockMessageHandler(messageArgumentCaptor)
        mockIntegrationContext.substituteMessageHandlerFor("mqttOutEndpoint", mockHandler.handleNext { })

        val payload = "hello jayhan, this is mqtt integration test"
        mqttChannelAdapter.outputChannel?.send(GenericMessage(payload))
        verify {
            myMessageHandler.handleMessage(eq(payload))
        }

        val replyMessage = qchann.receive()
        assertNotNull(replyMessage)
        val replyPayload = replyMessage?.payload as String
        assertEquals(myMessageHandler.handleMessage(payload), replyPayload)

        // check handler's input
        val handlerInput = messageArgumentCaptor.value
        assertEquals("testreply_${replyPayload[0]}", handlerInput.headers[MqttHeaders.TOPIC])
    }

    @TestConfiguration
    @EnableIntegration
    class FakeMqttBrokerAddressConfig {
        @Bean
        fun mqttConfig(): MqttConfig {
            return MqttConfig(server = "tcp://non.existing.host:1883", clientId = UUID.randomUUID().toString())
        }
    }
}
