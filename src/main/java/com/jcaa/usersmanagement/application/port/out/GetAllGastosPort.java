package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.GastoModel;
import java.util.List;

/**
 * Puerto de salida de lectura masiva. Alimenta el lado Query de CQRS y las
 * proyecciones analiticas, que derivan todos sus agregados de esta unica
 * fuente de lectura.
 */
public interface GetAllGastosPort {

  List<GastoModel> findAll();
}
