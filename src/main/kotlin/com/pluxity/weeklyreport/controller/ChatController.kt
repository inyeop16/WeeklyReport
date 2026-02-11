package com.pluxity.weeklyreport.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ChatController {

    @GetMapping("/")
    fun chat(): String = "chat"
}
