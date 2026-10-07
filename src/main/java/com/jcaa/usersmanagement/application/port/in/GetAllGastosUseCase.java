package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.query.GastoFilterQuery;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import java.util.List;

/**
 * Caso de uso de lectura: listar gastos aplicando filtros independientes.
 *
 * <p>Es la unica puerta de entrada del lado Query para datos masivos: tanto el
 * listado CRUDL como las proyecciones analiticas se apoyan en el.
 */
public interface GetAllGastosUseCase {
  List<GastoModel> execute(GastoFilterQuery query);
}
