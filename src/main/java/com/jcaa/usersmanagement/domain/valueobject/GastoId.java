package com.jcaa.usersmanagement.domain.valueobject;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoIdException;

/**
 * Identificador de un gasto ya persistido.
 *
 * <p>La tabla `gastos` usa un entero autoincremental, por lo que un gasto aun
 * no persistido carece de identidad. Esa ausencia se representa con
 * {@link #notAssigned()} y nunca con un cero o un negativo circulando por el
 * dominio.
 */
public record GastoId(int value) {

  private static final int NOT_ASSIGNED = 0;

  public GastoId {
    if (value != NOT_ASSIGNED && value < 0) {
      throw InvalidGastoIdException.becauseValueIsNotPositive(value);
    }
  }

  public static GastoId of(final int value) {
    if (value <= NOT_ASSIGNED) {
      throw InvalidGastoIdException.becauseValueIsNotPositive(value);
    }
    return new GastoId(value);
  }

  public static GastoId notAssigned() {
    return new GastoId(NOT_ASSIGNED);
  }

  public boolean isAssigned() {
    return value != NOT_ASSIGNED;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }
}
