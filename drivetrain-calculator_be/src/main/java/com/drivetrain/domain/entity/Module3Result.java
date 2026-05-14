package com.drivetrain.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "module3_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"designCase", "material", "shaftForces"})
public class Module3Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "input_t1_nmm", precision = 19, scale = 6)
    private BigDecimal inputT1Nmm;

    @Column(name = "input_n1_rpm", precision = 19, scale = 6)
    private BigDecimal inputN1Rpm;

    @Column(name = "input_u2", precision = 19, scale = 6)
    private BigDecimal inputU2;

    @Column(name = "service_life_hours", precision = 19, scale = 6)
    private BigDecimal serviceLifeHours;

    @Column(name = "allowable_contact_stress_mpa", precision = 19, scale = 6)
    private BigDecimal allowableContactStressMpa;

    @Column(name = "allowable_bending_stress_mpa", precision = 19, scale = 6)
    private BigDecimal allowableBendingStressMpa;

    @Column(name = "re_calculated", precision = 19, scale = 6)
    private BigDecimal reCalculated;

    @Column(name = "de1_calculated", precision = 19, scale = 6)
    private BigDecimal de1Calculated;

    @Column(name = "module_mte_selected", precision = 19, scale = 6)
    private BigDecimal moduleMteSelected;

    @Column(name = "teeth_z1")
    private Integer teethZ1;

    @Column(name = "teeth_z2")
    private Integer teethZ2;

    @Column(name = "actual_ratio_u2", precision = 19, scale = 6)
    private BigDecimal actualRatioU2;

    @Column(name = "width_b_mm", precision = 19, scale = 6)
    private BigDecimal widthBMm;

    @Column(name = "diameter_dm1_mm", precision = 19, scale = 6)
    private BigDecimal diameterDm1Mm;

    @Column(name = "diameter_dm2_mm", precision = 19, scale = 6)
    private BigDecimal diameterDm2Mm;

    @Column(name = "cone_angle_delta1_deg", precision = 19, scale = 6)
    private BigDecimal coneAngleDelta1Deg;

    @Column(name = "cone_angle_delta2_deg", precision = 19, scale = 6)
    private BigDecimal coneAngleDelta2Deg;

    @Column(name = "sigma_h_mpa", precision = 19, scale = 6)
    private BigDecimal sigmaHMpa;

    @Column(name = "sigma_f1_mpa", precision = 19, scale = 6)
    private BigDecimal sigmaF1Mpa;

    @Column(name = "sigma_f2_mpa", precision = 19, scale = 6)
    private BigDecimal sigmaF2Mpa;

    @Column(name = "contact_stress_pass", nullable = false)
    private boolean contactStressPass;

    @Column(name = "bending_stress_pass", nullable = false)
    private boolean bendingStressPass;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private GearMaterial material;

    @Builder.Default
    @OneToMany(mappedBy = "module3Result", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<Module3ShaftForce> shaftForces = new ArrayList<>();

    public void addShaftForce(Module3ShaftForce shaftForce) {
        shaftForces.add(shaftForce);
        shaftForce.setModule3Result(this);
    }

    public void removeShaftForce(Module3ShaftForce shaftForce) {
        shaftForces.remove(shaftForce);
        shaftForce.setModule3Result(null);
    }
}
