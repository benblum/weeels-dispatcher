package org.weeels.dispatcher.simulator;

import static java.lang.System.getenv;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.weeels.dispatcher.simulator.message.NYCTParatransitRideRequestMessage;

//@Configuration
public class RabbitConfiguration {

	public static final String requestExchangeName = "simulator_to_dispatcher";
	public static final String responseExchangeName = "dispatcher_to_simulator";
	public static final String requestQueueName = "request";
	public static final String responseQueueName = "response";
	
	@Bean
	public ConnectionFactory connectionFactory() {
		final URI ampqUrl;
        try {
            ampqUrl = new URI(getEnvOrFake("CLOUDAMQP_URL"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        final CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setUsername(ampqUrl.getUserInfo().split(":")[0]);
        factory.setPassword(ampqUrl.getUserInfo().split(":")[1]);
        factory.setHost(ampqUrl.getHost());
        factory.setPort(ampqUrl.getPort());
        factory.setVirtualHost(ampqUrl.getPath().substring(1));
        
        
		return factory;
	}
	
    private static String getEnvOrFake(String name) {
        String env = getenv(name);
        if (env == null) {
        	env = "amqp://guest:guest@localhost//";
        }
        return env;
    }
	
	@Bean
	public AmqpAdmin amqpAdmin() {
		AmqpAdmin admin = new RabbitAdmin(connectionFactory());
		
		admin.declareExchange(requestExchange());
		admin.declareExchange(responseExchange());
		admin.declareQueue(requestQueue());
		admin.declareQueue(responseQueue());
		admin.declareBinding(BindingBuilder.bind(requestQueue()).to(requestExchange()).withQueueName());
		admin.declareBinding(BindingBuilder.bind(responseQueue()).to(responseExchange()).withQueueName());
		
		return admin;
	}

	@Bean
	public RabbitTemplate requestTemplate() {
		RabbitTemplate template = new RabbitTemplate(connectionFactory());
		//The routing key is set to the name of the queue by the broker for the default exchange.
		template.setExchange(requestExchangeName);
		template.setRoutingKey(requestQueueName);
		template.setQueue(requestQueueName);
		template.setMessageConverter(jsonMessageConverter());
		return template;
	}
	
	@Bean
	public RabbitTemplate responseTemplate() {
		RabbitTemplate template = new RabbitTemplate(connectionFactory());
		//The routing key is set to the name of the queue by the broker for the default exchange.
		template.setExchange(responseExchangeName);
		template.setRoutingKey(responseQueueName);
		template.setQueue(responseQueueName);
		template.setMessageConverter(jsonMessageConverter());
		return template;
	}
	
	@Bean
	public MessageConverter jsonMessageConverter() {
		JsonMessageConverter converter = new JsonMessageConverter();
		converter.setClassMapper(typeMapper());
		return converter;
	}

	@Bean
	public Queue requestQueue() {
		return new Queue(requestQueueName, false, false, false, null);
	}
	
	@Bean
	public Queue responseQueue() {
		return new Queue(responseQueueName, false, false, false, null);
	}
	
	@Bean
	public DirectExchange responseExchange() {
		return new DirectExchange(responseExchangeName, false, true, null);
	}
	
	@Bean
	public DirectExchange requestExchange() {
		return new DirectExchange(requestExchangeName, false, true, null);
	}
	
	@Bean
	public RabbitMessageHandler messageHandler() {
		return new RabbitMessageHandler();
	}
	
	@Bean
	public SimpleMessageListenerContainer listenerContainer() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory());
		container.setQueueNames(RabbitConfiguration.requestQueueName);
		container.setMessageListener(new MessageListenerAdapter(messageHandler(), jsonMessageConverter()));
		return container;
	}
	
	@Bean
	public DefaultClassMapper typeMapper() {
		DefaultClassMapper typeMapper = new DefaultClassMapper();
		HashMap<String, Class<?>> idClassMapping = new HashMap<String, Class<?>>();
		idClassMapping.put("nyct_para_request", NYCTParatransitRideRequestMessage.class);
		typeMapper.setIdClassMapping(idClassMapping);
		return typeMapper;
	}
}
