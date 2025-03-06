//package com.smartcalendar.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/h2-console/**").permitAll() // Разрешить доступ к H2 Console
//                        .anyRequest().authenticated()
//                )
//                .csrf(csrf -> csrf.disable()) // Отключить CSRF
//                .headers(headers -> headers
//                        .frameOptions(frame -> frame.disable()) // Разрешить фреймы для H2 Console
//                );
//
//        return http.build();
//    }
//}