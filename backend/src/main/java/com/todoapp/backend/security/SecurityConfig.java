package com.todoapp.backend.security;

import com.todoapp.backend.services.UserDetailsServiceImpl;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    @Autowired
    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
    // Exception {
    // http
    // .csrf(csrf -> csrf.disable()) // Отключаем CSRF
    // .cors(cors -> cors.configurationSource(request -> {
    // CorsConfiguration config = new CorsConfiguration();
    // config.setAllowedOrigins(Arrays.asList("*")); // Настройте под свои origin
    // config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE",
    // "OPTIONS"));
    // config.setAllowedHeaders(Arrays.asList("authorization", "content-type",
    // "x-auth-token"));
    // config.setExposedHeaders(Arrays.asList("x-auth-token"));
    // config.setAllowCredentials(true);
    // return config;
    // }))
    // .authorizeHttpRequests(authz -> authz
    // .requestMatchers("/api/v1/auth/**").permitAll() // Путь для регистрации
    // .anyRequest().authenticated() // Все остальные пути требуют аутентификации
    // )
    // .sessionManagement(session ->
    // session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Отключаем
    // сессии
    // )
    // .addFilterBefore(jwtRequestFilter,
    // UsernamePasswordAuthenticationFilter.class); // Добавляем фильтр JWT

    // return http.build();
    // }

    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http,
    // CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws
    // Exception {
    // http
    // .csrf(csrf -> csrf.disable())
    // .cors(cors -> cors.configurationSource(request -> {
    // CorsConfiguration config = new CorsConfiguration();
    // config.setAllowedOrigins(Arrays.asList("*"));
    // config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE",
    // "OPTIONS"));
    // config.setAllowedHeaders(Arrays.asList("authorization", "content-type",
    // "x-auth-token"));
    // config.setExposedHeaders(Arrays.asList("x-auth-token"));
    // config.setAllowCredentials(true);
    // return config;
    // }))
    // .authorizeHttpRequests(authz -> authz
    // .requestMatchers("/api/v1/auth/**").permitAll()
    // .anyRequest().authenticated())
    // .exceptionHandling(exceptions -> exceptions
    // .authenticationEntryPoint(customAuthenticationEntryPoint) // Используем
    // кастомный обработчик
    // )
    // .sessionManagement(session ->
    // session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    // .addFilterBefore(jwtRequestFilter,
    // UsernamePasswordAuthenticationFilter.class);

    // return http.build();
    // }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Arrays.asList("*"));
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
                    config.setExposedHeaders(Arrays.asList("x-auth-token"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/v1/auth/**").permitAll() // Разрешаем доступ к авторизационным маршрутам
                        .requestMatchers("/v1/projects/**").authenticated() // Защищаем маршруты проектов
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
}
