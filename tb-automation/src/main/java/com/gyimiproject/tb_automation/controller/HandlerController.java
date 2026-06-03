package com.gyimiproject.tb_automation.controller;

import com.gyimiproject.tb_automation.OrchestrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HandlerController {

    private final OrchestrationService orchestrationService;

    public HandlerController(OrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
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

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("status", "Készen áll.");
        return "index";
    }

    @PostMapping("/run-input")
    public String runInput(Model model) {
        model.addAttribute("status", runFlow(orchestrationService::runInputFlow));
        return "index";
    }

    @PostMapping("/run-output")
    public String runOutput(Model model) {
        model.addAttribute("status", runFlow(orchestrationService::runOutputFlow));
        return "index";
    }

    @PostMapping("/run-full")
    public String runFull(Model model) {
        model.addAttribute("status", runFlow(orchestrationService::runFullFlow));
        return "index";
    }
}