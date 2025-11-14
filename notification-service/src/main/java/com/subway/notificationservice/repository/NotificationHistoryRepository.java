package com.subway.notificationservice.repository;

import com.subway.notificationservice.entity.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    List<NotificationHistory> findByRecipientOrderBySentAtDesc(String recipient);

    List<NotificationHistory> findBySentAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(n) FROM NotificationHistory n WHERE n.status = 'SUCCESS'")
    long countSuccessful();

    @Query("SELECT COUNT(n) FROM NotificationHistory n WHERE n.status = 'FAILED'")
    long countFailed();

    List<NotificationHistory> findTop10ByOrderBySentAtDesc();
}