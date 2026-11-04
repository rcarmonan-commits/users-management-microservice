package com.jcaa.usersmanagement.application.service.dto.response.report;

import java.math.BigDecimal;

/** Fila de la proyeccion 2: concentracion del gasto por establecimiento. */
public record PlaceRankingRowResponse(
    String lugar,
    int cantidad,
    BigDecimal baseGravable,
    BigDecimal ivaTotal,
    BigDecimal totalConIva,
    BigDecimal participacion) {}
