package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.GastoModel;

/** Puerto de salida para actualizar un gasto existente. */
public interface UpdateGastoPort {

  GastoModel update(GastoModel gasto);
}
