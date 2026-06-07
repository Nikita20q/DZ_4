package kosmoscan.analysis.controller;

import kosmoscan.analysis.dto.AnalysisReportDto;
import kosmoscan.analysis.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis API", description = "API для анализа сданных работ")
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/submissions/{submissionId}/analyze")
    @Operation(summary = "Анализировать работу",
            description = "Автоматический запуск анализа после загрузки файла")
    public ResponseEntity<AnalysisReportDto> analyzeSubmission(@PathVariable Long submissionId) {
        AnalysisReportDto report = analysisService.analyzeSubmission(submissionId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/submission/{submissionId}")
    @Operation(summary = "Получить отчет",
            description = "Получение отчета анализа по ID submission")
    public ResponseEntity<AnalysisReportDto> getReportBySubmissionId(@PathVariable Long submissionId) {
        return analysisService.getReportBySubmissionId(submissionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reports")
    @Operation(summary = "Получить все отчеты",
            description = "Получение списка всех отчетов анализа")
    public ResponseEntity<List<AnalysisReportDto>> getAllReports() {
        return ResponseEntity.ok(analysisService.getAllReports());
    }
}