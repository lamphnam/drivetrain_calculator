package com.drivetrain.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
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
@Table(name = "module4_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"designCase", "shaftForces"})
public class Module4Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "input_t2_nmm", precision = 19, scale = 6)
    private BigDecimal inputT2Nmm;

    @Column(name = "input_n2_rpm", precision = 19, scale = 6)
    private BigDecimal inputN2Rpm;

    @Column(name = "input_u3", precision = 19, scale = 6)
    private BigDecimal inputU3;

    @Column(name = "allowable_contact_stress_mpa", precision = 19, scale = 6)
    private BigDecimal allowableContactStressMpa;

    @Column(name = "allowable_bending_stress_gear1_mpa", precision = 19, scale = 6)
    private BigDecimal allowableBendingStressGear1Mpa;

    @Column(name = "allowable_bending_stress_gear2_mpa", precision = 19, scale = 6)
    private BigDecimal allowableBendingStressGear2Mpa;

    @Column(name = "center_distance_aw_mm", precision = 19, scale = 6)
    private BigDecimal centerDistanceAwMm;

    @Column(name = "module_m_selected", precision = 19, scale = 6)
    private BigDecimal moduleMSelected;

    @Column(name = "teeth_z1")
    private Integer teethZ1;

    @Column(name = "teeth_z2")
    private Integer teethZ2;

    @Column(name = "actual_ratio_u3", precision = 19, scale = 6)
    private BigDecimal actualRatioU3;

    @Column(name = "ratio_error_percent", precision = 19, scale = 6)
    private BigDecimal ratioErrorPercent;

    @Column(name = "diameter_dw1_mm", precision = 19, scale = 6)
    private BigDecimal diameterDw1Mm;

    @Column(name = "diameter_dw2_mm", precision = 19, scale = 6)
    private BigDecimal diameterDw2Mm;

    @Column(name = "width_bw_mm", precision = 19, scale = 6)
    private BigDecimal widthBwMm;

    @Column(name = "epsilon_alpha", precision = 19, scale = 6)
    private BigDecimal epsilonAlpha;

    @Column(name = "z_epsilon", precision = 19, scale = 6)
    private BigDecimal zEpsilon;

    @Column(name = "y_epsilon", precision = 19, scale = 6)
    private BigDecimal yEpsilon;

    @Column(name = "y_f1", precision = 19, scale = 6)
    private BigDecimal yF1;

    @Column(name = "y_f2", precision = 19, scale = 6)
    private BigDecimal yF2;

    @Column(name = "load_factor_kh", precision = 19, scale = 6)
    private BigDecimal loadFactorKh;

    @Column(name = "load_factor_kf", precision = 19, scale = 6)
    private BigDecimal loadFactorKf;

    @Column(name = "sigma_h_mpa", precision = 19, scale = 6)
    private BigDecimal sigmaHMpa;

    @Column(name = "sigma_f1_mpa", precision = 19, scale = 6)
    private BigDecimal sigmaF1Mpa;

    @Column(name = "sigma_f2_mpa", precision = 19, scale = 6)
    private BigDecimal sigmaF2Mpa;

    @Column(name = "contact_stress_pass", nullable = false)
    private boolean contactStressPass;

    @Column(name = "bending_stress_gear1_pass", nullable = false)
    private boolean bendingStressGear1Pass;

    @Column(name = "bending_stress_gear2_pass", nullable = false)
    private boolean bendingStressGear2Pass;

    @Column(name = "calculation_note", length = 4000)
    private String calculationNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "design_case_id", nullable = false, unique = true)
    private DesignCase designCase;

    @Builder.Default
    @OneToMany(mappedBy = "module4Result", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<Module4ShaftForce> shaftForces = new ArrayList<>();

    public void addShaftForce(Module4ShaftForce shaftForce) {
        shaftForces.add(shaftForce);
        shaftForce.setModule4Result(this);
    }

    public void removeShaftForce(Module4ShaftForce shaftForce) {
        shaftForces.remove(shaftForce);
        shaftForce.setModule4Result(null);
    }
}
