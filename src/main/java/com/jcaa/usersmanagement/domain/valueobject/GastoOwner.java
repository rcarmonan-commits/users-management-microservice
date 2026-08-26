package com.jcaa.usersmanagement.domain.valueobject;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoOwnerException;
import java.util.Objects;

/**
 * Usuario responsable del gasto.
 *
 * <p>Corresponde a la columna `gastos.nombre_usuario`, que es clave foranea
 * contra `usuarios(id)`. Es decir, almacena el IDENTIFICADOR de la cuenta y no
 * el nombre de presentacion. Confundir ambos produce consultas que nunca
 * devuelven resultados; el tipo existe precisamente para no mezclarlos.
 */
public record GastoOwner(String value) {

  private static final int MAXIMUM_LENGTH = 50;

  public GastoOwner {
    final String normalized = Objects.requireNonNull(value, "GastoOwner cannot be null").trim();
    if (normalized.isEmpty()) {
      throw InvalidGastoOwnerException.becauseValueIsEmpty();
    }
    if (normalized.length() > MAXIMUM_LENGTH) {
      throw InvalidGastoOwnerException.becauseLengthIsTooLong(MAXIMUM_LENGTH);
    }
    value = normalized;
  }

  public static GastoOwner of(final String value) {
    return new GastoOwner(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
