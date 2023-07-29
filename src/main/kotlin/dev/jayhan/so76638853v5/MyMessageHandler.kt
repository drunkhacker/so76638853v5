package dev.jayhan.so76638853v5

import org.springframework.stereotype.Service

@Service
class MyMessageHandler {
    fun handleMessage(payload: String): String {
        return payload.reversed()
    }
}
