package com.gyimiproject.tb_automation.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DisciplinaryCaseRepository
        extends JpaRepository<DisciplinaryCase, Long> {

    List<DisciplinaryCase> findByDocumentStatus(
            DisciplinaryCase.DocumentStatus status
    );
    boolean existsByFileName(String fileName);
    boolean existsByMatchCodeAndCaseNumbers(String matchCode, String caseNumbers);
}