package com.subway.notificationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NotBlank(message = "수신자 이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String to;

    @NotBlank(message = "제목을 입력해주세요")
    private String subject;

    @NotBlank(message = "내용을 입력해주세요")
    private String content;

    private String lineNumber;
    private String stationName;
    private Double congestion;
}