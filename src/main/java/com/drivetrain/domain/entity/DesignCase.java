package com.drivetrain.domain.entity;

import com.drivetrain.domain.enums.DesignCaseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "design_case")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"constantSet", "module1Result", "module3Result"})
public class DesignCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_code", unique = true, nullable = false, length = 100)
    private String caseCode;

    @Column(name = "case_name", length = 255)
    private String caseName;

    @Column(name = "required_power_kw", precision = 19, scale = 6)
    private BigDecimal requiredPowerKw;

    @Column(name = "required_output_rpm", precision = 19, scale = 6)
    private BigDecimal requiredOutputRpm;

    @Column(name = "service_life_hours", precision = 19, scale = 6)
    private BigDecimal serviceLifeHours;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private DesignCaseStatus status = DesignCaseStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "constant_set_id", nullable = false)
    private DesignConstantSet constantSet;

    @OneToOne(mappedBy = "designCase", fetch = FetchType.LAZY)
    private Module1Result module1Result;

    @OneToOne(mappedBy = "designCase", fetch = FetchType.LAZY)
    private Module3Result module3Result;
}
