package KosmoScan.services;

import KosmoScan.domain.Work;
import KosmoScan.repository.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class WorkService {
    private final WorkRepository workRepository;
    public Work findOrCreate(String title) {
        return workRepository.findByTitle(title)
                .orElseGet(() -> {
                    Work newWork = Work.builder()
                            .title(title)
                            .build();
                    return workRepository.save(newWork);
                });
    }
}
