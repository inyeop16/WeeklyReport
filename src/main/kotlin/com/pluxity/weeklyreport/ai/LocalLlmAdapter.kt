package com.pluxity.weeklyreport.ai

import com.pluxity.weeklyreport.ai.dto.AiRequest
import com.pluxity.weeklyreport.ai.dto.AiResponse
import org.springframework.web.client.RestClient

class LocalLlmAdapter(
    private val restClient: RestClient,
    private val model: String
) : AiAdapter {

    override fun generate(request: AiRequest): AiResponse {
        val body = mapOf(
            "model" to model,
            "messages" to listOf(
                mapOf("role" to "system", "content" to request.systemPrompt),
                mapOf("role" to "user", "content" to request.userMessage)
            ),
            "stream" to false
        )

        val response = restClient.post()
            .uri("/api/chat")
            .body(body)
            .retrieve()
            .body(Map::class.java)

        @Suppress("UNCHECKED_CAST")
        val content = (response?.get("message") as? Map<String, Any>)
            ?.get("content") as? String
            ?: throw RuntimeException("Failed to parse Ollama API response")

        return AiResponse(content = content)
    }
}
