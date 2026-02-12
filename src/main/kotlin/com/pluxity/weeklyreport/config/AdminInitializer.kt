package com.pluxity.weeklyreport.config

import com.pluxity.weeklyreport.domain.entity.Role
import com.pluxity.weeklyreport.domain.entity.User
import com.pluxity.weeklyreport.domain.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@EnableConfigurationProperties(AdminProperties::class)
class AdminInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val adminProperties: AdminProperties
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments?) {
        if (userRepository.existsByUsername(adminProperties.username)) {
            log.info("관리자 계정이 이미 존재합니다: {}", adminProperties.username)
            return
        }

        val admin = User(
            username = adminProperties.username,
            name = adminProperties.name,
            email = adminProperties.email,
            password = passwordEncoder.encode(adminProperties.password),
            role = Role.ADMIN
        )

        userRepository.save(admin)
        log.info("관리자 계정이 생성되었습니다: {}", adminProperties.username)
    }
}
