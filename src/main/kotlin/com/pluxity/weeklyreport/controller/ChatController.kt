package com.pluxity.weeklyreport.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ChatController {

    /**
     * Legacy route - redirect to new reports page
     */
    @GetMapping("/chat")
    fun chat(): String = "redirect:/reports"
}
