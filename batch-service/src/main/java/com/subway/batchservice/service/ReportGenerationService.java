package com.subway.batchservice.service;

import com.subway.batchservice.entity.DailyStatistics;
import com.subway.batchservice.entity.WeeklyReport;
import com.subway.batchservice.repository.DailyStatisticsRepository;
import com.subway.batchservice.repository.WeeklyReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGenerationService {

    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final WeeklyReportRepository weeklyReportRepository;

    @Value("${batch.report.output-path}")
    private String outputPath;

    /**
     * 주간 리포트 Excel 생성
     */
    public String generateWeeklyReport(LocalDate startDate, LocalDate endDate) throws IOException {
        log.info("Generating weekly report: {} to {}", startDate, endDate);

        List<DailyStatistics> weeklyData = dailyStatisticsRepository
                .findByStatisticsDateBetween(startDate, endDate);

        if (weeklyData.isEmpty()) {
            log.warn("No data found for weekly report");
            return null;
        }

        // Excel 생성
        Workbook workbook = new XSSFWorkbook();

        // Sheet 1: TOP 10 혼잡 역
        createTopCongestedSheet(workbook, weeklyData);

        // Sheet 2: 일별 통계
        createDailyStatisticsSheet(workbook, weeklyData);

        // 파일 저장
        String fileName = String.format("weekly_report_%s_to_%s.xlsx",
                startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        String filePath = outputPath + "/" + fileName;

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            workbook.write(outputStream);
        }

        workbook.close();

        // DB에 저장
        WeeklyReport report = WeeklyReport.builder()
                .weekStartDate(startDate)
                .weekEndDate(endDate)
                .weekNumber(startDate.getDayOfYear() / 7)
                .reportFilePath(filePath)
                .status("GENERATED")
                .build();
        weeklyReportRepository.save(report);

        log.info("Weekly report generated: {}", filePath);
        return filePath;
    }

    private void createTopCongestedSheet(Workbook workbook, List<DailyStatistics> data) {
        Sheet sheet = workbook.createSheet("TOP 10 혼잡 역");

        // 헤더 스타일
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = {"순위", "호선", "역 이름", "평균 혼잡도", "최대 혼잡도", "최고 혼잡 시간"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // TOP 10 데이터
        List<DailyStatistics> top10 = data.stream()
                .sorted(Comparator.comparing(DailyStatistics::getAvgCongestion).reversed())
                .limit(10)
                .collect(Collectors.toList());

        int rowNum = 1;
        for (int i = 0; i < top10.size(); i++) {
            DailyStatistics stat = top10.get(i);
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(i + 1);
            row.createCell(1).setCellValue(stat.getLineNumber());
            row.createCell(2).setCellValue(stat.getStationName());
            row.createCell(3).setCellValue(String.format("%.2f%%", stat.getAvgCongestion()));
            row.createCell(4).setCellValue(String.format("%.2f%%", stat.getMaxCongestion()));
            row.createCell(5).setCellValue(stat.getPeakHour() + "시");
        }

        // 열 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createDailyStatisticsSheet(Workbook workbook, List<DailyStatistics> data) {
        Sheet sheet = workbook.createSheet("일별 통계");

        // 헤더
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"날짜", "호선", "역 이름", "평균 혼잡도", "최대 혼잡도", "최소 혼잡도", "데이터 건수"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 데이터
        int rowNum = 1;
        for (DailyStatistics stat : data) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(stat.getStatisticsDate().toString());
            row.createCell(1).setCellValue(stat.getLineNumber());
            row.createCell(2).setCellValue(stat.getStationName());
            row.createCell(3).setCellValue(String.format("%.2f%%", stat.getAvgCongestion()));
            row.createCell(4).setCellValue(String.format("%.2f%%", stat.getMaxCongestion()));
            row.createCell(5).setCellValue(String.format("%.2f%%", stat.getMinCongestion()));
            row.createCell(6).setCellValue(stat.getTotalRecords());
        }

        // 열 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}