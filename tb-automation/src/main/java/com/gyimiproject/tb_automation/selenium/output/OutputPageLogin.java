package com.gyimiproject.tb_automation.selenium.output;

import com.gyimiproject.tb_automation.selenium.base.BasePage;
import com.gyimiproject.tb_automation.selenium.base.YamlLocatorReader;
import org.openqa.selenium.By;

public class OutputPageLogin extends BasePage {

    private final By usernameField;
    private final By passwordField;
    private final By loginButton;
    private final String loginUrl;

    public OutputPageLogin(YamlLocatorReader locatorReader) {
        super();
        this.loginUrl = locatorReader.get("output", "url");
        this.usernameField = By.cssSelector(locatorReader.get("output", "usernameField"));
        this.passwordField = By.cssSelector(locatorReader.get("output", "passwordField"));
        this.loginButton = By.cssSelector(locatorReader.get("output", "loginButton"));
    }

    public void login(String username, String password) {
        navigateTo(loginUrl);
        sendKeys(usernameField, username);
        sendKeys(passwordField, password);
        click(loginButton);
    }
}