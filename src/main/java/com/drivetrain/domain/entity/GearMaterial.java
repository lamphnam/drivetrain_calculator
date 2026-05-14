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
@Table(name = "gear_material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "module3Results")
public class GearMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "material_code", unique = true, nullable = false, length = 100)
    private String materialCode;

    @Column(name = "material_name", nullable = false, length = 255)
    private String materialName;

    @Column(name = "heat_treatment", length = 255)
    private String heatTreatment;

    @Column(name = "hb_min", precision = 19, scale = 6)
    private BigDecimal hbMin;

    @Column(name = "hb_max", precision = 19, scale = 6)
    private BigDecimal hbMax;

    @Column(name = "sigma_b_mpa", precision = 19, scale = 6)
    private BigDecimal sigmaBMpa;

    @Column(name = "sigma_ch_mpa", precision = 19, scale = 6)
    private BigDecimal sigmaChMpa;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "material", fetch = FetchType.LAZY)
    private List<Module3Result> module3Results = new ArrayList<>();
}
