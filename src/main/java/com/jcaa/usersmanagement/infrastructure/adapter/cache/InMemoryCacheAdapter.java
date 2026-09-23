package com.jcaa.usersmanagement.infrastructure.adapter.cache;

import com.jcaa.usersmanagement.application.port.out.CachePort;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adaptador de cache en memoria con expiracion por entrada (paso 21 de la guia).
 *
 * <p>Implementacion deliberadamente simple y sin dependencias externas: el
 * valor arquitectonico esta en que los casos de uso dependan del puerto
 * {@link CachePort} y no de esta clase. Sustituirla por Redis no exigiria
 * tocar la capa de aplicacion.
 *
 * <p>Usa {@link ConcurrentHashMap} porque Tomcat atiende peticiones en
 * paralelo y un mapa normal corromperia su estado interno bajo concurrencia.
 */
@Slf4j
@Component
public class InMemoryCacheAdapter implements CachePort {

  private record Entry(Object value, Instant expiresAt) {
    boolean isExpired() {
      return Instant.now().isAfter(expiresAt);
    }
  }

  private final Map<String, Entry> store = new ConcurrentHashMap<>();

  @Override
  public <T> Optional<T> get(final String key, final Class<T> type) {
    final Entry entry = store.get(key);
    if (entry == null) {
      return Optional.empty();
    }
    if (entry.isExpired()) {
      store.remove(key);
      return Optional.empty();
    }
    return Optional.of(type.cast(entry.value()));
  }

  @Override
  public <T> void put(final String key, final T value, final Duration timeToLive) {
    store.put(key, new Entry(value, Instant.now().plus(timeToLive)));
  }

  @Override
  public <T> T getOrCompute(
      final String key, final Class<T> type, final Duration timeToLive, final Supplier<T> supplier) {
    return get(key, type)
        .orElseGet(
            () -> {
              final T computed = supplier.get();
              put(key, computed, timeToLive);
              return computed;
            });
  }

  @Override
  public void evict(final String key) {
    store.remove(key);
  }

  @Override
  public void evictAll() {
    store.clear();
  }

  /** Numero de entradas vivas; util para pruebas y diagnostico. */
  public int size() {
    store.entrySet().removeIf(entry -> entry.getValue().isExpired());
    return store.size();
  }
}
