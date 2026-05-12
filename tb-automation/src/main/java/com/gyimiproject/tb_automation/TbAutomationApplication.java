package com.gyimiproject.tb_automation;

import com.gyimiproject.tb_automation.document.DocumentProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.gyimiproject.tb_automation.db.DocumentService;
import com.gyimiproject.tb_automation.db.DisciplinaryCase;
import com.gyimiproject.tb_automation.db.FileProcessorService;



import java.io.File;

@SpringBootApplication
public class TbAutomationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TbAutomationApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(FileProcessorService fileProcessorService) {
        return args -> {
            fileProcessorService.processDirectory("../test-files");
        };
    }
}