package com.subway.userservice.service;

import com.subway.userservice.dto.UserProfileResponse;
import com.subway.userservice.entity.User;
import com.subway.userservice.entity.UserPreference;
import com.subway.userservice.repository.UserPreferenceRepository;
import com.subway.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    /**
     * 사용자 프로필 조회
     */
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    /**
     * 사용자 설정 조회
     */
    public UserPreference getUserPreference(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        return userPreferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreference(user));
    }

    /**
     * 사용자 설정 업데이트
     */
    @Transactional
    public UserPreference updateUserPreference(String username, UserPreference preference) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        UserPreference existingPreference = userPreferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreference(user));

        existingPreference.setFavoriteLineNumber(preference.getFavoriteLineNumber());
        existingPreference.setFavoriteStationName(preference.getFavoriteStationName());
        existingPreference.setNotificationEnabled(preference.getNotificationEnabled());
        existingPreference.setCongestionThreshold(preference.getCongestionThreshold());

        return userPreferenceRepository.save(existingPreference);
    }

    private UserPreference createDefaultPreference(User user) {
        UserPreference preference = UserPreference.builder()
                .user(user)
                .notificationEnabled(true)
                .congestionThreshold(80.0)
                .build();
        return userPreferenceRepository.save(preference);
    }
}