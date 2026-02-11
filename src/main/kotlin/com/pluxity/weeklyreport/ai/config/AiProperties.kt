package com.pluxity.weeklyreport.ai.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.ai")
data class AiProperties(
    val provider: String = "claude",
    val claude: ClaudeProperties = ClaudeProperties(),
    val local: LocalProperties = LocalProperties(),
    val groq: GroqProperties = GroqProperties(),
) {
    data class ClaudeProperties(
        val apiKey: String = "",
        val model: String = "claude-sonnet-4-20250514"
    )

    data class LocalProperties(
        val baseUrl: String = "http://localhost:11434",
        val model: String = "llama3"
    )

    data class GroqProperties(
        val apiKey: String = "",
        val baseUrl: String = "https://api.groq.com/openai/v1",
        val model: String = "llama-3.3-70b-versatile"
    )
}
