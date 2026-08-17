package com.jcaa.usersmanagement.domain.exception;

import java.math.BigDecimal;

public final class InvalidGastoIvaRateException extends DomainException {

  private static final String MESSAGE_NULL = "The IVA rate must not be null.";
  private static final String MESSAGE_NOT_LEGAL =
      "The IVA rate %s%% is not a legal rate. Allowed rates are 19%%, 5%% and 0%%.";
  private static final String MESSAGE_INVALID_NAME = "The IVA rate '%s' does not exist.";

  private InvalidGastoIvaRateException(final String message) {
    super(message);
  }

  public static InvalidGastoIvaRateException becauseValueIsNull() {
    return new InvalidGastoIvaRateException(MESSAGE_NULL);
  }

  public static InvalidGastoIvaRateException becauseValueIsNotLegal(final BigDecimal value) {
    return new InvalidGastoIvaRateException(String.format(MESSAGE_NOT_LEGAL, value.toPlainString()));
  }

  public static InvalidGastoIvaRateException becauseNameIsInvalid(final String value) {
    return new InvalidGastoIvaRateException(String.format(MESSAGE_INVALID_NAME, value));
  }
}
