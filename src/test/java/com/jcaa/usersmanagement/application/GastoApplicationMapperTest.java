package com.jcaa.usersmanagement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.usersmanagement.application.service.dto.command.CreateGastoCommand;
import com.jcaa.usersmanagement.application.service.mapper.GastoApplicationMapper;
import com.jcaa.usersmanagement.domain.exception.InvalidGastoIvaConsistencyException;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GastoApplicationMapperTest {

  private final GastoApplicationMapper mapper = new GastoApplicationMapper();

  @Test
  @DisplayName("Con tarifa legal el dominio liquida el IVA y los totales cuadran solos")
  void liquidaDesdeTarifa() {
    final CreateGastoCommand comando =
        new CreateGastoCommand("2026-04-10", new BigDecimal("200000"), null, null,
            "admin", "Ferreteria Norte", "Herramientas", "GENERAL");

    final GastoModel gasto = mapper.toDomain(comando);

    assertEquals("200000.00", gasto.getValorTotalSinIva().toString());
    assertEquals("38000.00", gasto.getIvaTotal().toString());
    assertEquals("238000.00", gasto.getValorTotalConIva().toString());
  }

  @Test
  @DisplayName("Tarifa reducida del 5% liquidada por el dominio")
  void liquidaTarifaReducida() {
    final CreateGastoCommand comando =
        new CreateGastoCommand("2026-04-10", new BigDecimal("100000"), null, null,
            "admin", "Supermercado", null, "REDUCIDA");

    assertEquals("5000.00", mapper.toDomain(comando).getIvaTotal().toString());
  }

  @Test
  @DisplayName("Totales declarados incoherentes son rechazados por la invariante fiscal")
  void rechazaTotalesIncoherentes() {
    final CreateGastoCommand comando =
        new CreateGastoCommand("2026-04-10", new BigDecimal("100000"),
            new BigDecimal("19000"), new BigDecimal("500000"),
            "admin", "Tienda", null, null);

    assertThrows(InvalidGastoIvaConsistencyException.class, () -> mapper.toDomain(comando));
  }

  @Test
  @DisplayName("Una diferencia de medio centavo se tolera por redondeo")
  void toleraMedioCentavo() {
    final CreateGastoCommand comando =
        new CreateGastoCommand("2026-04-10", new BigDecimal("100.00"),
            new BigDecimal("19.00"), new BigDecimal("119.004"),
            "admin", "Tienda", null, null);

    assertEquals("119.00", mapper.toDomain(comando).getValorTotalConIva().toString());
  }
}
