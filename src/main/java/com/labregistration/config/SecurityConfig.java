package com.labregistration.config;

import com.labregistration.security.JwtAuthenticationFilter;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
//    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/error").permitAll()
                
                // Super Admin only
                .requestMatchers("/api/admin/faculties/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/admin/departments/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/admin/programs/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/admin/courses/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/admin/lab-managers/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/admin/users/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/admin/audit-logs/**").hasRole("SUPER_ADMIN")
                
                // Super Admin and Lab Manager
                .requestMatchers(HttpMethod.POST, "/api/lab-sessions/**").hasAnyRole("SUPER_ADMIN", "LAB_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/lab-sessions/**").hasAnyRole("SUPER_ADMIN", "LAB_MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/lab-sessions/**").hasAnyRole("SUPER_ADMIN", "LAB_MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/lab-sessions/**").hasAnyRole("SUPER_ADMIN", "LAB_MANAGER")
                .requestMatchers("/api/attendance/**").hasAnyRole("SUPER_ADMIN", "LAB_MANAGER")
                .requestMatchers("/api/reports/**").hasAnyRole("SUPER_ADMIN", "LAB_MANAGER")
                
                // Students
                .requestMatchers(HttpMethod.POST, "/api/registrations").hasRole("STUDENT")
                .requestMatchers("/api/registrations/my/**").hasRole("STUDENT")
                
                // All authenticated users
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Allow H2 console frames
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
}
