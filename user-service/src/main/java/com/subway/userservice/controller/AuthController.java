package com.subway.userservice.controller;

import com.subway.userservice.dto.*;
import com.subway.userservice.entity.User;
import com.subway.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<User>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request: {}", request.getUsername());

        try {
            User user = authService.signup(request);
            return ResponseEntity.ok(ApiResponse.success("회원가입 성공", user));
        } catch (Exception e) {
            log.error("Signup failed: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("회원가입 실패: " + e.getMessage()));
        }
    }

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request: {}", request.getUsername());

        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("로그인 실패: " + e.getMessage()));
        }
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String token) {
        // Token에서 username 추출하여 로그아웃 처리
        // 실제 구현 시 JwtTokenProvider를 통해 username 추출
        log.info("Logout request");

        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공", null));
    }

    /**
     * Token 갱신
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request");

        try {
            AuthResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Token 갱신 성공", response));
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Token 갱신 실패: " + e.getMessage()));
        }
    }
}