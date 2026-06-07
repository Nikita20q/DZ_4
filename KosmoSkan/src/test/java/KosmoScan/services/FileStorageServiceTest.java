package KosmoScan.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        ReflectionTestUtils.setField(
                fileStorageService,
                "storageLocation",
                tempDir.toString()
        );
        fileStorageService.init();
    }

    @Test
    void storeFile_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        String result = fileStorageService.storeFile(file);

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void init_CreatesDirectory() {
        Path newDir = tempDir.resolve("newdir");
        ReflectionTestUtils.setField(
                fileStorageService,
                "storageLocation",
                newDir.toString()
        );

        fileStorageService.init();

        assertTrue(Files.exists(newDir));
        assertTrue(Files.isDirectory(newDir));
    }
}