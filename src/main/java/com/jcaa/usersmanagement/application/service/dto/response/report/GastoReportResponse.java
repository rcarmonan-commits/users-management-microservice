package com.jcaa.usersmanagement.application.service.dto.response.report;

import com.jcaa.usersmanagement.application.service.dto.response.GastoTotalsResponse;
import java.util.List;

/**
 * Envoltura generica de una proyeccion analitica.
 *
 * <p>Cada reporte viaja con sus PROPIOS totales y con el eco de los filtros
 * aplicados. Esa autonomia es el nucleo del diseno: un reporte no depende del
 * estado de los demas, que fue justamente el defecto corregido en el ejercicio
 * gemelo (leccion LRN-002).
 */
public record GastoReportResponse<T>(
    String reporte, String filtrosAplicados, GastoTotalsResponse totales, List<T> filas) {}
