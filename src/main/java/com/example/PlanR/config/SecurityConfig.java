package com.example.PlanR.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Publicly accessible routes
                .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                // Example of restricting an endpoint to ADMIN only
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Protect all other routes
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                // Custom login page
                .loginPage("/login")
                // Redirect here on successful login
                .defaultSuccessUrl("/", true)
                // Redirect here on failed login
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                // Invalidate the session on logout
                .invalidateHttpSession(true)
                // Delete the JSESSIONID cookie
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
