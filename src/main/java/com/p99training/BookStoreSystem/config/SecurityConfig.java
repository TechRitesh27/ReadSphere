package com.p99training.BookStoreSystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Rules (first match wins):
     *   DELETE /books/**  → ROLE_ADMIN required
     *   everything else   → open, no auth needed
     *
     * Uses HTTP Basic Auth — stateless, no session created.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Swagger UI — must be open so docs are accessible without login
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                // Only admins can delete books
                .requestMatchers(HttpMethod.DELETE, "/books/**").hasRole("ADMIN")
                // Everything else is publicly accessible
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> {});

        return http.build();
    }

    /**
     * In-memory admin user.
     * Username : admin
     * Password : admin123   (BCrypt encoded below)
     *
     * To generate a new hash:
     *   new BCryptPasswordEncoder().encode("your-password")
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // BCrypt hash of "admin123"
        String encodedPassword = passwordEncoder.encode("admin123");

        return new InMemoryUserDetailsManager(
                User.builder()
                    .username("admin")
                    .password(encodedPassword)
                    .roles("ADMIN")
                    .build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
