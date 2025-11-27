package com.shalom.shalomhotel.Service;

import com.shalom.shalomhotel.Exception.OurException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveImageToLocal(MultipartFile photo) {
        try {
            // Create directory if not exists
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Create unique filename
            String originalFilename = photo.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = Paths.get(uploadDir, uniqueFilename);
            Files.write(filePath, photo.getBytes());

            // Return the file path (you can return relative path instead if you plan to serve files via HTTP)
            return uniqueFilename;


        } catch (IOException e) {
            throw new OurException("Unable to save image locally: " + e.getMessage());
        }
    }
}

