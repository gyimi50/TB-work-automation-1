package com.gyimiproject.tb_automation.selenium.input;

import com.gyimiproject.tb_automation.db.Report;
import com.gyimiproject.tb_automation.db.ReportRepository;
import com.gyimiproject.tb_automation.selenium.base.BasePage;
import com.gyimiproject.tb_automation.selenium.base.YamlLocatorReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class InputPageReports extends BasePage {

    private final By reportsLink;
    private final By reportTableCell;
    private final By nextButton = By.cssSelector("a.paginate_button.next");
    private final By eyeSpan;
    private final By closeButton;
    private final ReportRepository reportRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy. MM. dd HH:mm");

    public InputPageReports(WebDriver driver, YamlLocatorReader locatorReader,
                            ReportRepository reportRepository) {
        super(driver);
        this.reportsLink = By.cssSelector("a[href='" + locatorReader.get("input", "reportsPath") + "']");
        this.reportTableCell = By.cssSelector("table." + locatorReader.get("input", "tableClass") + " tbody tr td");
        this.eyeSpan = By.className(locatorReader.get("input", "eyeSpan"));
        this.closeButton = By.className(locatorReader.get("input", "closeButton"));
        this.reportRepository = reportRepository;
    }

    public void navigateToReports() {
        click(reportsLink);
    }

    public void collectAndSave(LocalDateTime cutoffDate) throws InterruptedException {
        while (true) {
            waitForElement(By.cssSelector("tbody tr[role='row']"));
            boolean foundOldCase = collectPage(cutoffDate);

            if (foundOldCase) {
                System.out.println("=== FOUND OLD RECORD, STOPPING ===");
                break;
            }

            if (!hasNextPage(nextButton)) {
                System.out.println("=== NO MORE PAGES ===");
                break;
            }

            goToNextPage(nextButton);
        }
    }

    private boolean collectPage(LocalDateTime cutoffDate) {
        WebElement currentParentRow = null;
        boolean foundOldCase = false;
        int rowCount = getRowCount();

        for (int i = 0; i < rowCount; i++) {
            WebElement tr = getRow(i);
            if (tr == null) break;

            if (isParentRow(tr)) {
                currentParentRow = tr;
            } else if (isChildRow(tr) && currentParentRow != null) {
                Report report = extractParentRowData(currentParentRow);
                if (report == null) continue;

                if (report.getMatchDate() != null &&
                        report.getMatchDate().isBefore(cutoffDate)) {
                    foundOldCase = true;
                    continue;
                }

                if (reportRepository.existsByMatchCode(report.getMatchCode())) {
                    updateReportsIfChanged(report.getMatchCode(), tr);
                    continue;
                }

                List<String> reportTexts = extractReportTexts(tr);
                setReportTexts(report, reportTexts);
                report.setStatus(Report.ReportStatus.PENDING);
                reportRepository.save(report);
                System.out.println("=== SAVED: " + report.getMatchCode() + " ===");
            }
        }
        return foundOldCase;
    }

    private Report extractParentRowData(WebElement parentRow) {
        List<WebElement> tds = parentRow.findElements(By.tagName("td"));
        if (tds.size() < 4) return null;

        String matchCode = tds.get(0).getText().split("\n")[0].trim();
        String teamHome = tds.get(1).getText().split("\n")[0].trim();
        String teamAway = tds.get(2).getText().split("\n")[0].trim();
        String rawDate = tds.get(3).getText().trim();

        return Report.builder()
                .matchCode(matchCode)
                .teamHome(teamHome)
                .teamAway(teamAway)
                .matchDate(parseDate(rawDate))
                .build();
    }

    private List<String> extractReportTexts(WebElement childRow) {
        List<String> texts = new ArrayList<>();
        List<WebElement> eyes = childRow.findElements(eyeSpan);

        for (WebElement eye : eyes) {
            try {
                jsClick(eye);
                WebElement panel = waitForPresence(By.cssSelector(".jsPanel .p_10 #text_field"));
                String text = panel.getText().trim();
                texts.add(text);
                ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("document.querySelector('.jsPanel').close()");
                wait.until(org.openqa.selenium.support.ui.ExpectedConditions
                        .invisibilityOfElementLocated(By.cssSelector(".jsPanel")));
            } catch (Exception e) {
                System.out.println("=== COULD NOT READ REPORT: " + e.getMessage().split("\n")[0] + " ===");
            }
        }
        return texts;
    }

    private void updateReportsIfChanged(String matchCode, WebElement childRow) {
        reportRepository.findByMatchCode(matchCode).ifPresent(existing -> {
            List<String> reportTexts = extractReportTexts(childRow);
            setReportTexts(existing, reportTexts);
            reportRepository.save(existing);
            System.out.println("=== UPDATED: " + matchCode + " ===");
        });
    }

    private void setReportTexts(Report report, List<String> texts) {
        report.setReport1(texts.size() > 0 ? texts.get(0) : null);
        report.setReport2(texts.size() > 1 ? texts.get(1) : null);
        report.setReport3(texts.size() > 2 ? texts.get(2) : null);
        report.setReport4(texts.size() > 3 ? texts.get(3) : null);
    }

    private LocalDateTime parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDateTime.parse(raw.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("=== UNPARSEABLE DATE: " + raw + " ===");
            return null;
        }
    }
}