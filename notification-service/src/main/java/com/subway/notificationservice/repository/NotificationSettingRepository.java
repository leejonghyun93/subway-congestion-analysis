package com.subway.notificationservice.repository;

import com.subway.notificationservice.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    List<NotificationSetting> findByEnabledTrue();

    List<NotificationSetting> findByLineNumberAndStationNameAndEnabledTrue(String lineNumber, String stationName);

    Optional<NotificationSetting> findByUserIdAndEmail(String userId, String email);

    List<NotificationSetting> findByUserId(String userId);
}