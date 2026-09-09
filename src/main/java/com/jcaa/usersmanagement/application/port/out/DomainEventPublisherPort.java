package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.event.DomainEvent;

/**
 * Puerto de salida de publicacion de eventos de dominio (paso 21: colas).
 *
 * <p>Permite que los casos de uso notifiquen hechos del negocio sin acoplarse
 * al transporte. Hoy la implementacion es una cola en memoria; manana podria
 * ser RabbitMQ o Kafka sin tocar una sola linea de la capa de aplicacion.
 */
public interface DomainEventPublisherPort {

  void publish(DomainEvent event);
}
