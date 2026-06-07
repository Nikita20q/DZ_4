package kosmoscan.analysis.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long submissionId;

    @Column(nullable = false)
    private String status;

    private Boolean formatValid;
    private Boolean sizeValid;
    private String errorMessage;

    private String wordCloudPath;

    @Column(nullable = false)
    private String studentName;

    private String workTitle;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}