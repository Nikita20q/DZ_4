package kosmoscan.service;

import kosmoscan.analysis.domain.AnalysisReport;
import kosmoscan.analysis.dto.AnalysisReportDto;
import kosmoscan.analysis.dto.SubmissionDto;
import kosmoscan.analysis.repository.AnalysisReportRepository;
import kosmoscan.analysis.service.AnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private AnalysisReportRepository reportRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AnalysisService analysisService;

    @BeforeEach
    void setUp() {
        // Установка URL через Reflection
        ReflectionTestUtils.setField(
                analysisService,
                "fileStoringServiceUrl",
                "http://localhost:8081"
        );

        // Настройка цепочки WebClient (lenient - чтобы не падало, если не все вызовы используются)
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString(), anyLong()))
                .thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void analyzeSubmission_ValidFile_ReturnsCompletedReport() {
        // Arrange
        SubmissionDto submission = createSubmission(1L, "pdf", 1024L);

        when(responseSpec.bodyToMono(SubmissionDto.class))
                .thenReturn(Mono.just(submission));
        when(reportRepository.findBySubmissionId(1L))
                .thenReturn(Optional.empty());
        when(reportRepository.save(any(AnalysisReport.class)))
                .thenAnswer(invocation -> {
                    AnalysisReport report = invocation.getArgument(0);
                    report.setId(1L);
                    return report;
                });

        // Act
        AnalysisReportDto result = analysisService.analyzeSubmission(1L);

        // Assert
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertTrue(result.getFormatValid());
        assertTrue(result.getSizeValid());
        assertNull(result.getErrorMessage());
        assertEquals("Иванов Иван Иванович", result.getStudentName());
        assertEquals("Контрольная №1", result.getWorkTitle());

        verify(reportRepository).save(any(AnalysisReport.class));
    }

    @Test
    void analyzeSubmission_InvalidFormat_ReturnsFailedReport() {
        // Arrange
        SubmissionDto submission = createSubmission(2L, "png", 512L);

        when(responseSpec.bodyToMono(SubmissionDto.class))
                .thenReturn(Mono.just(submission));
        when(reportRepository.findBySubmissionId(2L))
                .thenReturn(Optional.empty());
        when(reportRepository.save(any(AnalysisReport.class)))
                .thenAnswer(invocation -> {
                    AnalysisReport report = invocation.getArgument(0);
                    report.setId(2L);
                    return report;
                });

        // Act
        AnalysisReportDto result = analysisService.analyzeSubmission(2L);

        // Assert
        assertNotNull(result);
        assertEquals("FAILED", result.getStatus());
        assertFalse(result.getFormatValid());
        assertTrue(result.getSizeValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Недопустимый формат"));
    }

    @Test
    void analyzeSubmission_TooLargeFile_ReturnsFailedReport() {
        // Arrange
        SubmissionDto submission = createSubmission(3L, "pdf", 2_000_000L);

        when(responseSpec.bodyToMono(SubmissionDto.class))
                .thenReturn(Mono.just(submission));
        when(reportRepository.findBySubmissionId(3L))
                .thenReturn(Optional.empty());
        when(reportRepository.save(any(AnalysisReport.class)))
                .thenAnswer(invocation -> {
                    AnalysisReport report = invocation.getArgument(0);
                    report.setId(3L);
                    return report;
                });

        // Act
        AnalysisReportDto result = analysisService.analyzeSubmission(3L);

        // Assert
        assertNotNull(result);
        assertEquals("FAILED", result.getStatus());
        assertTrue(result.getFormatValid());
        assertFalse(result.getSizeValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("слишком большой"));
    }

    @Test
    void analyzeSubmission_ExistingReport_ReturnsExisting() {
        // Arrange
        SubmissionDto submission = createSubmission(1L, "pdf", 1024L);

        // ВАЖНО: WebClient должен вернуть submission (сервис сначала получает его)
        when(responseSpec.bodyToMono(SubmissionDto.class))
                .thenReturn(Mono.just(submission));

        AnalysisReport existingReport = AnalysisReport.builder()
                .id(10L)
                .submissionId(1L)
                .status("COMPLETED")
                .formatValid(true)
                .sizeValid(true)
                .studentName("Иванов Иван Иванович")
                .workTitle("Контрольная №1")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        when(reportRepository.findBySubmissionId(1L))
                .thenReturn(Optional.of(existingReport));

        // Act
        AnalysisReportDto result = analysisService.analyzeSubmission(1L);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("COMPLETED", result.getStatus());
        assertEquals("Иванов Иван Иванович", result.getStudentName());
        assertEquals("Контрольная №1", result.getWorkTitle());

        // Отчёт не должен сохраняться заново
        verify(reportRepository, never()).save(any());
    }

    @Test
    void analyzeSubmission_SubmissionNotFound_ThrowsException() {
        // Arrange
        when(responseSpec.bodyToMono(SubmissionDto.class))
                .thenReturn(Mono.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            analysisService.analyzeSubmission(999L);
        });

        assertTrue(exception.getMessage().contains("Submission not found"));
    }

    @Test
    void getReportBySubmissionId_ExistingReport_ReturnsDto() {
        // Arrange
        AnalysisReport report = AnalysisReport.builder()
                .id(1L)
                .submissionId(1L)
                .status("COMPLETED")
                .formatValid(true)
                .sizeValid(true)
                .studentName("Иванов Иван Иванович")
                .workTitle("Контрольная №1")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        when(reportRepository.findBySubmissionId(1L))
                .thenReturn(Optional.of(report));

        // Act
        Optional<AnalysisReportDto> result = analysisService.getReportBySubmissionId(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getSubmissionId());
        assertEquals("COMPLETED", result.get().getStatus());
        assertEquals("Иванов Иван Иванович", result.get().getStudentName());

        verify(reportRepository).findBySubmissionId(1L);
    }

    @Test
    void getReportBySubmissionId_NotFound_ReturnsEmpty() {
        // Arrange
        when(reportRepository.findBySubmissionId(999L))
                .thenReturn(Optional.empty());

        // Act
        Optional<AnalysisReportDto> result = analysisService.getReportBySubmissionId(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(reportRepository).findBySubmissionId(999L);
    }

    @Test
    void getAllReports_ReturnsAllReports() {
        // Arrange
        AnalysisReport report1 = AnalysisReport.builder()
                .id(1L)
                .submissionId(1L)
                .status("COMPLETED")
                .formatValid(true)
                .sizeValid(true)
                .studentName("Иванов Иван Иванович")
                .workTitle("Контрольная №1")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        AnalysisReport report2 = AnalysisReport.builder()
                .id(2L)
                .submissionId(2L)
                .status("FAILED")
                .formatValid(false)
                .sizeValid(true)
                .studentName("Петров Петр Петрович")
                .workTitle("Контрольная №2")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        when(reportRepository.findAll())
                .thenReturn(Arrays.asList(report1, report2));

        // Act
        List<AnalysisReportDto> result = analysisService.getAllReports();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("COMPLETED", result.get(0).getStatus());
        assertEquals("FAILED", result.get(1).getStatus());
        assertEquals("Иванов Иван Иванович", result.get(0).getStudentName());
        assertEquals("Петров Петр Петрович", result.get(1).getStudentName());

        verify(reportRepository).findAll();
    }

    // Вспомогательный метод для создания SubmissionDto
    private SubmissionDto createSubmission(Long id, String extension, Long size) {
        SubmissionDto dto = new SubmissionDto();
        dto.setId(id);
        dto.setStudentName("Иванов Иван Иванович");
        dto.setWorkTitle("Контрольная №1");
        dto.setFileExtension(extension);
        dto.setFileSize(size);
        return dto;
    }
}