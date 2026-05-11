package com.gyimiproject.tb_automation;

import com.gyimiproject.tb_automation.document.DocumentProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;



import java.io.File;

@SpringBootApplication
public class TbAutomationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TbAutomationApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(DocumentProcessor processor) {
        return args -> {
            File testFile = new File("../test-files/test.odt");
            String result = processor.process(testFile);
            System.out.println("=== RESULT ===");
            System.out.println(result);
            System.out.println("=== END ===");
        };
    }
}