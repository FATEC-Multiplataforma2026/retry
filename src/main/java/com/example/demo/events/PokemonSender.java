package com.example.demo.events;

import com.example.demo.entity.Pokemon;
import com.example.demo.events.dto.PokemonEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PokemonSender {
    private static final Logger log = LoggerFactory.getLogger(PokemonSender.class);

    private final RabbitTemplate rabbitTemplate;

    private final String queue;
    private final String exchange;

    public PokemonSender(
            RabbitTemplate rabbitTemplate,
            @Value("${pokemon.rabbitmq.queue}") String queue,
            @Value("${pokemon.rabbitmq.exchange}") String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.queue = queue;
        this.exchange = exchange;
    }

    public void enviar(Pokemon pokemon) {
        try {
            PokemonEvent mensagem = new PokemonEvent(pokemon.nome(), pokemon.height());
            rabbitTemplate.convertAndSend(exchange, queue, mensagem);
            log.info("Mensagem enviada para '{}' (routing key '{}'): {}", exchange, queue, mensagem);
        } catch (Exception ex) {
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

}
