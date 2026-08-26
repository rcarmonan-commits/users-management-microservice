package com.jcaa.usersmanagement.domain.valueobject;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoAmountException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Importe monetario de un gasto.
 *
 * <p>Se modela sobre {@link BigDecimal} con escala fija de dos decimales y
 * redondeo HALF_UP. Usar `double` para dinero arrastra errores de
 * representacion binaria que terminan rompiendo la invariante fiscal; el
 * dominio normaliza una sola vez, al construir, y el resto del calculo ya opera
 * sobre valores exactos.
 */
public record GastoAmount(BigDecimal value) {

  private static final int SCALE = 2;
  private static final BigDecimal MAXIMUM = new BigDecimal("999999999999.99");

  public GastoAmount {
    if (value == null) {
      throw InvalidGastoAmountException.becauseValueIsNull();
    }
    final BigDecimal normalized = value.setScale(SCALE, RoundingMode.HALF_UP);
    if (normalized.signum() < 0) {
      throw InvalidGastoAmountException.becauseValueIsNegative(normalized);
    }
    if (normalized.compareTo(MAXIMUM) > 0) {
      throw InvalidGastoAmountException.becauseValueIsTooLarge(normalized);
    }
    value = normalized;
  }

  public static GastoAmount of(final BigDecimal value) {
    return new GastoAmount(value);
  }

  public static GastoAmount of(final double value) {
    return new GastoAmount(BigDecimal.valueOf(value));
  }

  public static GastoAmount zero() {
    return new GastoAmount(BigDecimal.ZERO);
  }

  public GastoAmount add(final GastoAmount other) {
    return new GastoAmount(value.add(other.value));
  }

  public GastoAmount multiply(final BigDecimal factor) {
    return new GastoAmount(value.multiply(factor));
  }

  public BigDecimal difference(final GastoAmount other) {
    return value.subtract(other.value).abs();
  }

  public boolean isZero() {
    return value.signum() == 0;
  }

  public double toDouble() {
    return value.doubleValue();
  }

  @Override
  public String toString() {
    return value.toPlainString();
  }
}
