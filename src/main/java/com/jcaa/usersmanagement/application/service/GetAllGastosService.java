package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.GetAllGastosUseCase;
import com.jcaa.usersmanagement.application.port.out.CachePort;
import com.jcaa.usersmanagement.application.port.out.GetAllGastosPort;
import com.jcaa.usersmanagement.application.service.dto.query.GastoFilterQuery;
import com.jcaa.usersmanagement.domain.enums.GastoIvaRate;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Lado Query de CQRS: lee todos los gastos y aplica los filtros en memoria.
 *
 * <p>El filtrado se resuelve aqui y no en SQL de forma deliberada: todas las
 * proyecciones analiticas parten del MISMO conjunto cacheado, de modo que una
 * pantalla con siete reportes golpea la base una sola vez en lugar de siete.
 * El volumen de un ejercicio academico lo permite con holgura.
 */
@Service
@RequiredArgsConstructor
public class GetAllGastosService implements GetAllGastosUseCase {

  private static final String CACHE_KEY_ALL = "gastos.all";
  private static final Duration CACHE_TTL = Duration.ofSeconds(60);

  private final GetAllGastosPort getAllGastosPort;
  private final CachePort cachePort;

  @Override
  public List<GastoModel> execute(final GastoFilterQuery query) {
    final GastoFilterQuery criteria = query == null ? GastoFilterQuery.empty() : query;
    final List<GastoModel> all = readAllFromCache();
    return all.stream()
        .filter(gasto -> matchesDateRange(gasto, criteria))
        .filter(gasto -> matchesRate(gasto, criteria))
        .filter(gasto -> matchesPlace(gasto, criteria))
        .filter(gasto -> matchesOwner(gasto, criteria))
        .filter(gasto -> matchesAmountRange(gasto, criteria))
        .limit(criteria.limite() == null ? Long.MAX_VALUE : Math.max(0, criteria.limite()))
        .toList();
  }

  @SuppressWarnings("unchecked")
  private List<GastoModel> readAllFromCache() {
    return cachePort.getOrCompute(
        CACHE_KEY_ALL, List.class, CACHE_TTL, getAllGastosPort::findAll);
  }

  private boolean matchesDateRange(final GastoModel gasto, final GastoFilterQuery criteria) {
    final var fecha = gasto.getFecha().value();
    if (criteria.desde() != null && fecha.isBefore(criteria.desde())) {
      return false;
    }
    return criteria.hasta() == null || !fecha.isAfter(criteria.hasta());
  }

  private boolean matchesRate(final GastoModel gasto, final GastoFilterQuery criteria) {
    if (criteria.tarifaIva() == null || criteria.tarifaIva().isBlank()) {
      return true;
    }
    final BigDecimal effective = gasto.effectiveRatePercentage();
    try {
      return GastoIvaRate.fromString(criteria.tarifaIva()).percentage().compareTo(effective) == 0;
    } catch (final RuntimeException ignored) {
      return new BigDecimal(criteria.tarifaIva()).compareTo(effective) == 0;
    }
  }

  private boolean matchesPlace(final GastoModel gasto, final GastoFilterQuery criteria) {
    return criteria.lugar() == null
        || criteria.lugar().isBlank()
        || gasto
            .getLugar()
            .value()
            .toLowerCase(Locale.ROOT)
            .contains(criteria.lugar().toLowerCase(Locale.ROOT));
  }

  private boolean matchesOwner(final GastoModel gasto, final GastoFilterQuery criteria) {
    return criteria.nombreUsuario() == null
        || criteria.nombreUsuario().isBlank()
        || gasto
            .getNombreUsuario()
            .value()
            .toLowerCase(Locale.ROOT)
            .contains(criteria.nombreUsuario().toLowerCase(Locale.ROOT));
  }

  private boolean matchesAmountRange(final GastoModel gasto, final GastoFilterQuery criteria) {
    final BigDecimal total = gasto.getValorTotalConIva().value();
    if (criteria.montoMinimo() != null && total.compareTo(criteria.montoMinimo()) < 0) {
      return false;
    }
    return criteria.montoMaximo() == null || total.compareTo(criteria.montoMaximo()) <= 0;
  }
}
