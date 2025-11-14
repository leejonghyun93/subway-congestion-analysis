package com.subway.batchservice.repository;

import com.subway.batchservice.entity.BatchJobHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BatchJobHistoryRepository extends JpaRepository<BatchJobHistory, Long> {

    List<BatchJobHistory> findByJobNameOrderByCreatedAtDesc(String jobName);

    List<BatchJobHistory> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(b) FROM BatchJobHistory b WHERE b.status = 'SUCCESS'")
    long countSuccessful();

    @Query("SELECT COUNT(b) FROM BatchJobHistory b WHERE b.status = 'FAILED'")
    long countFailed();

    List<BatchJobHistory> findTop10ByOrderByCreatedAtDesc();
}