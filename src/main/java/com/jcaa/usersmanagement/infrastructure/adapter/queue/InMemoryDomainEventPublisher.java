package com.jcaa.usersmanagement.infrastructure.adapter.queue;

import com.jcaa.usersmanagement.application.port.out.DomainEventPublisherPort;
import com.jcaa.usersmanagement.domain.event.DomainEvent;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adaptador de cola de eventos de dominio en memoria (paso 21 de la guia).
 *
 * <p>Registra cada hecho del negocio y mantiene una ventana de los ultimos
 * eventos para poder auditarlos. La cola esta acotada: un publicador sin
 * consumidor que crece sin limite acaba agotando la memoria del contenedor,
 * que en Render Free son 512 MB.
 */
@Slf4j
@Component
public class InMemoryDomainEventPublisher implements DomainEventPublisherPort {

  private static final int MAXIMUM_RETAINED_EVENTS = 500;

  private final Queue<DomainEvent> publishedEvents = new ConcurrentLinkedQueue<>();

  @Override
  public void publish(final DomainEvent event) {
    publishedEvents.add(event);
    while (publishedEvents.size() > MAXIMUM_RETAINED_EVENTS) {
      publishedEvents.poll();
    }
    log.info("Domain event published: {} payload={}", event.getEventName(), event.payload());
  }

  /** Instantanea de los eventos retenidos, del mas antiguo al mas reciente. */
  public List<DomainEvent> retainedEvents() {
    return List.copyOf(publishedEvents);
  }

  public void clear() {
    publishedEvents.clear();
  }
}
