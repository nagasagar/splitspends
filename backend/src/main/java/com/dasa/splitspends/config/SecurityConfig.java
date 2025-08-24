
package com.dasa.splitspends.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.UserRepository;
import com.dasa.splitspends.security.JwtAuthenticationFilter;
import com.dasa.splitspends.security.JwtTokenProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationEntryPoint restAuthenticationEntryPoint)
            throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/google",
                                "/api/auth/verify-email",
                                "/api/auth/resend-verification",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**")
                        .permitAll()
                        .requestMatchers("/api/test").authenticated()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                    corsConfig.setAllowedOriginPatterns(java.util.List.of("*"));
                    corsConfig.setAllowedMethods(java.util.List.of("*"));
                    corsConfig.setAllowedHeaders(java.util.List.of("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(restAuthenticationEntryPoint))
                .exceptionHandling(eh -> eh.authenticationEntryPoint(restAuthenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return email -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPasswordHash())
                    .roles("USER") // or user.getRoles() if you have roles
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint(
            LoggingAuthenticationEntryPoint loggingAuthenticationEntryPoint) {
        return loggingAuthenticationEntryPoint;
    }
}
