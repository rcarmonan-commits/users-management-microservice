package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.UpdateGastoUseCase;
import com.jcaa.usersmanagement.application.port.out.CachePort;
import com.jcaa.usersmanagement.application.port.out.DomainEventPublisherPort;
import com.jcaa.usersmanagement.application.port.out.GetGastoByIdPort;
import com.jcaa.usersmanagement.application.port.out.UpdateGastoPort;
import com.jcaa.usersmanagement.application.service.dto.command.UpdateGastoCommand;
import com.jcaa.usersmanagement.application.service.mapper.GastoApplicationMapper;
import com.jcaa.usersmanagement.domain.event.GastoUpdatedDomainEvent;
import com.jcaa.usersmanagement.domain.exception.GastoNotFoundException;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Orquesta la modificacion de un gasto existente. */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateGastoService implements UpdateGastoUseCase {

  private final UpdateGastoPort updateGastoPort;
  private final GetGastoByIdPort getGastoByIdPort;
  private final DomainEventPublisherPort eventPublisher;
  private final CachePort cachePort;
  private final GastoApplicationMapper mapper;
  private final Validator validator;

  @Override
  public GastoModel execute(final UpdateGastoCommand command) {
    validateCommand(command);
    ensureGastoExists(command.id());
    final GastoModel updated = updateGastoPort.update(mapper.toDomain(command));
    cachePort.evictAll();
    eventPublisher.publish(new GastoUpdatedDomainEvent(updated));
    log.info("Gasto updated with id {}", updated.getId());
    return updated;
  }

  private void ensureGastoExists(final int id) {
    getGastoByIdPort
        .findById(GastoId.of(id))
        .orElseThrow(() -> GastoNotFoundException.becauseIdDoesNotExist(id));
  }

  private void validateCommand(final UpdateGastoCommand command) {
    final Set<ConstraintViolation<UpdateGastoCommand>> violations = validator.validate(command);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
