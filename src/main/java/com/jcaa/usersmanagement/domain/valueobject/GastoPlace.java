package com.jcaa.usersmanagement.domain.valueobject;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoPlaceException;
import java.util.Objects;

/** Establecimiento o proveedor donde se incurrio en el gasto. */
public record GastoPlace(String value) {

  private static final int MINIMUM_LENGTH = 3;
  private static final int MAXIMUM_LENGTH = 150;

  public GastoPlace {
    final String normalized = Objects.requireNonNull(value, "GastoPlace cannot be null").trim();
    if (normalized.isEmpty()) {
      throw InvalidGastoPlaceException.becauseValueIsEmpty();
    }
    if (normalized.length() < MINIMUM_LENGTH) {
      throw InvalidGastoPlaceException.becauseLengthIsTooShort(MINIMUM_LENGTH);
    }
    if (normalized.length() > MAXIMUM_LENGTH) {
      throw InvalidGastoPlaceException.becauseLengthIsTooLong(MAXIMUM_LENGTH);
    }
    value = normalized;
  }

  public static GastoPlace of(final String value) {
    return new GastoPlace(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
