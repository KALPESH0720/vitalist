package com.hospital.config;

import com.hospital.model.User;
import com.hospital.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.*;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            if (!user.isActive()) throw new DisabledException("Account disabled");
            return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Login endpoint - completely open, controller does manual validation
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // Admin only
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                // Patients, Appointments - all roles
                .requestMatchers("/api/patients/**").hasAnyRole("ADMIN","DOCTOR","RECEPTIONIST")
                .requestMatchers("/api/appointments/**").hasAnyRole("ADMIN","DOCTOR","RECEPTIONIST")
                // Inventory write - Admin only; read - all authenticated
                .requestMatchers(HttpMethod.POST,   "/api/inventory/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/inventory/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/inventory/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,    "/api/inventory/**").hasAnyRole("ADMIN","DOCTOR","RECEPTIONIST")
                // AI features - Admin + Doctor only
                .requestMatchers("/api/ai/**").hasAnyRole("ADMIN","DOCTOR")
                // Dashboard - all authenticated
                .requestMatchers("/api/dashboard/**").authenticated()
                .anyRequest().authenticated()
            )
            // Use HTTP Basic for authenticated endpoints
            .httpBasic(basic -> basic
                .authenticationEntryPoint((request, response, authException) -> {
                    // Return JSON 401 instead of browser popup
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"success\":false}");
                })
            );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
