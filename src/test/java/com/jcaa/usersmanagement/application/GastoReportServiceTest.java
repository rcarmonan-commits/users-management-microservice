package com.jcaa.usersmanagement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcaa.usersmanagement.application.port.in.GetAllGastosUseCase;
import com.jcaa.usersmanagement.application.port.in.GetAllUsersUseCase;
import com.jcaa.usersmanagement.application.service.GastoReportService;
import com.jcaa.usersmanagement.application.service.dto.query.GastoFilterQuery;
import com.jcaa.usersmanagement.application.service.mapper.GastoApplicationMapper;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.GastoAmount;
import com.jcaa.usersmanagement.domain.valueobject.GastoDate;
import com.jcaa.usersmanagement.domain.valueobject.GastoDescription;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.valueobject.GastoOwner;
import com.jcaa.usersmanagement.domain.valueobject.GastoPlace;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GastoReportServiceTest {

  private GastoReportService reportes;

  private static GastoModel gasto(int id, String fecha, String base, String iva, String total,
      String usuario, String lugar) {
    return GastoModel.reconstitute(
        GastoId.of(id), GastoDate.fromString(fecha),
        GastoAmount.of(new BigDecimal(base)), GastoAmount.of(new BigDecimal(iva)),
        GastoAmount.of(new BigDecimal(total)), GastoOwner.of(usuario),
        GastoPlace.of(lugar), GastoDescription.empty());
  }

  @BeforeEach
  void preparar() {
    final GetAllGastosUseCase lectura = Mockito.mock(GetAllGastosUseCase.class);
    final GetAllUsersUseCase usuarios = Mockito.mock(GetAllUsersUseCase.class);
    Mockito.when(lectura.execute(Mockito.any()))
        .thenReturn(List.of(
            gasto(1, "2026-01-10", "100000", "19000", "119000", "admin", "Ferreteria"),
            gasto(2, "2026-01-20", "200000", "38000", "238000", "admin", "Ferreteria"),
            gasto(3, "2026-02-05", "100000", "5000", "105000", "Rosa car", "Supermercado"),
            gasto(4, "2026-03-01", "50000", "0", "50000", "Rosa car", "Libreria")));
    Mockito.when(usuarios.execute()).thenReturn(List.of());
    reportes = new GastoReportService(lectura, usuarios, new GastoApplicationMapper());
  }

  @Test
  @DisplayName("Auditoria de IVA: agrupa por tarifa efectiva y suma correctamente")
  void auditoriaIva() {
    final var r = reportes.ivaAudit(GastoFilterQuery.empty());

    assertEquals(3, r.filas().size());
    assertEquals(4, r.totales().cantidad());
    assertEquals("512000.00", r.totales().totalConIVA().toPlainString());

    final var general = r.filas().stream().filter(f -> f.tarifa().equals("GENERAL")).findFirst().orElseThrow();
    assertEquals(2, general.cantidad());
    assertEquals("57000.00", general.ivaLiquidado().toPlainString());

    final var exenta = r.filas().stream().filter(f -> f.tarifa().equals("EXENTA")).findFirst().orElseThrow();
    assertEquals("0.00", exenta.ivaLiquidado().toPlainString());
  }

  @Test
  @DisplayName("Ranking por lugar: ordena de mayor a menor y reparte la participacion")
  void rankingLugar() {
    final var r = reportes.placeRanking(GastoFilterQuery.empty());

    assertEquals(3, r.filas().size());
    assertEquals("Ferreteria", r.filas().get(0).lugar());
    assertEquals(2, r.filas().get(0).cantidad());
    assertEquals("357000.00", r.filas().get(0).totalConIva().toPlainString());
    assertEquals("69.73", r.filas().get(0).participacion().toPlainString());
  }

  @Test
  @DisplayName("Consolidado por usuario: usa el identificador de cuenta, no el nombre")
  void consolidadoUsuario() {
    final var r = reportes.ownerConsolidation(GastoFilterQuery.empty());

    assertEquals(2, r.filas().size());
    assertEquals("admin", r.filas().get(0).usuario());
    assertEquals("357000.00", r.filas().get(0).totalConIva().toPlainString());
  }

  @Test
  @DisplayName("Evolucion mensual: periodos en orden cronologico con su promedio")
  void evolucionMensual() {
    final var r = reportes.monthlyEvolution(GastoFilterQuery.empty());

    assertEquals(3, r.filas().size());
    assertEquals("2026-01", r.filas().get(0).periodo());
    assertEquals("178500.00", r.filas().get(0).promedio().toPlainString());
    assertEquals("2026-03", r.filas().get(2).periodo());
  }

  @Test
  @DisplayName("Top gastos: respeta el limite y numera las posiciones")
  void topGastos() {
    final var r = reportes.topGastos(
        new GastoFilterQuery(null, null, null, null, null, null, null, 2));

    assertEquals(2, r.filas().size());
    assertEquals(1, r.filas().get(0).posicion());
    assertEquals("238000.00", r.filas().get(0).totalConIva().toPlainString());
    assertEquals(2, r.filas().get(1).posicion());
  }

  @Test
  @DisplayName("Cada reporte viaja con sus propios totales y el eco de sus filtros")
  void reportesAutonomos() {
    final var sinFiltro = reportes.ivaAudit(GastoFilterQuery.empty());
    assertEquals("sin filtros", sinFiltro.filtrosAplicados());

    final var conFiltro = reportes.placeRanking(
        new GastoFilterQuery(null, null, null, "ferre", null, null, null, null));
    assertTrue(conFiltro.filtrosAplicados().contains("ferre"));
    assertEquals("ranking-lugar", conFiltro.reporte());
  }
}
