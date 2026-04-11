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
@Table(name = "module1_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"designCase", "selectedMotor", "shaftStates"})
public class Module1Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_efficiency", precision = 19, scale = 6)
    private BigDecimal totalEfficiency;

    @Column(name = "required_motor_power_kw", precision = 19, scale = 6)
    private BigDecimal requiredMotorPowerKw;

    @Column(name = "preliminary_motor_rpm_nsb", precision = 19, scale = 6)
    private BigDecimal preliminaryMotorRpmNsb;

    @Column(name = "total_transmission_ratio_u", precision = 19, scale = 6)
    private BigDecimal totalTransmissionRatioU;

    @Column(name = "belt_ratio_u1", precision = 19, scale = 6)
    private BigDecimal beltRatioU1;

    @Column(name = "gearbox_transmission_ratio_uh", precision = 19, scale = 6)
    private BigDecimal gearboxTransmissionRatioUh;

    @Column(name = "bevel_gear_ratio_u2", precision = 19, scale = 6)
    private BigDecimal bevelGearRatioU2;

    @Column(name = "spur_gear_ratio_u3", precision = 19, scale = 6)
    private BigDecimal spurGearRatioU3;

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
    @JoinColumn(name = "selected_motor_id", nullable = false)
    private Motor selectedMotor;

    @Builder.Default
    @OneToMany(mappedBy = "module1Result", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ShaftState> shaftStates = new ArrayList<>();

    public void addShaftState(ShaftState shaftState) {
        shaftStates.add(shaftState);
        shaftState.setModule1Result(this);
    }

    public void removeShaftState(ShaftState shaftState) {
        shaftStates.remove(shaftState);
        shaftState.setModule1Result(null);
    }
}
