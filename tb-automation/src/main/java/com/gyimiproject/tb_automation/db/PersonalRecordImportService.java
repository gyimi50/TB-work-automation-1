package com.gyimiproject.tb_automation.db;

import com.gyimiproject.tb_automation.selenium.base.YamlConfigReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonalRecordImportService {

    private final PersonalRecordRepository repository;
    private final YamlConfigReader configReader;

    private static final DateTimeFormatter FORMAT_SLASH = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DateTimeFormatter FORMAT_DOT = DateTimeFormatter.ofPattern("yy.MM.dd");

    public void importFromCsv() throws Exception {
        List<PersonalRecord> records = new ArrayList<>();
        String csvPath = configReader.getPersonalRecordsCsvPath();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath, java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.replace(",", "").isBlank()) continue;

                String[] cols = parseCsvLine(line);
                if (cols.length < 6) continue;

                String rawDate = getCol(cols, 2);
                String matchCode = getCol(cols, 3);
                String teams = getCol(cols, 4);
                String playerName = getCol(cols, 5);
                String team = getCol(cols, 6);
                String notes = getCol(cols, 7);

                if (matchCode.isBlank() || playerName.isBlank()) continue;

                LocalDate matchDate = parseDate(rawDate);
                if (matchDate == null) continue;

                if (repository.existsByMatchCodeAndPlayerName(matchCode, playerName)) continue;

                records.add(PersonalRecord.builder()
                        .matchDate(matchDate)
                        .matchCode(matchCode)
                        .teams(teams)
                        .playerName(playerName)
                        .team(team.isBlank() ? null : team)
                        .notes(notes.isBlank() ? null : notes)
                        .build());
            }
        }

        repository.saveAll(records);
        System.out.println("=== IMPORTED: " + records.size() + " records ===");
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw.trim(), FORMAT_SLASH);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(raw.trim(), FORMAT_DOT);
            } catch (DateTimeParseException e2) {
                System.out.println("=== UNPARSEABLE DATE: " + raw + " ===");
                return null;
            }
        }
    }

    private String getCol(String[] cols, int index) {
        return index < cols.length ? cols[index].trim() : "";
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
}