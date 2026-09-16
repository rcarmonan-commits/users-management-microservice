package com.jcaa.usersmanagement.infrastructure.adapter.persistence.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO intermedio entre el dominio y la entidad de persistencia. Mantiene los
 * importes en {@link BigDecimal} mientras el dato esta bajo control de la
 * aplicacion, y solo degrada a `double` al tocar la columna.
 */
public record GastoPersistenceDto(
    int idGasto,
    LocalDate fecha,
    BigDecimal valorTotalSinIva,
    BigDecimal ivaTotal,
    BigDecimal valorTotalConIva,
    String nombreUsuario,
    String lugar,
    String descripcion) {}
