package com.drivetrain.domain.entity;

import com.drivetrain.domain.enums.ShaftCode;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "shaft_state",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_shaft_state_result_shaft",
                columnNames = {"module1_result_id", "shaft_code"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "module1Result")
public class ShaftState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "shaft_code", nullable = false, length = 20)
    private ShaftCode shaftCode;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Column(name = "power_kw", precision = 19, scale = 6)
    private BigDecimal powerKw;

    @Column(name = "rpm", precision = 19, scale = 6)
    private BigDecimal rpm;

    @Column(name = "torque_nmm", precision = 19, scale = 6)
    private BigDecimal torqueNmm;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module1_result_id", nullable = false)
    private Module1Result module1Result;
}
