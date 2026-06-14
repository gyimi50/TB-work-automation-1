package com.gyimiproject.tb_automation.selenium.input;

import com.gyimiproject.tb_automation.db.Report;
import com.gyimiproject.tb_automation.db.ReportRepository;
import com.gyimiproject.tb_automation.selenium.base.BasePage;
import com.gyimiproject.tb_automation.selenium.base.YamlLocatorReader;
import com.gyimiproject.tb_automation.selenium.base.YamlConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class InputPageWriteBack extends BasePage {

    private final By reportsLink;
    private final ReportRepository reportRepository;
    private final YamlConfigReader configReader;

    public InputPageWriteBack(WebDriver driver, YamlLocatorReader locatorReader,
                              ReportRepository reportRepository,
                              YamlConfigReader configReader) {
        super(driver);
        this.reportsLink = By.cssSelector("a[href='" + locatorReader.get("input", "reportsPath") + "']");
        this.reportRepository = reportRepository;
        this.configReader = configReader;
    }

    public void navigateToReports() {
        click(reportsLink);
    }

    public void writeBackAll() throws InterruptedException {
        List<Report> pending = reportRepository.findByStatusOrderByMatchDateAsc(
                Report.ReportStatus.REGISTERED_PENDING);
        System.out.println("=== WRITEBACK PENDING: " + pending.size() + " records ===");
        for (Report report : pending) {
            writeBack(report);
        }
    }

    private void writeBack(Report report) throws InterruptedException {
        System.out.println("=== WRITING BACK: " + report.getMatchCode() + " ===");

        WebElement parentRow = findParentRowByMatchCode(report.getMatchCode());
        if (parentRow == null) {
            System.out.println("=== NOT FOUND: " + report.getMatchCode() + " ===");
            return;
        }

        WebElement editSpan = parentRow.findElement(By.cssSelector("span.szerkesztes"));
        jsClick(editSpan);

        waitForPresence(By.cssSelector(".jsPanel"));

        WebElement createButton = waitForPresence(
                By.cssSelector("button.submit_process[change_mode='create_dis_in']"));
        jsClick(createButton);

        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        waitForPresence(By.cssSelector(".jsPanel"));
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        wait.until(org.openqa.selenium.support.ui.ExpectedConditions
                .presenceOfElementLocated(By.cssSelector(".jsPanel iframe")));

        wait.until(d -> {
            Object result = ((org.openqa.selenium.JavascriptExecutor) d)
                    .executeScript("return typeof tinymce !== 'undefined' && tinymce.activeEditor !== null;");
            return Boolean.TRUE.equals(result);
        });

        String html = "<span style=\"background-color: " + configReader.getRegistrationColor() + ";\">"
                + configReader.getRegistrationText() + "</span>";

        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "var ed = tinymce.activeEditor;" +
                        "ed.setContent('" + html + "');" +
                        "ed.save();");

        WebElement saveButton = waitForPresence(
                By.cssSelector("button[name='save_warning']"));
        saveButton.click();

        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("document.querySelector('.jsPanel').close()");

        wait.until(org.openqa.selenium.support.ui.ExpectedConditions
                .invisibilityOfElementLocated(By.cssSelector(".jsPanel")));

        report.setStatus(Report.ReportStatus.REGISTERED);
        reportRepository.save(report);
        System.out.println("=== WRITTEN BACK: " + report.getMatchCode() + " ===");
    }

    private WebElement findParentRowByMatchCode(String matchCode) {
        int rowCount = getRowCount();
        for (int i = 0; i < rowCount; i++) {
            WebElement tr = getRow(i);
            if (tr == null) continue;
            if (!isParentRow(tr)) continue;
            List<WebElement> tds = tr.findElements(By.tagName("td"));
            if (tds.isEmpty()) continue;
            String text = tds.get(0).getText();
            if (text.contains(matchCode)) return tr;
        }
        return null;
    }
}