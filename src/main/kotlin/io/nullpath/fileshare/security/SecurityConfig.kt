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
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) } // This line correctly applies the CORS config
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(
                        "/h2-console/**", // H2 Console (for development, access is usually limited or removed in prod)
                        "/",             // Root path
                        "/swagger-ui/**",// Swagger UI documentation
                        "/v3/api-docs/**", // OpenAPI API definition
                        "/api/**",       // All your /api endpoints (including files and delete)
                        "/api/public/**" // Any explicitly public API endpoints
                    ).permitAll() // Allow all these paths publicly
                    .anyRequest().denyAll() // Deny access to any other unlisted paths by default
            }
            .csrf { csrf ->
                // Disable CSRF for H2 console and all /api endpoints, as they are likely stateless/API-driven
                csrf.ignoringRequestMatchers("/h2-console/**", "/api/**")
            }
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() } // Allow H2 console in a frame
                headers.addHeaderWriter { request, response ->
                    response.setHeader("X-XSS-Protection", "1; mode=block") // Basic XSS protection
                }
                headers.contentTypeOptions { } // Prevent MIME sniffing
                headers.cacheControl { } // Add Cache-Control header
                headers.httpStrictTransportSecurity { hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000) } // HSTS for HTTPS
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

            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP methods for CORS requests
            allowedHeaders = listOf("*") // Allows all headers
            allowCredentials = false // <--- THIS IS THE FIX: Set to false when allowedOrigins is "*"
            maxAge = 3600L // Cache pre-flight response for 1 hour
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration) // Apply this CORS configuration to all paths
        return source
    }
}