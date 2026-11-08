package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import com.jcaa.usersmanagement.application.port.in.GetGastoReportsUseCase;
import com.jcaa.usersmanagement.application.service.dto.query.GastoFilterQuery;
import com.jcaa.usersmanagement.application.service.dto.response.GastoResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.GastoReportResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.IvaAuditRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.MonthlyEvolutionRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.OwnerConsolidationRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.PlaceRankingRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.TopGastoRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.UserRoleRowResponse;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.csv.CsvWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entrypoint de las proyecciones analiticas.
 *
 * <p>Cada reporte tiene su propia ruta y su propio juego de parametros. Afinar
 * uno no altera a los demas, que es el equivalente REST de las barras de
 * filtro independientes del ejercicio gemelo.
 *
 * <p>Todas las rutas aceptan `?formato=csv` para descargar la proyeccion.
 */
@Tag(name = "Reportes", description = "Proyecciones analiticas de solo lectura (lado Query de CQRS)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class GastoReportRestController {

  private static final String CSV = "csv";

  private final GetGastoReportsUseCase reports;
  private final CsvWriter csvWriter;

  @Operation(summary = "Auditoria fiscal agrupada por tarifa de IVA")
  @GetMapping("/iva")
  public ResponseEntity<?> ivaAudit(
      @RequestParam(required = false) final String desde,
      @RequestParam(required = false) final String hasta,
      @RequestParam(required = false) final String tarifaIva,
      @RequestParam(required = false) final String formato) {
    final GastoReportResponse<IvaAuditRowResponse> report =
        reports.ivaAudit(filter(desde, hasta, tarifaIva, null, null, null, null, null));
    if (isCsv(formato)) {
      return csv(
          "auditoria_iva",
          List.of("Tarifa", "Porcentaje", "Cantidad", "Base", "IVA", "Total", "Participacion"),
          report.filas(),
          row ->
              List.of(
                  row.tarifa(),
                  row.porcentaje().toPlainString(),
                  String.valueOf(row.cantidad()),
                  row.baseGravable().toPlainString(),
                  row.ivaLiquidado().toPlainString(),
                  row.totalConIva().toPlainString(),
                  row.participacion().toPlainString()));
    }
    return ResponseEntity.ok(report);
  }

  @Operation(summary = "Ranking y concentracion por establecimiento")
  @GetMapping("/lugares")
  public ResponseEntity<?> placeRanking(
      @RequestParam(required = false) final String lugar,
      @RequestParam(required = false) final String desde,
      @RequestParam(required = false) final String hasta,
      @RequestParam(required = false) final String formato) {
    final GastoReportResponse<PlaceRankingRowResponse> report =
        reports.placeRanking(filter(desde, hasta, null, lugar, null, null, null, null));
    if (isCsv(formato)) {
      return csv(
          "ranking_lugares",
          List.of("Lugar", "Cantidad", "Base", "IVA", "Total", "Participacion"),
          report.filas(),
          row ->
              List.of(
                  row.lugar(),
                  String.valueOf(row.cantidad()),
                  row.baseGravable().toPlainString(),
                  row.ivaTotal().toPlainString(),
                  row.totalConIva().toPlainString(),
                  row.participacion().toPlainString()));
    }
    return ResponseEntity.ok(report);
  }

  @Operation(summary = "Consolidado por usuario responsable")
  @GetMapping("/usuarios")
  public ResponseEntity<?> ownerConsolidation(
      @RequestParam(required = false) final String nombreUsuario,
      @RequestParam(required = false) final BigDecimal montoMinimo,
      @RequestParam(required = false) final BigDecimal montoMaximo,
      @RequestParam(required = false) final String formato) {
    final GastoReportResponse<OwnerConsolidationRowResponse> report =
        reports.ownerConsolidation(
            filter(null, null, null, null, nombreUsuario, montoMinimo, montoMaximo, null));
    if (isCsv(formato)) {
      return csv(
          "consolidado_usuarios",
          List.of("Usuario", "Cantidad", "Base", "IVA", "Total", "Participacion"),
          report.filas(),
          row ->
              List.of(
                  row.usuario(),
                  String.valueOf(row.cantidad()),
                  row.baseGravable().toPlainString(),
                  row.ivaTotal().toPlainString(),
                  row.totalConIva().toPlainString(),
                  row.participacion().toPlainString()));
    }
    return ResponseEntity.ok(report);
  }

  @Operation(summary = "Evolucion temporal mensual")
  @GetMapping("/mensual")
  public ResponseEntity<?> monthlyEvolution(
      @RequestParam(required = false) final String desde,
      @RequestParam(required = false) final String hasta,
      @RequestParam(required = false) final String formato) {
    final GastoReportResponse<MonthlyEvolutionRowResponse> report =
        reports.monthlyEvolution(filter(desde, hasta, null, null, null, null, null, null));
    if (isCsv(formato)) {
      return csv(
          "evolucion_mensual",
          List.of("Periodo", "Cantidad", "Base", "IVA", "Total", "Promedio"),
          report.filas(),
          row ->
              List.of(
                  row.periodo(),
                  String.valueOf(row.cantidad()),
                  row.baseGravable().toPlainString(),
                  row.ivaTotal().toPlainString(),
                  row.totalConIva().toPlainString(),
                  row.promedio().toPlainString()));
    }
    return ResponseEntity.ok(report);
  }

  @Operation(summary = "Gastos de mayor impacto presupuestal")
  @GetMapping("/top")
  public ResponseEntity<GastoReportResponse<TopGastoRowResponse>> topGastos(
      @RequestParam(required = false) final Integer limite,
      @RequestParam(required = false) final String lugar) {
    return ResponseEntity.ok(
        reports.topGastos(filter(null, null, null, lugar, null, null, null, limite)));
  }

  @Operation(summary = "Detalle transaccional completo")
  @GetMapping("/detalle")
  public ResponseEntity<?> transactionDetail(
      @RequestParam(required = false) final String desde,
      @RequestParam(required = false) final String hasta,
      @RequestParam(required = false) final String nombreUsuario,
      @RequestParam(required = false) final String tarifaIva,
      @RequestParam(required = false) final BigDecimal montoMinimo,
      @RequestParam(required = false) final BigDecimal montoMaximo,
      @RequestParam(required = false) final String formato) {
    final GastoReportResponse<GastoResponse> report =
        reports.transactionDetail(
            filter(desde, hasta, tarifaIva, null, nombreUsuario, montoMinimo, montoMaximo, null));
    if (isCsv(formato)) {
      return csv(
          "detalle_transaccional",
          List.of("Id", "Fecha", "Base", "IVA", "Total", "Usuario", "Lugar", "Descripcion"),
          report.filas(),
          row ->
              List.of(
                  String.valueOf(row.id()),
                  row.fecha(),
                  row.valorTotalSinIVA().toPlainString(),
                  row.ivaTotal().toPlainString(),
                  row.valorTotalConIVA().toPlainString(),
                  row.nombreUsuario(),
                  row.lugar(),
                  row.descripcion() == null ? "" : row.descripcion()));
    }
    return ResponseEntity.ok(report);
  }

  @Operation(summary = "Directorio de usuarios agrupado por rol")
  @GetMapping("/roles")
  public ResponseEntity<GastoReportResponse<UserRoleRowResponse>> userDirectory() {
    return ResponseEntity.ok(reports.userDirectory(GastoFilterQuery.empty()));
  }

  // ---------------------------------------------------------------- apoyo

  private static boolean isCsv(final String formato) {
    return CSV.equalsIgnoreCase(formato);
  }

  private <T> ResponseEntity<byte[]> csv(
      final String nombre,
      final List<String> headers,
      final List<T> rows,
      final java.util.function.Function<T, List<String>> extractor) {
    final byte[] body = csvWriter.write(headers, rows, extractor);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + nombre + ".csv\"")
        .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
        .body(body);
  }

  private static GastoFilterQuery filter(
      final String desde,
      final String hasta,
      final String tarifaIva,
      final String lugar,
      final String nombreUsuario,
      final BigDecimal montoMinimo,
      final BigDecimal montoMaximo,
      final Integer limite) {
    return new GastoFilterQuery(
        parseDate(desde),
        parseDate(hasta),
        tarifaIva,
        lugar,
        nombreUsuario,
        montoMinimo,
        montoMaximo,
        limite);
  }

  private static LocalDate parseDate(final String rawValue) {
    return rawValue == null || rawValue.isBlank() ? null : LocalDate.parse(rawValue.trim());
  }
}
