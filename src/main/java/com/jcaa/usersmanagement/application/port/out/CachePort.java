package com.jcaa.usersmanagement.application.port.out;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Puerto de salida de cache (paso 21 de la guia: adaptadores de infraestructura).
 *
 * <p>El dominio y los casos de uso no saben si detras hay memoria, Redis o un
 * fichero: solo conocen este contrato. Las proyecciones de lectura lo usan para
 * no recalcular agregaciones en cada peticion.
 */
public interface CachePort {

  <T> Optional<T> get(String key, Class<T> type);

  <T> void put(String key, T value, Duration timeToLive);

  /** Devuelve el valor cacheado o lo calcula, lo guarda y lo devuelve. */
  <T> T getOrCompute(String key, Class<T> type, Duration timeToLive, Supplier<T> supplier);

  void evict(String key);

  void evictAll();
}
