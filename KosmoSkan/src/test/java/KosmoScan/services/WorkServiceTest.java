package KosmoScan.services;

import KosmoScan.domain.Work;
import KosmoScan.repository.WorkRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkServiceTest {

    @Mock
    private WorkRepository workRepository;

    @InjectMocks
    private WorkService workService;

    private Work existingWork;

    @BeforeEach
    void setUp() {
        existingWork = Work.builder()
                .id(1L)
                .title("Контрольная №1")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findOrCreate_ExistingWork_ReturnsFound() {
        when(workRepository.findByTitle("Контрольная №1"))
                .thenReturn(Optional.of(existingWork));

        Work result = workService.findOrCreate("Контрольная №1");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Контрольная №1", result.getTitle());
        verify(workRepository).findByTitle("Контрольная №1");
        verify(workRepository, never()).save(any());
    }

    @Test
    void findOrCreate_NewWork_CreatesAndReturns() {
        when(workRepository.findByTitle("Новая работа"))
                .thenReturn(Optional.empty());

        Work newWork = Work.builder()
                .id(2L)
                .title("Новая работа")
                .build();

        when(workRepository.save(any(Work.class)))
                .thenReturn(newWork);

        Work result = workService.findOrCreate("Новая работа");

        assertNotNull(result);
        assertEquals("Новая работа", result.getTitle());
        verify(workRepository).findByTitle("Новая работа");
        verify(workRepository).save(any(Work.class));
    }
}