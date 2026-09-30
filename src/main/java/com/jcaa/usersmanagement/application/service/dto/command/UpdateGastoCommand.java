package com.jcaa.usersmanagement.application.service.dto.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Comando de escritura para modificar un gasto existente. */
public record UpdateGastoCommand(
    @Min(value = 1, message = "id must be a positive integer") int id,
    @NotBlank(message = "fecha must not be blank")
        @Pattern(
            regexp = "\\d{4}-\\d{2}-\\d{2}",
            message = "fecha must follow the ISO format yyyy-MM-dd")
        String fecha,
    @NotNull(message = "valorTotalSinIVA must not be null")
        @DecimalMin(value = "0.0", message = "valorTotalSinIVA must not be negative")
        BigDecimal valorTotalSinIVA,
    @DecimalMin(value = "0.0", message = "ivaTotal must not be negative") BigDecimal ivaTotal,
    @DecimalMin(value = "0.0", message = "valorTotalConIVA must not be negative")
        BigDecimal valorTotalConIVA,
    @NotBlank(message = "nombreUsuario must not be blank")
        @Size(max = 50, message = "nombreUsuario must not exceed 50 characters")
        String nombreUsuario,
    @NotBlank(message = "lugar must not be blank")
        @Size(min = 3, max = 150, message = "lugar must have between 3 and 150 characters")
        String lugar,
    @Size(max = 2000, message = "descripcion must not exceed 2000 characters") String descripcion,
    String tarifaIva) {

  public boolean liquidatesFromRate() {
    return tarifaIva != null && !tarifaIva.isBlank();
  }
}
