package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Cuerpo de la peticion para registrar un gasto. */
@Schema(description = "Datos para registrar un gasto")
public record CreateGastoRestRequest(
    @Schema(example = "2026-05-10", description = "Fecha del gasto en formato ISO")
        @NotBlank
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "fecha must follow yyyy-MM-dd")
        String fecha,
    @Schema(example = "100000.00", description = "Base gravable, sin IVA")
        @NotNull
        @DecimalMin(value = "0.0")
        BigDecimal valorTotalSinIVA,
    @Schema(example = "19000.00", description = "IVA liquidado. Opcional si se envia tarifaIva")
        @DecimalMin(value = "0.0")
        BigDecimal ivaTotal,
    @Schema(example = "119000.00", description = "Total. Opcional si se envia tarifaIva")
        @DecimalMin(value = "0.0")
        BigDecimal valorTotalConIVA,
    @Schema(example = "admin", description = "Identificador del usuario responsable")
        @NotBlank
        @Size(max = 50)
        String nombreUsuario,
    @Schema(example = "Ferreteria Norte") @NotBlank @Size(min = 3, max = 150) String lugar,
    @Schema(example = "Compra de herramientas") @Size(max = 2000) String descripcion,
    @Schema(
            example = "GENERAL",
            allowableValues = {"GENERAL", "REDUCIDA", "EXENTA"},
            description = "Si se informa, el dominio liquida el IVA y los totales cuadran solos")
        String tarifaIva) {}
