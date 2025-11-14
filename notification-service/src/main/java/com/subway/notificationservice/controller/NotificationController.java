package com.subway.notificationservice.controller;

import com.subway.notificationservice.dto.ApiResponse;
import com.subway.notificationservice.dto.EmailRequest;
import com.subway.notificationservice.dto.NotificationResponse;
import com.subway.notificationservice.entity.NotificationHistory;
import com.subway.notificationservice.entity.NotificationSetting;
import com.subway.notificationservice.repository.NotificationHistoryRepository;
import com.subway.notificationservice.repository.NotificationSettingRepository;
import com.subway.notificationservice.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    /**
     * 이메일 수동 발송
     * POST /api/notification/email
     */
    @PostMapping("/email")
    public ResponseEntity<ApiResponse<String>> sendEmail(@Valid @RequestBody EmailRequest request) {
        log.info("Manual email request: to={}, subject={}", request.getTo(), request.getSubject());

        try {
            emailService.sendEmail(request);
            return ResponseEntity.ok(ApiResponse.success("이메일 발송 완료", null));
        } catch (Exception e) {
            log.error("Email sending failed: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("이메일 발송 실패: " + e.getMessage()));
        }
    }

    /**
     * 알림 이력 조회
     * GET /api/notification/history
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getHistory(
            @RequestParam(required = false) String recipient) {
        log.info("Get notification history: recipient={}", recipient);

        List<NotificationHistory> histories;
        if (recipient != null && !recipient.isEmpty()) {
            histories = notificationHistoryRepository.findByRecipientOrderBySentAtDesc(recipient);
        } else {
            histories = notificationHistoryRepository.findTop10ByOrderBySentAtDesc();
        }

        List<NotificationResponse> responses = histories.stream()
                .map(h -> NotificationResponse.builder()
                        .id(h.getId())
                        .notificationType(h.getNotificationType())
                        .recipient(h.getRecipient())
                        .subject(h.getSubject())
                        .status(h.getStatus())
                        .sentAt(h.getSentAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("알림 이력 조회 완료", responses));
    }

    /**
     * 알림 설정 등록
     * POST /api/notification/settings
     */
    @PostMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSetting>> createSetting(
            @Valid @RequestBody NotificationSetting setting) {
        log.info("Create notification setting: email={}", setting.getEmail());

        NotificationSetting saved = notificationSettingRepository.save(setting);
        return ResponseEntity.ok(ApiResponse.success("알림 설정 등록 완료", saved));
    }

    /**
     * 알림 설정 조회
     * GET /api/notification/settings
     */
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<List<NotificationSetting>>> getSettings(
            @RequestParam(required = false) String userId) {
        log.info("Get notification settings: userId={}", userId);

        List<NotificationSetting> settings;
        if (userId != null && !userId.isEmpty()) {
            settings = notificationSettingRepository.findByUserId(userId);
        } else {
            settings = notificationSettingRepository.findByEnabledTrue();
        }

        return ResponseEntity.ok(ApiResponse.success("알림 설정 조회 완료", settings));
    }

    /**
     * 알림 설정 삭제
     * DELETE /api/notification/settings/{id}
     */
    @DeleteMapping("/settings/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSetting(@PathVariable Long id) {
        log.info("Delete notification setting: id={}", id);

        notificationSettingRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("알림 설정 삭제 완료", null));
    }

    /**
     * 통계 조회
     * GET /api/notification/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getStats() {
        log.info("Get notification statistics");

        long totalSuccess = notificationHistoryRepository.countSuccessful();
        long totalFailed = notificationHistoryRepository.countFailed();

        var stats = new Object() {
            public final long successful = totalSuccess;
            public final long failed = totalFailed;
            public final long total = totalSuccess + totalFailed;
        };

        return ResponseEntity.ok(ApiResponse.success("통계 조회 완료", stats));
    }

    /**
     * 헬스 체크
     * GET /api/notification/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Notification Service is running", "OK"));
    }
}