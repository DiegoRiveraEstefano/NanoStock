package cl.diego.NanoStock.dataexport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductExportFilter {
    private String search;
    private Long categoryId;
    private Long brandId;
    private Boolean isPerishable;
    private Boolean onlyLowStock;

    public boolean hasFilters() {
        return (search != null && !search.isBlank())
            || categoryId != null
            || brandId != null
            || isPerishable != null
            || (onlyLowStock != null && onlyLowStock);
    }
}
