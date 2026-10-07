package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.command.CreateGastoCommand;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Caso de uso: registrar un gasto nuevo. */
public interface CreateGastoUseCase {
  GastoModel execute(@NotNull @Valid CreateGastoCommand command);
}
