package KosmoScan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
}