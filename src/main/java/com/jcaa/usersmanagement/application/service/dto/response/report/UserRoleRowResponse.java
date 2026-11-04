package com.jcaa.usersmanagement.application.service.dto.response.report;

/** Fila de la proyeccion 7: directorio de usuarios agrupado por rol. */
public record UserRoleRowResponse(String rol, int cantidad, java.util.List<String> usuarios) {}
