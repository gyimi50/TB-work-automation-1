package com.gyimiproject.tb_automation.db;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    private String fileName;
    private String matchCode;
    private Integer disciplinaryCount;
    private String initiatedBy;
    private String teamHome;
    private String teamAway;
    @Column(columnDefinition = "TEXT")
    private String matchDate;
    private String caseNumber;
    private String personInvolved;
    private String involvedType;
    @Column(columnDefinition = "TEXT")
    private String affiliation;
    @Column(columnDefinition = "TEXT")
    private String disciplinaryReason;
    private String regulation;
    private String matchIdentifier;
    @Column(columnDefinition = "TEXT")
    private String caseNumbers;
    private String downloadUrl;
    private String localFileName;


    @Enumerated(EnumType.STRING)
    private DisciplinaryStatus disciplinaryStatus;

    @Column(columnDefinition = "TEXT")
    private String bbcodeContent;

    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus;

    private LocalDate processedDate;

    public enum DisciplinaryStatus {
        NO_DECISION, DECISION_SENT, REJECTED
    }

    public enum DocumentStatus {
        PENDING, PROCESSED, POSTED, FAILED, AWAITING_DECISION
    }
}