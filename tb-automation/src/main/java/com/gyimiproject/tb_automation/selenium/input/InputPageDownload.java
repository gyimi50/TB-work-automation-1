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
    private final By nextButton = By.cssSelector("a.paginate_button.next");

    public InputPageDownload(WebDriver driver, YamlLocatorReader locatorReader) {
        super(driver);
        String caseListPath = locatorReader.get("input", "caseListPath");
        String caseTableClass = locatorReader.get("input", "caseTableClass");
        this.caseListLink = By.cssSelector("a[href='" + caseListPath + "']");
        this.caseTableCell = By.cssSelector("table." + caseTableClass + " tbody tr td");
    }

    private static class PageResult {
        List<DisciplinaryData> data;
        boolean foundOldCase;

        PageResult(List<DisciplinaryData> data, boolean foundOldCase) {
            this.data = data;
            this.foundOldCase = foundOldCase;
        }
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

public List<DisciplinaryData> collectAndSave(String currentSeason) throws InterruptedException {
    List<DisciplinaryData> allResults = new ArrayList<>();

    while (true) {
        waitForElement(dataRow);
        PageResult pageResult = collectPage(currentSeason);
        allResults.addAll(pageResult.data);

        if (pageResult.foundOldCase) {
            System.out.println("=== FOUND OLD SEASON CASE, STOPPING ===");
            break;
        }

        List<WebElement> nextButtons = driver.findElements(nextButton);
        if (nextButtons.isEmpty() || nextButtons.get(0).getAttribute("class").contains("disabled")) {
            System.out.println("=== NO MORE PAGES ===");
            break;
        }

        nextButtons.get(0).click();
        System.out.println("=== NAVIGATING TO NEXT PAGE ===");
        waitForInvisibility(processingIndicator);
        waitForRowCountAtLeast(dataRow, 1);
    }

    return allResults;
}

    private PageResult collectPage(String currentSeason) throws InterruptedException {
        List<DisciplinaryData> results = new ArrayList<>();
        WebElement currentParentRow = null;
        boolean foundOldCase = false;

        int rowCount = driver.findElements(By.cssSelector("tbody tr")).size();

        for (int i = 0; i < rowCount; i++) {
            List<WebElement> allRows = driver.findElements(By.cssSelector("tbody tr"));
            if (i >= allRows.size()) break;
            WebElement tr = allRows.get(i);

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
                    data.setLeagueCode(matchInfo.length > 1 ?
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
                if (childTds.size() >= 8) {
                    String rawCaseNumbers = childTds.get(0).getText().trim();
                    String caseNumbers = java.util.Arrays.stream(rawCaseNumbers.split("\\s+"))
                            .filter(s -> s.startsWith("FEGY/"))
                            .collect(java.util.stream.Collectors.joining("\n"));
                    data.setCaseNumbers(caseNumbers);

                    // season check
                    if (!caseNumbers.isEmpty() && !caseNumbers.contains("/" + currentSeason + "/")) {
                        foundOldCase = true;
                        continue; // ne adja hozzá a results-hoz, ugrik a következő sorra
                    }

                    data.setPersonInvolved(childTds.get(2).getText().trim());
                    data.setInvolvedType(childTds.get(3).getText().trim());
                    data.setOrganization(childTds.get(4).getText().trim());
                    data.setDisciplinaryReason(childTds.get(5).getText().trim());
                    data.setRegulation(childTds.get(6).getText().trim());
                    data.setDisciplinaryStatus(childTds.get(7).getText().trim());
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

                        java.net.http.HttpResponse<java.io.InputStream> response = client.send(request,
                                java.net.http.HttpResponse.BodyHandlers.ofInputStream());

                        String fileName = response.headers()
                                .firstValue("Content-Disposition")
                                .map(cd -> cd.replaceAll(".*filename[^;=\n]*=(['\"]?)([^'\"\n]*)\\1", "$2").trim())
                                .orElse(downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1));

                        Path targetPath = downloadDir.toPath().resolve(fileName);
                        Files.copy(response.body(), targetPath,
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        data.setLocalFileName(fileName);
                    } catch (IOException e) {
                        System.out.println("=== DOWNLOAD ERROR: " + e.getMessage() + " ===");
                    }
                }

                results.add(data);
            }
        }
        return new PageResult(results, foundOldCase);
    }
}