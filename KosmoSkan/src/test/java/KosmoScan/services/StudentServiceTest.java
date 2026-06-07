package KosmoScan.services;

import KosmoScan.domain.Student;
import KosmoScan.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student existingStudent;

    @BeforeEach
    void setUp() {
        existingStudent = Student.builder()
                .id(1L)
                .firstName("Иван")
                .lastName("Иванов")
                .middleName("Иванович")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findOrCreate_ExistingStudent_ReturnsFound() {
        when(studentRepository.findByFirstNameAndLastNameAndMiddleName(
                "Иван", "Иванов", "Иванович"))
                .thenReturn(Optional.of(existingStudent));

        Student result = studentService.findOrCreate("Иван", "Иванов", "Иванович");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Иван", result.getFirstName());
        verify(studentRepository).findByFirstNameAndLastNameAndMiddleName(
                "Иван", "Иванов", "Иванович");
        verify(studentRepository, never()).save(any());
    }

    @Test
    void findOrCreate_NewStudent_CreatesAndReturns() {
        when(studentRepository.findByFirstNameAndLastNameAndMiddleName(
                "Петр", "Петров", "Петрович"))
                .thenReturn(Optional.empty());

        Student newStudent = Student.builder()
                .id(2L)
                .firstName("Петр")
                .lastName("Петров")
                .middleName("Петрович")
                .build();

        when(studentRepository.save(any(Student.class)))
                .thenReturn(newStudent);

        Student result = studentService.findOrCreate("Петр", "Петров", "Петрович");

        assertNotNull(result);
        assertEquals("Петр", result.getFirstName());
        assertEquals("Петров", result.getLastName());
        verify(studentRepository).save(any(Student.class));
    }
}