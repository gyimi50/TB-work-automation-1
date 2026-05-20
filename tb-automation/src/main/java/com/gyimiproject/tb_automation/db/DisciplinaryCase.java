package com.gyimiproject.tb_automation.db;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "disciplinary_cases")
public class DisciplinaryCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matchCode;
    private String matchDate;
    private String teamHome;
    private String teamAway;
    private String personInvolved;
    private String involvedType;
    private String disciplinaryStatus;
    private String leagueCode;
    private String initiatedBy;
    private String organization;
    @Column(columnDefinition = "TEXT")
    private String caseNumbers;
    @Column(columnDefinition = "TEXT")
    private String disciplinaryReason;
    private String regulation;
    private String downloadUrl;
    private String localFileName;
    @Column(columnDefinition = "TEXT")
    private String bbcodeContent;

    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus;

    private LocalDate processedDate;

    public enum DocumentStatus {
        PENDING, PROCESSED, POSTED, FAILED, AWAITING_DECISION
    }
}