package com.jcaa.usersmanagement.application.service.dto.query;

import jakarta.validation.constraints.Min;

/** Consulta puntual por identidad (lado Query de CQRS). */
public record GetGastoByIdQuery(
    @Min(value = 1, message = "id must be a positive integer") int id) {}
