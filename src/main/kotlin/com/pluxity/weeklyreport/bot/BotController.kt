package com.pluxity.weeklyreport.bot

import com.fasterxml.jackson.databind.ObjectMapper
import com.pluxity.weeklyreport.bot.dto.Activity
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.HttpURLConnection
import java.net.URL

@RestController
class BotController(
    private val botService: BotService,
    private val objectMapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/api/messages")
    fun receiveActivity(@RequestBody activity: Activity): ResponseEntity<Void> {
        log.info("수신: type={}, text={}", activity.type, activity.text)

        if (activity.type != "message") {
            return ResponseEntity.ok().build()
        }

        val reply = botService.handleMessage(activity)
        sendReply(activity, reply)

        return ResponseEntity.ok().build()
    }

    private fun sendReply(incoming: Activity, reply: Activity) {
        val serviceUrl = incoming.serviceUrl ?: return
        val conversationId = incoming.conversation?.id ?: return

        // |를 인코딩하지 않고 raw URL 그대로 사용
        val url = "${serviceUrl.trimEnd('/')}/v3/conversations/$conversationId/activities"
        log.info("Reply URL: {}", url)

        try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            conn.outputStream.use { os ->
                objectMapper.writeValue(os, reply)
            }

            val code = conn.responseCode
            log.info("Reply 응답코드: {}", code)

            if (code !in 200..299) {
                val error = conn.errorStream?.bufferedReader()?.readText()
                log.error("Reply 실패: code={}, body={}", code, error)
            }
        } catch (e: Exception) {
            log.error("Reply 전송 실패", e)
        }
    }
}
