package com.gyimiproject.tb_automation.selenium.base;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.time.LocalDateTime;

@Component
public class YamlConfigReader {

    private final Map<String, Object> config;

    public YamlConfigReader() {
        Yaml yaml = new Yaml();
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("config.yaml")) {
            config = yaml.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.yaml", e);
        }
    }

    public String getTestFilesPath() {
        return (String) config.get("testFilesPath");
    }

    public String getInvolvedTypeOutput(String involvedType) {
        if (involvedType == null) return "";
        Map<String, String> mapping = (Map<String, String>) config.get("involvedTypeMapping");
        return mapping.getOrDefault(involvedType, involvedType);
    }

    public String getSeasonTopic() {
        return (String) config.get("seasonTopic");
    }

    public String getCurrentSeason() {
        return (String) config.get("currentSeason");
    }

    public LocalDateTime getReportsCutoffDate() {
        return LocalDateTime.parse((String) config.get("reportsCutoffDate"));
    }

    public String getRegistrationText() {
        return (String) config.get("registrationText");
    }

    public String getRegistrationColor() {
        return (String) config.get("registrationColor");
    }

    public String getPersonalRecordsCsvPath() {
        return (String) config.get("personalRecordsCsvPath");
    }
}