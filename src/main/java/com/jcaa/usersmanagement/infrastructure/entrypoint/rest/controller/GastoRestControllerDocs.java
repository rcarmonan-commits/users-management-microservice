package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.CreateGastoRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.UpdateGastoRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.GastoRestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;

/**
 * Documentacion OpenAPI del recurso Gasto.
 *
 * <p>Se mantiene separada del controlador para que la logica de transporte no
 * quede sepultada bajo anotaciones. El controlador implementa esta interfaz y
 * hereda toda la documentacion.
 */
@Tag(name = "Gastos", description = "CRUDL y consultas analiticas de la entidad Gasto")
@SecurityRequirement(name = "bearerAuth")
public interface GastoRestControllerDocs {

  @Operation(
      summary = "Registrar un gasto",
      description =
          "Crea un gasto. Si se informa 'tarifaIva', el dominio liquida el IVA y los totales "
              + "cuadran por construccion; en caso contrario se verifica la invariante fiscal "
              + "valorTotalSinIVA + ivaTotal = valorTotalConIVA.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Gasto registrado"),
    @ApiResponse(responseCode = "400", description = "Datos malformados", content = @io.swagger.v3.oas.annotations.media.Content()),
    @ApiResponse(responseCode = "401", description = "Token ausente o invalido", content = @io.swagger.v3.oas.annotations.media.Content()),
    @ApiResponse(responseCode = "422", description = "Los totales no cuadran", content = @io.swagger.v3.oas.annotations.media.Content())
  })
  GastoRestResponse create(CreateGastoRestRequest request);

  @Operation(summary = "Modificar un gasto existente")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Gasto actualizado"),
    @ApiResponse(responseCode = "404", description = "El gasto no existe", content = @io.swagger.v3.oas.annotations.media.Content()),
    @ApiResponse(responseCode = "422", description = "Los totales no cuadran", content = @io.swagger.v3.oas.annotations.media.Content())
  })
  GastoRestResponse update(
      @Parameter(description = "Identificador del gasto", example = "7") int id,
      UpdateGastoRestRequest request);

  @Operation(summary = "Eliminar un gasto")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Gasto eliminado"),
    @ApiResponse(responseCode = "404", description = "El gasto no existe", content = @io.swagger.v3.oas.annotations.media.Content())
  })
  void delete(@Parameter(description = "Identificador del gasto", example = "7") int id);

  @Operation(summary = "Consultar un gasto por identificador")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Gasto encontrado"),
    @ApiResponse(responseCode = "404", description = "El gasto no existe", content = @io.swagger.v3.oas.annotations.media.Content())
  })
  GastoRestResponse findById(
      @Parameter(description = "Identificador del gasto", example = "7") int id);

  @Operation(
      summary = "Listar gastos con filtros independientes",
      description =
          "Todos los parametros son opcionales y se combinan entre si. Cada consulta analitica "
              + "usa su propio juego de filtros, sin interferir con las demas.")
  @ApiResponse(responseCode = "200", description = "Listado de gastos")
  List<GastoRestResponse> findAll(
      @Parameter(description = "Fecha inicial inclusive", example = "2026-01-01") String desde,
      @Parameter(description = "Fecha final inclusive", example = "2026-12-31") String hasta,
      @Parameter(description = "Tarifa de IVA", example = "GENERAL") String tarifaIva,
      @Parameter(description = "Texto contenido en el lugar", example = "ferreteria") String lugar,
      @Parameter(description = "Identificador del responsable", example = "admin")
          String nombreUsuario,
      @Parameter(description = "Monto minimo con IVA", example = "50000") BigDecimal montoMinimo,
      @Parameter(description = "Monto maximo con IVA", example = "500000") BigDecimal montoMaximo,
      @Parameter(description = "Numero maximo de resultados", example = "10") Integer limite);
}
