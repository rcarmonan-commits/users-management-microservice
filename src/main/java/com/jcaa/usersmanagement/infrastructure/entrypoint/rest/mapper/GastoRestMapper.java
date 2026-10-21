package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.mapper;

import com.jcaa.usersmanagement.application.service.dto.command.CreateGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.command.UpdateGastoCommand;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.CreateGastoRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.UpdateGastoRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.GastoRestResponse;
import org.springframework.stereotype.Component;

/**
 * Traduce entre el contrato HTTP y los comandos de la capa de aplicacion.
 *
 * <p>Esta separacion permite que el contrato publico de la API evolucione sin
 * arrastrar al nucleo, y al reves.
 */
@Component
public class GastoRestMapper {

  public CreateGastoCommand toCommand(final CreateGastoRestRequest request) {
    return new CreateGastoCommand(
        request.fecha(),
        request.valorTotalSinIVA(),
        request.ivaTotal(),
        request.valorTotalConIVA(),
        request.nombreUsuario(),
        request.lugar(),
        request.descripcion(),
        request.tarifaIva());
  }

  public UpdateGastoCommand toCommand(final int id, final UpdateGastoRestRequest request) {
    return new UpdateGastoCommand(
        id,
        request.fecha(),
        request.valorTotalSinIVA(),
        request.ivaTotal(),
        request.valorTotalConIVA(),
        request.nombreUsuario(),
        request.lugar(),
        request.descripcion(),
        request.tarifaIva());
  }

  public GastoRestResponse toResponse(final GastoModel model) {
    return new GastoRestResponse(
        model.getId().value(),
        model.getFecha().toString(),
        model.getValorTotalSinIva().value(),
        model.getIvaTotal().value(),
        model.getValorTotalConIva().value(),
        model.getNombreUsuario().value(),
        model.getLugar().value(),
        model.getDescripcion().isEmpty() ? null : model.getDescripcion().value(),
        model.effectiveRatePercentage());
  }
}
