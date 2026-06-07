package kosmoscan.analysis.controller;

import kosmoscan.analysis.repository.AnalysisReportRepository;
import kosmoscan.analysis.domain.AnalysisReport;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/wordcloud")
public class WordCloudController {

    private final AnalysisReportRepository reportRepository;

    public WordCloudController(AnalysisReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<Resource> getWordCloud(@PathVariable Long submissionId) {
        try {
            AnalysisReport report = reportRepository.findBySubmissionId(submissionId)
                    .orElseThrow(() -> new RuntimeException("Отчет не найден"));

            if (report.getWordCloudPath() == null) {
                return ResponseEntity.notFound().build();
            }

            Path path = Paths.get(report.getWordCloudPath());
            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"wordcloud_" + submissionId + ".png\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}