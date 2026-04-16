package com.example.PlanR.config;

import com.example.PlanR.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomUserDetailsService userDetailsService)
            throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Publicly accessible routes
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                
                // STRICTLY locks down user creation, faculty, and settings to Superadmin
                .requestMatchers("/admin/**", "/faculty", "/settings").hasRole("SUPERADMIN")
                
                // Routine builder access
                .requestMatchers("/routine-builder", "/generate-routine", "/api/schedule/**").hasAnyRole("SUPERADMIN", "COORDINATOR")
                
                // Protect AI endpoints
                .requestMatchers("/api/ai/**").authenticated()
                
                // Protect all other routes
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            .formLogin(form -> form
                // Custom login page
                .loginPage("/login")
                // Explicitly set the processing URL
                .loginProcessingUrl("/login")
                // Form parameter names (must match login.html)
                .usernameParameter("username")
                .passwordParameter("password")
                // Redirect here on successful login
                .defaultSuccessUrl("/allocation", true)
                // Redirect here on failed login
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "SESSION") 
                .permitAll()
            )
            .userDetailsService(userDetailsService)
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) -> {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        try (PrintWriter writer = response.getWriter()) {
                            writer.write("{\"error\": \"Unauthorized access\"}");
                        }
                    }, 
                    request -> request.getRequestURI().startsWith("/api/")
                )
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        try (PrintWriter writer = response.getWriter()) {
                            writer.write("{\"error\": \"Insufficient privileges\"}");
                        }
                    } else {
                        response.sendRedirect("/dashboard");
                    }
                })
            );

            return http.build();
        }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
