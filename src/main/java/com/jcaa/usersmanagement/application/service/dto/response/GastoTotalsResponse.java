package com.jcaa.usersmanagement.application.service.dto.response;

import java.math.BigDecimal;

/** Totales agregados de un conjunto de gastos, para las proyecciones analiticas. */
public record GastoTotalsResponse(
    int cantidad,
    BigDecimal totalSinIVA,
    BigDecimal totalIVA,
    BigDecimal totalConIVA,
    BigDecimal promedioConIVA) {

  public static GastoTotalsResponse zero() {
    return new GastoTotalsResponse(
        0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
  }
}
