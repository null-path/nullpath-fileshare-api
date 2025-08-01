package io.nullpath.fileshare.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(
                        "/h2-console/**",
                        "/",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/api/**",
                        "/api/public/**"
                    ).permitAll()
                    .anyRequest().denyAll()
            }
            .csrf { csrf ->
                csrf.ignoringRequestMatchers("/h2-console/**", "/api/**")
            }
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
                headers.addHeaderWriter { request, response ->
                    response.setHeader("X-XSS-Protection", "1; mode=block")
                }
                headers.contentTypeOptions { }
                headers.cacheControl { }
                headers.httpStrictTransportSecurity { hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000) }
            }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            // !!! IMPORTANT FOR PRODUCTION !!!
            // For a truly anonymous, no-account service, allowing '*' is fine.
            // If you later add any form of session or cookie-based authentication,
            // you *must* change this to explicit origins (e.g., listOf("https://yourfrontend.com"))
            // and adjust 'allowCredentials' accordingly.
            allowedOrigins = listOf("*") // Allows requests from any origin

            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = false // <--- THIS IS THE FIX: Set to false when allowedOrigins is "*"
            maxAge = 3600L
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}