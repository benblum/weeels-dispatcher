package org.weeels.dispatcher.lms;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.weeels.dispatcher.config.RabbitConfiguration;
import org.weeels.dispatcher.domain.Hub;
import org.weeels.dispatcher.domain.Location;
import org.weeels.dispatcher.lms.message.*;
import org.weeels.dispatcher.repository.HubRepository;

@Configuration
public class LMSRabbitConfiguration extends RabbitConfiguration {

	@Autowired
	private HubRepository hubRepository;
	
	public static final String requestExchangeName = "node_to_java";
	public static final String responseExchangeName = "java_to_node";
	public static final String requestQueueName = "request";
	public static final String responseQueueName = "response";

	@Bean
	public RabbitTemplate lmsRequestTemplate() {
		RabbitTemplate template = new RabbitTemplate(connectionFactory());
		//The routing key is set to the name of the queue by the broker for the default exchange.
		template.setExchange(requestExchangeName);
		template.setRoutingKey(requestQueueName);
		template.setQueue(requestQueueName);
		template.setMessageConverter(lmsJsonMessageConverter());
		return template;
	}
	
	@Bean
	public RabbitTemplate lmsResponseTemplate() {
		RabbitTemplate template = new RabbitTemplate(connectionFactory());
		//The routing key is set to the name of the queue by the broker for the default exchange.
		template.setExchange(responseExchangeName);
		template.setRoutingKey(responseQueueName);
		template.setQueue(responseQueueName);
		template.setMessageConverter(lmsJsonMessageConverter());
		return template;
	}	
	
	@Bean
	public MessageConverter lmsJsonMessageConverter() {
		JsonMessageConverter converter = new JsonMessageConverter();
		converter.setClassMapper(lmsTypeMapper());
		return converter;
	}

	@Bean
	public Queue lmsRequestQueue() {
		return new Queue(requestQueueName, false, false, false, null);
	}
	
	@Bean
	public Queue lmsResponseQueue() {
		return new Queue(responseQueueName, false, false, false, null);
	}
	
	@Bean
	public DirectExchange lmsResponseExchange() {
		return new DirectExchange(responseExchangeName, false, true, null);
	}
	
	@Bean
	public DirectExchange lmsRequestExchange() {
		return new DirectExchange(requestExchangeName, false, true, null);
	}
	
	@Bean
	public LMSRabbitMessageHandler lmsMessageHandler() {
		return new LMSRabbitMessageHandler();
	}
	
	@Bean
	public SimpleMessageListenerContainer lmsListenerContainer() {
		AmqpAdmin admin = amqpAdmin();
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		
		admin.declareExchange(lmsRequestExchange());
		admin.declareExchange(lmsResponseExchange());
		admin.declareQueue(lmsRequestQueue());
		admin.declareQueue(lmsResponseQueue());
		admin.declareBinding(BindingBuilder.bind(lmsRequestQueue()).to(lmsRequestExchange()).withQueueName());
		admin.declareBinding(BindingBuilder.bind(lmsResponseQueue()).to(lmsResponseExchange()).withQueueName());

		container.setConnectionFactory(connectionFactory());
		container.setQueueNames(LMSRabbitConfiguration.requestQueueName);
		container.setMessageListener(new MessageListenerAdapter(lmsMessageHandler(), lmsJsonMessageConverter()));
		return container;
	}
	
	@Bean
	public DefaultClassMapper lmsTypeMapper() {
		DefaultClassMapper typeMapper = new DefaultClassMapper();
		HashMap<String, Class<?>> idClassMapping = new HashMap<String, Class<?>>();
		idClassMapping.put("request", RideRequestMessage.class);
		idClassMapping.put("match", MatchMessage.class);
		idClassMapping.put("expire_request", ExpireRequestMessage.class);
		idClassMapping.put("expire_match", ExpireMatchMessage.class);
		idClassMapping.put("state_request", StateRequestMessage.class);
		idClassMapping.put("state", StateMessage.class);
		idClassMapping.put("match_request", MatchRequestMessage.class);
		idClassMapping.put("passenger", RideRequestResponseMessage.class);
		typeMapper.setIdClassMapping(idClassMapping);
		return typeMapper;
	}
	
	@Bean
	public Hub laGuardia() {
		Hub	laGuardia = hubRepository.findOneByName("LaGuardia");
		if(laGuardia == null)
			laGuardia = hubRepository.save(new Hub("LaGuardia", "LaGuardia", new Location(-73.8737, 40.7721)));
		return laGuardia;
	}
}
