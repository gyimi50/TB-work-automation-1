package com.gyimiproject.tb_automation.controller;

import com.gyimiproject.tb_automation.OrchestrationService;
import com.gyimiproject.tb_automation.db.DisciplinaryCase;
import com.gyimiproject.tb_automation.db.DisciplinaryCaseRepository;
import com.gyimiproject.tb_automation.db.PersonalRecord;
import com.gyimiproject.tb_automation.db.PersonalRecordImportService;
import com.gyimiproject.tb_automation.db.PersonalRecordRepository;
import com.gyimiproject.tb_automation.db.Report;
import com.gyimiproject.tb_automation.db.ReportRepository;
import com.gyimiproject.tb_automation.selenium.base.YamlConfigReader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class HandlerController {

    private final OrchestrationService orchestrationService;
    private final DisciplinaryCaseRepository repository;
    private final PersonalRecordRepository personalRecordRepository;
    private final PersonalRecordImportService importService;
    private final ReportRepository reportRepository;
    private final YamlConfigReader configReader;

    public HandlerController(OrchestrationService orchestrationService,
                             DisciplinaryCaseRepository repository,
                             PersonalRecordRepository personalRecordRepository,
                             PersonalRecordImportService importService,
                             ReportRepository reportRepository,
                             YamlConfigReader configReader) {
        this.orchestrationService = orchestrationService;
        this.repository = repository;
        this.personalRecordRepository = personalRecordRepository;
        this.importService = importService;
        this.reportRepository = reportRepository;
        this.configReader = configReader;
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    private String runFlow(ThrowingRunnable flow) {
        try {
            flow.run();
            return "Flow lefutott.";
        } catch (Exception e) {
            return "Hiba: " + e.getMessage();
        }
    }

    private String buildTitle(DisciplinaryCase dc) {
        String involvedTypeOutput = configReader.getInvolvedTypeOutput(dc.getInvolvedType());
        return dc.getPersonInvolved() +
                (involvedTypeOutput.isEmpty() ? "" : " " + involvedTypeOutput) +
                " (" + dc.getMatchCode() + ")";
    }

    private void addCasesToModel(Model model) {
        List<DisciplinaryCase> cases = repository.findByBbcodeContentIsNotNull();
        Map<Long, String> titles = cases.stream()
                .collect(java.util.stream.Collectors.toMap(
                        DisciplinaryCase::getId,
                        this::buildTitle
                ));
        model.addAttribute("cases", cases);
        model.addAttribute("titles", titles);
    }

    private String[] splitTeams(String teams) {
        if (teams == null) return new String[]{"?", "?"};
        String[] parts = teams.split("\\s*-+\\s*");
        if (parts.length >= 2) return new String[]{parts[0].trim(), parts[1].trim()};
        return new String[]{teams.trim(), "?"};
    }

    private void addRecordsToModel(Model model) {
        List<PersonalRecord> records = personalRecordRepository.findByMatchDateAfterOrderByMatchDateDesc(LocalDate.of(2025, 5, 1));
        Map<Long, String[]> teamSplits = records.stream()
                .collect(java.util.stream.Collectors.toMap(
                        PersonalRecord::getId,
                        r -> splitTeams(r.getTeams())
                ));
        model.addAttribute("records", records);
        model.addAttribute("teamSplits", teamSplits);
    }

    private void addReportsToModel(Model model) {
        model.addAttribute("reports", reportRepository.findByStatusOrderByMatchDateDesc(Report.ReportStatus.PENDING));
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("status", "Készen áll.");
        addCasesToModel(model);
        return "index";
    }

    @PostMapping("/run-input")
    public String runInput(Model model) {
        model.addAttribute("status", runFlow(orchestrationService::runInputFlow));
        addCasesToModel(model);
        return "index";
    }

    @PostMapping("/run-output")
    public String runOutput(Model model) {
        model.addAttribute("status", runFlow(orchestrationService::runOutputFlow));
        addCasesToModel(model);
        return "index";
    }

    @PostMapping("/run-full")
    public String runFull(Model model) {
        model.addAttribute("status", runFlow(orchestrationService::runFullFlow));
        addCasesToModel(model);
        return "index";
    }

    @PostMapping("/import-personal-records")
    public String importPersonalRecords(Model model) {
        model.addAttribute("status", runFlow(importService::importFromCsv));
        addCasesToModel(model);
        return "index";
    }

    @PostMapping("/run-reports")
    public String runReports(Model model) {
        model.addAttribute("status", runFlow(orchestrationService::runReportsFlow));
        addCasesToModel(model);
        return "index";
    }

    @GetMapping("/personal-records")
    public String personalRecords(Model model) {
        addRecordsToModel(model);
        return "personal-records";
    }

    @PostMapping("/personal-records/update-team")
    public String updateTeam(@RequestParam Long id,
                             @RequestParam String team,
                             Model model) {
        personalRecordRepository.findById(id).ifPresent(record -> {
            record.setTeam(team);
            personalRecordRepository.save(record);
        });
        addRecordsToModel(model);
        return "personal-records";
    }

    @PostMapping("/personal-records/delete")
    public String deleteRecord(@RequestParam Long id, Model model) {
        personalRecordRepository.deleteById(id);
        addRecordsToModel(model);
        return "personal-records";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        addReportsToModel(model);
        return "reports";
    }

    @PostMapping("/reports/skip")
    public String skipReport(@RequestParam Long id, Model model) {
        reportRepository.findById(id).ifPresent(report -> {
            report.setStatus(Report.ReportStatus.IGNORED);
            reportRepository.save(report);
        });
        addReportsToModel(model);
        return "reports";
    }

    @PostMapping("/reports/register")
    public String registerReport(@RequestParam Long id,
                                 @RequestParam String playerName,
                                 @RequestParam String team,
                                 @RequestParam(required = false) String notes,
                                 Model model) {
        reportRepository.findById(id).ifPresent(report -> {
            PersonalRecord record = PersonalRecord.builder()
                    .matchDate(report.getMatchDate().toLocalDate())
                    .matchCode(report.getMatchCode())
                    .teams(report.getTeamHome() + " - " + report.getTeamAway())
                    .playerName(playerName)
                    .team(team)
                    .notes(notes)
                    .build();
            personalRecordRepository.save(record);
            report.setStatus(Report.ReportStatus.REGISTERED_PENDING);
            reportRepository.save(report);
        });
        addReportsToModel(model);
        return "reports";
    }

    @PostMapping("/run-writeback")
    public String runWriteBack(Model model) {
        model.addAttribute("status", runFlow(orchestrationService::runWriteBackFlow));
        addCasesToModel(model);
        return "index";
    }
}