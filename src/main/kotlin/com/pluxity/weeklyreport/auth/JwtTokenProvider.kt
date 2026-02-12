package com.pluxity.weeklyreport.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val key = Jwts.SIG.HS256.key().build()

    fun generateToken(userId: Long, username: String, role: String): String {
        val now = Date()
        val expiry = Date(now.time + jwtProperties.expiration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("username", username)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    // 단순 유효성 검증 (이제 파싱 성공 여부만 확인)
    fun validateToken(token: String): Boolean = parseClaims(token) != null

    // 인증 객체 생성 (이미 파싱된 Claims를 이용하거나 새로 파싱)
    fun getAuthentication(token: String): Authentication? {
        val claims = parseClaims(token) ?: return null // 여기서 한 번만 파싱됨

        val userId = claims.subject.toLong()
        val authorities = (claims["auth"] as? String)?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.map { SimpleGrantedAuthority(it) }
            ?: emptyList()

        return UsernamePasswordAuthenticationToken(userId, token, authorities)
    }

    private fun parseClaims(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: JwtException) {
            log.warn("JWT 토큰 검증 실패: {}", e.message)
            null
        } catch (_: IllegalArgumentException) {
            log.warn("JWT 토큰이 비어있거나 잘못되었습니다")
            null
        }
    }

}
