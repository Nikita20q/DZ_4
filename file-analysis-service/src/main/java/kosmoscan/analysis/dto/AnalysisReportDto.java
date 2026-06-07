package kosmoscan.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisReportDto {
    private Long id;
    private Long submissionId;
    private String status;
    private Boolean formatValid;
    private Boolean sizeValid;
    private String errorMessage;
    private String studentName;
    private String workTitle;
    private String wordCloudPath;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}