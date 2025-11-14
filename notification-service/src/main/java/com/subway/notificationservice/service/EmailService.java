package com.subway.notificationservice.service;

import com.subway.notificationservice.dto.EmailRequest;
import com.subway.notificationservice.entity.NotificationHistory;
import com.subway.notificationservice.repository.NotificationHistoryRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationHistoryRepository notificationHistoryRepository;

    @Value("${notification.email.from}")
    private String fromEmail;

    public void sendEmail(EmailRequest request) {
        log.info("Sending email to: {}, subject: {}", request.getTo(), request.getSubject());

        NotificationHistory history = NotificationHistory.builder()
                .notificationType("EMAIL")
                .recipient(request.getTo())
                .subject(request.getSubject())
                .content(request.getContent())
                .lineNumber(request.getLineNumber())
                .stationName(request.getStationName())
                .congestion(request.getCongestion())
                .sentAt(LocalDateTime.now())
                .build();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getContent(), true);  // HTML 지원

            mailSender.send(message);

            history.setStatus("SUCCESS");
            log.info("Email sent successfully to: {}", request.getTo());

        } catch (MessagingException e) {
            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
            log.error("Failed to send email to {}: {}", request.getTo(), e.getMessage());
        } finally {
            notificationHistoryRepository.save(history);
        }
    }

    public String buildCongestionAlertEmailContent(String lineNumber, String stationName, Double congestion) {
        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2 style="color: #d32f2f;">🚇 지하철 혼잡도 알림</h2>
                    <div style="background-color: #f5f5f5; padding: 20px; border-radius: 5px;">
                        <p><strong>호선:</strong> %s호선</p>
                        <p><strong>역:</strong> %s</p>
                        <p><strong>혼잡도:</strong> <span style="color: #d32f2f; font-size: 20px; font-weight: bold;">%.1f%%</span></p>
                        <p style="margin-top: 20px;">현재 해당 역이 매우 혼잡합니다. 다른 시간대를 이용하시거나 대체 경로를 고려해보세요.</p>
                    </div>
                    <p style="color: #666; font-size: 12px; margin-top: 20px;">
                        본 메일은 지하철 혼잡도 알림 시스템에서 자동으로 발송되었습니다.
                    </p>
                </body>
                </html>
                """, lineNumber, stationName, congestion);
    }
}