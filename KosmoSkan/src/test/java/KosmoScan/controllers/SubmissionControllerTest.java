package KosmoScan.controllers;

import KosmoScan.dto.SubmissionDto;
import KosmoScan.enums.SubmissionStatus;
import KosmoScan.services.SubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionControllerTest {

    @Mock
    private SubmissionService submissionService;

    @InjectMocks
    private SubmissionController submissionController;

    private SubmissionDto testSubmissionDto;

    @BeforeEach
    void setUp() {
        testSubmissionDto = SubmissionDto.builder()
                .id(1L)
                .studentId(1L)
                .studentName("Иванов Иван Иванович")
                .workId(1L)
                .workTitle("Контрольная №1")
                .originalFileName("test.pdf")
                .fileSize(1024L)
                .fileExtension("pdf")
                .submittedAt(LocalDateTime.now())
                .status(SubmissionStatus.UPLOADED)
                .build();
    }

    @Test
    void uploadSubmission_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(submissionService.uploadSubmission(any(), any()))
                .thenReturn(testSubmissionDto);

        ResponseEntity<SubmissionDto> response = submissionController.uploadSubmission(
                "Иван",
                "Иванов",
                "Иванович",
                "Контрольная №1",
                file
        );

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Иванов Иван Иванович", response.getBody().getStudentName());
        assertEquals("test.pdf", response.getBody().getOriginalFileName());

        verify(submissionService).uploadSubmission(any(), any());
    }

    @Test
    void getSubmission_Success() {
        when(submissionService.getSubmissionById(1L)).thenReturn(testSubmissionDto);

        ResponseEntity<SubmissionDto> response = submissionController.getSubmission(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Иванов Иван Иванович", response.getBody().getStudentName());

        verify(submissionService).getSubmissionById(1L);
    }
}