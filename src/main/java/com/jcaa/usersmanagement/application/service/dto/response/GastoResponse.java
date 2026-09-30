package com.jcaa.usersmanagement.application.service.dto.response;

import java.math.BigDecimal;

/**
 * Proyeccion de lectura de un gasto hacia el exterior.
 *
 * <p>Es un DTO plano a proposito: nunca se expone el agregado de dominio por
 * la API. Si se expusiera, cualquier cambio interno del modelo rompería el
 * contrato publico de los clientes.
 */
public record GastoResponse(
    int id,
    String fecha,
    BigDecimal valorTotalSinIVA,
    BigDecimal ivaTotal,
    BigDecimal valorTotalConIVA,
    String nombreUsuario,
    String lugar,
    String descripcion,
    BigDecimal tarifaEfectiva) {}
