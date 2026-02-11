package com.pluxity.weeklyreport.ai

import com.pluxity.weeklyreport.ai.dto.AiRequest
import com.pluxity.weeklyreport.ai.dto.AiResponse
import org.springframework.web.client.RestClient

class ClaudeAiAdapter(
    private val restClient: RestClient,
    private val model: String
) : AiAdapter {

    override fun generate(request: AiRequest): AiResponse {
        val body = mapOf(
            "model" to model,
            "max_tokens" to 4096,
            "system" to request.systemPrompt,
            "messages" to listOf(
                mapOf("role" to "user", "content" to request.userMessage)
            )
        )

        val response = restClient.post()
            .uri("/v1/messages")
            .body(body)
            .retrieve()
            .body(Map::class.java)

        @Suppress("UNCHECKED_CAST")
        val content = (response?.get("content") as? List<Map<String, Any>>)
            ?.firstOrNull()
            ?.get("text") as? String
            ?: throw RuntimeException("Failed to parse Claude API response")

        return AiResponse(content = content)
    }
}
