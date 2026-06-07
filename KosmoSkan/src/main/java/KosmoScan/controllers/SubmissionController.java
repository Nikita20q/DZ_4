package KosmoScan.controllers;

import KosmoScan.dto.SubmissionDto;
import KosmoScan.dto.SubmissionRequest;
import KosmoScan.services.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@Tag(name = "Submission API", description = "API для работы с загруженными работами")
public class SubmissionController {
    private final SubmissionService submissionService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Загрузить работу",
            description = "Загрузка файла работы студента"
    )
    public ResponseEntity<SubmissionDto> uploadSubmission(
            @Parameter(description = "Имя студента", required = true)
            @RequestPart("studentFirstName") String studentFirstName,

            @Parameter(description = "Фамилия студента", required = true)
            @RequestPart("studentLastName") String studentLastName,

            @Parameter(description = "Отчество студента", required = true)
            @RequestPart("studentMiddleName") String studentMiddleName,

            @Parameter(description = "Название работы", required = true)
            @RequestPart("workTitle") String workTitle,

            @Parameter(
                    description = "Файл работы (PDF, DOCX, TXT)",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart("file") MultipartFile file) throws IOException {

        SubmissionRequest request = SubmissionRequest.builder()
                .studentFirstName(studentFirstName)
                .studentLastName(studentLastName)
                .studentMiddleName(studentMiddleName)
                .workTitle(workTitle)
                .build();

        SubmissionDto submission = submissionService.uploadSubmission(request, file);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить информацию о сдаче", description = "Получение данных о конкретной сдаче работы")
    public ResponseEntity<SubmissionDto> getSubmission(@PathVariable Long id) {
        SubmissionDto submission = submissionService.getSubmissionById(id);
        return ResponseEntity.ok(submission);
    }
}