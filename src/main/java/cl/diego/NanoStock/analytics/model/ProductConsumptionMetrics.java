package cl.diego.NanoStock.analytics.model;

import java.time.LocalDateTime;

import cl.diego.NanoStock.catalog.model.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nanostock_product_consumption_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductConsumptionMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "average_daily_consumption", nullable = false)
    @Builder.Default
    private Double averageDailyConsumption = 0.0;

    @Column(name = "estimated_days_of_autonomy", nullable = false)
    @Builder.Default
    private Double estimatedDaysOfAutonomy = 0.0;

    @Column(name = "last_recalculated_at", nullable = false)
    private LocalDateTime lastRecalculatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.lastRecalculatedAt == null) {
            this.lastRecalculatedAt = LocalDateTime.now();
        }
        if (this.averageDailyConsumption == null) {
            this.averageDailyConsumption = 0.0;
        }
        if (this.estimatedDaysOfAutonomy == null) {
            this.estimatedDaysOfAutonomy = 0.0;
        }
    }
}
