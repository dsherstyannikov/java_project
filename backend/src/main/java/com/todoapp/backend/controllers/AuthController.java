package com.todoapp.backend.controllers;

import com.todoapp.backend.models.Role;
import com.todoapp.backend.models.User;
import com.todoapp.backend.dto.LoginRequest;
import com.todoapp.backend.dto.RegisterRequest;
import com.todoapp.backend.exceptions.ErrorDetails;
import com.todoapp.backend.dto.JwtResponse;
import com.todoapp.backend.repositories.RoleRepository;
import com.todoapp.backend.repositories.UserRepository;
import com.todoapp.backend.security.JwtUtil;
import com.todoapp.backend.services.UserDetailsImpl;
import com.todoapp.backend.services.UserDetailsServiceImpl;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            RoleRepository roleRepository,
            UserDetailsServiceImpl userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
        this.userDetailsService = userDetailsService;
    }

//    private ResponseEntity makeTokensResponse(UserDetailsImpl userDetails) {
//        // Генерация токенов
//        String accessToken = jwtUtil.generateToken(userDetails);
//        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
//
//        // Создаем HttpOnly куку для refresh-токена
//        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
//                .httpOnly(true)
//                .secure(false) // Используйте true, если используете HTTPS
//                .path("/api/v1/auth/refresh")
//                .maxAge(7 * 24 * 60 * 60) // 7 дней
//                .sameSite("Strict") // Защита от CSRF
//                .build();
//
//        // Возвращаем access-токен в теле ответа и refresh-токен в куке
//        return ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
//                .body(new JwtResponse(accessToken, null)); // refresh-токен не возвращаем в теле
//    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        try {
            // Аутентификация пользователя по email
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
    
            SecurityContextHolder.getContext().setAuthentication(authentication);
    
            // Получаем UserDetails из аутентификации
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    
            // Генерация токенов
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);
    
            // Создаем HttpOnly куку для refresh-токена
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false) // Используйте true, если используете HTTPS
                    .path("/api/v1/auth/refresh")
                    .maxAge(7 * 24 * 60 * 60) // 7 дней
                    .sameSite("Strict") // Защита от CSRF
                    .build();
    
            // Возвращаем access-токен в теле ответа и refresh-токен в куке
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(new JwtResponse(accessToken, null)); // refresh-токен не возвращаем в теле
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> registerUser(@RequestBody RegisterRequest signUpRequest) {
        try {
            // Проверка существования пользователя
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity.badRequest().body(null);
            }
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest().body(null);
            }

            // Хеширование пароля
            String hashedPassword = passwordEncoder.encode(signUpRequest.getPassword());

            // Создание пользователя
            User user = new User(
                    signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    hashedPassword // Используем хешированный пароль
            );

            // Назначение роли "ROLE_USER"
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Роль не найдена"));
            user.addRole(userRole);

            // Сохранение пользователя
            userRepository.save(user);

            // Создаем UserDetailsImpl для генерации токенов
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);

            // Генерация токенов
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Возврат токенов в ответе
            // return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken));
            // Создаем HttpOnly куку для refresh-токена
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false) // Используйте true, если используете HTTPS
                    .path("/api/v1/auth/refresh")
                    .maxAge(7 * 24 * 60 * 60) // 7 дней
                    .sameSite("Strict") // Защита от CSRF
                    .build();

            // Возвращаем access-токен в теле ответа и refresh-токен в куке
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(new JwtResponse(accessToken, null)); // refresh-токен не возвращаем в теле
        } catch (Exception e) {
            logger.error("Registration error: ", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie) {
        try {
            // Проверка наличия refresh-токена в куке
            if (refreshTokenCookie == null) {
                throw new RuntimeException("Refresh token is missing");
            }

            // Проверка валидности refresh-токена
            if (jwtUtil.validateToken(refreshTokenCookie)) {
                // Извлекаем username из токена
                String username = jwtUtil.extractUsername(refreshTokenCookie);

                // Загружаем пользователя по username
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Генерация новых токенов
                String newAccessToken = jwtUtil.generateToken(userDetails);
                String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

                // Создаем HttpOnly куку для нового refresh-токена
                ResponseCookie refreshTokenCookieResponse = ResponseCookie.from("refreshToken", newRefreshToken)
                        .httpOnly(true)
                        .secure(false) // Используйте true, если используете HTTPS
                        .path("/api/v1/auth/refresh")
                        .maxAge(7 * 24 * 60 * 60) // 7 дней
                        .sameSite("Strict") // Защита от CSRF
                        .build();

                // Возвращаем новый access-токен в теле ответа и новый refresh-токен в куке
                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, refreshTokenCookieResponse.toString())
                        .body(new JwtResponse(newAccessToken, null)); // refresh-токен не возвращаем в теле
            } else {
                throw new RuntimeException("Invalid refresh token");
            }
        } catch (Exception e) {
            // Логируем ошибку для отладки
            logger.error("Refresh token error: ", e);

            // Возвращаем ошибку с описанием
            ErrorDetails errorDetails = new ErrorDetails(new Date(), e.getMessage(), "/api/v1/auth/refresh");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
        }
    }
}