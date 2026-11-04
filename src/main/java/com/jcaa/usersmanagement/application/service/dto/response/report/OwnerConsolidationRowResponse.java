package com.jcaa.usersmanagement.application.service.dto.response.report;

import java.math.BigDecimal;

/**
 * Fila de la proyeccion 3: consolidado por usuario responsable.
 *
 * <p>El campo `usuario` contiene el IDENTIFICADOR de la cuenta, que es lo que
 * almacena `gastos.nombre_usuario`, y no el nombre de presentacion. Mezclar
 * ambos dominios de datos produce consultas que nunca devuelven resultados.
 */
public record OwnerConsolidationRowResponse(
    String usuario,
    int cantidad,
    BigDecimal baseGravable,
    BigDecimal ivaTotal,
    BigDecimal totalConIva,
    BigDecimal participacion) {}
