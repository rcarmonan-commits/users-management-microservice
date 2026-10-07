package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.command.UpdateGastoCommand;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Caso de uso: modificar un gasto existente. */
public interface UpdateGastoUseCase {
  GastoModel execute(@NotNull @Valid UpdateGastoCommand command);
}
