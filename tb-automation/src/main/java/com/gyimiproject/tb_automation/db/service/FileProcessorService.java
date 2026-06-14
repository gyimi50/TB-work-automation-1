package com.gyimiproject.tb_automation.db;

import org.springframework.stereotype.Service;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@Service
public class FileProcessorService {

    private final DocumentService documentService;
    private static final List<String> SUPPORTED_EXTENSIONS =
            List.of(".docx", ".odt", ".doc");

    public FileProcessorService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void processDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Directory not found: " + directoryPath);
            return;
        }

        File[] files = directory.listFiles(file ->
                SUPPORTED_EXTENSIONS.stream()
                        .anyMatch(ext -> file.getName().toLowerCase().endsWith(ext))
        );

        if (files == null || files.length == 0) {
            System.out.println("No supported files found in: " + directoryPath);
            return;
        }

        System.out.println("Found " + files.length + " files to process");

        for (File file : files) {
            if (documentService.isAlreadyProcessed(file.getName())) {
                System.out.println("Skipping (already processed): " + file.getName());
                continue;
            }
            try {
                System.out.println("Processing: " + file.getName());
                DisciplinaryCase result = documentService.processAndSave(file);
                System.out.println("Saved with ID: " + result.getId());
            } catch (Exception e) {
                System.out.println("Failed to process: " + file.getName() + " - " + e.getMessage());
            }
        }
    }
}