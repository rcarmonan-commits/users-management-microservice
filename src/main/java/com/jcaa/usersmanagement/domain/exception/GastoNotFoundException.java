package com.jcaa.usersmanagement.domain.exception;

public final class GastoNotFoundException extends DomainException {

  private static final String MESSAGE_BY_ID = "No gasto was found with identifier %d.";

  private GastoNotFoundException(final String message) {
    super(message);
  }

  public static GastoNotFoundException becauseIdDoesNotExist(final int id) {
    return new GastoNotFoundException(String.format(MESSAGE_BY_ID, id));
  }
}
