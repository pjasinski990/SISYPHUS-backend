package space.hexd.sisyphusBackend.services

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import space.hexd.sisyphusBackend.repositories.AppUserRepository

@Service
class UserDetailsServiceImpl(private val appUserRepository: AppUserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = appUserRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found with username: $username")

        val authorities = user.roles.map { SimpleGrantedAuthority(it) }
        return org.springframework.security.core.userdetails.User(
            user.username,
            user.password,
            authorities
        )
    }
}