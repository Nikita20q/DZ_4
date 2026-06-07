package kosmoscan.analysis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WordCloudServiceTest {

    private WordCloudService wordCloudService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        wordCloudService = new WordCloudService();
        ReflectionTestUtils.setField(wordCloudService, "storageLocation", tempDir.toString());
        wordCloudService.init();
    }

    @Test
    void generateAndSave_NullText_ReturnsNull() throws IOException {
        String result = wordCloudService.generateAndSave(null);

        assertNull(result);
    }

    @Test
    void generateAndSave_EmptyText_ReturnsNull() throws IOException {
        String result = wordCloudService.generateAndSave("");

        assertNull(result);
    }

    @Test
    void generateAndSave_OnlySpaces_ReturnsNull() throws IOException {
        String result = wordCloudService.generateAndSave("   ");

        assertNull(result);
    }

    @Test
    void generateAndSave_ValidText_ReturnsPath() throws IOException {
        String text = "Микросервисы Микросервисы Микросервисы " +
                "Анализ Анализ Анализ " +
                "Файл Файл " +
                "Студент Работа Проект";

        String result = wordCloudService.generateAndSave(text);

        assertNotNull(result);
        assertTrue(result.startsWith(tempDir.toString()));
        assertTrue(result.endsWith(".png"));
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void generateAndSave_OnlyStopWords_ReturnsNull() throws IOException {
        String text = "и в не на с что а по за от для";

        String result = wordCloudService.generateAndSave(text);

        assertNull(result);
    }

    @Test
    void generateAndSave_ShortWords_ReturnsNull() throws IOException {
        String text = "я мы ты он она";

        String result = wordCloudService.generateAndSave(text);

        assertNull(result);
    }

    @Test
    void generateAndSave_CreatesFileWithCorrectExtension() throws IOException {
        String text = "Тест Тест Тест Работа Работа Проект";

        String result = wordCloudService.generateAndSave(text);

        assertNotNull(result);
        assertTrue(result.endsWith(".png"));

        Path path = Path.of(result);
        assertTrue(Files.size(path) > 0);
    }
}