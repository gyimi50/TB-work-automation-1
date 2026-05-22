package com.gyimiproject.tb_automation.selenium.input;

import com.gyimiproject.tb_automation.selenium.base.BasePage;
import com.gyimiproject.tb_automation.selenium.base.YamlLocatorReader;
import org.openqa.selenium.By;

public class InputPageLogin extends BasePage {

    private final By usernameField;
    private final By passwordField;
    private final By loginButton;
    private final String loginUrl;

    public InputPageLogin (YamlLocatorReader locatorReader) {
        super();
        this.loginUrl = locatorReader.get("input", "url");
        this.usernameField = By.name(locatorReader.get("input", "usernameField"));
        this.passwordField = By.name(locatorReader.get("input", "passwordField"));
        this.loginButton = By.id(locatorReader.get("input", "loginButton"));
    }

    public void login(String username, String password) {
        navigateTo(loginUrl);
        sendKeys(usernameField, username);
        sendKeys(passwordField, password);
        click(loginButton);
    }
}