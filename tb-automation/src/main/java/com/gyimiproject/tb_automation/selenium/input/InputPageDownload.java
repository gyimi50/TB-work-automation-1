package com.gyimiproject.tb_automation.selenium.input;

import com.gyimiproject.tb_automation.db.DownloadService;
import com.gyimiproject.tb_automation.selenium.base.BasePage;
import com.gyimiproject.tb_automation.selenium.base.YamlLocatorReader;
import com.gyimiproject.tb_automation.selenium.DisciplinaryData;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InputPageDownload extends BasePage {

    private final By caseListLink;
    private final By caseTableCell;
    private final By nextButton = By.cssSelector("a.paginate_button.next");
    private final DownloadService downloadService;

    public InputPageDownload(WebDriver driver, YamlLocatorReader locatorReader,
                             DownloadService downloadService) {
        super(driver);
        String caseListPath = locatorReader.get("input", "caseListPath");
        String caseTableClass = locatorReader.get("input", "caseTableClass");
        this.caseListLink = By.cssSelector("a[href='" + caseListPath + "']");
        this.caseTableCell = By.cssSelector("table." + caseTableClass + " tbody tr td");
        this.downloadService = downloadService;
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

    public List<DisciplinaryData> collectAndSave(String currentSeason, String downloadDir) throws InterruptedException {
        List<DisciplinaryData> allResults = new ArrayList<>();

        while (true) {
            waitForElement(By.cssSelector("tbody tr[role='row']"));
            PageResult pageResult = collectPage(currentSeason, downloadDir);
            allResults.addAll(pageResult.data);

            if (pageResult.foundOldCase) {
                System.out.println("=== FOUND OLD SEASON CASE, STOPPING ===");
                break;
            }

            if (!hasNextPage(nextButton)) {
                System.out.println("=== NO MORE PAGES ===");
                break;
            }

            goToNextPage(nextButton);
        }

        return allResults;
    }

    private DisciplinaryData extractParentRowData(WebElement parentRow) {
        DisciplinaryData data = new DisciplinaryData();
        List<WebElement> tds = parentRow.findElements(By.tagName("td"));
        if (tds.size() >= 4) {
            String[] matchInfo = tds.get(0).getText().split("\n");
            data.setMatchCode(matchInfo[0].trim());
            data.setLeagueCode(matchInfo.length > 1 ?
                    matchInfo[1].replaceAll("[()]", "").trim() : "");
            data.setInitiatedBy(tds.get(1).getText().trim());
            data.setTeamHome(tds.get(2).getText().split("\n")[0].trim());
            data.setTeamAway(tds.get(3).getText().split("\n")[0].trim());
            if (tds.size() >= 5) {
                data.setMatchDate(tds.get(4).getText().trim());
            }
        }
        return data;
    }

    private boolean extractCaseNumbers(WebElement childRow, DisciplinaryData data,
                                       String currentSeason) {
        List<WebElement> tds = childRow.findElements(caseTableCell);
        if (tds.size() < 8) return false;

        String rawCaseNumbers = tds.get(0).getText().trim();
        String caseNumbers = java.util.Arrays.stream(rawCaseNumbers.split("\\s+"))
                .filter(s -> s.startsWith("FEGY/"))
                .collect(java.util.stream.Collectors.joining("\n"));
        data.setCaseNumbers(caseNumbers);

        return !caseNumbers.isEmpty() && !caseNumbers.contains("/" + currentSeason + "/");
    }

    private void enrichChildData(WebElement childRow, DisciplinaryData data) {
        List<WebElement> tds = childRow.findElements(caseTableCell);
        if (tds.size() < 8) return;
        data.setPersonInvolved(tds.get(2).getText().trim());
        data.setInvolvedType(tds.get(3).getText().trim());
        data.setOrganization(tds.get(4).getText().trim());
        data.setDisciplinaryReason(tds.get(5).getText().trim());
        data.setRegulation(tds.get(6).getText().trim());
        data.setDisciplinaryStatus(tds.get(7).getText().trim());
    }

    private void downloadIfAvailable(WebElement childRow, DisciplinaryData data, String downloadDir) {
        List<WebElement> downloadLinks = childRow.findElements(By.cssSelector("span.download"));
        if (downloadLinks.isEmpty()) return;

        WebElement link = downloadLinks.get(0).findElement(By.xpath("./parent::a"));
        String downloadUrl = link.getAttribute("href");
        data.setDownloadUrl(downloadUrl);

        Set<Cookie> cookies = driver.manage().getCookies();
        String fileName = downloadService.downloadFile(downloadUrl, cookies, downloadDir);
        data.setLocalFileName(fileName);
    }

    private PageResult collectPage(String currentSeason, String downloadDir) throws InterruptedException {
        List<DisciplinaryData> results = new ArrayList<>();
        WebElement currentParentRow = null;
        boolean foundOldCase = false;
        int rowCount = getRowCount();

        for (int i = 0; i < rowCount; i++) {
            WebElement tr = getRow(i);
            if (tr == null) break;

            if (isParentRow(tr)) {
                currentParentRow = tr;
            } else if (isChildRow(tr) && currentParentRow != null) {
                DisciplinaryData data = extractParentRowData(currentParentRow);
                boolean isOldCase = extractCaseNumbers(tr, data, currentSeason);
                if (isOldCase) {
                    foundOldCase = true;
                    continue;
                }
                enrichChildData(tr, data);
                downloadIfAvailable(tr, data, downloadDir);
                results.add(data);
            }
        }
        return new PageResult(results, foundOldCase);
    }
}