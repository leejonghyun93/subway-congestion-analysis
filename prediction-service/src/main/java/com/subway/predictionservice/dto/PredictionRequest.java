package com.subway.predictionservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {

    @NotBlank(message = "호선 번호는 필수입니다")
    private String lineNumber;

    @NotBlank(message = "역 이름은 필수입니다")
    private String stationName;

    @NotNull(message = "시간대는 필수입니다")
    @Min(value = 0, message = "시간대는 0-23 사이여야 합니다")
    @Max(value = 23, message = "시간대는 0-23 사이여야 합니다")
    private Integer hourSlot;

    private Integer dayOfWeek;  // 0=일요일, 6=토요일

    private Boolean isHoliday;  // 공휴일 여부
}