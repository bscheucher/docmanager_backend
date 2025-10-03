package com.app.docmanager.config;

import com.app.docmanager.security.CustomUserDetailsService;
import com.app.docmanager.security.JwtAuthenticationEntryPoint;
import com.app.docmanager.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true
)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final SecurityProperties securityProperties; // ← ADD THIS

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // Public endpoints
                    auth.requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/api/public/**").permitAll();

                    // Conditionally permit H2 console (only in dev)
                    if (securityProperties.isH2ConsoleEnabled()) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }

                    // Conditionally permit Swagger
                    if (securityProperties.isSwaggerEnabled()) {
                        auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll();
                    }

                    // Admin endpoints
                    auth.requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                            .requestMatchers("/api/admin/**").hasRole("ADMIN")

                            // User management endpoints
                            .requestMatchers(HttpMethod.GET, "/api/users/check-username/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/users/check-email/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("USER", "ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyRole("USER", "ADMIN")

                            // Document endpoints
                            .requestMatchers("/api/documents/**").hasAnyRole("USER", "ADMIN")

                            // All other endpoints require authentication
                            .anyRequest().authenticated();
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Conditionally enable H2 console frame options (only in dev)
        if (securityProperties.isH2ConsoleEnabled()) {
            http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
        }

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Use SecurityProperties to get CORS settings
        configuration.setAllowedOrigins(
                Arrays.asList(securityProperties.getCors().getAllowedOrigins().split(","))
        );
        configuration.setAllowedMethods(
                Arrays.asList(securityProperties.getCors().getAllowedMethods().split(","))
        );
        configuration.setAllowedHeaders(
                Arrays.asList(securityProperties.getCors().getAllowedHeaders().split(","))
        );
        configuration.setAllowCredentials(securityProperties.getCors().isAllowCredentials());
        configuration.setMaxAge(securityProperties.getCors().getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}