package tech.hexd.adaptiveLearningCompanion.services

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        // TODO return from the db

        val role = if (username == "admin") "ROLE_ADMIN" else "ROLE_USER"

        val authorities = mutableListOf<GrantedAuthority>(SimpleGrantedAuthority(role))
        return User(username, "password", authorities)
    }
}