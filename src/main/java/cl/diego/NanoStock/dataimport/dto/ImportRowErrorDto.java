package cl.diego.NanoStock.dataimport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportRowErrorDto {
    private int rowNumber;
    private String identifier;
    private String field;
    private String errorMessage;
    private String rejectedValue;
}
