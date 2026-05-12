package com.gyimiproject.tb_automation.db;

import com.gyimiproject.tb_automation.document.DocumentProcessor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;

@Service
public class DocumentService {

    private final DocumentProcessor documentProcessor;
    private final DisciplinaryCaseRepository repository;
    public boolean isAlreadyProcessed(String fileName) {
        return repository.existsByFileName(fileName);
    }

    public DocumentService(DocumentProcessor documentProcessor,
                           DisciplinaryCaseRepository repository) {
        this.documentProcessor = documentProcessor;
        this.repository = repository;
    }

    public DisciplinaryCase processAndSave(File file) throws Exception {
        String bbcodeContent = documentProcessor.process(file);

        DisciplinaryCase disciplinaryCase = DisciplinaryCase.builder()
                .fileName(file.getName())
                .bbcodeContent(bbcodeContent)
                .documentStatus(DisciplinaryCase.DocumentStatus.PENDING)
                .processedDate(LocalDate.now())
                .build();

        return repository.save(disciplinaryCase);
    }
}