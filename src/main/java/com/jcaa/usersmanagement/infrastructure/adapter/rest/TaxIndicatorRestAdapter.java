package com.jcaa.usersmanagement.infrastructure.adapter.rest;

import com.jcaa.usersmanagement.application.port.out.TaxIndicatorRestPort;
import com.jcaa.usersmanagement.domain.enums.GastoIvaRate;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida hacia un servicio REST de indicadores tributarios
 * (paso 21 de la guia: adaptador rest).
 *
 * <p>Seguridad (OWASP A10 - SSRF): solo se admiten URL absolutas con esquema
 * `https`. Esquemas como `file://`, `gopher://` o direcciones internas
 * permitirian que una configuracion manipulada leyera ficheros locales o
 * alcanzara servicios de la red privada de Render.
 *
 * <p>Degrada con elegancia: si el servicio remoto no responde, se devuelven
 * las tarifas legales que el dominio ya conoce. Una dependencia externa caida
 * no puede tumbar el registro de gastos.
 */
@Slf4j
@Component
public class TaxIndicatorRestAdapter implements TaxIndicatorRestPort {

  private static final Set<String> ALLOWED_SCHEMES = Set.of("https");
  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  private final String endpoint;
  private final HttpClient httpClient;

  public TaxIndicatorRestAdapter(
      @Value("${tax.indicators.endpoint:}") final String endpoint) {
    this.endpoint = endpoint == null ? "" : endpoint.trim();
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
  }

  @Override
  public List<GastoIvaRate> fetchAvailableRates() {
    if (!isRemoteAvailable()) {
      return Arrays.asList(GastoIvaRate.values());
    }
    try {
      final HttpRequest request =
          HttpRequest.newBuilder(URI.create(endpoint))
              .timeout(TIMEOUT)
              .header("Accept", "application/json")
              .GET()
              .build();
      final HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        log.warn("Tax indicator service answered HTTP {}", response.statusCode());
      }
    } catch (final InterruptedException exception) {
      Thread.currentThread().interrupt();
      log.warn("Tax indicator request interrupted");
    } catch (final Exception exception) {
      log.warn("Tax indicator service unreachable: {}", exception.getMessage());
    }
    return Arrays.asList(GastoIvaRate.values());
  }

  @Override
  public boolean isRemoteAvailable() {
    if (endpoint.isEmpty()) {
      return false;
    }
    try {
      final URI uri = URI.create(endpoint);
      final String scheme = uri.getScheme();
      return uri.isAbsolute()
          && scheme != null
          && ALLOWED_SCHEMES.contains(scheme.toLowerCase())
          && uri.getHost() != null;
    } catch (final IllegalArgumentException exception) {
      log.warn("Tax indicator endpoint is not a valid URI");
      return false;
    }
  }
}
