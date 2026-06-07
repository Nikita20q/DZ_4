package KosmoScan.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${file.storage.location:./uploads}")
    private String storageLocation;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для хранения файлов", e);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path targetLocation = rootLocation.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation);
        return targetLocation.toString();
    }
}