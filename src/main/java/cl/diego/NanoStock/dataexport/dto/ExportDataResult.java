package cl.diego.NanoStock.dataexport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportDataResult {
    private byte[] data;
    private String filename;
    private String contentType;
    private int recordCount;
}
