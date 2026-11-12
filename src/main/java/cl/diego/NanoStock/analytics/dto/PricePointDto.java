package cl.diego.NanoStock.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricePointDto {

    private LocalDate date;
    private String storeName;
    private BigDecimal unitPrice;
}
