package kosmoscan.analysis.service;

import kosmoscan.analysis.domain.AnalysisReport;
import kosmoscan.analysis.dto.AnalysisReportDto;
import kosmoscan.analysis.dto.SubmissionDto;
import kosmoscan.analysis.repository.AnalysisReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnalysisService {

    private final AnalysisReportRepository reportRepository;
    private final WebClient.Builder webClientBuilder;
    private final FileTextExtractor fileTextExtractor;
    private final WordCloudService wordCloudService;

    @Value("${file-storing-service.url:http://file-storing-service:8081}")
    private String fileStoringServiceUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "docx", "txt");
    private static final long MAX_FILE_SIZE = 1_000_000;

    public AnalysisService(
            AnalysisReportRepository reportRepository,
            WebClient.Builder webClientBuilder,
            FileTextExtractor fileTextExtractor,
            WordCloudService wordCloudService) {
        this.reportRepository = reportRepository;
        this.webClientBuilder = webClientBuilder;
        this.fileTextExtractor = fileTextExtractor;
        this.wordCloudService = wordCloudService;
    }

    public AnalysisReportDto analyzeSubmission(Long submissionId) {
        log.info("Начало анализа submission id={}", submissionId);

        SubmissionDto submission = getSubmissionFromStoringService(submissionId);
        if (submission == null) {
            log.error("Submission не найден: {}", submissionId);
            throw new RuntimeException("Submission not found: " + submissionId);
        }

        Optional<AnalysisReport> existingReport = reportRepository.findBySubmissionId(submissionId);
        if (existingReport.isPresent()) {
            log.info("Отчет уже существует для submission id={}", submissionId);
            return convertToDto(existingReport.get());
        }

        AnalysisReport report = AnalysisReport.builder()
                .submissionId(submissionId)
                .status("PROCESSING")
                .studentName(submission.getStudentName())
                .workTitle(submission.getWorkTitle())
                .createdAt(LocalDateTime.now())
                .build();

        try {
            boolean formatValid = validateFileExtension(submission.getFileExtension());
            report.setFormatValid(formatValid);

            boolean sizeValid = validateFileSize(submission.getFileSize());
            report.setSizeValid(sizeValid);

            if (formatValid && sizeValid) {
                report.setStatus("COMPLETED");

                try {
                    String text = fileTextExtractor.extractText(
                            submission.getStoredFilePath(),
                            submission.getFileExtension()
                    );
                    String wordCloudPath = wordCloudService.generateAndSave(text);
                    report.setWordCloudPath(wordCloudPath);
                    log.info("Облако слов сгенерировано для submission id={}", submissionId);
                } catch (Exception e) {
                    log.warn("Не удалось сгенерировать облако слов для submission id={}: {}",
                            submissionId, e.getMessage());
                }

                log.info("Анализ успешен для submission id={}", submissionId);
            } else {
                report.setStatus("FAILED");
                StringBuilder errorMsg = new StringBuilder();
                if (!formatValid) {
                    errorMsg.append("Недопустимый формат файла. Разрешены: ")
                            .append(ALLOWED_EXTENSIONS);
                }
                if (!sizeValid) {
                    if (errorMsg.length() > 0) errorMsg.append("; ");
                    errorMsg.append("Файл слишком большой (максимум 1 MB)");
                }
                report.setErrorMessage(errorMsg.toString());
                log.warn("Анализ не пройден для submission id={}: {}",
                        submissionId, report.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("Ошибка при анализе submission id={}", submissionId, e);
            report.setStatus("FAILED");
            report.setErrorMessage("Ошибка анализа: " + e.getMessage());
        }

        report.setCompletedAt(LocalDateTime.now());
        report = reportRepository.save(report);
        log.info("Отчет сохранен с id={}", report.getId());

        return convertToDto(report);
    }

    public Optional<AnalysisReportDto> getReportBySubmissionId(Long submissionId) {
        return reportRepository.findBySubmissionId(submissionId)
                .map(this::convertToDto);
    }

    public List<AnalysisReportDto> getAllReports() {
        return reportRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private SubmissionDto getSubmissionFromStoringService(Long submissionId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(fileStoringServiceUrl + "/api/submissions/{id}", submissionId)
                    .retrieve()
                    .bodyToMono(SubmissionDto.class)
                    .block();
        } catch (Exception e) {
            log.error("Ошибка при получении submission id={}: {}", submissionId, e.getMessage());
            return null;
        }
    }

    private boolean validateFileExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    private boolean validateFileSize(Long fileSize) {
        return fileSize != null && fileSize <= MAX_FILE_SIZE;
    }

    private AnalysisReportDto convertToDto(AnalysisReport report) {
        return AnalysisReportDto.builder()
                .id(report.getId())
                .submissionId(report.getSubmissionId())
                .status(report.getStatus())
                .formatValid(report.getFormatValid())
                .sizeValid(report.getSizeValid())
                .errorMessage(report.getErrorMessage())
                .studentName(report.getStudentName())
                .workTitle(report.getWorkTitle())
                .wordCloudPath(report.getWordCloudPath())
                .createdAt(report.getCreatedAt())
                .completedAt(report.getCompletedAt())
                .build();
    }
}