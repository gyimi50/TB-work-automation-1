package com.gyimiproject.tb_automation.selenium.output;

import com.gyimiproject.tb_automation.selenium.base.BasePage;
import com.gyimiproject.tb_automation.selenium.base.YamlLocatorReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OutputPagePost extends BasePage {

    private final By forumLink;
    private final By caseBoardLink;
    private final String seasonTopicText;
    private final By replyButton;
    private final By subjectField;
    private final By messageField;
    private final By submitButton;

    public OutputPagePost(WebDriver driver, YamlLocatorReader locatorReader) {
        super(driver);
        this.forumLink = By.xpath(locatorReader.get("output", "forumLink"));
        this.caseBoardLink = By.xpath(locatorReader.get("output", "caseBoardLink"));
        this.seasonTopicText = locatorReader.get("output", "seasonTopic");
        this.replyButton = By.cssSelector(locatorReader.get("output", "replyButton"));
        this.subjectField = By.cssSelector(locatorReader.get("output", "subjectField"));
        this.messageField = By.cssSelector(locatorReader.get("output", "messageField"));
        this.submitButton = By.xpath(locatorReader.get("output", "submitButton"));
    }

    public void clickSeasonTopic() {
        WebElement topicLink = waitForElement(
                By.xpath("//a[contains(text(),'" + seasonTopicText + "')]"));
        topicLink.click();
    }

    public void navigateToSeasonTopic() {
        click(forumLink);
        switchToFrame(By.id("jfusioniframe"));
        waitForPresence(caseBoardLink).click();
        clickSeasonTopic();
    }

    public void submitPost(String subject, String message) {
        click(replyButton);
        WebElement subjectEl = waitForElement(subjectField);
        subjectEl.clear();
        subjectEl.sendKeys(subject);
        sendKeys(messageField, message);
        click(submitButton);
        clickSeasonTopic();
    }
}