package dev.jayhan.so76638853v5

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "mqtt")
@ConstructorBinding
data class MqttConfig(
    val server: String = "",
    val clientId: String = "",
    val qos: List<Int> = emptyList(),
    val topics: List<String> = emptyList(),
    val username: String? = null,
    val password: String? = null,
    val connectionTimeout: Int = 30,
    val keepAliveInterval: Int = 60,
    val cleanSession: Boolean = true,
    val charset: String = "UTF-8",
    val binary: Boolean = false
)
