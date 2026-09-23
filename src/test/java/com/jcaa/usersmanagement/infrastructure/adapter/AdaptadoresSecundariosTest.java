package com.jcaa.usersmanagement.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.event.GastoDeletedDomainEvent;
import com.jcaa.usersmanagement.infrastructure.adapter.cache.InMemoryCacheAdapter;
import com.jcaa.usersmanagement.infrastructure.adapter.queue.InMemoryDomainEventPublisher;
import com.jcaa.usersmanagement.infrastructure.adapter.rest.TaxIndicatorRestAdapter;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdaptadoresSecundariosTest {

  @Test
  @DisplayName("Cache: una entrada expirada no se devuelve y se purga del almacen")
  void cacheExpiraEntradas() throws InterruptedException {
    final InMemoryCacheAdapter cache = new InMemoryCacheAdapter();
    cache.put("totales", "123", Duration.ofMillis(60));
    assertTrue(cache.get("totales", String.class).isPresent());
    Thread.sleep(120);
    assertFalse(cache.get("totales", String.class).isPresent());
    assertEquals(0, cache.size());
  }

  @Test
  @DisplayName("Cache: getOrCompute calcula una sola vez y reutiliza el valor")
  void cacheCalculaUnaSolaVez() {
    final InMemoryCacheAdapter cache = new InMemoryCacheAdapter();
    final int[] invocaciones = {0};
    for (int i = 0; i < 3; i++) {
      cache.getOrCompute("k", String.class, Duration.ofMinutes(1), () -> {
        invocaciones[0]++;
        return "valor";
      });
    }
    assertEquals(1, invocaciones[0]);
  }

  @Test
  @DisplayName("Cola: retiene los eventos publicados y respeta el tope de memoria")
  void colaRetieneEventos() {
    final InMemoryDomainEventPublisher cola = new InMemoryDomainEventPublisher();
    for (int i = 1; i <= 520; i++) {
      cola.publish(new GastoDeletedDomainEvent(GastoId.of(i)));
    }
    assertEquals(500, cola.retainedEvents().size());
    assertEquals("gasto.deleted", cola.retainedEvents().get(0).getEventName());
  }

  @Test
  @DisplayName("REST anti-SSRF: rechaza file://, http:// y rutas relativas; acepta https")
  void restBloqueaEsquemasPeligrosos() {
    assertFalse(new TaxIndicatorRestAdapter("file:///C:/Windows/win.ini").isRemoteAvailable());
    assertFalse(new TaxIndicatorRestAdapter("http://169.254.169.254/latest/meta-data").isRemoteAvailable());
    assertFalse(new TaxIndicatorRestAdapter("/etc/passwd").isRemoteAvailable());
    assertFalse(new TaxIndicatorRestAdapter("").isRemoteAvailable());
    assertTrue(new TaxIndicatorRestAdapter("https://api.ejemplo.com/iva").isRemoteAvailable());
  }

  @Test
  @DisplayName("REST: sin servicio remoto devuelve las tarifas legales del dominio")
  void restDegradaConElegancia() {
    assertEquals(3, new TaxIndicatorRestAdapter("").fetchAvailableRates().size());
  }
}
