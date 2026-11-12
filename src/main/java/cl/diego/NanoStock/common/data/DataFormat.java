package cl.diego.NanoStock.common.data;

import java.util.Arrays;
import java.util.Optional;

public enum DataFormat {
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    JSON("application/json", "json"),
    CSV("text/csv", "csv");

    private final String mediaType;
    private final String fileExtension;

    DataFormat(String mediaType, String fileExtension) {
        this.mediaType = mediaType;
        this.fileExtension = fileExtension;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public static Optional<DataFormat> fromString(String formatStr) {
        if (formatStr == null || formatStr.isBlank()) {
            return Optional.empty();
        }
        String clean = formatStr.trim().toUpperCase();
        return Arrays.stream(values())
            .filter(f -> f.name().equalsIgnoreCase(clean) || f.fileExtension.equalsIgnoreCase(clean))
            .findFirst();
    }

    public static DataFormat fromFilename(String filename) {
        if (filename == null) {
            return CSV;
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
            return EXCEL;
        }
        if (lower.endsWith(".json")) {
            return JSON;
        }
        return CSV;
    }
}
