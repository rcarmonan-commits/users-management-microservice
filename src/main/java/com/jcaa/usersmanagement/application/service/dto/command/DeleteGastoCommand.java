package com.jcaa.usersmanagement.application.service.dto.command;

import jakarta.validation.constraints.Min;

/** Comando de escritura para eliminar un gasto por identidad. */
public record DeleteGastoCommand(
    @Min(value = 1, message = "id must be a positive integer") int id) {}
