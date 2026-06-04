package com.gyimiproject.tb_automation;

import com.gyimiproject.tb_automation.db.DocumentService;
import com.gyimiproject.tb_automation.db.DownloadService;
import com.gyimiproject.tb_automation.selenium.DisciplinaryData;
import com.gyimiproject.tb_automation.selenium.base.YamlConfigReader;
import com.gyimiproject.tb_automation.selenium.base.YamlLocatorReader;
import com.gyimiproject.tb_automation.selenium.input.InputPageDownload;
import com.gyimiproject.tb_automation.selenium.input.InputPageLogin;
import com.gyimiproject.tb_automation.selenium.output.OutputPageLogin;
import com.gyimiproject.tb_automation.selenium.output.OutputPagePost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrchestrationService {

    @Value("${input.username}")
    private String inputUsername;

    @Value("${input.password}")
    private String inputPassword;

    @Value("${output.username}")
    private String outputUsername;

    @Value("${output.password}")
    private String outputPassword;

    private final DocumentService documentService;
    private final DownloadService downloadService;
    private final YamlLocatorReader locatorReader;
    private final YamlConfigReader configReader;

    public OrchestrationService(DocumentService documentService,
                                DownloadService downloadService,
                                YamlLocatorReader locatorReader,
                                YamlConfigReader configReader) {
        this.documentService = documentService;
        this.downloadService = downloadService;
        this.locatorReader = locatorReader;
        this.configReader = configReader;
    }

    public void runInputFlow() throws Exception {
        InputPageLogin loginPage = new InputPageLogin(locatorReader);
        loginPage.login(inputUsername, inputPassword);
        System.out.println("=== INPUT LOGIN DONE ===");

        InputPageDownload downloadPage = new InputPageDownload(
                loginPage.getDriver(), locatorReader, downloadService);
        downloadPage.navigateToCaseList();
        downloadPage.setPageSizeTo100();
        System.out.println("=== NAVIGATED AND PAGE SIZE SET ===");

        List<DisciplinaryData> data = downloadPage.collectAndSave(
                configReader.getCurrentSeason(), configReader.getTestFilesPath());
        for (DisciplinaryData d : data) {
            documentService.saveFromWeb(d);
        }

        documentService.processPendingFiles(configReader.getTestFilesPath());
        System.out.println("=== PROCESSING DONE ===");

        loginPage.quit();
        System.out.println("=== INPUT BROWSER CLOSED ===");
    }

    public void runOutputFlow() throws Exception {
        OutputPageLogin outputLogin = new OutputPageLogin(locatorReader);
        outputLogin.login(outputUsername, outputPassword);
        System.out.println("=== OUTPUT LOGIN DONE ===");

        OutputPagePost postPage = new OutputPagePost(
                outputLogin.getDriver(), locatorReader);
        postPage.navigateToSeasonTopic();
        documentService.postProcessedCases(postPage);

        outputLogin.quit();
        System.out.println("=== OUTPUT BROWSER CLOSED ===");
    }

    public void runFullFlow() throws Exception {
        runInputFlow();
        runOutputFlow();
    }
}