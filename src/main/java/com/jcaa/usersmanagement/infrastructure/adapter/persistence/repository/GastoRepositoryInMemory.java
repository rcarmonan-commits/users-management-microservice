package com.jcaa.usersmanagement.infrastructure.adapter.persistence.repository;

import com.jcaa.usersmanagement.application.port.out.DeleteGastoPort;
import com.jcaa.usersmanagement.application.port.out.GetAllGastosPort;
import com.jcaa.usersmanagement.application.port.out.GetGastoByIdPort;
import com.jcaa.usersmanagement.application.port.out.SaveGastoPort;
import com.jcaa.usersmanagement.application.port.out.UpdateGastoPort;
import com.jcaa.usersmanagement.domain.exception.GastoNotFoundException;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de Gasto en memoria.
 *
 * <p>Permite ejecutar la aplicacion y su suite de pruebas sin una base de datos
 * disponible, que es justo lo que necesita la integracion continua: el runner
 * de GitHub Actions no tiene acceso a Alwaysdata.
 *
 * <p>Se activa con el perfil `inmemory`; sin el, el bean efectivo es
 * {@link GastoRepositoryMySQL}. Asi nunca hay dos implementaciones del mismo
 * puerto compitiendo en el contexto de Spring.
 */
@Repository
@Profile("inmemory")
public class GastoRepositoryInMemory
    implements SaveGastoPort, UpdateGastoPort, GetGastoByIdPort, GetAllGastosPort, DeleteGastoPort {

  private final Map<Integer, GastoModel> store = new ConcurrentHashMap<>();
  private final AtomicInteger sequence = new AtomicInteger(0);

  @Override
  public GastoModel save(final GastoModel gasto) {
    final int assignedId = sequence.incrementAndGet();
    final GastoModel persisted = gasto.withId(GastoId.of(assignedId));
    store.put(assignedId, persisted);
    return persisted;
  }

  @Override
  public GastoModel update(final GastoModel gasto) {
    final int id = gasto.getId().value();
    if (!store.containsKey(id)) {
      throw GastoNotFoundException.becauseIdDoesNotExist(id);
    }
    store.put(id, gasto);
    return gasto;
  }

  @Override
  public Optional<GastoModel> findById(final GastoId id) {
    return Optional.ofNullable(store.get(id.value()));
  }

  @Override
  public List<GastoModel> findAll() {
    final List<GastoModel> gastos = new ArrayList<>(store.values());
    gastos.sort(
        Comparator.comparing((GastoModel gasto) -> gasto.getFecha().value())
            .thenComparing(gasto -> gasto.getId().value())
            .reversed());
    return gastos;
  }

  @Override
  public boolean deleteById(final GastoId id) {
    return store.remove(id.value()) != null;
  }
}
