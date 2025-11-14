package com.subway.notificationservice.service;

import com.subway.notificationservice.dto.CongestionAlert;
import com.subway.notificationservice.dto.EmailRequest;
import com.subway.notificationservice.entity.NotificationSetting;
import com.subway.notificationservice.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final EmailService emailService;

    @Value("${notification.congestion-threshold}")
    private Double congestionThreshold;

    public void processCongestionAlert(CongestionAlert alert) {
        log.info("Processing congestion alert: {}", alert);

        // 혼잡도가 임계값 이하면 무시
        if (alert.getCongestion() < congestionThreshold) {
            log.debug("Congestion {} is below threshold {}, skipping notification",
                    alert.getCongestion(), congestionThreshold);
            return;
        }

        // 해당 역을 구독하는 사용자 조회
        List<NotificationSetting> settings = notificationSettingRepository
                .findByLineNumberAndStationNameAndEnabledTrue(
                        alert.getLineNumber(),
                        alert.getStationName()
                );

        log.info("Found {} subscribers for line={}, station={}",
                settings.size(), alert.getLineNumber(), alert.getStationName());

        // 각 구독자에게 알림 발송
        for (NotificationSetting setting : settings) {
            // 사용자별 임계값 체크
            if (setting.getThresholdCongestion() != null &&
                    alert.getCongestion() < setting.getThresholdCongestion()) {
                continue;
            }

            sendCongestionNotification(setting, alert);
        }
    }

    private void sendCongestionNotification(NotificationSetting setting, CongestionAlert alert) {
        String subject = String.format("[혼잡도 알림] %s호선 %s - %.1f%%",
                alert.getLineNumber(), alert.getStationName(), alert.getCongestion());

        String content = emailService.buildCongestionAlertEmailContent(
                alert.getLineNumber(),
                alert.getStationName(),
                alert.getCongestion()
        );

        EmailRequest emailRequest = EmailRequest.builder()
                .to(setting.getEmail())
                .subject(subject)
                .content(content)
                .lineNumber(alert.getLineNumber())
                .stationName(alert.getStationName())
                .congestion(alert.getCongestion())
                .build();

        emailService.sendEmail(emailRequest);
    }
}