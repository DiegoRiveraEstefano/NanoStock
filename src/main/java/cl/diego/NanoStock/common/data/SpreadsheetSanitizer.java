package cl.diego.NanoStock.common.data;

/**
 * Utilidad de seguridad para mitigar ataques de CSV / Spreadsheet Formula Injection.
 * Previene la ejecución arbitraria de fórmulas en software como Excel o LibreOffice Calc
 * cuando los valores exportados provienen de entradas de usuario.
 */
public final class SpreadsheetSanitizer {

    private static final char[] DANGEROUS_PREFIXES = {'=', '+', '-', '@', '\t', '\r'};

    private SpreadsheetSanitizer() {
    }

    public static String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        char firstChar = input.charAt(0);
        for (char dangerous : DANGEROUS_PREFIXES) {
            if (firstChar == dangerous) {
                return "'" + input;
            }
        }
        return input;
    }
}
