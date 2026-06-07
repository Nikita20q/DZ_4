package KosmoScan.repository;

import KosmoScan.domain.Work;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkRepository extends JpaRepository<Work,Long> {
    Optional<Work> findByTitle(String title);
    boolean existsByTitle(String title);
}