package com.gyimiproject.tb_automation.selenium.base;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@Component
public class YamlLocatorReader {

    private final Map<String, Object> locators;

    public YamlLocatorReader() {
        Yaml yaml = new Yaml();
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("locators.yaml")) {
            if (is == null) {
                throw new RuntimeException("locators.yaml not found in resources");
            }
            locators = yaml.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load locators.yaml", e);
        }
    }

    public String get(String section, String key) {
        Map<String, Object> section_map = (Map<String, Object>) locators.get(section);
        if (section_map == null) {
            throw new RuntimeException("Section not found: " + section);
        }
        Object value = section_map.get(key);
        if (value == null) {
            throw new RuntimeException("Key not found: " + key + " in section: " + section);
        }
        return value.toString();
    }
}