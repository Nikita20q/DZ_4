package kosmoscan.analysis.dto;

import lombok.Data;

@Data
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
    private String submittedAt;
    private String status;
    private Long analysisReportId;
}