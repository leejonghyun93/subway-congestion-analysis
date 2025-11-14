package com.subway.batchservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_job_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jobName;  // Job 이름

    @Column(nullable = false)
    private String jobType;  // DAILY, WEEKLY, MONTHLY

    @Column(nullable = false)
    private String status;   // SUCCESS, FAILED, RUNNING

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Long executionTimeMs;  // 실행 시간 (밀리초)

    private Integer processedRecords;  // 처리된 레코드 수
    private Integer failedRecords;     // 실패한 레코드 수

    @Column(columnDefinition = "TEXT")
    private String errorMessage;  // 에러 메시지

    @Column(columnDefinition = "TEXT")
    private String jobParameters;  // Job 파라미터 (JSON)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}