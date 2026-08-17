package com.jcaa.usersmanagement.domain.exception;

public final class InvalidGastoPlaceException extends DomainException {

  private static final String MESSAGE_EMPTY = "The gasto place must not be empty.";
  private static final String MESSAGE_TOO_SHORT =
      "The gasto place must have at least %d characters.";
  private static final String MESSAGE_TOO_LONG =
      "The gasto place must not exceed %d characters.";

  private InvalidGastoPlaceException(final String message) {
    super(message);
  }

  public static InvalidGastoPlaceException becauseValueIsEmpty() {
    return new InvalidGastoPlaceException(MESSAGE_EMPTY);
  }

  public static InvalidGastoPlaceException becauseLengthIsTooShort(final int minimumLength) {
    return new InvalidGastoPlaceException(String.format(MESSAGE_TOO_SHORT, minimumLength));
  }

  public static InvalidGastoPlaceException becauseLengthIsTooLong(final int maximumLength) {
    return new InvalidGastoPlaceException(String.format(MESSAGE_TOO_LONG, maximumLength));
  }
}
