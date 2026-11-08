package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.csv;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Component;

/**
 * Serializador CSV con blindaje contra inyeccion de formulas (CSV Injection).
 *
 * <p>Excel, LibreOffice y Google Sheets ejecutan el contenido de una celda que
 * empieza por `=`, `+`, `-`, `@`, tabulador o retorno de carro. Un gasto cuyo
 * lugar fuese `=cmd|'/c calc'!A1` se convertiria en codigo ejecutable en la
 * maquina de quien abra el archivo. Anteponer un apostrofo neutraliza la
 * interpretacion sin alterar el dato visible.
 *
 * <p>Se emite BOM UTF-8 para que Excel respete las tildes en lugar de mostrar
 * caracteres corruptos.
 */
@Component
public class CsvWriter {

  private static final String BOM = "\uFEFF";
  private static final String SEPARATOR = ";";
  private static final String LINE_BREAK = "\r\n";
  private static final String DANGEROUS_PREFIXES = "=+-@\t\r";

  public <T> byte[] write(
      final List<String> headers, final List<T> rows, final Function<T, List<String>> extractor) {
    final StringBuilder builder = new StringBuilder(BOM);
    builder.append(joinSanitized(headers)).append(LINE_BREAK);
    for (final T row : rows) {
      builder.append(joinSanitized(extractor.apply(row))).append(LINE_BREAK);
    }
    return builder.toString().getBytes(StandardCharsets.UTF_8);
  }

  private String joinSanitized(final List<String> values) {
    return values.stream().map(this::sanitizeCell).reduce((a, b) -> a + SEPARATOR + b).orElse("");
  }

  /** Neutraliza formulas y escapa comillas y separadores. */
  String sanitizeCell(final String rawValue) {
    final String value = rawValue == null ? "" : rawValue;
    final String guarded =
        !value.isEmpty() && DANGEROUS_PREFIXES.indexOf(value.charAt(0)) >= 0 ? "'" + value : value;
    if (guarded.contains(SEPARATOR) || guarded.contains("\"") || guarded.contains("\n")) {
      return "\"" + guarded.replace("\"", "\"\"") + "\"";
    }
    return guarded;
  }
}
