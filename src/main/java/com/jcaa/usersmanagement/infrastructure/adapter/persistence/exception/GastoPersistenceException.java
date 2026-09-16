package com.jcaa.usersmanagement.infrastructure.adapter.persistence.exception;

public final class GastoPersistenceException extends RuntimeException {

  private static final String MESSAGE_SAVE = "Failed to save the gasto.";
  private static final String MESSAGE_UPDATE = "Failed to update gasto with ID: '%d'.";
  private static final String MESSAGE_FIND = "Failed to find gasto with ID: '%d'.";
  private static final String MESSAGE_ALL = "Failed to retrieve all gastos.";
  private static final String MESSAGE_DELETE = "Failed to delete gasto with ID: '%d'.";
  private static final String MESSAGE_NO_ID =
      "The database did not return a generated identifier for the gasto.";

  private GastoPersistenceException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public static GastoPersistenceException becauseSaveFailed(final Throwable cause) {
    return new GastoPersistenceException(MESSAGE_SAVE, cause);
  }

  public static GastoPersistenceException becauseUpdateFailed(final int id, final Throwable cause) {
    return new GastoPersistenceException(String.format(MESSAGE_UPDATE, id), cause);
  }

  public static GastoPersistenceException becauseFindByIdFailed(
      final int id, final Throwable cause) {
    return new GastoPersistenceException(String.format(MESSAGE_FIND, id), cause);
  }

  public static GastoPersistenceException becauseFindAllFailed(final Throwable cause) {
    return new GastoPersistenceException(MESSAGE_ALL, cause);
  }

  public static GastoPersistenceException becauseDeleteFailed(final int id, final Throwable cause) {
    return new GastoPersistenceException(String.format(MESSAGE_DELETE, id), cause);
  }

  public static GastoPersistenceException becauseIdentifierWasNotGenerated() {
    return new GastoPersistenceException(MESSAGE_NO_ID, null);
  }
}
