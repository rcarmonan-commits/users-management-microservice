package com.jcaa.usersmanagement.domain.valueobject;

/**
 * Detalle libre del gasto. Es el unico atributo opcional del agregado: la
 * columna `descripcion` admite nulos.
 */
public record GastoDescription(String value) {

  private static final int MAXIMUM_LENGTH = 2000;

  public GastoDescription {
    final String normalized = value == null ? "" : value.trim();
    value = normalized.length() > MAXIMUM_LENGTH
        ? normalized.substring(0, MAXIMUM_LENGTH)
        : normalized;
  }

  public static GastoDescription of(final String value) {
    return new GastoDescription(value);
  }

  public static GastoDescription empty() {
    return new GastoDescription("");
  }

  public boolean isEmpty() {
    return value.isEmpty();
  }

  @Override
  public String toString() {
    return value;
  }
}
