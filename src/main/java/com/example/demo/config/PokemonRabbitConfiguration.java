package com.example.demo.config;

import java.time.Duration;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PokemonRabbitConfiguration {

    @Value("${pokemon.rabbitmq.exchange}")
    private String exchange;

    @Value("${pokemon.rabbitmq.queue}")
    private String routingKey;

    @Value("${pokemon.rabbitmq.dead-letter}")
    private String deadLetter;

    @Value("${pokemon.rabbitmq.parking-lot}")
    private String parkingLot;

    @Value("${pokemon.rabbitmq.retry.delay}")
    private Duration retryDelay;

    @Bean
    Queue queue() {
        return QueueBuilder.durable(routingKey)
                .deadLetterExchange(exchange)
                .deadLetterRoutingKey(deadLetter)
                .build();
    }

    @Bean
    Queue deadLetter() {
        return QueueBuilder.durable(deadLetter)
                .ttl((int) retryDelay.toMillis())
                .deadLetterExchange(exchange)
                .deadLetterRoutingKey(routingKey)
                .build();
    }

    @Bean
    Queue parkingLot() {
        return new Queue(parkingLot);
    }

    @Bean
    DirectExchange directExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    Binding bindingQueue() {
        return BindingBuilder.bind(queue()).to(directExchange()).with(routingKey);
    }

    @Bean
    Binding bindingDeadLetter() {
        return BindingBuilder.bind(deadLetter()).to(directExchange()).with(deadLetter);
    }

    @Bean
    Binding bindingParkingLot() {
        return BindingBuilder.bind(parkingLot()).to(directExchange()).with(parkingLot);
    }

}
