package kosmoscan.controller;

import kosmoscan.analysis.controller.AnalysisController;
import kosmoscan.analysis.domain.AnalysisReport;
import kosmoscan.analysis.dto.AnalysisReportDto;
import kosmoscan.analysis.service.AnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisControllerTest {

    @Mock
    private AnalysisService analysisService;

    @InjectMocks
    private AnalysisController analysisController;

    private AnalysisReportDto testReportDto;

    @BeforeEach
    void setUp() {
        testReportDto = AnalysisReportDto.builder()
                .id(1L)
                .submissionId(1L)
                .status("COMPLETED")
                .formatValid(true)
                .sizeValid(true)
                .errorMessage(null)
                .studentName("Иванов Иван Иванович")
                .workTitle("Контрольная №1")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void analyzeSubmission_Success() {
        // Arrange
        when(analysisService.analyzeSubmission(1L)).thenReturn(testReportDto);

        // Act
        ResponseEntity<AnalysisReportDto> response = analysisController.analyzeSubmission(1L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("COMPLETED", response.getBody().getStatus());

        verify(analysisService).analyzeSubmission(1L);
    }

    @Test
    void getAllReports_Success() {
        // Arrange
        List<AnalysisReportDto> reports = Arrays.asList(
                testReportDto,
                AnalysisReportDto.builder()
                        .id(2L)
                        .submissionId(2L)
                        .status("FAILED")
                        .formatValid(false)
                        .sizeValid(true)
                        .studentName("Петров Петр Петрович")
                        .workTitle("Контрольная №2")
                        .createdAt(LocalDateTime.now())
                        .completedAt(LocalDateTime.now())
                        .build()
        );

        when(analysisService.getAllReports()).thenReturn(reports);

        // Act
        ResponseEntity<List<AnalysisReportDto>> response = analysisController.getAllReports();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(analysisService).getAllReports();
    }

    @Test
    void getReportBySubmissionId_Found() {
        // Arrange
        when(analysisService.getReportBySubmissionId(1L))
                .thenReturn(Optional.of(testReportDto));

        // Act
        ResponseEntity<AnalysisReportDto> response = analysisController.getReportBySubmissionId(1L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(analysisService).getReportBySubmissionId(1L);
    }

    @Test
    void getReportBySubmissionId_NotFound() {
        // Arrange
        when(analysisService.getReportBySubmissionId(999L))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<AnalysisReportDto> response = analysisController.getReportBySubmissionId(999L);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(analysisService).getReportBySubmissionId(999L);
    }
}