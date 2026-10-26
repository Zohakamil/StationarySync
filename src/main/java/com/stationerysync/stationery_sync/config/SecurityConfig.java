package com.stationerysync.stationery_sync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService users(PasswordEncoder encoder) {
        UserDetails operator = User.withUsername("operator")
                .password(encoder.encode("op123"))
                .roles("OPERATOR")
                .build();
        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(operator, admin);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())                       // OK for a stateless REST API
                .cors(Customizer.withDefaults())                    // reuse our CorsConfig bean
                .authorizeHttpRequests(auth -> auth
                        // Admin-only write endpoints on tariffs and discounts
                        .requestMatchers(HttpMethod.POST, "/api/tariffs", "/api/discounts").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,  "/api/tariffs/**", "/api/discounts/**").hasRole("ADMIN")
                        // Everything else requires at least a logged-in user
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());               // HTTP Basic auth
        return http.build();
    }
}