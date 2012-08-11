package org.weeels.dispatcher.nyctpara;

import java.util.HashMap;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.weeels.dispatcher.config.RabbitConfiguration;
import org.weeels.dispatcher.nyctpara.message.NYCTParatransitRideRequestMessage;

@Configuration
public class NYCTParatransitRabbitConfiguration extends RabbitConfiguration {

	public static final String exchangeName = "para_exchange";
	public static final String requestQueueName = "para_request";
	public static final String responseQueueName = "para_response";

	@Bean
	public RabbitTemplate paraRequestTemplate() {
		RabbitTemplate template = new RabbitTemplate(connectionFactory());
		//The routing key is set to the name of the queue by the broker for the default exchange.
		template.setExchange(exchangeName);
		template.setRoutingKey(requestQueueName);
		template.setQueue(requestQueueName);
		template.setMessageConverter(paraJsonMessageConverter());
		return template;
	}
	
	@Bean
	public RabbitTemplate paraResponseTemplate() {
		RabbitTemplate template = new RabbitTemplate(connectionFactory());
		//The routing key is set to the name of the queue by the broker for the default exchange.
		template.setExchange(exchangeName);
		template.setRoutingKey(responseQueueName);
		template.setQueue(responseQueueName);
		template.setMessageConverter(paraJsonMessageConverter());
		return template;
	}
	
	@Bean
	public MessageConverter paraJsonMessageConverter() {
		JsonMessageConverter converter = new JsonMessageConverter();
		converter.setClassMapper(paraTypeMapper());
		return converter;
	}

	@Bean
	public Queue paraRequestQueue() {
		return new Queue(requestQueueName, false, false, false, null);
	}
	
	@Bean
	public Queue paraResponseQueue() {
		return new Queue(responseQueueName, false, false, false, null);
	}
	
	@Bean
	public DirectExchange paraExchange() {
		return new DirectExchange(exchangeName, false, true, null);
	}
	
	
	@Bean
	public NYCTParaRabbitMessageHandler paraMessageHandler() {
		return new NYCTParaRabbitMessageHandler();
	}
	
	@Bean
	public SimpleMessageListenerContainer paraListenerContainer() {
		AmqpAdmin admin = amqpAdmin();
		
		admin.declareExchange(paraExchange());
		admin.declareQueue(paraRequestQueue());
		admin.declareQueue(paraResponseQueue());
		admin.declareBinding(BindingBuilder.bind(paraRequestQueue()).to(paraExchange()).withQueueName());
		admin.declareBinding(BindingBuilder.bind(paraResponseQueue()).to(paraExchange()).withQueueName());
		
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory());
		container.setQueueNames(NYCTParatransitRabbitConfiguration.requestQueueName);
		container.setMessageListener(new MessageListenerAdapter(paraMessageHandler(), paraJsonMessageConverter()));
		return container;
	}
	
	@Bean
	public DefaultClassMapper paraTypeMapper() {
		DefaultClassMapper typeMapper = new DefaultClassMapper();
		HashMap<String, Class<?>> idClassMapping = new HashMap<String, Class<?>>();
		idClassMapping.put("nyct_para_request", NYCTParatransitRideRequestMessage.class);
		typeMapper.setIdClassMapping(idClassMapping);
		return typeMapper;
	}
}
