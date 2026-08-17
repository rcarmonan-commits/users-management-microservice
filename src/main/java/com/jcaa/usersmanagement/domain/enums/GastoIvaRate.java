package com.jcaa.usersmanagement.domain.enums;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoIvaRateException;
import java.math.BigDecimal;

/**
 * Tarifas de IVA vigentes en la legislacion colombiana aplicables a un gasto.
 *
 * <p>Se modela como enumeracion y no como un numero suelto para que el dominio
 * solo pueda expresar tarifas legalmente validas: cualquier otro porcentaje es
 * un dato invalido y no un caso de negocio.
 */
public enum GastoIvaRate {
  GENERAL(new BigDecimal("19.00")),
  REDUCIDA(new BigDecimal("5.00")),
  EXENTA(BigDecimal.ZERO);

  private final BigDecimal percentage;

  GastoIvaRate(final BigDecimal percentage) {
    this.percentage = percentage;
  }

  public BigDecimal percentage() {
    return percentage;
  }

  /** Factor multiplicador de la base gravable, por ejemplo 0.19 para la tarifa general. */
  public BigDecimal factor() {
    return percentage.movePointLeft(2);
  }

  public static GastoIvaRate fromPercentage(final BigDecimal value) {
    if (value == null) {
      throw InvalidGastoIvaRateException.becauseValueIsNull();
    }
    for (final GastoIvaRate rate : values()) {
      if (rate.percentage.compareTo(value) == 0) {
        return rate;
      }
    }
    throw InvalidGastoIvaRateException.becauseValueIsNotLegal(value);
  }

  public static GastoIvaRate fromString(final String value) {
    for (final GastoIvaRate rate : values()) {
      if (rate.name().equalsIgnoreCase(value)) {
        return rate;
      }
    }
    throw InvalidGastoIvaRateException.becauseNameIsInvalid(value);
  }
}
