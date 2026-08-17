package com.jcaa.usersmanagement.domain.exception;

public final class InvalidGastoDateException extends DomainException {

  private static final String MESSAGE_NULL = "The gasto date must not be null.";
  private static final String MESSAGE_NOT_PARSEABLE =
      "The gasto date '%s' is not a valid calendar date in ISO format (yyyy-MM-dd).";
  private static final String MESSAGE_IN_FUTURE =
      "The gasto date %s cannot be in the future: an expense is always already incurred.";

  private InvalidGastoDateException(final String message) {
    super(message);
  }

  public static InvalidGastoDateException becauseValueIsNull() {
    return new InvalidGastoDateException(MESSAGE_NULL);
  }

  public static InvalidGastoDateException becauseValueIsNotParseable(final String value) {
    return new InvalidGastoDateException(String.format(MESSAGE_NOT_PARSEABLE, value));
  }

  public static InvalidGastoDateException becauseValueIsInTheFuture(final String value) {
    return new InvalidGastoDateException(String.format(MESSAGE_IN_FUTURE, value));
  }
}
