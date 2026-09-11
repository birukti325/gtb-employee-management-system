package com.sms.gtbemployeemanagementsystem.Service;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class ProfilePhotoService {

    private static final Path PHOTOS_DIR =
            Path.of(System.getProperty("user.home"), ".gtb_ems", "profile_photos");

    public File findExistingPhoto(Long employeeId) {
        File dir = PHOTOS_DIR.toFile();
        if (!dir.exists()) return null;
        File[] matches = dir.listFiles((d, name) -> name.startsWith("profile_" + employeeId + "."));
        return (matches != null && matches.length > 0) ? matches[0] : null;
    }

    public void savePhoto(Long employeeId, File sourceFile) throws IOException {
        Files.createDirectories(PHOTOS_DIR);
        deleteExistingPhoto(employeeId);

        String extension = getExtension(sourceFile.getName());
        Path destination = PHOTOS_DIR.resolve("profile_" + employeeId + "." + extension);
        Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
    }

    public void deleteExistingPhoto(Long employeeId) {
        File existing = findExistingPhoto(employeeId);
        if (existing != null) {
            //noinspection ResultOfMethodCallIgnored
            existing.delete();
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "png";
    }
}