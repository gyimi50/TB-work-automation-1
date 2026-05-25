package com.gyimiproject.tb_automation.db;

import com.gyimiproject.tb_automation.document.DocumentProcessor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.time.LocalDate;
import com.gyimiproject.tb_automation.selenium.DisciplinaryData;
import com.gyimiproject.tb_automation.selenium.output.OutputPagePost;
import com.gyimiproject.tb_automation.selenium.base.YamlConfigReader;

@Service
public class DocumentService {

    private final DocumentProcessor documentProcessor;
    private final DisciplinaryCaseRepository repository;
    private final YamlConfigReader configReader;

    public DocumentService(DocumentProcessor documentProcessor,
                           DisciplinaryCaseRepository repository,
                           YamlConfigReader configReader) {
        this.documentProcessor = documentProcessor;
        this.repository = repository;
        this.configReader = configReader;
    }

    public boolean isAlreadyProcessed(String fileName) {
        return repository.existsByLocalFileName(fileName);
    }

    public DisciplinaryCase processAndSave(File file) throws Exception {
        String bbcodeContent = documentProcessor.process(file);

        DisciplinaryCase disciplinaryCase = DisciplinaryCase.builder()
                .localFileName(file.getName())
                .bbcodeContent(bbcodeContent)
                .documentStatus(DisciplinaryCase.DocumentStatus.PENDING)
                .processedDate(LocalDate.now())
                .build();

        return repository.save(disciplinaryCase);
    }

    public DisciplinaryCase saveFromWeb(DisciplinaryData data) {
        // duplicate check
        if (data.getMatchCode() != null && data.getCaseNumbers() != null) {
            if (repository.existsByMatchCodeAndCaseNumbers(
                    data.getMatchCode(), data.getCaseNumbers())) {
                System.out.println("Skipping duplicate: " + data.getMatchCode());
                return null;
            }
        }

        DisciplinaryCase disciplinaryCase = DisciplinaryCase.builder()
                .matchCode(data.getMatchCode())
                .leagueCode(data.getLeagueCode())
                .initiatedBy(data.getInitiatedBy())
                .teamHome(data.getTeamHome())
                .teamAway(data.getTeamAway())
                .caseNumbers(data.getCaseNumbers())
                .personInvolved(data.getPersonInvolved())
                .involvedType(data.getInvolvedType())
                .organization(data.getOrganization())
                .disciplinaryReason(data.getDisciplinaryReason())
                .regulation(data.getRegulation())
                .localFileName(data.getLocalFileName())
                .downloadUrl(data.getDownloadUrl())
                .documentStatus(data.getDownloadUrl() != null ?
                        DisciplinaryCase.DocumentStatus.PENDING :
                        DisciplinaryCase.DocumentStatus.AWAITING_DECISION)
                .processedDate(LocalDate.now())
                .matchDate(data.getMatchDate())
                .disciplinaryStatus(data.getDisciplinaryStatus())
                .build();

        return repository.save(disciplinaryCase);
    }

    public void processPendingFiles(String downloadDir) throws Exception {
        List<DisciplinaryCase> pending = repository
                .findByDocumentStatus(DisciplinaryCase.DocumentStatus.PENDING);

        for (DisciplinaryCase dc : pending) {
            if (dc.getLocalFileName() == null) continue;

            File file = new File(downloadDir, dc.getLocalFileName());

            if (!file.exists()) {
                System.out.println("File not found: " + dc.getLocalFileName());
                continue;
            }

            try {
                String bbcode = documentProcessor.process(file);
                dc.setBbcodeContent(bbcode);
                dc.setDocumentStatus(DisciplinaryCase.DocumentStatus.PROCESSED);
                repository.save(dc);

            } catch (Exception e) {
                dc.setDocumentStatus(DisciplinaryCase.DocumentStatus.FAILED);
                repository.save(dc);
                System.out.println("Failed: " + dc.getLocalFileName() + " - " + e.getMessage());
            }
        }
    }

    public void postProcessedCases(OutputPagePost postPage) throws Exception {
        List<DisciplinaryCase> processed = repository
                .findByDocumentStatus(DisciplinaryCase.DocumentStatus.PROCESSED);

        System.out.println("=== CASES TO POST: " + processed.size() + " ===");

        for (DisciplinaryCase dc : processed) {
            try {
                String involvedTypeOutput = configReader.getInvolvedTypeOutput(dc.getInvolvedType());
                String title = dc.getPersonInvolved() +
                        (involvedTypeOutput.isEmpty() ? "" : " " + involvedTypeOutput) +
                        " (" + dc.getMatchCode() + ")";
                postPage.submitPost(title, dc.getBbcodeContent());
                dc.setDocumentStatus(DisciplinaryCase.DocumentStatus.POSTED);
                repository.save(dc);
                System.out.println("=== POSTED: " + title + " ===");
                Thread.sleep(6000);
            } catch (Exception e) {
                dc.setDocumentStatus(DisciplinaryCase.DocumentStatus.FAILED);
                repository.save(dc);
                System.out.println("=== POST FAILED: " + dc.getMatchCode() + " - " + e.getMessage() + " ===");
            }
        }
    }
}