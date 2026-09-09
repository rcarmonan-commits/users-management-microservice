package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.enums.GastoIvaRate;
import java.util.List;

/**
 * Puerto de salida hacia un servicio REST externo de indicadores tributarios
 * (paso 21: adaptador rest).
 *
 * <p>Expone las tarifas de IVA vigentes. El adaptador que lo implemente debe
 * degradar con elegancia: si el servicio externo no responde, el sistema sigue
 * operando con las tarifas legales que ya conoce el dominio.
 */
public interface TaxIndicatorRestPort {

  List<GastoIvaRate> fetchAvailableRates();

  boolean isRemoteAvailable();
}
