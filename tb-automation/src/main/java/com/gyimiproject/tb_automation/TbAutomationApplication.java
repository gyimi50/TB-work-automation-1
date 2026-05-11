package com.gyimiproject.tb_automation;

import com.gyimiproject.tb_automation.document.DocumentProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.gyimiproject.tb_automation.db.DocumentService;
import com.gyimiproject.tb_automation.db.DisciplinaryCase;



import java.io.File;

@SpringBootApplication
public class TbAutomationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TbAutomationApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(DocumentService documentService) {
        return args -> {
            File testFile = new File("../test-files/test.odt");
            DisciplinaryCase result = documentService.processAndSave(testFile);
            System.out.println("=== SAVED TO DB ===");
            System.out.println("ID: " + result.getId());
            System.out.println("File: " + result.getFileName());
            System.out.println("Status: " + result.getDocumentStatus());
            System.out.println("=== END ===");
        };
    }
}