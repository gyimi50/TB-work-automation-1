package com.gyimiproject.tb_automation.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PersonalRecordRepository extends JpaRepository<PersonalRecord, Long> {

    List<PersonalRecord> findByPlayerName(String playerName);
    boolean existsByMatchCodeAndPlayerName(String matchCode, String playerName);
    List<PersonalRecord> findByMatchDateAfterOrderByMatchDateDesc(LocalDate date);
}