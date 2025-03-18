package com.todoapp.backend.controllers;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.todoapp.backend.dto.JwtResponse;
import com.todoapp.backend.dto.LoginRequest;
import com.todoapp.backend.dto.RegisterRequest;
import com.todoapp.backend.exceptions.ErrorDetails;
import com.todoapp.backend.models.Role;
import com.todoapp.backend.models.User;
import com.todoapp.backend.repositories.RoleRepository;
import com.todoapp.backend.repositories.UserRepository;
import com.todoapp.backend.security.JwtUtil;
import com.todoapp.backend.services.UserDetailsImpl;
import com.todoapp.backend.services.UserDetailsServiceImpl;
import com.todoapp.backend.utils.UsernameGenerator;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

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

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody @Valid LoginRequest loginRequest,
            BindingResult bindingResult,
            HttpServletResponse response) {

        // Проверяем, есть ли ошибки валидации
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder("Ошибка валидации: ");
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                errorMessage.append(fieldError.getField())
                        .append(" - ")
                        .append(fieldError.getDefaultMessage())
                        .append("; ");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorDetails(new Date(), errorMessage.toString(), "/api/v1/auth/login"));
        }

        try {
            if (!userRepository.existsByEmail(loginRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorDetails(new Date(), "Пользователь с таким email не найден.",
                                "/api/v1/auth/login"));
            }

            // Аутентификация пользователя по email и паролю
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
                    .secure(false)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7 дней
                    .sameSite("lax") // Защита от CSRF
                    .build();

            // Возвращаем access-токен в теле ответа и refresh-токен в куке
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(new JwtResponse(accessToken)); // refresh-токен не возвращаем в теле
        } catch (AuthenticationException e) {
            logger.error("Authentication failed: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorDetails(new Date(), "Неверный email или пароль.", "/api/v1/auth/login"));
        } catch (Exception e) {
            logger.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorDetails(new Date(), "Произошла внутренняя ошибка при аутентификации.",
                            "/api/v1/auth/login"));
        }
    }



@PostMapping("/register")
public ResponseEntity<?> registerUser(@RequestBody @Valid RegisterRequest signUpRequest,
                                      BindingResult bindingResult) {
    // Проверяем, есть ли ошибки валидации
    if (bindingResult.hasErrors()) {
        StringBuilder errorMessage = new StringBuilder("Ошибка валидации: ");
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errorMessage.append(fieldError.getField())
                    .append(" - ")
                    .append(fieldError.getDefaultMessage())
                    .append("; ");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDetails(new Date(), errorMessage.toString(), "/api/v1/auth/register"));
    }

    try {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorDetails(new Date(), "Почта уже используется.", "/api/v1/auth/register"));
        }

        // Генерация случайного никнейма
        String randomUsername = UsernameGenerator.generateRandomUsername();

        // Хешируем пароль
        String hashedPassword = passwordEncoder.encode(signUpRequest.getPassword());

        // Создаем пользователя
        User user = new User(
                randomUsername, // Используем сгенерированный никнейм
                signUpRequest.getEmail(),
                hashedPassword);

        // Назначаем роль "ROLE_USER"
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Роль не найдена"));
        user.addRole(userRole);

        // Сохраняем пользователя
        userRepository.save(user);

        // Генерация токенов
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // Создаем HttpOnly куку для refresh-токена
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("lax")
                .build();

        // Возвращаем успешный ответ
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new JwtResponse(accessToken));

    } catch (RuntimeException e) {
        logger.error("Role assignment failed: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDetails(new Date(), "Не удалось найти роль пользователя.", "/api/v1/auth/register"));
    } catch (Exception e) {
        logger.error("Registration error: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDetails(new Date(), "Произошла ошибка при регистрации.", "/api/v1/auth/register"));
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
            if (!jwtUtil.validateToken(refreshTokenCookie)) {
                throw new RuntimeException("Invalid refresh token");
            }

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
                    .secure(false)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7 дней
                    .sameSite("lax") // Защита от CSRF
                    .build();

            // Возвращаем новый access-токен в теле ответа и новый refresh-токен в куке
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookieResponse.toString())
                    .body(new JwtResponse(newAccessToken)); // refresh-токен не возвращаем в теле
        } catch (RuntimeException e) {
            logger.error("Refresh token error: ", e);
            ErrorDetails errorDetails = new ErrorDetails(new Date(), e.getMessage(), "/api/v1/auth/refresh");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
        } catch (Exception e) {
            logger.error("Refresh token processing error: ", e);
            ErrorDetails errorDetails = new ErrorDetails(new Date(), "Произошла ошибка при обновлении токена.",
                    "/api/v1/auth/refresh");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletResponse response) {
        // Создаем пустую куку для удаления refresh-токена
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // Устанавливаем срок жизни куки в 0, чтобы удалить её
                .sameSite("lax")
                .build();

        // Возвращаем успешный ответ с пустой кукой
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new JwtResponse(null)); // Возвращаем только accessToken, refreshToken не нужен
    }
}
