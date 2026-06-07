package kosmoscan.analysis.repository;

import kosmoscan.analysis.domain.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {

    Optional<AnalysisReport> findBySubmissionId(Long submissionId);

    boolean existsBySubmissionId(Long submissionId);

    List<AnalysisReport> findAll();
}