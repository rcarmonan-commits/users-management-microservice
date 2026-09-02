package com.jcaa.usersmanagement.domain.model;

import com.jcaa.usersmanagement.domain.enums.GastoIvaRate;
import com.jcaa.usersmanagement.domain.exception.InvalidGastoIvaConsistencyException;
import com.jcaa.usersmanagement.domain.valueobject.GastoAmount;
import com.jcaa.usersmanagement.domain.valueobject.GastoDate;
import com.jcaa.usersmanagement.domain.valueobject.GastoDescription;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.valueobject.GastoOwner;
import com.jcaa.usersmanagement.domain.valueobject.GastoPlace;
import java.math.BigDecimal;
import lombok.Value;

/**
 * Agregado raiz del ejercicio N.º 6. Reune los siete atributos de negocio
 * exigidos: fecha, valorTotalSinIVA, ivaTotal, valorTotalConIVA,
 * nombreUsuario, lugar y descripcion.
 *
 * <p>Es un modelo <em>rico</em>: no se limita a transportar datos, custodia la
 * invariante fiscal del gasto. Ningun gasto puede existir en memoria con unos
 * totales que no cuadren, de modo que la capa de aplicacion y la de
 * infraestructura nunca tienen que revalidar lo mismo.
 *
 * <pre>valorTotalSinIVA + ivaTotal == valorTotalConIVA (+- 0.005)</pre>
 *
 * <p>La tolerancia de medio centavo absorbe el redondeo que llega desde la
 * interfaz; por encima de ella se considera un dato incoherente y se rechaza.
 */
@Value
public class GastoModel {

  /** Medio centavo: margen maximo admitido al contrastar los totales. */
  public static final BigDecimal TOLERANCE = new BigDecimal("0.005");

  GastoId id;
  GastoDate fecha;
  GastoAmount valorTotalSinIva;
  GastoAmount ivaTotal;
  GastoAmount valorTotalConIva;
  GastoOwner nombreUsuario;
  GastoPlace lugar;
  GastoDescription descripcion;

  private GastoModel(
      final GastoId id,
      final GastoDate fecha,
      final GastoAmount valorTotalSinIva,
      final GastoAmount ivaTotal,
      final GastoAmount valorTotalConIva,
      final GastoOwner nombreUsuario,
      final GastoPlace lugar,
      final GastoDescription descripcion) {
    assertFiscalInvariant(valorTotalSinIva, ivaTotal, valorTotalConIva);
    this.id = id;
    this.fecha = fecha;
    this.valorTotalSinIva = valorTotalSinIva;
    this.ivaTotal = ivaTotal;
    this.valorTotalConIva = valorTotalConIva;
    this.nombreUsuario = nombreUsuario;
    this.lugar = lugar;
    this.descripcion = descripcion;
  }

  /** Reconstruye un gasto ya persistido, con identidad asignada. */
  public static GastoModel reconstitute(
      final GastoId id,
      final GastoDate fecha,
      final GastoAmount valorTotalSinIva,
      final GastoAmount ivaTotal,
      final GastoAmount valorTotalConIva,
      final GastoOwner nombreUsuario,
      final GastoPlace lugar,
      final GastoDescription descripcion) {
    return new GastoModel(
        id, fecha, valorTotalSinIva, ivaTotal, valorTotalConIva, nombreUsuario, lugar, descripcion);
  }

  /** Crea un gasto nuevo, aun sin identidad, con totales declarados por el usuario. */
  public static GastoModel create(
      final GastoDate fecha,
      final GastoAmount valorTotalSinIva,
      final GastoAmount ivaTotal,
      final GastoAmount valorTotalConIva,
      final GastoOwner nombreUsuario,
      final GastoPlace lugar,
      final GastoDescription descripcion) {
    return new GastoModel(
        GastoId.notAssigned(),
        fecha,
        valorTotalSinIva,
        ivaTotal,
        valorTotalConIva,
        nombreUsuario,
        lugar,
        descripcion);
  }

  /**
   * Crea un gasto liquidando el IVA a partir de la base y una tarifa legal.
   * Al calcularse en el dominio, los totales cuadran por construccion.
   */
  public static GastoModel createApplyingRate(
      final GastoDate fecha,
      final GastoAmount valorTotalSinIva,
      final GastoIvaRate tarifa,
      final GastoOwner nombreUsuario,
      final GastoPlace lugar,
      final GastoDescription descripcion) {
    final GastoAmount iva = valorTotalSinIva.multiply(tarifa.factor());
    return new GastoModel(
        GastoId.notAssigned(),
        fecha,
        valorTotalSinIva,
        iva,
        valorTotalSinIva.add(iva),
        nombreUsuario,
        lugar,
        descripcion);
  }

  /** Devuelve una copia con la identidad que asigno la base de datos. */
  public GastoModel withId(final GastoId assignedId) {
    return new GastoModel(
        assignedId,
        fecha,
        valorTotalSinIva,
        ivaTotal,
        valorTotalConIva,
        nombreUsuario,
        lugar,
        descripcion);
  }

  /**
   * Tarifa efectiva aplicada, deducida de los importes. Puede no coincidir con
   * ninguna tarifa legal cuando el gasto se registro con totales manuales.
   */
  public BigDecimal effectiveRatePercentage() {
    if (valorTotalSinIva.isZero()) {
      return BigDecimal.ZERO;
    }
    return ivaTotal
        .value()
        .divide(valorTotalSinIva.value(), 6, java.math.RoundingMode.HALF_UP)
        .movePointRight(2)
        .setScale(2, java.math.RoundingMode.HALF_UP);
  }

  private static void assertFiscalInvariant(
      final GastoAmount base, final GastoAmount iva, final GastoAmount total) {
    final BigDecimal difference = base.add(iva).difference(total);
    if (difference.compareTo(TOLERANCE) > 0) {
      throw InvalidGastoIvaConsistencyException.becauseTotalsDoNotMatch(
          base.value(), iva.value(), total.value(), TOLERANCE);
    }
  }
}
