package com.jcaa.usersmanagement.infrastructure.adapter.persistence.entity;

/**
 * Representacion plana de una fila de la tabla `gastos`.
 *
 * <p>Los nombres reproducen las columnas reales y los tipos, lo que devuelve el
 * driver JDBC. Es deliberadamente anemica: su unico trabajo es cruzar la
 * frontera entre SQL y el dominio sin arrastrar reglas de negocio.
 */
public record GastoEntity(
    int idGasto,
    java.sql.Date fecha,
    double valorTotalSinIva,
    double ivaTotal,
    double valorTotalConIva,
    String nombreUsuario,
    String lugar,
    String descripcion) {}
