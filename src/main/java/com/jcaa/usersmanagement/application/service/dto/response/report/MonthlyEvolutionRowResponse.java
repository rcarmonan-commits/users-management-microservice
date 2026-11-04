package com.jcaa.usersmanagement.application.service.dto.response.report;

import java.math.BigDecimal;

/** Fila de la proyeccion 4: evolucion temporal mensual, en orden cronologico. */
public record MonthlyEvolutionRowResponse(
    String periodo,
    int cantidad,
    BigDecimal baseGravable,
    BigDecimal ivaTotal,
    BigDecimal totalConIva,
    BigDecimal promedio) {}
