package com.jcaa.usersmanagement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcaa.usersmanagement.application.service.CreateGastoService;
import com.jcaa.usersmanagement.application.service.DeleteGastoService;
import com.jcaa.usersmanagement.application.service.GetAllGastosService;
import com.jcaa.usersmanagement.application.service.dto.command.CreateGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.command.DeleteGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.query.GastoFilterQuery;
import com.jcaa.usersmanagement.application.service.mapper.GastoApplicationMapper;
import com.jcaa.usersmanagement.domain.exception.GastoNotFoundException;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.infrastructure.adapter.cache.InMemoryCacheAdapter;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.repository.GastoRepositoryInMemory;
import com.jcaa.usersmanagement.infrastructure.adapter.queue.InMemoryDomainEventPublisher;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GastoServiciosTest {

  private GastoRepositoryInMemory repositorio;
  private InMemoryDomainEventPublisher cola;
  private InMemoryCacheAdapter cache;
  private CreateGastoService crear;
  private DeleteGastoService eliminar;
  private GetAllGastosService listar;

  @BeforeEach
  void preparar() {
    repositorio = new GastoRepositoryInMemory();
    cola = new InMemoryDomainEventPublisher();
    cache = new InMemoryCacheAdapter();
    final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    final GastoApplicationMapper mapper = new GastoApplicationMapper();
    crear = new CreateGastoService(repositorio, cola, cache, mapper, validator);
    eliminar = new DeleteGastoService(repositorio, cola, cache, validator);
    listar = new GetAllGastosService(repositorio, cache);
  }

  private CreateGastoCommand comando(String fecha, String base, String tarifa, String usuario, String lugar) {
    return new CreateGastoCommand(fecha, new BigDecimal(base), null, null, usuario, lugar, null, tarifa);
  }

  @Test
  @DisplayName("Alta: persiste, asigna identidad y publica el evento de dominio")
  void altaCompleta() {
    final GastoModel guardado = crear.execute(comando("2026-05-10", "100000", "GENERAL", "admin", "Ferreteria"));

    assertTrue(guardado.getId().isAssigned());
    assertEquals("119000.00", guardado.getValorTotalConIva().toString());
    assertEquals(1, cola.retainedEvents().size());
    assertEquals("gasto.created", cola.retainedEvents().get(0).getEventName());
  }

  @Test
  @DisplayName("Alta: un comando invalido se rechaza antes de tocar la persistencia")
  void rechazaComandoInvalido() {
    final CreateGastoCommand malo =
        new CreateGastoCommand("10/05/2026", new BigDecimal("100"), null, null, "admin", "ab", null, "GENERAL");

    assertThrows(ConstraintViolationException.class, () -> crear.execute(malo));
    assertTrue(repositorio.findAll().isEmpty());
  }

  @Test
  @DisplayName("Baja: eliminar un gasto inexistente lanza GastoNotFound")
  void bajaInexistente() {
    assertThrows(GastoNotFoundException.class, () -> eliminar.execute(new DeleteGastoCommand(999)));
  }

  @Test
  @DisplayName("Filtros independientes: cada criterio acota el conjunto sin afectar a los demas")
  void filtrosIndependientes() {
    crear.execute(comando("2026-01-15", "100000", "GENERAL", "admin", "Ferreteria Norte"));
    crear.execute(comando("2026-06-20", "200000", "REDUCIDA", "Rosa car", "Supermercado"));
    crear.execute(comando("2026-09-05", "300000", "EXENTA", "admin", "Libreria"));

    assertEquals(3, listar.execute(GastoFilterQuery.empty()).size());

    final List<GastoModel> porUsuario =
        listar.execute(new GastoFilterQuery(null, null, null, null, "admin", null, null, null));
    assertEquals(2, porUsuario.size());

    final List<GastoModel> porFecha =
        listar.execute(new GastoFilterQuery(LocalDate.parse("2026-06-01"), null, null, null, null, null, null, null));
    assertEquals(2, porFecha.size());

    final List<GastoModel> porLugar =
        listar.execute(new GastoFilterQuery(null, null, null, "super", null, null, null, null));
    assertEquals(1, porLugar.size());

    final List<GastoModel> porMonto =
        listar.execute(new GastoFilterQuery(null, null, null, null, null, new BigDecimal("250000"), null, null));
    assertEquals(1, porMonto.size());
  }

  @Test
  @DisplayName("La cache se invalida al escribir, de modo que el listado nunca queda obsoleto")
  void cacheSeInvalidaAlEscribir() {
    crear.execute(comando("2026-02-01", "50000", "GENERAL", "admin", "Cafeteria"));
    assertEquals(1, listar.execute(GastoFilterQuery.empty()).size());

    crear.execute(comando("2026-02-02", "60000", "GENERAL", "admin", "Papeleria"));
    assertEquals(2, listar.execute(GastoFilterQuery.empty()).size());
  }
}
