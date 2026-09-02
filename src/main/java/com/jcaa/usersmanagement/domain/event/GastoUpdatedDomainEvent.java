package com.jcaa.usersmanagement.domain.event;

import com.jcaa.usersmanagement.domain.model.GastoModel;
import java.util.Map;
import lombok.Getter;

@Getter
public final class GastoUpdatedDomainEvent extends DomainEvent {

  private static final String EVENT_NAME = "gasto.updated";

  private final GastoModel gasto;

  public GastoUpdatedDomainEvent(final GastoModel gasto) {
    super(EVENT_NAME);
    this.gasto = gasto;
  }

  @Override
  public Map<String, String> payload() {
    return Map.of(
        "id", gasto.getId().toString(),
        "fecha", gasto.getFecha().toString(),
        "valorTotalConIVA", gasto.getValorTotalConIva().toString(),
        "nombreUsuario", gasto.getNombreUsuario().value(),
        "lugar", gasto.getLugar().value());
  }
}
