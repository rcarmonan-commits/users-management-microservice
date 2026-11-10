package com.jcaa.usersmanagement.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Verifica la barrera contra fuerza bruta en el inicio de sesion (OWASP A07).
 */
class LoginRateLimitFilterTest {

  private static final String IP = "203.0.113.45";

  private LoginRateLimitFilter filtro;

  @BeforeEach
  void preparar() {
    filtro = new LoginRateLimitFilter();
    filtro.reset();
  }

  private MockHttpServletRequest loginDesde(final String ip) {
    final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
    request.setRemoteAddr(ip);
    return request;
  }

  /** Cadena que simula una respuesta con el estado indicado. */
  private FilterChain cadenaQueResponde(final int status) {
    return (request, response) -> ((MockHttpServletResponse) response).setStatus(status);
  }

  private int intentar(final String ip, final int statusSimulado)
      throws ServletException, IOException {
    final MockHttpServletResponse response = new MockHttpServletResponse();
    filtro.doFilter(loginDesde(ip), response, cadenaQueResponde(statusSimulado));
    return response.getStatus();
  }

  @Test
  @DisplayName("Permite los cinco primeros intentos fallidos y bloquea el sexto con 429")
  void bloqueaTrasCincoFallos() throws Exception {
    for (int i = 1; i <= 5; i++) {
      assertEquals(
          HttpStatus.UNAUTHORIZED.value(),
          intentar(IP, HttpStatus.UNAUTHORIZED.value()),
          "el intento " + i + " aun debe permitirse");
    }

    assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), intentar(IP, HttpStatus.OK.value()));
  }

  @Test
  @DisplayName("El bloqueo es por origen: otra direccion no hereda el castigo")
  void bloqueoAisladoPorOrigen() throws Exception {
    for (int i = 0; i < 5; i++) {
      intentar(IP, HttpStatus.UNAUTHORIZED.value());
    }

    assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), intentar(IP, HttpStatus.OK.value()));
    assertEquals(HttpStatus.OK.value(), intentar("198.51.100.7", HttpStatus.OK.value()));
  }

  @Test
  @DisplayName("Un inicio de sesion correcto limpia el contador acumulado")
  void elExitoLimpiaElContador() throws Exception {
    for (int i = 0; i < 4; i++) {
      intentar(IP, HttpStatus.UNAUTHORIZED.value());
    }

    assertEquals(HttpStatus.OK.value(), intentar(IP, HttpStatus.OK.value()));

    for (int i = 0; i < 5; i++) {
      assertEquals(HttpStatus.UNAUTHORIZED.value(), intentar(IP, HttpStatus.UNAUTHORIZED.value()));
    }
  }

  @Test
  @DisplayName("Las rutas distintas del login no se limitan")
  void noLimitaOtrasRutas() throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/gastos");
    request.setRemoteAddr(IP);
    for (int i = 0; i < 20; i++) {
      final MockHttpServletResponse response = new MockHttpServletResponse();
      filtro.doFilter(request, response, cadenaQueResponde(HttpStatus.OK.value()));
      assertEquals(HttpStatus.OK.value(), response.getStatus());
    }
  }

  @Test
  @DisplayName("Detras del proxy de Render se usa la primera IP de X-Forwarded-For")
  void respetaXForwardedFor() throws Exception {
    for (int i = 0; i < 5; i++) {
      final MockHttpServletRequest request = loginDesde("10.0.0.1");
      request.addHeader("X-Forwarded-For", "198.51.100.33, 10.0.0.1");
      final MockHttpServletResponse response = new MockHttpServletResponse();
      filtro.doFilter(request, response, cadenaQueResponde(HttpStatus.UNAUTHORIZED.value()));
    }

    final MockHttpServletRequest bloqueada = loginDesde("10.0.0.1");
    bloqueada.addHeader("X-Forwarded-For", "198.51.100.33, 10.0.0.1");
    final MockHttpServletResponse response = new MockHttpServletResponse();
    filtro.doFilter(bloqueada, response, cadenaQueResponde(HttpStatus.OK.value()));

    assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.getStatus());
    assertTrue(response.getContentAsString().contains("Too many failed login attempts"));
  }
}
