package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.GetAllGastosUseCase;
import com.jcaa.usersmanagement.application.port.in.GetAllUsersUseCase;
import com.jcaa.usersmanagement.application.port.in.GetGastoReportsUseCase;
import com.jcaa.usersmanagement.application.service.dto.query.GastoFilterQuery;
import com.jcaa.usersmanagement.application.service.dto.response.GastoResponse;
import com.jcaa.usersmanagement.application.service.dto.response.GastoTotalsResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.GastoReportResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.IvaAuditRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.MonthlyEvolutionRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.OwnerConsolidationRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.PlaceRankingRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.TopGastoRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.UserRoleRowResponse;
import com.jcaa.usersmanagement.application.service.mapper.GastoApplicationMapper;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.model.UserModel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Lado Query de CQRS: calcula las siete proyecciones analiticas.
 *
 * <p>Todas derivan del mismo caso de uso de lectura, {@link GetAllGastosUseCase},
 * que a su vez se apoya en la cache. Una pantalla que pida los siete reportes
 * golpea la base una sola vez.
 *
 * <p>Cada metodo recibe su PROPIO filtro: afinar la auditoria de IVA no altera
 * el consolidado por usuario ni ningun otro reporte.
 */
@Service
@RequiredArgsConstructor
public class GastoReportService implements GetGastoReportsUseCase {

  private static final int SCALE = 2;
  private static final DateTimeFormatter PERIOD = DateTimeFormatter.ofPattern("yyyy-MM");
  private static final int DEFAULT_TOP = 5;

  private final GetAllGastosUseCase getAllGastosUseCase;
  private final GetAllUsersUseCase getAllUsersUseCase;
  private final GastoApplicationMapper mapper;

  // ------------------------------------------------------------------ 1
  @Override
  public GastoReportResponse<IvaAuditRowResponse> ivaAudit(final GastoFilterQuery query) {
    final List<GastoModel> gastos = read(query);
    final BigDecimal total = sumTotal(gastos);
    final Map<String, List<GastoModel>> grouped =
        gastos.stream()
            .collect(
                Collectors.groupingBy(
                    gasto -> gasto.effectiveRatePercentage().toPlainString(),
                    TreeMap::new,
                    Collectors.toList()));
    final List<IvaAuditRowResponse> filas = new ArrayList<>();
    grouped.forEach(
        (percentage, bucket) ->
            filas.add(
                new IvaAuditRowResponse(
                    describeRate(percentage),
                    new BigDecimal(percentage),
                    bucket.size(),
                    sumBase(bucket),
                    sumIva(bucket),
                    sumTotal(bucket),
                    share(sumTotal(bucket), total))));
    return wrap("auditoria-iva", query, gastos, filas);
  }

