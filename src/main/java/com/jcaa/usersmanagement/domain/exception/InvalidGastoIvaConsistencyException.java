package com.jcaa.usersmanagement.domain.exception;

import java.math.BigDecimal;

/**
 * Se lanza cuando se viola la invariante fiscal del agregado Gasto:
 *
 * <pre>valorTotalSinIVA + ivaTotal == valorTotalConIVA</pre>
 *
 * <p>La comparacion se hace con una tolerancia de medio centavo para absorber
 * el redondeo de los calculos en coma flotante que llegan desde la interfaz.
 */
public final class InvalidGastoIvaConsistencyException extends DomainException {

  private static final String MESSAGE =
      "Fiscal invariant violated: base %s + IVA %s = %s, but the declared total is %s "
          + "(difference of %s exceeds the tolerance of %s).";

  private InvalidGastoIvaConsistencyException(final String message) {
    super(message);
  }

  public static InvalidGastoIvaConsistencyException becauseTotalsDoNotMatch(
      final BigDecimal base,
      final BigDecimal iva,
      final BigDecimal declaredTotal,
      final BigDecimal tolerance) {
    final BigDecimal computedTotal = base.add(iva);
    final BigDecimal difference = computedTotal.subtract(declaredTotal).abs();
    return new InvalidGastoIvaConsistencyException(
        String.format(
            MESSAGE,
            base.toPlainString(),
            iva.toPlainString(),
            computedTotal.toPlainString(),
            declaredTotal.toPlainString(),
            difference.toPlainString(),
            tolerance.toPlainString()));
  }
}
