package com.subway.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String notificationType;  // EMAIL, PUSH, SMS

    @Column(nullable = false)
    private String recipient;  // 수신자 (이메일 또는 디바이스 토큰)

    @Column(nullable = false)
    private String subject;  // 제목

    @Column(columnDefinition = "TEXT")
    private String content;  // 내용

    private String lineNumber;  // 호선 번호
    private String stationName;  // 역 이름
    private Double congestion;  // 혼잡도

    @Column(nullable = false)
    private String status;  // SUCCESS, FAILED

    private String errorMessage;  // 실패 시 오류 메시지

    @Column(nullable = false)
    private LocalDateTime sentAt;  // 발송 시간

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}