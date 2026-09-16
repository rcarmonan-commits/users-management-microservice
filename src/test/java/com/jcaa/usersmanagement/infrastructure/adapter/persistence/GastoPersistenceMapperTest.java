package com.jcaa.usersmanagement.infrastructure.adapter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.entity.GastoEntity;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.mapper.GastoPersistenceMapper;
import java.sql.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GastoPersistenceMapperTest {

  private final GastoPersistenceMapper mapper = new GastoPersistenceMapper();

  @Test
  @DisplayName("Convierte una fila real de la tabla gastos en el agregado de dominio")
  void convierteFilaADominio() {
    final GastoEntity fila =
        new GastoEntity(
            7, Date.valueOf("2026-03-15"), 100000.00, 19000.00, 119000.00,
            "admin", "Papeleria Central", "Resmas de papel");

    final GastoModel gasto = mapper.toDomain(fila);

    assertEquals(7, gasto.getId().value());
    assertEquals("2026-03-15", gasto.getFecha().toString());
    assertEquals("100000.00", gasto.getValorTotalSinIva().toString());
    assertEquals("19000.00", gasto.getIvaTotal().toString());
    assertEquals("119000.00", gasto.getValorTotalConIva().toString());
    assertEquals("admin", gasto.getNombreUsuario().value());
    assertEquals("19.00", gasto.effectiveRatePercentage().toPlainString());
  }

  @Test
  @DisplayName("El viaje de ida y vuelta dominio - persistencia no pierde informacion")
  void viajeDeIdaYVuelta() {
    final GastoEntity fila =
        new GastoEntity(
            11, Date.valueOf("2026-05-02"), 50000.00, 2500.00, 52500.00,
            "Rosa car", "Cafeteria", null);

    final GastoModel gasto = mapper.toDomain(fila);
    final var dto = mapper.toDto(gasto);

    assertEquals(11, dto.idGasto());
    assertEquals("52500.00", dto.valorTotalConIva().toPlainString());
    assertTrue(gasto.getDescripcion().isEmpty());
  }
}
