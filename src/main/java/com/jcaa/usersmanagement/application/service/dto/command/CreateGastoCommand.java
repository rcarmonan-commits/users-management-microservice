package com.jcaa.usersmanagement.application.service.dto.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Comando de escritura para registrar un gasto (lado Command de CQRS).
 *
 * <p>Las anotaciones de validacion atajan los datos malformados en la frontera
 * de la aplicacion; las reglas de negocio de verdad —la invariante fiscal—
 * viven en el dominio y no aqui. Validar el formato no es validar el negocio.
 *
 * <p>Si `tarifaIva` viene informada, el dominio liquida el IVA y los totales
 * cuadran por construccion. Si no, se respetan los totales declarados y se
 * verifica la invariante.
 */
public record CreateGastoCommand(
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

  /** Indica si el IVA debe liquidarse en el dominio a partir de una tarifa legal. */
  public boolean liquidatesFromRate() {
    return tarifaIva != null && !tarifaIva.isBlank();
  }
}
