package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import com.jcaa.usersmanagement.application.port.in.CreateGastoUseCase;
import com.jcaa.usersmanagement.application.port.in.DeleteGastoUseCase;
import com.jcaa.usersmanagement.application.port.in.GetAllGastosUseCase;
import com.jcaa.usersmanagement.application.port.in.GetGastoByIdUseCase;
import com.jcaa.usersmanagement.application.port.in.UpdateGastoUseCase;
import com.jcaa.usersmanagement.application.service.dto.command.DeleteGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.query.GastoFilterQuery;
import com.jcaa.usersmanagement.application.service.dto.query.GetGastoByIdQuery;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.CreateGastoRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.UpdateGastoRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.GastoRestResponse;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.mapper.GastoRestMapper;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entrypoint primario del agregado Gasto.
 *
 * <p>Su unica responsabilidad es transportar: traduce HTTP a comandos y
 * consultas, delega en los puertos de entrada y devuelve DTO. No decide nada
 * del negocio, y por eso no depende de ninguna clase de infraestructura.
 */
@RestController
@RequestMapping("/api/gastos")
@RequiredArgsConstructor
public class GastoRestController implements GastoRestControllerDocs {

  private final CreateGastoUseCase createGastoUseCase;
  private final UpdateGastoUseCase updateGastoUseCase;
  private final DeleteGastoUseCase deleteGastoUseCase;
  private final GetGastoByIdUseCase getGastoByIdUseCase;
  private final GetAllGastosUseCase getAllGastosUseCase;
  private final GastoRestMapper mapper;

  @Override
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public GastoRestResponse create(@Valid @RequestBody final CreateGastoRestRequest request) {
    return mapper.toResponse(createGastoUseCase.execute(mapper.toCommand(request)));
  }

  @Override
  @PutMapping("/{id}")
  public GastoRestResponse update(
      @PathVariable final int id, @Valid @RequestBody final UpdateGastoRestRequest request) {
    return mapper.toResponse(updateGastoUseCase.execute(mapper.toCommand(id, request)));
  }

  @Override
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable final int id) {
    deleteGastoUseCase.execute(new DeleteGastoCommand(id));
  }

  @Override
  @GetMapping("/{id}")
  public GastoRestResponse findById(@PathVariable final int id) {
    return mapper.toResponse(getGastoByIdUseCase.execute(new GetGastoByIdQuery(id)));
  }

  @Override
  @GetMapping
  public List<GastoRestResponse> findAll(
      @RequestParam(required = false) final String desde,
      @RequestParam(required = false) final String hasta,
      @RequestParam(required = false) final String tarifaIva,
      @RequestParam(required = false) final String lugar,
      @RequestParam(required = false) final String nombreUsuario,
      @RequestParam(required = false) final BigDecimal montoMinimo,
      @RequestParam(required = false) final BigDecimal montoMaximo,
      @RequestParam(required = false) final Integer limite) {
    final GastoFilterQuery filtro =
        new GastoFilterQuery(
            parseDate(desde),
            parseDate(hasta),
            tarifaIva,
            lugar,
            nombreUsuario,
            montoMinimo,
            montoMaximo,
            limite);
    return getAllGastosUseCase.execute(filtro).stream().map(mapper::toResponse).toList();
  }

  private static LocalDate parseDate(final String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
      return null;
    }
    return LocalDate.parse(rawValue.trim());
  }
}
