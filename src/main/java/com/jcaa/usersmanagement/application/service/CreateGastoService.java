package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.CreateGastoUseCase;
import com.jcaa.usersmanagement.application.port.out.CachePort;
import com.jcaa.usersmanagement.application.port.out.DomainEventPublisherPort;
import com.jcaa.usersmanagement.application.port.out.SaveGastoPort;
import com.jcaa.usersmanagement.application.service.dto.command.CreateGastoCommand;
import com.jcaa.usersmanagement.application.service.mapper.GastoApplicationMapper;
import com.jcaa.usersmanagement.domain.event.GastoCreatedDomainEvent;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Orquesta el alta de un gasto: valida el comando, construye el agregado
 * —que verifica la invariante fiscal—, persiste, invalida la cache de lectura
 * y publica el evento de dominio.
 *
 * <p>No contiene reglas de negocio: esas viven en el dominio. Su
 * responsabilidad es coordinar puertos, que es justo lo que separa la capa de
 * aplicacion del nucleo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateGastoService implements CreateGastoUseCase {

  private final SaveGastoPort saveGastoPort;
  private final DomainEventPublisherPort eventPublisher;
  private final CachePort cachePort;
  private final GastoApplicationMapper mapper;
  private final Validator validator;

  @Override
  public GastoModel execute(final CreateGastoCommand command) {
    validateCommand(command);
    final GastoModel saved = saveGastoPort.save(mapper.toDomain(command));
    cachePort.evictAll();
    eventPublisher.publish(new GastoCreatedDomainEvent(saved));
    log.info("Gasto created with id {}", saved.getId());
    return saved;
  }

  private void validateCommand(final CreateGastoCommand command) {
    final Set<ConstraintViolation<CreateGastoCommand>> violations = validator.validate(command);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
