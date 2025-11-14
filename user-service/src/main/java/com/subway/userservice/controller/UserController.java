package com.subway.userservice.controller;

import com.subway.userservice.dto.ApiResponse;
import com.subway.userservice.dto.UserProfileResponse;
import com.subway.userservice.entity.UserPreference;
import com.subway.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 프로필 조회
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication authentication) {
        log.info("Get my profile: {}", authentication.getName());

        try {
            UserProfileResponse profile = userService.getUserProfile(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("프로필 조회 성공", profile));
        } catch (Exception e) {
            log.error("Get profile failed: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("프로필 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 내 설정 조회
     * GET /api/users/preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<UserPreference>> getMyPreferences(Authentication authentication) {
        log.info("Get my preferences: {}", authentication.getName());

        try {
            UserPreference preference = userService.getUserPreference(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("설정 조회 성공", preference));
        } catch (Exception e) {
            log.error("Get preferences failed: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("설정 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 내 설정 업데이트
     * PUT /api/users/preferences
     */
    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<UserPreference>> updateMyPreferences(
            Authentication authentication,
            @RequestBody UserPreference preference) {
        log.info("Update my preferences: {}", authentication.getName());

        try {
            UserPreference updated = userService.updateUserPreference(authentication.getName(), preference);
            return ResponseEntity.ok(ApiResponse.success("설정 업데이트 성공", updated));
        } catch (Exception e) {
            log.error("Update preferences failed: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("설정 업데이트 실패: " + e.getMessage()));
        }
    }

    /**
     * 헬스 체크
     * GET /api/users/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("User Service is running", "OK"));
    }
}