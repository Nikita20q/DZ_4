package KosmoScan.dto;

import KosmoScan.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long workId;
    private String workTitle;
    private String originalFileName;
    private Long fileSize;
    private String fileExtension;
    private String storedFilePath;
    private LocalDateTime submittedAt;
    private SubmissionStatus status;
    private Long analysisReportId;
}