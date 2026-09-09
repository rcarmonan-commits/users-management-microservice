package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import java.util.Optional;

/** Puerto de salida de lectura por identidad. */
public interface GetGastoByIdPort {

  Optional<GastoModel> findById(GastoId id);
}
