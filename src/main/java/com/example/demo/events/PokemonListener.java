package com.example.demo.events;

import java.util.List;
import java.util.Map;

import com.example.demo.events.dto.PokemonEvent;
import com.example.demo.service.ErrorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PokemonListener {

	private static final Logger log = LoggerFactory.getLogger(PokemonListener.class);

	private final ErrorService errorService;

	private final RabbitTemplate rabbitTemplate;

	private final String queue;

	private final String exchange;

	private final String parkingLot;

	private final int maxRetries;

	public PokemonListener(ErrorService errorService, RabbitTemplate rabbitTemplate,
			@Value("${pokemon.rabbitmq.queue}") String queue,
			@Value("${pokemon.rabbitmq.exchange}") String exchange,
			@Value("${pokemon.rabbitmq.parking-lot}") String parkingLot,
			@Value("${pokemon.rabbitmq.retry.max-retries}") int maxRetries) {
		this.errorService = errorService;
		this.rabbitTemplate = rabbitTemplate;
		this.queue = queue;
		this.exchange = exchange;
		this.parkingLot = parkingLot;
		this.maxRetries = maxRetries;
	}

	@RabbitListener(queues = "${pokemon.rabbitmq.queue}")
	public void receber(PokemonEvent mensagem, Message message) {
		long retries = retries(message);
		log.info("Processando {} (retry {}/{})", mensagem, retries, maxRetries);
		try {
			errorService.onError();
		}
		catch (Exception ex) {
			if (retries < maxRetries) {
				throw new AmqpRejectAndDontRequeueException(ex);
			}
			log.error("Esgotou {} retries para {}; enviando para '{}'", maxRetries, mensagem, parkingLot);
			message.getMessageProperties().setHeader("x-exception-message", ex.getMessage());
			rabbitTemplate.send(exchange, parkingLot, message);
		}
	}

	private long retries(Message message) {
		List<Map<String, ?>> xDeath = message.getMessageProperties().getXDeathHeader();
		if (xDeath == null) {
			return 0;
		}
		return xDeath.stream()
			.filter(death -> queue.equals(death.get("queue")) && "rejected".equals(String.valueOf(death.get("reason"))))
			.map(death -> death.get("count"))
			.filter(Number.class::isInstance)
			.mapToLong(count -> ((Number) count).longValue())
			.findFirst()
			.orElse(0);
	}
}