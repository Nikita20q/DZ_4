package KosmoScan.repository;

import KosmoScan.domain.Submission;
import KosmoScan.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByWorkId(Long workId);
    List<Submission> findByStudentId(Long studentId);
    Optional<Submission> findByStudentIdAndWorkId(Long studentId, Long workId);
    List<Submission> findByStatus(SubmissionStatus status);
    List<Submission> findByWorkIdAndStatus(Long workId, SubmissionStatus status);
}