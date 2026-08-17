package com.jcaa.usersmanagement.domain.exception;

public final class InvalidGastoIdException extends DomainException {

  private static final String MESSAGE_NOT_POSITIVE =
      "The gasto identifier must be a positive integer, but was %d.";

  private InvalidGastoIdException(final String message) {
    super(message);
  }

  public static InvalidGastoIdException becauseValueIsNotPositive(final int value) {
    return new InvalidGastoIdException(String.format(MESSAGE_NOT_POSITIVE, value));
  }
}
