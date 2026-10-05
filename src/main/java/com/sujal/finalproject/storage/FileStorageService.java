package com.sujal.finalproject.storage;

import com.sujal.finalproject.config.ArchiveProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(ArchiveProperties properties) {
        this.rootLocation = Paths.get(properties.getStorageDir());
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Failed to store empty file.");
            }
            String storedFileName = UUID.randomUUID().toString() + ".zip";
            Path destinationFile = this.rootLocation.resolve(Paths.get(storedFileName)).normalize().toAbsolutePath();

            file.transferTo(destinationFile.toFile());
            return storedFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    public void delete(String storedFileName) {
        try {
            Path file = rootLocation.resolve(storedFileName);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete stored file.", e);
        }
    }

    public File getFile(String storedFileName) {
        return rootLocation.resolve(storedFileName).toFile();
    }
}