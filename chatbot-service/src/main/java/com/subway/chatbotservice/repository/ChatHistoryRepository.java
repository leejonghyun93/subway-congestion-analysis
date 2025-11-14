package com.subway.chatbotservice.repository;

import com.subway.chatbotservice.entity.ChatHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatHistoryRepository extends MongoRepository<ChatHistory, String> {

    List<ChatHistory> findBySessionIdOrderByTimestampDesc(String sessionId);

    List<ChatHistory> findByUserIdOrderByTimestampDesc(String userId);

    List<ChatHistory> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    long countByIntent(String intent);
}