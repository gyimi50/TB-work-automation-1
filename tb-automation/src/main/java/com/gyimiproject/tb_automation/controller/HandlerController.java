package com.gyimiproject.tb_automation.controller;

import com.gyimiproject.tb_automation.OrchestrationService;
import com.gyimiproject.tb_automation.db.DisciplinaryCase;
import com.gyimiproject.tb_automation.db.DisciplinaryCaseRepository;
import com.gyimiproject.tb_automation.selenium.base.YamlConfigReader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.Map;
import java.util.List;

@Controller
public class HandlerController {

    private final OrchestrationService orchestrationService;
    private final DisciplinaryCaseRepository repository;
    private final YamlConfigReader configReader;

    public HandlerController(OrchestrationService orchestrationService,
                             DisciplinaryCaseRepository repository,
                             YamlConfigReader configReader) {
        this.orchestrationService = orchestrationService;
        this.repository = repository;
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
}