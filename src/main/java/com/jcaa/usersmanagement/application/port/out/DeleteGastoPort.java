package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.valueobject.GastoId;

/** Puerto de salida para eliminar un gasto por su identificador. */
public interface DeleteGastoPort {

  boolean deleteById(GastoId id);
}
