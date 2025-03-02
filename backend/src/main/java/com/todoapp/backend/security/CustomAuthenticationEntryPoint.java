package com.todoapp.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoapp.backend.exceptions.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Создаем объект с описанием ошибки
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                "Unauthorized: " + authException.getMessage(),
                request.getRequestURI()
        );

        // Устанавливаем статус ответа
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        // Записываем JSON в тело ответа
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}