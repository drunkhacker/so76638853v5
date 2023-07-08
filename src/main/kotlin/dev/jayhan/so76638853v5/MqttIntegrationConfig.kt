package dev.jayhan.so76638853v5

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.endpoint.MessageProducerSupport
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory
import org.springframework.integration.mqtt.core.MqttPahoClientFactory
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter
import org.springframework.integration.mqtt.support.MqttHeaders
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHandler
import org.springframework.integration.dsl.integrationFlow

@Configuration
class MqttIntegrationConfig(
    private val mqttConfig: MqttConfig,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private fun connectOptions(): MqttConnectOptions {
        val opt = MqttConnectOptions()
        opt.serverURIs = arrayOf(mqttConfig.server)
        opt.isCleanSession = mqttConfig.cleanSession
        opt.mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1
        opt.connectionTimeout = mqttConfig.connectionTimeout
        opt.keepAliveInterval = mqttConfig.keepAliveInterval
        if (mqttConfig.username != null) {
            opt.userName = mqttConfig.username
        }
        if (mqttConfig.password != null) {
            opt.password = mqttConfig.password.toCharArray()
        }

        return opt
    }


    private fun mqttClientFactory(): MqttPahoClientFactory {
        val factory = DefaultMqttPahoClientFactory()
        factory.connectionOptions = connectOptions()
        return factory
    }

    private fun pahoMessageConverter(): DefaultPahoMessageConverter {
        val converter = DefaultPahoMessageConverter(mqttConfig.charset)
        converter.isPayloadAsBytes = mqttConfig.binary
        return converter
    }

    @Bean
    @Qualifier("mqttChannelAdapter")
    fun mqttChannelAdapter(): MessageProducerSupport {
        val adapter = MqttPahoMessageDrivenChannelAdapter(
            mqttConfig.clientId,
            mqttClientFactory(),
            *mqttConfig.topics.toTypedArray()
        )
        adapter.setQos(*mqttConfig.qos.toIntArray())
        adapter.setConverter(pahoMessageConverter())

        // XXX : `MessageChannels.direct()` throws "Dispatcher has no subscribers" error
        val outputChannel = MessageChannels.direct().get()
        adapter.outputChannel = outputChannel
        return adapter
    }

    @Bean
    @Qualifier("mqttOutboundHandler")
    fun mqttOutboundHandler(): MessageHandler {
        val messageHandler = MqttPahoMessageHandler(MqttAsyncClient.generateClientId(), mqttClientFactory())
        messageHandler.setAsync(true)
        messageHandler.setDefaultTopic("myreply")
        return messageHandler
    }

    @Bean
    @Qualifier("mqttOutboundChannel")
    fun mqttOutboundChannel(): MessageChannel {
        val chann = DirectChannel()
        chann.isLoggingEnabled = true
        chann.componentName = "mqttOutboundChannel"
        return chann
    }

    @Bean
    fun mqttOutbound(): IntegrationFlow = IntegrationFlows.from(mqttOutboundChannel())
        .enrichHeaders { h -> h.headerFunction<String>(MqttHeaders.TOPIC) { msg -> "testtopic" } }
        .transform<String, String> { "hello! $it" }
        .handle(mqttOutboundHandler())
        .get()


    @Bean
    fun mqttInbound(): IntegrationFlow {
        return IntegrationFlows.from(mqttChannelAdapter())
            .handle<String> { payload, headers ->
                logger.info("payload=$payload, headers=$headers")
                payload
            }
            .channel(mqttOutboundChannel())
            .get()
    }

//    @Bean
//    fun mqttInbound() = integrationFlow(mqttChannelAdapter()) {
//        handle {
//            logger.info("payload=${it.payload}, headers=${it.headers}")
//            it.payload
//        }
//        channel(mqttOutboundChannel())
//    }
}