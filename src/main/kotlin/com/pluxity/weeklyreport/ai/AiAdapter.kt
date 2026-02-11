package com.pluxity.weeklyreport.ai

import com.pluxity.weeklyreport.ai.dto.AiRequest
import com.pluxity.weeklyreport.ai.dto.AiResponse

interface AiAdapter {
    fun generate(request: AiRequest): AiResponse
}
