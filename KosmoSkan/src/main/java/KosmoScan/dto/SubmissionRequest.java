package KosmoScan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRequest {
    private String studentFirstName;
    private String studentLastName;
    private String studentMiddleName;
    private String workTitle;
}