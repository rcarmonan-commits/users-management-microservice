package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.query.GetGastoByIdQuery;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Caso de uso de lectura: recuperar un gasto por identidad. */
public interface GetGastoByIdUseCase {
  GastoModel execute(@NotNull @Valid GetGastoByIdQuery query);
}
