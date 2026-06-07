package KosmoScan.services;

import KosmoScan.domain.Student;
import KosmoScan.domain.Submission;
import KosmoScan.domain.Work;
import KosmoScan.dto.SubmissionDto;
import KosmoScan.dto.SubmissionRequest;
import KosmoScan.enums.SubmissionStatus;
import KosmoScan.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private StudentService studentService;

    @Mock
    private WorkService workService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private SubmissionService submissionService;

    private Student testStudent;
    private Work testWork;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        testStudent = Student.builder()
                .id(1L)
                .firstName("Иван")
                .lastName("Иванов")
                .middleName("Иванович")
                .createdAt(LocalDateTime.now())
                .build();

        testWork = Work.builder()
                .id(1L)
                .title("Контрольная №1")
                .createdAt(LocalDateTime.now())
                .build();

        testFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        ReflectionTestUtils.setField(
                submissionService,
                "analysisServiceUrl",
                "http://localhost:8082/api/analysis"
        );
    }

    @Test
    void uploadSubmission_Success() throws IOException {
        // Arrange
        SubmissionRequest request = SubmissionRequest.builder()
                .studentFirstName("Иван")
                .studentLastName("Иванов")
                .studentMiddleName("Иванович")
                .workTitle("Контрольная №1")
                .build();

        when(studentService.findOrCreate(anyString(), anyString(), anyString()))
                .thenReturn(testStudent);
        when(workService.findOrCreate(anyString()))
                .thenReturn(testWork);
        when(fileStorageService.storeFile(any(MultipartFile.class)))
                .thenReturn("/uploads/test.pdf");
        when(submissionRepository.save(any()))
                .thenAnswer(invocation -> {
                    Submission s = invocation.getArgument(0);
                    s.setId(1L);
                    return s;
                });

        SubmissionDto result = submissionService.uploadSubmission(request, testFile);

        assertNotNull(result, "Результат не должен быть null");
        assertNotNull(result.getStudentName(), "Имя студента не должно быть null");
        assertNotNull(result.getWorkTitle(), "Название работы не должно быть null");
        assertEquals("test.pdf", result.getOriginalFileName());
        assertEquals(SubmissionStatus.UPLOADED, result.getStatus());
    }

    @Test
    void getSubmissionById_Success() {
        Submission testSubmission = Submission.builder()
                .id(1L)
                .student(testStudent)
                .work(testWork)
                .originFileName("test.pdf")
                .storedFilePath("/uploads/test.pdf")
                .fileSize(1024L)
                .fileExtension("pdf")
                .submittedAt(LocalDateTime.now())
                .status(SubmissionStatus.UPLOADED)
                .build();

        when(submissionRepository.findById(1L))
                .thenReturn(Optional.of(testSubmission));

        SubmissionDto result = submissionService.getSubmissionById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Иванов Иван Иванович", result.getStudentName());
        verify(submissionRepository).findById(1L);
    }

    @Test
    void getSubmissionById_NotFound_ThrowsException() {
        when(submissionRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            submissionService.getSubmissionById(999L);
        });

        assertEquals("Submission not found with id: 999", exception.getMessage());
    }
}