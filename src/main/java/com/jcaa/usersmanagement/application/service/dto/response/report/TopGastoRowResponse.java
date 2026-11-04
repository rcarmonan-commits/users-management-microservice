package com.jcaa.usersmanagement.application.service.dto.response.report;

import java.math.BigDecimal;

/** Fila de la proyeccion 5: gastos de mayor impacto presupuestal. */
public record TopGastoRowResponse(
    int posicion,
    int id,
    String fecha,
    String lugar,
    String usuario,
    BigDecimal totalConIva,
    BigDecimal participacion) {}
