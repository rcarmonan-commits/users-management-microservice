package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.GetGastoByIdUseCase;
import com.jcaa.usersmanagement.application.port.out.GetGastoByIdPort;
import com.jcaa.usersmanagement.application.service.dto.query.GetGastoByIdQuery;
import com.jcaa.usersmanagement.domain.exception.GastoNotFoundException;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Lectura puntual por identidad. */
@Service
@RequiredArgsConstructor
public class GetGastoByIdService implements GetGastoByIdUseCase {

  private final GetGastoByIdPort getGastoByIdPort;

  @Override
  public GastoModel execute(final GetGastoByIdQuery query) {
    return getGastoByIdPort
        .findById(GastoId.of(query.id()))
        .orElseThrow(() -> GastoNotFoundException.becauseIdDoesNotExist(query.id()));
  }
}
