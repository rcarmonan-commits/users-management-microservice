package com.jcaa.usersmanagement.application.service.dto.response.report;

import java.math.BigDecimal;

/**
 * Fila de la proyeccion 1: auditoria fiscal agrupada por tarifa de IVA.
 *
 * <p>Permite contrastar cuanto IVA se declaro en cada tarifa legal, que es la
 * lectura que pide cualquier revision contable.
 */
public record IvaAuditRowResponse(
    String tarifa,
    BigDecimal porcentaje,
    int cantidad,
    BigDecimal baseGravable,
    BigDecimal ivaLiquidado,
    BigDecimal totalConIva,
    BigDecimal participacion) {}
