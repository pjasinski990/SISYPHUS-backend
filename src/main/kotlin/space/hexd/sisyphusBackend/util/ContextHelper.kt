package space.hexd.sisyphusBackend.util

import org.springframework.security.core.context.SecurityContextHolder

class ContextHelper {
    companion object {
        fun getCurrentlyLoggedUsername(): String {
            return SecurityContextHolder.getContext().authentication.name
                ?: throw IllegalStateException("Trying to get current user but no user available")
        }
    }
}