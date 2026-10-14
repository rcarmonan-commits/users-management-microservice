package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.DeleteGastoUseCase;
import com.jcaa.usersmanagement.application.port.out.CachePort;
import com.jcaa.usersmanagement.application.port.out.DeleteGastoPort;
import com.jcaa.usersmanagement.application.port.out.DomainEventPublisherPort;
import com.jcaa.usersmanagement.application.service.dto.command.DeleteGastoCommand;
import com.jcaa.usersmanagement.domain.event.GastoDeletedDomainEvent;
import com.jcaa.usersmanagement.domain.exception.GastoNotFoundException;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Orquesta la eliminacion de un gasto. */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteGastoService implements DeleteGastoUseCase {

  private final DeleteGastoPort deleteGastoPort;
  private final DomainEventPublisherPort eventPublisher;
  private final CachePort cachePort;
  private final Validator validator;

  @Override
  public void execute(final DeleteGastoCommand command) {
    validateCommand(command);
    final GastoId id = GastoId.of(command.id());
    if (!deleteGastoPort.deleteById(id)) {
      throw GastoNotFoundException.becauseIdDoesNotExist(command.id());
    }
    cachePort.evictAll();
    eventPublisher.publish(new GastoDeletedDomainEvent(id));
    log.info("Gasto deleted with id {}", id);
  }

  private void validateCommand(final DeleteGastoCommand command) {
    final Set<ConstraintViolation<DeleteGastoCommand>> violations = validator.validate(command);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
