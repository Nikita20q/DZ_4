package KosmoScan.services;

import KosmoScan.domain.Student;
import KosmoScan.domain.Submission;
import KosmoScan.domain.Work;
import KosmoScan.dto.SubmissionDto;
import KosmoScan.dto.SubmissionRequest;
import KosmoScan.enums.SubmissionStatus;
import KosmoScan.repository.SubmissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final StudentService studentService;
    private final WorkService workService;
    private final FileStorageService fileStorageService;
    private final WebClient.Builder webClientBuilder;

    @Value("${analysis-service.url:http://api-gateway:8080/api/analysis}")
    private String analysisServiceUrl;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            StudentService studentService,
            WorkService workService,
            FileStorageService fileStorageService,
            WebClient.Builder webClientBuilder) {
        this.submissionRepository = submissionRepository;
        this.studentService = studentService;
        this.workService = workService;
        this.fileStorageService = fileStorageService;
        this.webClientBuilder = webClientBuilder;
    }

    public SubmissionDto uploadSubmission(SubmissionRequest submissionRequest, MultipartFile file) throws IOException {

        Student student = studentService.findOrCreate(
                submissionRequest.getStudentFirstName(),
                submissionRequest.getStudentLastName(),
                submissionRequest.getStudentMiddleName());

        Work work = workService.findOrCreate(submissionRequest.getWorkTitle());

        String storedFilePath = fileStorageService.storeFile(file);

        String fileExtension = getFileExtension(file.getOriginalFilename());

        Submission submission = Submission.builder()
                .student(student)
                .work(work)
                .originFileName(file.getOriginalFilename())
                .storedFilePath(storedFilePath)
                .fileSize(file.getSize())
                .fileExtension(fileExtension)
                .submittedAt(LocalDateTime.now())
                .status(SubmissionStatus.UPLOADED)
                .build();

        submission = submissionRepository.save(submission);
        triggerAnalysis(submission.getId());
        return convertToDto(submission);
    }

    private void triggerAnalysis(Long submissionId) {
        try {
            log.info("Запуск анализа для submission id={}", submissionId);

            webClientBuilder.build()
                    .post()
                    .uri(analysisServiceUrl + "/submissions/{id}/analyze", submissionId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> log.info("Анализ запущен успешно для submission {}", submissionId),
                            error -> log.error("Не удалось запустить анализ для submission {}: {}",
                                    submissionId, error.getMessage()));

        } catch (Exception e) {
            log.error("Ошибка при запуске анализа для submission id={}", submissionId, e);
        }
    }

    public SubmissionDto getSubmissionById(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with id: " + id));
        return convertToDto(submission);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private SubmissionDto convertToDto(Submission submission) {
        return SubmissionDto.builder()
                .id(submission.getId())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getLastName() + " " +
                        submission.getStudent().getFirstName() + " " +
                        submission.getStudent().getMiddleName())
                .workId(submission.getWork().getId())
                .workTitle(submission.getWork().getTitle())
                .originalFileName(submission.getOriginFileName())
                .fileSize(submission.getFileSize())
                .fileExtension(submission.getFileExtension())
                .storedFilePath(submission.getStoredFilePath())
                .submittedAt(submission.getSubmittedAt())
                .status(submission.getStatus())
                .analysisReportId(submission.getAnalysisReportId())
                .build();
    }
}