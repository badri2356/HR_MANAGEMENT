package com.employee.services.serviceImpl;

import com.employee.exceptions.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf", "image/jpeg", "image/png");

    private final Path rootDir;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) throws IOException {
        this.rootDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(rootDir);
    }

    public String store(MultipartFile file, Long employeeId) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Please choose a file to upload");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new InvalidFileException("Only PDF, JPG and PNG files are allowed");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String storedName = UUID.randomUUID() + "-" + safeName;

        Path employeeDir = rootDir.resolve("employees").resolve(String.valueOf(employeeId));
        Path target = employeeDir.resolve(storedName).normalize();

        if (!target.startsWith(rootDir)) {
            throw new InvalidFileException("Invalid file name");
        }

        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(employeeDir);
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save the file", e);
        }

        return rootDir.relativize(target).toString().replace("\\", "/");
    }

    public Resource load(String relativePath) {
        try {
            Path file = rootDir.resolve(relativePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!file.startsWith(rootDir) || !resource.exists()) {
                throw new InvalidFileException("File not found");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new InvalidFileException("File not found");
        }
    }

    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(rootDir.resolve(relativePath).normalize());
        } catch (IOException ignored) {
            // the old file is just left behind; the database row is what matters
        }
    }
}