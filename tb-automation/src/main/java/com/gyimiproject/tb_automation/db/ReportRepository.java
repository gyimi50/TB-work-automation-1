package com.gyimiproject.tb_automation.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByMatchCode(String matchCode);
    boolean existsByMatchCode(String matchCode);
    List<Report> findByStatusOrderByMatchDateDesc(Report.ReportStatus status);
    List<Report> findByStatusOrderByMatchDateAsc(Report.ReportStatus status);
    List<Report> findByMatchDateAfter(LocalDateTime dateTime);
}