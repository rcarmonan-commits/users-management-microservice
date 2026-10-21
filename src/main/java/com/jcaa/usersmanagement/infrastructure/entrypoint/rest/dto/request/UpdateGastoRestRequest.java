package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Cuerpo de la peticion para modificar un gasto. El identificador viaja en la ruta. */
@Schema(description = "Datos para modificar un gasto existente")
public record UpdateGastoRestRequest(
    @Schema(example = "2026-05-11")
        @NotBlank
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "fecha must follow yyyy-MM-dd")
        String fecha,
    @Schema(example = "150000.00") @NotNull @DecimalMin(value = "0.0") BigDecimal valorTotalSinIVA,
    @Schema(example = "28500.00") @DecimalMin(value = "0.0") BigDecimal ivaTotal,
    @Schema(example = "178500.00") @DecimalMin(value = "0.0") BigDecimal valorTotalConIVA,
    @Schema(example = "admin") @NotBlank @Size(max = 50) String nombreUsuario,
    @Schema(example = "Ferreteria Sur") @NotBlank @Size(min = 3, max = 150) String lugar,
    @Schema(example = "Ajuste de la compra") @Size(max = 2000) String descripcion,
    @Schema(example = "GENERAL", allowableValues = {"GENERAL", "REDUCIDA", "EXENTA"})
        String tarifaIva) {}
