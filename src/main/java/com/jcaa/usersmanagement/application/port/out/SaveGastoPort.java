package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.GastoModel;

/** Puerto de salida para persistir un gasto nuevo. */
public interface SaveGastoPort {

  /**
   * @param gasto agregado sin identidad asignada
   * @return el mismo gasto con el identificador que asigno el almacen
   */
  GastoModel save(GastoModel gasto);
}
