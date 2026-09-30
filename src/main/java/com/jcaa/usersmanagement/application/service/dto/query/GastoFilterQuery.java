package com.jcaa.usersmanagement.application.service.dto.query;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Criterios de filtrado del lado de lectura.
 *
 * <p>Cada proyeccion analitica recibe su PROPIA instancia de este filtro. Esa
 * independencia es deliberada: una barra de filtros global obliga a limpiar
 * una consulta para poder hacer otra, y fue justamente el defecto que hubo que
 * corregir en el ejercicio gemelo (leccion LRN-002).
 *
 * <p>Todos los campos son opcionales; `null` significa "sin restriccion".
 */
public record GastoFilterQuery(
    LocalDate desde,
    LocalDate hasta,
    String tarifaIva,
    String lugar,
    String nombreUsuario,
    BigDecimal montoMinimo,
    BigDecimal montoMaximo,
    Integer limite) {

  public static GastoFilterQuery empty() {
    return new GastoFilterQuery(null, null, null, null, null, null, null, null);
  }

  public boolean hasAnyCriteria() {
    return desde != null
        || hasta != null
        || (tarifaIva != null && !tarifaIva.isBlank())
        || (lugar != null && !lugar.isBlank())
        || (nombreUsuario != null && !nombreUsuario.isBlank())
        || montoMinimo != null
        || montoMaximo != null;
  }

  /** Clave estable para cachear el resultado de esta consulta. */
  public String cacheKey() {
    return String.join(
        "|",
        String.valueOf(desde),
        String.valueOf(hasta),
        String.valueOf(tarifaIva),
        String.valueOf(lugar),
        String.valueOf(nombreUsuario),
        String.valueOf(montoMinimo),
        String.valueOf(montoMaximo),
        String.valueOf(limite));
  }
}
