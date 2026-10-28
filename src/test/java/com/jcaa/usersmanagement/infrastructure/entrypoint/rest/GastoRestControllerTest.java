package com.jcaa.usersmanagement.infrastructure.entrypoint.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jcaa.usersmanagement.application.port.in.CreateGastoUseCase;
import com.jcaa.usersmanagement.application.port.in.DeleteGastoUseCase;
import com.jcaa.usersmanagement.application.port.in.GetAllGastosUseCase;
import com.jcaa.usersmanagement.application.port.in.GetGastoByIdUseCase;
import com.jcaa.usersmanagement.application.port.in.UpdateGastoUseCase;
import com.jcaa.usersmanagement.domain.enums.GastoIvaRate;
import com.jcaa.usersmanagement.domain.exception.GastoNotFoundException;
import com.jcaa.usersmanagement.domain.exception.InvalidGastoIvaConsistencyException;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.GastoAmount;
import com.jcaa.usersmanagement.domain.valueobject.GastoDate;
import com.jcaa.usersmanagement.domain.valueobject.GastoDescription;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.valueobject.GastoOwner;
import com.jcaa.usersmanagement.domain.valueobject.GastoPlace;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller.GastoRestController;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.mapper.GastoRestMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.advice.GastoExceptionHandler;

/**
 * Prueba del entrypoint REST sin contexto de Spring.
 *
 * <p>Se usa standaloneSetup a proposito: el slice @WebMvcTest arrastra la
 * configuracion de seguridad y sus colaboradores, y la integracion continua no
 * dispone de base de datos. Asi la prueba es hermetica, rapida y ejecutable en
 * el runner de GitHub Actions.
 */
class GastoRestControllerTest {

  private MockMvc mockMvc;

  private CreateGastoUseCase createGastoUseCase;
  private UpdateGastoUseCase updateGastoUseCase;
  private DeleteGastoUseCase deleteGastoUseCase;
  private GetGastoByIdUseCase getGastoByIdUseCase;
  private GetAllGastosUseCase getAllGastosUseCase;

  @BeforeEach
  void preparar() {
    createGastoUseCase = Mockito.mock(CreateGastoUseCase.class);
    updateGastoUseCase = Mockito.mock(UpdateGastoUseCase.class);
    deleteGastoUseCase = Mockito.mock(DeleteGastoUseCase.class);
    getGastoByIdUseCase = Mockito.mock(GetGastoByIdUseCase.class);
    getAllGastosUseCase = Mockito.mock(GetAllGastosUseCase.class);
    final GastoRestController controlador =
        new GastoRestController(
            createGastoUseCase,
            updateGastoUseCase,
            deleteGastoUseCase,
            getGastoByIdUseCase,
            getAllGastosUseCase,
            new GastoRestMapper());
    mockMvc =
        MockMvcBuilders.standaloneSetup(controlador)
            .setControllerAdvice(new GastoExceptionHandler())
            .build();
  }

  private static GastoModel gastoEjemplo() {
    return GastoModel.reconstitute(
        GastoId.of(7),
        GastoDate.fromString("2026-05-10"),
        GastoAmount.of(new BigDecimal("100000")),
        GastoAmount.of(new BigDecimal("19000")),
        GastoAmount.of(new BigDecimal("119000")),
        GastoOwner.of("admin"),
        GastoPlace.of("Ferreteria Norte"),
        GastoDescription.of("Herramientas"));
  }

  @Test
  @DisplayName("POST /api/gastos responde 201 con el gasto creado")
  void creaGasto() throws Exception {
    when(createGastoUseCase.execute(any())).thenReturn(gastoEjemplo());

    mockMvc
        .perform(
            post("/api/gastos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fecha\":\"2026-05-10\",\"valorTotalSinIVA\":100000,"
                    + "\"nombreUsuario\":\"admin\",\"lugar\":\"Ferreteria Norte\","
                    + "\"tarifaIva\":\"GENERAL\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.valorTotalConIVA").value(119000.00))
        .andExpect(jsonPath("$.tarifaEfectiva").value(19.00));
  }

  @Test
  @DisplayName("POST con fecha malformada responde 400 sin alcanzar el caso de uso")
  void rechazaFechaMalformada() throws Exception {
    mockMvc
        .perform(
            post("/api/gastos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fecha\":\"10/05/2026\",\"valorTotalSinIVA\":100,"
                    + "\"nombreUsuario\":\"admin\",\"lugar\":\"Tienda\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Totales incoherentes responden 422 Unprocessable Entity")
  void totalesIncoherentesDan422() throws Exception {
    when(createGastoUseCase.execute(any()))
        .thenThrow(
            InvalidGastoIvaConsistencyException.becauseTotalsDoNotMatch(
                new BigDecimal("100000"), new BigDecimal("19000"),
                new BigDecimal("500000"), GastoModel.TOLERANCE));

    mockMvc
        .perform(
            post("/api/gastos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fecha\":\"2026-05-10\",\"valorTotalSinIVA\":100000,"
                    + "\"ivaTotal\":19000,\"valorTotalConIVA\":500000,"
                    + "\"nombreUsuario\":\"admin\",\"lugar\":\"Tienda\"}"))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  @DisplayName("GET de un gasto inexistente responde 404")
  void gastoInexistenteDa404() throws Exception {
    when(getGastoByIdUseCase.execute(any()))
        .thenThrow(GastoNotFoundException.becauseIdDoesNotExist(999));

    mockMvc.perform(get("/api/gastos/999")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/gastos admite filtros independientes y devuelve la proyeccion")
  void listaConFiltros() throws Exception {
    when(getAllGastosUseCase.execute(any())).thenReturn(List.of(gastoEjemplo()));

    mockMvc
        .perform(get("/api/gastos")
            .param("desde", "2026-01-01").param("nombreUsuario", "admin")
            .param("tarifaIva", GastoIvaRate.GENERAL.name()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].nombreUsuario").value("admin"))
        .andExpect(jsonPath("$[0].lugar").value("Ferreteria Norte"));
  }

  @Test
  @DisplayName("DELETE responde 204 No Content")
  void eliminaGasto() throws Exception {
    mockMvc.perform(delete("/api/gastos/7")).andExpect(status().isNoContent());
  }
}
