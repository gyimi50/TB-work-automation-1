package com.gyimiproject.tb_automation;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.gyimiproject.tb_automation.db.DocumentService;
import com.gyimiproject.tb_automation.selenium.base.YamlLocatorReader;
import com.gyimiproject.tb_automation.selenium.input.InputPageLogin;
import com.gyimiproject.tb_automation.selenium.input.InputPageDownload;
import com.gyimiproject.tb_automation.selenium.output.OutputPageLogin;
import com.gyimiproject.tb_automation.selenium.DisciplinaryData;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;
import com.gyimiproject.tb_automation.selenium.output.OutputPagePost;
import com.gyimiproject.tb_automation.selenium.base.YamlConfigReader;

@SpringBootApplication
public class TbAutomationApplication {

    @Value("${input.username}")
    private String inputUsername;

    @Value("${input.password}")
    private String inputPassword;

    @Value("${output.username}")
    private String outputUsername;

    @Value("${output.password}")
    private String outputPassword;

    public static void main(String[] args) {
        SpringApplication.run(TbAutomationApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(YamlLocatorReader locatorReader,
                                 DocumentService documentService,
                                 YamlConfigReader configReader) {
        return args -> {
            // INPUT FLOW
            InputPageLogin loginPage = new InputPageLogin(locatorReader);
            loginPage.login(inputUsername, inputPassword);
            System.out.println("=== INPUT LOGIN DONE ===");

            InputPageDownload downloadPage = new InputPageDownload(loginPage.getDriver(), locatorReader);
            downloadPage.navigateToCaseList();
            downloadPage.setPageSizeTo100();
            System.out.println("=== NAVIGATED AND PAGE SIZE SET ===");

            List<DisciplinaryData> data = downloadPage.collectAndSave(
                    configReader.getCurrentSeason());
            for (DisciplinaryData d : data) {
                documentService.saveFromWeb(d);
            }

            documentService.processPendingFiles("../test-files");
            System.out.println("=== PROCESSING DONE ===");

            loginPage.quit();
            System.out.println("=== INPUT BROWSER CLOSED ===");

            // OUTPUT FLOW
            OutputPageLogin outputLogin = new OutputPageLogin(locatorReader);
            outputLogin.login(outputUsername, outputPassword);
            System.out.println("=== OUTPUT LOGIN DONE ===");

            OutputPagePost postPage = new OutputPagePost(outputLogin.getDriver(), locatorReader);
            postPage.navigateToSeasonTopic();
            documentService.postProcessedCases(postPage);

            outputLogin.quit();
            System.out.println("=== OUTPUT BROWSER CLOSED ===");
        };
    }
}