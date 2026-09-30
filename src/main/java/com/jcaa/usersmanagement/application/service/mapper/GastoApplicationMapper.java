package com.jcaa.usersmanagement.application.service.mapper;

import com.jcaa.usersmanagement.application.service.dto.command.CreateGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.command.UpdateGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.response.GastoResponse;
import com.jcaa.usersmanagement.domain.enums.GastoIvaRate;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.GastoAmount;
import com.jcaa.usersmanagement.domain.valueobject.GastoDate;
import com.jcaa.usersmanagement.domain.valueobject.GastoDescription;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.valueobject.GastoOwner;
import com.jcaa.usersmanagement.domain.valueobject.GastoPlace;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Traduce entre los DTO de la capa de aplicacion y el agregado de dominio.
 *
 * <p>Concentrar aqui la conversion evita que cada caso de uso repita la
 * construccion de Value Objects, y deja un unico sitio donde revisar como
 * entra y sale un Gasto del nucleo.
 */
@Component
public class GastoApplicationMapper {

  public GastoModel toDomain(final CreateGastoCommand command) {
    if (command.liquidatesFromRate()) {
      return GastoModel.createApplyingRate(
          GastoDate.fromString(command.fecha()),
          GastoAmount.of(command.valorTotalSinIVA()),
          GastoIvaRate.fromString(command.tarifaIva()),
          GastoOwner.of(command.nombreUsuario()),
          GastoPlace.of(command.lugar()),
          GastoDescription.of(command.descripcion()));
    }
    return GastoModel.create(
        GastoDate.fromString(command.fecha()),
        GastoAmount.of(command.valorTotalSinIVA()),
        GastoAmount.of(orZero(command.ivaTotal())),
        GastoAmount.of(orZero(command.valorTotalConIVA())),
        GastoOwner.of(command.nombreUsuario()),
        GastoPlace.of(command.lugar()),
        GastoDescription.of(command.descripcion()));
  }

  public GastoModel toDomain(final UpdateGastoCommand command) {
    final GastoModel base =
        command.liquidatesFromRate()
            ? GastoModel.createApplyingRate(
                GastoDate.fromString(command.fecha()),
                GastoAmount.of(command.valorTotalSinIVA()),
                GastoIvaRate.fromString(command.tarifaIva()),
                GastoOwner.of(command.nombreUsuario()),
                GastoPlace.of(command.lugar()),
                GastoDescription.of(command.descripcion()))
            : GastoModel.create(
                GastoDate.fromString(command.fecha()),
                GastoAmount.of(command.valorTotalSinIVA()),
                GastoAmount.of(orZero(command.ivaTotal())),
                GastoAmount.of(orZero(command.valorTotalConIVA())),
                GastoOwner.of(command.nombreUsuario()),
                GastoPlace.of(command.lugar()),
                GastoDescription.of(command.descripcion()));
    return base.withId(GastoId.of(command.id()));
  }

  public GastoResponse toResponse(final GastoModel model) {
    return new GastoResponse(
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

  private static BigDecimal orZero(final BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }
}
