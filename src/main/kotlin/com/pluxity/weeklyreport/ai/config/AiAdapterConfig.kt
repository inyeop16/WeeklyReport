package com.pluxity.weeklyreport.ai.config

import com.pluxity.weeklyreport.ai.AiAdapter
import com.pluxity.weeklyreport.ai.ClaudeAiAdapter
import com.pluxity.weeklyreport.ai.GroqAiAdapter
import com.pluxity.weeklyreport.ai.LocalLlmAdapter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(AiProperties::class)
class AiAdapterConfig(
    private val aiProperties: AiProperties
) {

    @Bean
    fun aiAdapter(): AiAdapter = when (aiProperties.provider) {
        "claude" -> ClaudeAiAdapter(
            RestClient.builder()
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("x-api-key", aiProperties.claude.apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build(),
            aiProperties.claude.model
        )
        "local" -> LocalLlmAdapter(
            RestClient.builder()
                .baseUrl(aiProperties.local.baseUrl)
                .defaultHeader("content-type", "application/json")
                .build(),
            aiProperties.local.model
        )
        "groq" -> GroqAiAdapter(
            restClient = RestClient.builder()
                .baseUrl("https://api.groq.com/openai")
                .defaultHeader("Authorization", "Bearer ${aiProperties.groq.apiKey}")
                .defaultHeader("Content-Type", "application/json")
                .build(),
            model = aiProperties.groq.model
        )

        else -> throw IllegalArgumentException("Unknown AI provider: ${aiProperties.provider}")
    }
}
