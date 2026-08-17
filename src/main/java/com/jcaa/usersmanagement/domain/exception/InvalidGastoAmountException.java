package com.jcaa.usersmanagement.domain.exception;

import java.math.BigDecimal;

public final class InvalidGastoAmountException extends DomainException {

  private static final String MESSAGE_NULL = "The gasto amount must not be null.";
  private static final String MESSAGE_NEGATIVE = "The gasto amount must not be negative, but was %s.";
  private static final String MESSAGE_TOO_LARGE =
      "The gasto amount %s exceeds the maximum supported value.";

  private InvalidGastoAmountException(final String message) {
    super(message);
  }

  public static InvalidGastoAmountException becauseValueIsNull() {
    return new InvalidGastoAmountException(MESSAGE_NULL);
  }

  public static InvalidGastoAmountException becauseValueIsNegative(final BigDecimal value) {
    return new InvalidGastoAmountException(String.format(MESSAGE_NEGATIVE, value.toPlainString()));
  }

  public static InvalidGastoAmountException becauseValueIsTooLarge(final BigDecimal value) {
    return new InvalidGastoAmountException(String.format(MESSAGE_TOO_LARGE, value.toPlainString()));
  }
}
