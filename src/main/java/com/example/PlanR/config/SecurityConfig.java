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
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
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
            // Custom exception handling
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/dashboard");
                })
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
