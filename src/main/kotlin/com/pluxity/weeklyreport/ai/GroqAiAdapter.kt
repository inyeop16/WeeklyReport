package com.pluxity.weeklyreport.ai

import com.pluxity.weeklyreport.ai.dto.AiRequest
import com.pluxity.weeklyreport.ai.dto.AiResponse
import org.springframework.web.client.RestClient

class GroqAiAdapter(
    private val restClient: RestClient,
    private val model: String
) : AiAdapter {

    override fun generate(request: AiRequest): AiResponse {
        val body = mapOf(
            "model" to model,
            "max_tokens" to 4096,
            "messages" to listOf(
                mapOf("role" to "system", "content" to request.systemPrompt),
                mapOf("role" to "user", "content" to request.userMessage),
            )
        )

        val response = restClient.post()
            .uri("/v1/chat/completions")
            .body(body)
            .retrieve()
            .body(Map::class.java)



        @Suppress("UNCHECKED_CAST")
        val content = (response?.get("choices") as? List<Map<String, Any>>)
            ?.firstOrNull()
            ?.let { it["message"] as? Map<String, Any> }
            ?.get("content") as? String
            ?: throw RuntimeException("Failed to parse Groq API response")

        return AiResponse(content = content)
    }
}