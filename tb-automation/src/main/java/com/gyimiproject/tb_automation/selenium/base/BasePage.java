package com.gyimiproject.tb_automation.selenium.base;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import java.time.Duration;

public abstract class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;
    private static final int TIMEOUT_SECONDS = 10;

    public BasePage() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-position=2560,0");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory",
                new File("../test-files").getAbsolutePath());
        prefs.put("download.prompt_for_download", false);
        options.setExperimentalOption("prefs", prefs);
        this.driver = new ChromeDriver(options);
        this.driver.manage().window().maximize();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
    }

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
    }

    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void waitForInvisibility(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void waitForRowCountAtLeast(By locator, int min) {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(locator, min - 1));
    }

    protected WebElement waitForPresence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected void jsClick(By locator) {
        WebElement element = driver.findElement(locator);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    protected void switchToFrame(By locator) {
        driver.switchTo().frame(waitForElement(locator));
    }

    protected void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    protected void click(By locator) {
        waitForElement(locator).click();
    }

    protected void sendKeys(By locator, String text) {
        waitForElement(locator).sendKeys(text);
    }

    protected void navigateTo(String url) {
        driver.get(url);
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void quit() {
        if (driver != null) {
            driver.quit();
        }
    }
    protected void jsClick(WebElement element) {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    protected boolean isParentRow(WebElement tr) {
        return "row".equals(tr.getAttribute("role"));
    }

    protected boolean isChildRow(WebElement tr) {
        String cls = tr.getAttribute("class");
        return cls != null && cls.contains("child_row");
    }

    protected int getRowCount() {
        return driver.findElements(By.cssSelector("tbody tr")).size();
    }

    protected WebElement getRow(int index) {
        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));
        return index < rows.size() ? rows.get(index) : null;
    }

    protected boolean hasNextPage(By nextButton) {
        List<WebElement> buttons = driver.findElements(nextButton);
        return !buttons.isEmpty() && !buttons.get(0).getAttribute("class").contains("disabled");
    }

    protected void goToNextPage(By nextButton) {
        driver.findElements(nextButton).get(0).click();
        System.out.println("=== NAVIGATING TO NEXT PAGE ===");
        waitForInvisibility(By.id("DataTables_Table_0_processing"));
        waitForRowCountAtLeast(By.cssSelector("tbody tr[role='row']"), 1);
    }

    public void setPageSizeTo100() {
        By pageLengthSelect = By.name("DataTables_Table_0_length");
        waitForElement(pageLengthSelect);
        new org.openqa.selenium.support.ui.Select(driver.findElement(pageLengthSelect)).selectByValue("100");
        waitForInvisibility(By.id("DataTables_Table_0_processing"));
        waitForRowCountAtLeast(By.cssSelector("tbody tr[role='row']"), 1);
    }
}