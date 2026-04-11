package com.drivetrain.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "design_constant_set")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "designCases")
public class DesignConstantSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "set_code", unique = true, nullable = false, length = 100)
    private String setCode;

    @Column(name = "set_name", length = 255)
    private String setName;

    @Column(name = "eta_kn", precision = 19, scale = 6)
    private BigDecimal etaKn;

    @Column(name = "eta_d", precision = 19, scale = 6)
    private BigDecimal etaD;

    @Column(name = "eta_brc", precision = 19, scale = 6)
    private BigDecimal etaBrc;

    @Column(name = "eta_brt", precision = 19, scale = 6)
    private BigDecimal etaBrt;

    @Column(name = "eta_ol", precision = 19, scale = 6)
    private BigDecimal etaOl;

    @Column(name = "default_belt_ratio_u1", precision = 19, scale = 6)
    private BigDecimal defaultBeltRatioU1;

    @Column(name = "default_gearbox_ratio_uh", precision = 19, scale = 6)
    private BigDecimal defaultGearboxRatioUh;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "constantSet", fetch = FetchType.LAZY)
    private List<DesignCase> designCases = new ArrayList<>();
}
