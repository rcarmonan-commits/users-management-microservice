package com.jcaa.usersmanagement.domain.valueobject;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoDateException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Fecha en la que se incurrio en el gasto.
 *
 * <p>Rechaza fechas inexistentes en el calendario gregoriano (por ejemplo
 * 2026-02-30) y fechas futuras: un gasto se registra cuando ya ocurrio.
 */
public record GastoDate(LocalDate value) {

  private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

  public GastoDate {
    Objects.requireNonNull(value, "GastoDate cannot be null");
    if (value.isAfter(LocalDate.now())) {
      throw InvalidGastoDateException.becauseValueIsInTheFuture(value.format(ISO));
    }
  }

  public static GastoDate fromString(final String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
      throw InvalidGastoDateException.becauseValueIsNull();
    }
    try {
      return new GastoDate(LocalDate.parse(rawValue.trim(), ISO));
    } catch (final DateTimeParseException exception) {
      throw InvalidGastoDateException.becauseValueIsNotParseable(rawValue);
    }
  }

  public static GastoDate of(final LocalDate value) {
    if (value == null) {
      throw InvalidGastoDateException.becauseValueIsNull();
    }
    return new GastoDate(value);
  }

  @Override
  public String toString() {
    return value.format(ISO);
  }
}
