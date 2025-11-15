package com.subway.chatbotservice.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class IntentClassifier {

    // 의도 분류
    public IntentResult classify(String message) {
        String normalized = message.toLowerCase();

        // 1. 실시간 혼잡도 조회
        if (normalized.contains("지금") || normalized.contains("현재") ||
                normalized.contains("혼잡") && !normalized.contains("예측")) {
            return extractStationInfo(message, "REALTIME_CONGESTION");
        }

        // 2. 혼잡도 예측
        if (normalized.contains("예측") || normalized.contains("내일") ||
                normalized.contains("예상") || normalized.contains("미래")) {
            return extractStationInfo(message, "PREDICTION");
        }

        // 3. 통계 조회
        if (normalized.contains("통계") || normalized.contains("평균") ||
                normalized.contains("추이") || normalized.contains("트렌드")) {
            return extractStationInfo(message, "STATISTICS");
        }

        // 4. TOP N 조회
        if (normalized.contains("가장") || normalized.contains("제일") ||
                normalized.contains("순위") || normalized.contains("top")) {
            return new IntentResult("TOP_CONGESTED", null, null);
        }

        // 기본값
        return new IntentResult("UNKNOWN", null, null);
    }

    // 역 이름과 호선 추출
    private IntentResult extractStationInfo(String message, String intent) {
        String lineNumber = extractLineNumber(message);
        String stationName = extractStationName(message);

        return new IntentResult(intent, lineNumber, stationName);
    }

    // 호선 번호 추출 (예: "2호선" → "2")
    private String extractLineNumber(String message) {
        Pattern pattern = Pattern.compile("(\\d+)호선");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // 역 이름 추출 (간단한 방식)
    private String extractStationName(String message) {
        // 일반적인 역 이름 패턴
        String[] stations = {
                "강남", "역삼", "선릉", "삼성", "종합운동장",
                "신림", "서울대입구", "봉천", "신대방", "구로디지털단지",
                "신도림", "영등포구청", "당산", "합정", "홍대입구",
                "신촌", "이대", "아현", "충정로", "시청"
        };

        for (String station : stations) {
            // "강남역" 또는 "강남" 모두 매칭
            if (message.contains(station + "역") || message.contains(station)) {
                return station;  // "역" 없이 반환 (나중에 추가됨)
            }
        }

        return null;
    }

    @Data
    @AllArgsConstructor
    public static class IntentResult {
        private String intent;
        private String lineNumber;
        private String stationName;
    }
}