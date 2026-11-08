package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifica el blindaje del exportador CSV.
 *
 * <p>Las cargas usadas son ataques reales de CSV Injection: una celda que
 * empieza por '=' se ejecuta al abrir el archivo en Excel o LibreOffice.
 */
class CsvWriterTest {

  private static final String BOM = "﻿";
  private static final String CRLF = "\r\n";

  private final CsvWriter writer = new CsvWriter();

  @Test
  @DisplayName("Neutraliza formulas de hoja de calculo anteponiendo un apostrofo")
  void neutralizaFormulas() {
    assertEquals("'=cmd|'/c calc'!A1", writer.sanitizeCell("=cmd|'/c calc'!A1"));
    assertEquals("'+SUM(A1:A9)", writer.sanitizeCell("+SUM(A1:A9)"));
    assertEquals("'-2+3", writer.sanitizeCell("-2+3"));
    assertEquals("'@SUM(1)", writer.sanitizeCell("@SUM(1)"));
  }

  @Test
  @DisplayName("Un valor legitimo no se altera")
  void respetaValoresNormales() {
    assertEquals("Ferreteria Norte", writer.sanitizeCell("Ferreteria Norte"));
    assertEquals("119000.00", writer.sanitizeCell("119000.00"));
    assertEquals("", writer.sanitizeCell(null));
  }

  @Test
  @DisplayName("Escapa el separador y las comillas para no romper las columnas")
  void escapaSeparadorYComillas() {
    assertEquals("\"Bogota; Colombia\"", writer.sanitizeCell("Bogota; Colombia"));
    assertEquals("\"Dijo \"\"hola\"\"\"", writer.sanitizeCell("Dijo \"hola\""));
  }

  @Test
  @DisplayName("Emite BOM UTF-8 y terminacion CRLF para que Excel respete las tildes")
  void emiteBomUtf8() {
    final byte[] salida =
        writer.write(
            List.of("Lugar", "Total"), List.of("x"), fila -> List.of("Cafeteria", "100"));
    final String texto = new String(salida, StandardCharsets.UTF_8);

    assertTrue(texto.startsWith(BOM), "debe empezar por el BOM UTF-8");
    assertTrue(texto.contains("Lugar;Total"), "debe escribir la cabecera");
    assertTrue(texto.contains("Cafeteria;100"), "debe escribir la fila");
    assertTrue(texto.endsWith(CRLF), "debe terminar con CRLF");
  }

  @Test
  @DisplayName("Una celda con formula se neutraliza tambien dentro del archivo completo")
  void blindaElArchivoCompleto() {
    final byte[] salida =
        writer.write(
            List.of("Lugar"), List.of("x"), fila -> List.of("=HYPERLINK(\"http://malo\")"));
    final String texto = new String(salida, StandardCharsets.UTF_8);

    assertTrue(texto.contains("'=HYPERLINK"), "la formula debe quedar neutralizada");
  }
}
