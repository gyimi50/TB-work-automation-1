package com.gyimiproject.tb_automation.db;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime matchDate;
    private String matchCode;
    private String teamHome;
    private String teamAway;

    @Column(columnDefinition = "TEXT")
    private String report1;

    @Column(columnDefinition = "TEXT")
    private String report2;

    @Column(columnDefinition = "TEXT")
    private String report3;

    @Column(columnDefinition = "TEXT")
    private String report4;

    private Integer pageNumber;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    public enum ReportStatus {
        PENDING, IGNORED, TO_DISCIPLINARY, REGISTERED_PENDING, REGISTERED
    }
}