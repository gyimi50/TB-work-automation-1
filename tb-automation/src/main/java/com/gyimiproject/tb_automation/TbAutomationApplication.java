package com.gyimiproject.tb_automation;

import com.gyimiproject.tb_automation.document.DocumentProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.gyimiproject.tb_automation.db.DocumentService;
import com.gyimiproject.tb_automation.db.DisciplinaryCase;
import com.gyimiproject.tb_automation.db.FileProcessorService;
import com.gyimiproject.tb_automation.selenium.base.YamlLocatorReader;
import com.gyimiproject.tb_automation.selenium.input.InputLoginPage;
import org.springframework.beans.factory.annotation.Value;
import com.gyimiproject.tb_automation.selenium.input.InputPageDownload;
import com.gyimiproject.tb_automation.selenium.DisciplinaryData;
import java.util.List;





@SpringBootApplication
public class TbAutomationApplication {
    @Value("${input.username}")
    private String inputUsername;

    @Value("${input.password}")
    private String inputPassword;


    public static void main(String[] args) {
        SpringApplication.run(TbAutomationApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(YamlLocatorReader locatorReader,
                                 DocumentService documentService) {
        return args -> {
            InputLoginPage loginPage = new InputLoginPage(locatorReader);
            loginPage.login(inputUsername, inputPassword);
            System.out.println("=== LOGIN DONE ===");


            InputPageDownload downloadPage = new InputPageDownload(loginPage.getDriver(), locatorReader);
            downloadPage.navigateToCaseList();
            downloadPage.setPageSizeTo100();
            System.out.println("=== NAVIGATED AND PAGE SIZE SET ===");

            List<DisciplinaryData> data = downloadPage.collectAndSave();
            for (DisciplinaryData d : data) {
                documentService.saveFromWeb(d);
            }

            documentService.processPendingFiles("../test-files");
            System.out.println("=== PROCESSING DONE ===");
        };
    }
}