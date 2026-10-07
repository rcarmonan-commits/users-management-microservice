package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.command.DeleteGastoCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Caso de uso: eliminar un gasto por identidad. */
public interface DeleteGastoUseCase {
  void execute(@NotNull @Valid DeleteGastoCommand command);
}
