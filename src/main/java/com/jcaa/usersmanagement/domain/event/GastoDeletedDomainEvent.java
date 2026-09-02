package com.jcaa.usersmanagement.domain.event;

import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import java.util.Map;
import lombok.Getter;

@Getter
public final class GastoDeletedDomainEvent extends DomainEvent {

  private static final String EVENT_NAME = "gasto.deleted";

  private final GastoId gastoId;

  public GastoDeletedDomainEvent(final GastoId gastoId) {
    super(EVENT_NAME);
    this.gastoId = gastoId;
  }

  @Override
  public Map<String, String> payload() {
    return Map.of("id", gastoId.toString());
  }
}
