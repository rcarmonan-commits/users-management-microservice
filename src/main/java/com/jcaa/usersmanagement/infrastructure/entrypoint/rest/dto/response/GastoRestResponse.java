package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/** Representacion publica de un gasto. */
@Schema(description = "Gasto registrado")
public record GastoRestResponse(
    @Schema(example = "7") int id,
    @Schema(example = "2026-05-10") String fecha,
    @Schema(example = "100000.00") BigDecimal valorTotalSinIVA,
    @Schema(example = "19000.00") BigDecimal ivaTotal,
    @Schema(example = "119000.00") BigDecimal valorTotalConIVA,
    @Schema(example = "admin") String nombreUsuario,
    @Schema(example = "Ferreteria Norte") String lugar,
    @Schema(example = "Compra de herramientas") String descripcion,
    @Schema(example = "19.00", description = "Tarifa efectiva deducida de los importes")
        BigDecimal tarifaEfectiva) {}
