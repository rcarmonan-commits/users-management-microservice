package com.jcaa.usersmanagement.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Limitador de intentos de autenticacion (OWASP A07: fallos de identificacion).
 *
 * <p>Sin esta barrera, un atacante puede probar contrasenas de forma ilimitada
 * contra `/api/auth/login`. El filtro aplica una ventana deslizante por
 * direccion de origen: superados los intentos permitidos dentro del periodo,
 * responde HTTP 429 sin llegar a consultar la base de datos.
 *
 * <p>Solo cuentan los intentos FALLIDOS: una sesion legitima que entra bien no
 * consume cupo, de modo que un usuario normal nunca se ve bloqueado.
 *
 * <p>El estado vive en memoria. En un despliegue con varias instancias habria
 * que moverlo a un almacen compartido; para un servicio unico en Render es
 * suficiente y no anade dependencias.
 */
@Slf4j
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

  private static final String LOGIN_PATH = "/api/auth/login";
  private static final int MAXIMUM_ATTEMPTS = 5;
  private static final Duration WINDOW = Duration.ofMinutes(15);
  private static final String FORWARDED_HEADER = "X-Forwarded-For";

  private final Map<String, Deque<Instant>> failedAttempts = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain)
      throws ServletException, IOException {

    if (!isLoginAttempt(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    final String client = resolveClient(request);
    if (isBlocked(client)) {
      log.warn("Rate limit exceeded for login attempts from {}", client);
      rejectWithTooManyRequests(response);
      return;
    }

    filterChain.doFilter(request, response);

    if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
      recordFailure(client);
    } else if (response.getStatus() < HttpStatus.BAD_REQUEST.value()) {
      failedAttempts.remove(client);
    }
  }

  private boolean isLoginAttempt(final HttpServletRequest request) {
    return LOGIN_PATH.equals(request.getRequestURI())
        && HttpStatus.OK.name() != null
        && "POST".equalsIgnoreCase(request.getMethod());
  }

  /**
   * Resuelve el origen. Detras del balanceador de Render la direccion directa
   * es siempre la del proxy, por lo que se toma la primera entrada de
   * X-Forwarded-For, que es la del cliente real.
   */
  private String resolveClient(final HttpServletRequest request) {
    final String forwarded = request.getHeader(FORWARDED_HEADER);
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
  }

  private boolean isBlocked(final String client) {
    final Deque<Instant> attempts = failedAttempts.get(client);
    if (attempts == null) {
      return false;
    }
    purgeExpired(attempts);
    return attempts.size() >= MAXIMUM_ATTEMPTS;
  }

  private void recordFailure(final String client) {
    final Deque<Instant> attempts =
        failedAttempts.computeIfAbsent(client, ignored -> new ArrayDeque<>());
    synchronized (attempts) {
      purgeExpired(attempts);
      attempts.addLast(Instant.now());
    }
  }

  private void purgeExpired(final Deque<Instant> attempts) {
    final Instant threshold = Instant.now().minus(WINDOW);
    while (!attempts.isEmpty() && attempts.peekFirst().isBefore(threshold)) {
      attempts.pollFirst();
    }
  }

  private void rejectWithTooManyRequests(final HttpServletResponse response) throws IOException {
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response
        .getWriter()
        .write(
            "{\"status\":429,\"message\":\"Too many failed login attempts. "
                + "Please wait before trying again.\"}");
  }

  /** Expuesto para las pruebas: limpia el estado acumulado. */
  void reset() {
    failedAttempts.clear();
  }
}
