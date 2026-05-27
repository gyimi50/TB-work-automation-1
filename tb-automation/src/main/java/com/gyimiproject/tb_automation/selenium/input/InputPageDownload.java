package com.gyimiproject.tb_automation.selenium.input;

import com.gyimiproject.tb_automation.db.DownloadService;
import com.gyimiproject.tb_automation.selenium.base.BasePage;
import com.gyimiproject.tb_automation.selenium.base.YamlLocatorReader;
import com.gyimiproject.tb_automation.selenium.DisciplinaryData;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

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
    private final DownloadService downloadService;
    private static final String DOWNLOAD_DIR = "../test-files";

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

            if (!hasNextPage()) {
                System.out.println("=== NO MORE PAGES ===");
                break;
            }

            goToNextPage();
        }

        return allResults;
    }

    private boolean hasNextPage() {
        List<WebElement> buttons = driver.findElements(nextButton);
        return !buttons.isEmpty() && !buttons.get(0).getAttribute("class").contains("disabled");
    }

    private void goToNextPage() {
        driver.findElements(nextButton).get(0).click();
        System.out.println("=== NAVIGATING TO NEXT PAGE ===");
        waitForInvisibility(processingIndicator);
        waitForRowCountAtLeast(dataRow, 1);
    }

    private int getRowCount() {
        return driver.findElements(By.cssSelector("tbody tr")).size();
    }

    private WebElement getRow(int index) {
        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));
        return index < rows.size() ? rows.get(index) : null;
    }

    private boolean isParentRow(WebElement tr) {
        return "row".equals(tr.getAttribute("role"));
    }

    private boolean isChildRow(WebElement tr) {
        String cls = tr.getAttribute("class");
        return cls != null && cls.contains("child_row");
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

    private void downloadIfAvailable(WebElement childRow, DisciplinaryData data) {
        List<WebElement> downloadLinks = childRow.findElements(By.cssSelector("span.download"));
        if (downloadLinks.isEmpty()) return;

        WebElement link = downloadLinks.get(0).findElement(By.xpath("./parent::a"));
        String downloadUrl = link.getAttribute("href");
        data.setDownloadUrl(downloadUrl);

        Set<Cookie> cookies = driver.manage().getCookies();
        String fileName = downloadService.downloadFile(downloadUrl, cookies, DOWNLOAD_DIR);
        data.setLocalFileName(fileName);
    }

    private PageResult collectPage(String currentSeason) throws InterruptedException {
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
                downloadIfAvailable(tr, data);
                results.add(data);
            }
        }
        return new PageResult(results, foundOldCase);
    }
}