package com.jcaa.usersmanagement.domain.exception;

public final class InvalidGastoOwnerException extends DomainException {

  private static final String MESSAGE_EMPTY = "The gasto owner must not be empty.";
  private static final String MESSAGE_TOO_LONG =
      "The gasto owner must not exceed %d characters.";

  private InvalidGastoOwnerException(final String message) {
    super(message);
  }

  public static InvalidGastoOwnerException becauseValueIsEmpty() {
    return new InvalidGastoOwnerException(MESSAGE_EMPTY);
  }

  public static InvalidGastoOwnerException becauseLengthIsTooLong(final int maximumLength) {
    return new InvalidGastoOwnerException(String.format(MESSAGE_TOO_LONG, maximumLength));
  }
}