  // ------------------------------------------------------------------ 2
  @Override
  public GastoReportResponse<PlaceRankingRowResponse> placeRanking(final GastoFilterQuery query) {
    final List<GastoModel> gastos = read(query);
    final BigDecimal total = sumTotal(gastos);
    final List<PlaceRankingRowResponse> filas =
        gastos.stream()
            .collect(Collectors.groupingBy(gasto -> gasto.getLugar().value(), Collectors.toList()))
            .entrySet()
            .stream()
            .map(
                entry ->
                    new PlaceRankingRowResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        sumBase(entry.getValue()),
                        sumIva(entry.getValue()),
                        sumTotal(entry.getValue()),
                        share(sumTotal(entry.getValue()), total)))
            .sorted(Comparator.comparing(PlaceRankingRowResponse::totalConIva).reversed())
            .toList();
    return wrap("ranking-lugar", query, gastos, filas);
  }

  // ------------------------------------------------------------------ 3
  @Override
  public GastoReportResponse<OwnerConsolidationRowResponse> ownerConsolidation(
      final GastoFilterQuery query) {
    final List<GastoModel> gastos = read(query);
    final BigDecimal total = sumTotal(gastos);
    final List<OwnerConsolidationRowResponse> filas =
        gastos.stream()
            .collect(
                Collectors.groupingBy(
                    gasto -> gasto.getNombreUsuario().value(), Collectors.toList()))
            .entrySet()
            .stream()
            .map(
                entry ->
                    new OwnerConsolidationRowResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        sumBase(entry.getValue()),
                        sumIva(entry.getValue()),
                        sumTotal(entry.getValue()),
                        share(sumTotal(entry.getValue()), total)))
            .sorted(Comparator.comparing(OwnerConsolidationRowResponse::totalConIva).reversed())
            .toList();
    return wrap("consolidado-usuario", query, gastos, filas);
  }

  // ------------------------------------------------------------------ 4
  @Override
  public GastoReportResponse<MonthlyEvolutionRowResponse> monthlyEvolution(
      final GastoFilterQuery query) {
    final List<GastoModel> gastos = read(query);
    final Map<String, List<GastoModel>> grouped =
        gastos.stream()
            .collect(
                Collectors.groupingBy(
                    gasto -> gasto.getFecha().value().format(PERIOD),
                    TreeMap::new,
                    Collectors.toList()));
    final List<MonthlyEvolutionRowResponse> filas = new ArrayList<>();
    grouped.forEach(
        (periodo, bucket) ->
            filas.add(
                new MonthlyEvolutionRowResponse(
                    periodo,
                    bucket.size(),
                    sumBase(bucket),
                    sumIva(bucket),
                    sumTotal(bucket),
                    average(bucket))));
    return wrap("evolucion-mensual", query, gastos, filas);
  }

  // ------------------------------------------------------------------ 5
  @Override
  public GastoReportResponse<TopGastoRowResponse> topGastos(final GastoFilterQuery query) {
    final List<GastoModel> gastos = read(query);
    final BigDecimal total = sumTotal(gastos);
    final int limite = query == null || query.limite() == null ? DEFAULT_TOP : query.limite();
    final List<GastoModel> ordenados =
        gastos.stream()
            .sorted(
                Comparator.comparing(
                        (GastoModel gasto) -> gasto.getValorTotalConIva().value())
                    .reversed())
            .limit(Math.max(1, limite))
            .toList();
    final List<TopGastoRowResponse> filas = new ArrayList<>();
    for (int i = 0; i < ordenados.size(); i++) {
      final GastoModel gasto = ordenados.get(i);
      filas.add(
          new TopGastoRowResponse(
              i + 1,
              gasto.getId().value(),
              gasto.getFecha().toString(),
              gasto.getLugar().value(),
              gasto.getNombreUsuario().value(),
              gasto.getValorTotalConIva().value(),
              share(gasto.getValorTotalConIva().value(), total)));
    }
    return wrap("top-gastos", query, gastos, filas);
  }

  // ------------------------------------------------------------------ 6
  @Override
  public GastoReportResponse<GastoResponse> transactionDetail(final GastoFilterQuery query) {
    final List<GastoModel> gastos = read(query);
    return wrap("detalle-transaccional", query, gastos, gastos.stream().map(mapper::toResponse).toList());
  }

  // ------------------------------------------------------------------ 7
  @Override
  public GastoReportResponse<UserRoleRowResponse> userDirectory(final GastoFilterQuery query) {
    final List<UserModel> usuarios = getAllUsersUseCase.execute();
    final Map<String, List<String>> porRol = new LinkedHashMap<>();
    usuarios.forEach(
        usuario ->
            porRol
                .computeIfAbsent(usuario.getRole().name(), ignored -> new ArrayList<>())
                .add(usuario.getName().value()));
    final List<UserRoleRowResponse> filas =
        porRol.entrySet().stream()
            .map(entry -> new UserRoleRowResponse(entry.getKey(), entry.getValue().size(), entry.getValue()))
            .sorted(Comparator.comparing(UserRoleRowResponse::rol))
            .toList();
    return new GastoReportResponse<>(
        "directorio-usuarios", describe(query), GastoTotalsResponse.zero(), filas);
  }

  // ---------------------------------------------------------------- apoyo

  private List<GastoModel> read(final GastoFilterQuery query) {
    return getAllGastosUseCase.execute(query == null ? GastoFilterQuery.empty() : query);
  }

  private <T> GastoReportResponse<T> wrap(
      final String nombre,
      final GastoFilterQuery query,
      final List<GastoModel> gastos,
      final List<T> filas) {
    return new GastoReportResponse<>(nombre, describe(query), totals(gastos), filas);
  }

  private GastoTotalsResponse totals(final List<GastoModel> gastos) {
    if (gastos.isEmpty()) {
      return GastoTotalsResponse.zero();
    }
    return new GastoTotalsResponse(
        gastos.size(), sumBase(gastos), sumIva(gastos), sumTotal(gastos), average(gastos));
  }

  private static String describe(final GastoFilterQuery query) {
    return query == null || !query.hasAnyCriteria() ? "sin filtros" : query.cacheKey();
  }

  private static String describeRate(final String percentage) {
    return switch (percentage) {
      case "19.00" -> "GENERAL";
      case "5.00" -> "REDUCIDA";
      case "0.00" -> "EXENTA";
      default -> "OTRA";
    };
  }

  private static BigDecimal sumBase(final List<GastoModel> gastos) {
    return gastos.stream()
        .map(gasto -> gasto.getValorTotalSinIva().value())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private static BigDecimal sumIva(final List<GastoModel> gastos) {
    return gastos.stream()
        .map(gasto -> gasto.getIvaTotal().value())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private static BigDecimal sumTotal(final List<GastoModel> gastos) {
    return gastos.stream()
        .map(gasto -> gasto.getValorTotalConIva().value())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private static BigDecimal average(final List<GastoModel> gastos) {
    if (gastos.isEmpty()) {
      return BigDecimal.ZERO;
    }
    return sumTotal(gastos).divide(BigDecimal.valueOf(gastos.size()), SCALE, RoundingMode.HALF_UP);
  }

  private static BigDecimal share(final BigDecimal part, final BigDecimal total) {
    if (total == null || total.signum() == 0) {
      return BigDecimal.ZERO;
    }
    return part.multiply(BigDecimal.valueOf(100)).divide(total, SCALE, RoundingMode.HALF_UP);
  }
}
