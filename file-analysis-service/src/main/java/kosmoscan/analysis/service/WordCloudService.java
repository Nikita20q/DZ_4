package kosmoscan.analysis.service;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.ColorPalette;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WordCloudService {

    @Value("${wordcloud.storage.location:./wordclouds}")
    private String storageLocation;

    private Path rootLocation;

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "и", "в", "не", "на", "с", "что", "а", "по", "за", "от", "для",
            "как", "так", "же", "но", "или", "если", "то", "бы", "мне", "все",
            "это", "его", "она", "они", "оно", "мы", "вы", "ты", "я"
    ));

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
            log.info("WordCloudService: Директория для облаков слов: {}", rootLocation);
        } catch (IOException e) {
            log.error("WordCloudService: Не удалось создать директорию", e);
            throw new RuntimeException("Не удалось создать директорию для облаков слов", e);
        }
    }

    public String generateAndSave(String text) {
        log.info("WordCloudService: Начало генерации облака слов");
        log.info("Длина текста: {} символов", text != null ? text.length() : 0);

        if (text == null || text.trim().isEmpty()) {
            log.warn("WordCloudService: Текст пустой, облако не генерируется");
            return null;
        }

        try {
            log.info("   - Разбиение текста на слова...");
            List<String> words = Arrays.stream(text.split("\\s+"))
                    .filter(w -> w.length() >= 3)
                    .collect(Collectors.toList());

            log.info("Найдено слов: {}", words.size());

            FrequencyAnalyzer analyzer = new FrequencyAnalyzer();
            analyzer.setWordFrequenciesToReturn(50);
            analyzer.setMinWordLength(3);

            List<WordFrequency> frequencies = analyzer.load(words);
            log.info("Частоты подсчитаны: {}", frequencies.size());

            frequencies.removeIf(f -> STOP_WORDS.contains(f.getWord().toLowerCase()));
            log.info("После фильтрации стоп-слов: {}", frequencies.size());

            if (frequencies.isEmpty()) {
                log.warn("WordCloudService: После фильтрации слов не осталось");
                return null;
            }

            log.info("Создание WordCloud (800x600)...");
            WordCloud wordCloud = new WordCloud(new Dimension(800, 600), CollisionMode.RECTANGLE);
            wordCloud.setBackground(new RectangleBackground(new Dimension(800, 600)));
            wordCloud.setKumoFont(new KumoFont(new Font(Font.SANS_SERIF, Font.BOLD, 20)));
            wordCloud.setColorPalette(new ColorPalette(
                    new Color(30, 136, 229),
                    new Color(76, 175, 80),
                    new Color(255, 193, 7),
                    new Color(255, 87, 34),
                    new Color(156, 39, 176)
            ));

            log.info("Построение облака...");
            wordCloud.build(frequencies);
            log.info("Облако построено");

            String fileName = "wordcloud_" + System.currentTimeMillis() + ".png";
            Path targetPath = rootLocation.resolve(fileName);

            log.info("Сохранение в: {}", targetPath);
            javax.imageio.ImageIO.write(wordCloud.getBufferedImage(), "png", targetPath.toFile());

            log.info("WordCloudService: Облако слов сохранено: {}", targetPath);
            return targetPath.toString();

        } catch (Exception e) {
            log.error("WordCloudService: Ошибка генерации: {}", e.getMessage(), e);
            return null;
        }
    }
}