package com.gyimiproject.tb_automation.selenium.input;

import com.gyimiproject.tb_automation.selenium.base.BasePage;
import com.gyimiproject.tb_automation.selenium.base.YamlLocatorReader;
import com.gyimiproject.tb_automation.selenium.DisciplinaryData;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InputPageDownload extends BasePage {

    private final By caseListLink;
    private final By caseTableCell;
    private final By pageLengthSelect = By.name("DataTables_Table_0_length");
    private final By processingIndicator = By.id("DataTables_Table_0_processing");
    private final By dataRow = By.cssSelector("tbody tr[role='row']");

    public InputPageDownload(WebDriver driver, YamlLocatorReader locatorReader) {
        super(driver);
        String caseListPath = locatorReader.get("input", "caseListPath");
        String caseTableClass = locatorReader.get("input", "caseTableClass");
        this.caseListLink = By.cssSelector("a[href='" + caseListPath + "']");
        this.caseTableCell = By.cssSelector("table." + caseTableClass + " tbody tr td");
    }

    public void navigateToCaseList() {
        click(caseListLink);
    }

    public void setPageSizeTo100() {
        waitForElement(pageLengthSelect);
        Select select = new Select(driver.findElement(pageLengthSelect));
        select.selectByValue("100");
        waitForInvisibility(processingIndicator);
        waitForRowCountAtLeast(dataRow, 1);
    }

    public List<DisciplinaryData> collectAndSave() throws InterruptedException {
        List<DisciplinaryData> results = new ArrayList<>();
        waitForElement(dataRow);

        List<WebElement> allRows = driver.findElements(By.cssSelector("tbody tr"));
        WebElement currentParentRow = null;

        for (WebElement tr : allRows) {
            String role = tr.getAttribute("role");
            String cls = tr.getAttribute("class");

            if ("row".equals(role)) {
                currentParentRow = tr;
            } else if (cls != null && cls.contains("child_row") && currentParentRow != null) {

                DisciplinaryData data = new DisciplinaryData();

                // parent row data
                List<WebElement> parentTds = currentParentRow.findElements(By.tagName("td"));
                if (parentTds.size() >= 4) {
                    String[] matchInfo = parentTds.get(0).getText().split("\n");
                    data.setMatchCode(matchInfo[0].trim());
                    data.setMatchIdentifier(matchInfo.length > 1 ?
                            matchInfo[1].replaceAll("[()]", "").trim() : "");
                    data.setInitiatedBy(parentTds.get(1).getText().trim());
                    data.setTeamHome(parentTds.get(2).getText().split("\n")[0].trim());
                    data.setTeamAway(parentTds.get(3).getText().split("\n")[0].trim());
                    if (parentTds.size() >= 5) {
                        data.setMatchDate(parentTds.get(4).getText().trim());
                    }
                }

                // child row data
                List<WebElement> childTds = tr.findElements(caseTableCell);
                if (childTds.size() >= 7) {
                    data.setCaseNumbers(childTds.get(0).getText().trim());
                    data.setPersonInvolved(childTds.get(1).getText().trim());
                    data.setInvolvedType(childTds.get(2).getText().trim());
                    data.setAffiliation(childTds.get(3).getText().trim());
                    data.setDisciplinaryReason(childTds.get(4).getText().trim());
                    data.setRegulation(childTds.get(5).getText().trim());
                    data.setDisciplinaryStatus(childTds.get(6).getText().trim());
                }

                // download
                List<WebElement> downloadLinks = tr.findElements(By.cssSelector("span.download"));
                if (!downloadLinks.isEmpty()) {
                    WebElement link = downloadLinks.get(0).findElement(By.xpath("./parent::a"));
                    String downloadUrl = link.getAttribute("href");
                    data.setDownloadUrl(downloadUrl);

                    Set<Cookie> cookies = driver.manage().getCookies();
                    String cookieHeader = cookies.stream()
                            .map(c -> c.getName() + "=" + c.getValue())
                            .collect(java.util.stream.Collectors.joining("; "));

                    java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                            .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
                            .build();

                    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create(downloadUrl))
                            .header("Cookie", cookieHeader)
                            .GET()
                            .build();

                    try {
                        File downloadDir = new File("../test-files").getCanonicalFile();

                        // request headers first
                        java.net.http.HttpResponse<java.io.InputStream> response = client.send(request,
                                java.net.http.HttpResponse.BodyHandlers.ofInputStream());

                        // filename from Content-Disposition header
                        String fileName = response.headers()
                                .firstValue("Content-Disposition")
                                .map(cd -> cd.replaceAll(".*filename[^;=\n]*=(['\"]?)([^'\"\n]*)\\1", "$2").trim())
                                .orElse(downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1));

                        Path targetPath = downloadDir.toPath().resolve(fileName);
                        Files.copy(response.body(), targetPath,
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        data.setLocalFileName(fileName); // ← ez az új sor
                        System.out.println("=== DOWNLOADED: " + fileName + " ===");
                    } catch (IOException e) {
                        System.out.println("=== DOWNLOAD ERROR: " + e.getMessage() + " ===");
                    }
                }

                results.add(data);
            }
        }
        return results;
    }
}