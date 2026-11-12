package cl.diego.NanoStock.dataimport.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDto {
    private String entityType;
    private int totalRows;
    private int successCount;
    private int skippedCount;
    private int errorCount;
    private boolean dryRun;
    @Builder.Default
    private List<ImportRowErrorDto> errors = new ArrayList<>();
    @Builder.Default
    private List<Long> affectedIds = new ArrayList<>();
    private String summaryMessage;

    public boolean isSuccessful() {
        return errorCount == 0;
    }
}
