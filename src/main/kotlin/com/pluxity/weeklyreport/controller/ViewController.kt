package com.pluxity.weeklyreport.controller

import com.pluxity.weeklyreport.service.UserService
import com.pluxity.weeklyreport.service.TemplateService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ViewController(
    private val userService: UserService,
    private val templateService: TemplateService
) {

    @GetMapping("/")
    fun index(): String = "redirect:/login"

    @GetMapping("/login")
    fun login(): String = "pages/login"

    @GetMapping("/reports")
    fun reports(model: Model): String {
        model.addAttribute("activePage", "reports")
        loadCommonData(model)
        return "pages/reports"
    }

    @GetMapping("/dashboard")
    fun dashboard(model: Model): String {
        model.addAttribute("activePage", "dashboard")
        return "pages/dashboard"
    }

    @GetMapping("/team-report")
    fun teamReport(model: Model): String {
        model.addAttribute("activePage", "team-report")
        return "pages/team-report"
    }

    @GetMapping("/templates")
    fun templates(model: Model): String {
        model.addAttribute("activePage", "templates")
        return "pages/templates"
    }

    /**
     * Load common data for reports page (users, templates)
     */
    private fun loadCommonData(model: Model) {
        try {
            val users = userService.findAll()
            val templates = templateService.findActive(null)
            model.addAttribute("users", users)
            model.addAttribute("templates", templates)
        } catch (e: Exception) {
            model.addAttribute("users", emptyList<Any>())
            model.addAttribute("templates", emptyList<Any>())
        }
    }
}
