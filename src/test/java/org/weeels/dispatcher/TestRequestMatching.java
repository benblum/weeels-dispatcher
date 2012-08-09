package org.weeels.dispatcher;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.RideProposal;
import org.weeels.dispatcher.domain.RideRequest.LuggageSize;
import org.weeels.dispatcher.lms.RabbitConfiguration;
import org.weeels.dispatcher.lms.message.*;
import org.weeels.dispatcher.repository.RideBookingRepository;
import org.weeels.dispatcher.repository.RideProposalRepository;
import org.weeels.dispatcher.repository.RideRequestRepository;
import org.weeels.dispatcher.repository.RiderRepository;

@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
public class TestRequestMatching {
	@Autowired
	private RabbitTemplate requestTemplate;
	@Autowired
	private RabbitTemplate responseTemplate;
	@Autowired
	private AmqpAdmin amqpAdmin;
	@Autowired
	private RideRequestRepository rideRequestRepository;
	@Autowired
	private RiderRepository riderRepository;
	@Autowired
	private RideBookingRepository rideBookingRepository;
	@Autowired
	private RideProposalRepository rideProposalRepository;
	@Autowired
	private MongoOperations mongoTemplate;
	
	
	private Object poll(RabbitTemplate template) {
		Object ret = template.receiveAndConvert();
		while(ret == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ret = template.receiveAndConvert();
		}
		return ret;
	}
	
	public void test() {
		amqpAdmin.purgeQueue(RabbitConfiguration.requestQueueName, true);
		amqpAdmin.purgeQueue(RabbitConfiguration.responseQueueName, true);
		rideRequestRepository.deleteAll();
		riderRepository.deleteAll();
		rideBookingRepository.deleteAll();
		rideProposalRepository.deleteAll();
		RideRequestMessage msgA = new RideRequestMessage("LaGuardia","LaGuardia","337 Bedford Ave, Brooklyn NY 11211", 
				"337 bedford", 40.712346,-73.962665,40.712346,-73.962665, 1, 12334560, LuggageSize.low, "AA", "bob@gmail.com", "New York");
		requestTemplate.convertAndSend(msgA);
		RideRequestMessage msgB = new RideRequestMessage("LaGuardia","LaGuardia","2960 Broadway, New York NY 10013", 
				"Columbia University", 40.712346,-73.962665,40.80801,-73.963169, 1, 12334562, LuggageSize.low, "BB", "bob@gmail.com", "New York");
		RideRequestMessage msgC = new RideRequestMessage("LaGuardia","LaGuardia","496 Broadway, Brooklyn NY 11211", 
				"496 broadway, brooklyn", 40.712346,-73.962665,40.705595,-73.950734, 1, 12334561, LuggageSize.low, "CC", "bob@gmail.com", "New York");
		requestTemplate.convertAndSend(msgB);
		requestTemplate.convertAndSend(msgC);
		//MatchMessage match = (MatchMessage)poll(responseTemplate);		
		//System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		
	}
	
	@Test
	public void testAll() {
		amqpAdmin.purgeQueue(RabbitConfiguration.requestQueueName, true);
		amqpAdmin.purgeQueue(RabbitConfiguration.responseQueueName, true);
		rideRequestRepository.deleteAll();
		riderRepository.deleteAll();
		rideBookingRepository.deleteAll();
		rideProposalRepository.deleteAll();
		mongoTemplate.indexOps(RideBooking.class).ensureIndex(new GeospatialIndex("itinerary.destination"));
		mongoTemplate.indexOps(RideProposal.class).ensureIndex(new GeospatialIndex("itinerary.destination"));
		
		RideRequestMessage msgA = new RideRequestMessage("LaGuardia","LaGuardia","337 Bedford Ave, Brooklyn NY 11211", 
				"337 bedford", 40.712346,-73.962665,40.712346,-73.962665, 1, 12334560, LuggageSize.low, "AA", "bob@gmail.com", "New York");
		RideRequestMessage msgB = new RideRequestMessage("LaGuardia","LaGuardia","2960 Broadway, New York NY 10013", 
				"Columbia University", 40.712346,-73.962665,40.80801,-73.963169, 1, 12334562, LuggageSize.low, "BB", "bob@gmail.com", "New York");
		RideRequestMessage msgC = new RideRequestMessage("LaGuardia","LaGuardia","496 Broadway, Brooklyn NY 11211", 
				"496 broadway, brooklyn", 40.712346,-73.962665,40.705595,-73.950734, 1, 12334561, LuggageSize.low, "CC", "bob@gmail.com", "New York");
		RideRequestMessage msgD = new RideRequestMessage("LaGuardia","LaGuardia","496 Broadway, Brooklyn NY 11211", 
				"496 broadway, brooklyn", 40.712346,-73.962665,40.705595,-73.950734, 1, 12334561, LuggageSize.low, "DD", "bob@gmail.com", "New York");
		RideRequestMessage msgE = new RideRequestMessage("LaGuardia","LaGuardia","337 Bedford Ave, Brooklyn NY 11211", 
				"337 bedford", 40.712346,-73.962665,40.712346,-73.962665, 1, 12334560, LuggageSize.low, "EE", "bob@gmail.com", "New York");
		
		requestTemplate.convertAndSend(msgA);
		requestTemplate.convertAndSend(msgB);
		requestTemplate.convertAndSend(msgC);
		requestTemplate.convertAndSend(msgD);
		requestTemplate.convertAndSend(msgE);
		RideBooking booking;
		
		RideRequestResponseMessage response = (RideRequestResponseMessage)poll(responseTemplate);
		assert(response.getName().equals("AA"));
		response = (RideRequestResponseMessage)poll(responseTemplate);
		assert(response.getName().equals("BB"));
		response = (RideRequestResponseMessage)poll(responseTemplate);
		assert(response.getName().equals("CC"));
		MatchMessage match = (MatchMessage)poll(responseTemplate);
		booking = rideBookingRepository.findOne(match.getMatchId());
		System.out.println("Received match: "+booking.getRideRequests().get(0).getRider().getName() + 
				" and "+booking.getRideRequests().get(1).getRider().getName());	
		Assert.assertEquals("AA",booking.getRideRequests().get(0).getRider().getName());
		Assert.assertEquals("CC",booking.getRideRequests().get(1).getRider().getName());
		
		response = (RideRequestResponseMessage)poll(responseTemplate);
		assert(response.getName().equals("DD"));
		response = (RideRequestResponseMessage)poll(responseTemplate);
		assert(response.getName().equals("EE"));
		match = (MatchMessage) poll(responseTemplate);
		booking = rideBookingRepository.findOne(match.getMatchId());
		System.out.println("Received match: "+booking.getRideRequests().get(0).getRider().getName() + 
				" and "+booking.getRideRequests().get(1).getRider().getName());	
		Assert.assertEquals("DD",booking.getRideRequests().get(0).getRider().getName());
		Assert.assertEquals("EE",booking.getRideRequests().get(1).getRider().getName());
		
	/*
		RideRequestMessage requestA = new RideRequestMessage("A", "LaGuardia","LaGuardia","337 Bedford Ave, Brooklyn NY 11211", 
				"337 bedford", 40.712346,-73.962665,40.712346,-73.962665, 1, 12334560, LuggageSize.low, "Bob", "bob@gmail.com", "New York");
		RideRequestMessage requestB = new RideRequestMessage("B", "LaGuardia","LaGuardia","2960 Broadway, New York NY 10013", 
				"Columbia University", 40.712346,-73.962665,40.80801,-73.963169, 1, 12334562, LuggageSize.low, "Bob", "bob@gmail.com", "New York");
		RideRequestMessage requestC = new RideRequestMessage("C", "LaGuardia","LaGuardia","496 Broadway, Brooklyn NY 11211", 
				"496 broadway, brooklyn", 40.712346,-73.962665,40.705595,-73.950734, 1, 12334561, LuggageSize.low, "Bob", "bob@gmail.com", "New York");
		RideRequestMessage requestD = new RideRequestMessage("D", "LaGuardia","LaGuardia","496 Broadway, Brooklyn NY 11211", 
				"496 broadway, brooklyn", 40.712346,-73.962665,40.705595,-73.950734, 1, 12334561, LuggageSize.low, "Bob", "bob@gmail.com", "New York");
		ExpireRequestMessage expireC = new ExpireRequestMessage("C", true);
		RideRequestMessage requestE = new RideRequestMessage("E", "LaGuardia","LaGuardia","337 Bedford Ave, Brooklyn NY 11211", 
				"337 bedford", 40.712346,-73.962665,40.712346,-73.962665, 1, 12334560, LuggageSize.low, "Bob", "bob@gmail.com", "New York");
		
		requestTemplate.convertAndSend(requestA);
		requestTemplate.convertAndSend(requestB);
		requestTemplate.convertAndSend(requestC);
		requestTemplate.convertAndSend(requestD);
		requestTemplate.convertAndSend(expireC);
		requestTemplate.convertAndSend(requestE);
		
		
		match = (MatchMessage)poll(responseTemplate);		
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		Assert.assertEquals("BB",match.requestIds[0]);
		Assert.assertEquals("B",match.requestIds[1]);
		match = (MatchMessage) poll(responseTemplate);
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		Assert.assertEquals("A",match.requestIds[0]);
		Assert.assertEquals("C",match.requestIds[1]);
		match = (MatchMessage) poll(responseTemplate);
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		Assert.assertEquals("D",match.requestIds[0]);
		Assert.assertEquals("A",match.requestIds[1]);
	
		 requestA = new RideRequestMessage("AAA", "LaGuardia","LaGuardia","337 Bedford Ave, Brooklyn NY 11211", 
				"337 bedford", 40.712346,-73.962665,40.712346,-73.962665, 1, 12334560, LuggageSize.low, "Bob", "bob@gmail.com", "New York");
		 requestB = new RideRequestMessage("BBB", "LaGuardia","LaGuardia","2960 Broadway, New York NY 10013", 
				"Columbia University", 40.712346,-73.962665,40.80801,-73.963169, 1, 12334562, LuggageSize.low, "Bob", "bob@gmail.com", "New York");
		 requestC = new RideRequestMessage("CCC", "LaGuardia","LaGuardia","496 Broadway, Brooklyn NY 11211", 
				"496 broadway, brooklyn", 40.712346,-73.962665,40.705595,-73.950734, 1, 12334561, LuggageSize.low, "Bob", "bob@gmail.com", "New York");
		 requestD = new RideRequestMessage("DDD", "LaGuardia","LaGuardia","496 Broadway, Brooklyn NY 11211", 
				"496 broadway, brooklyn", 40.712346,-73.962665,40.705595,-73.950734, 1, 12334561, LuggageSize.low, "Bob", "bob@gmail.com", "New York");
		 requestE = new RideRequestMessage("EEE", "LaGuardia","LaGuardia","337 Bedford Ave, Brooklyn NY 11211", 
				"337 bedford", 40.712346,-73.962665,40.712346,-73.962665, 1, 12334560, LuggageSize.low, "Bob", "bob@gmail.com", "New York");
		
		requestTemplate.convertAndSend(requestA);
		
		 match = (MatchMessage)poll(responseTemplate);		
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		Assert.assertEquals("E",match.requestIds[0]);
		Assert.assertEquals("AAA",match.requestIds[1]);
		
		ExpireMatchMessage expireAAAE = new ExpireMatchMessage(match.matchId,true);
		requestTemplate.convertAndSend(requestB);
		requestTemplate.convertAndSend(requestC);
		requestTemplate.convertAndSend(requestD);
		requestTemplate.convertAndSend(requestE);
		requestTemplate.convertAndSend(expireAAAE);
		match = (MatchMessage)poll(responseTemplate);
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		Assert.assertEquals("CCC",match.requestIds[0]);
		Assert.assertEquals("DDD",match.requestIds[1]);
		match = (MatchMessage)poll(responseTemplate);
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		Assert.assertEquals("EEE",match.requestIds[0]);
		Assert.assertEquals("E",match.requestIds[1]);
		
		ExpireMatchMessage expireEEEE = new ExpireMatchMessage(match.matchId,true);
		MatchRequestMessage matchEEEE = new MatchRequestMessage(match.requestIds[0],match.requestIds[1]);
		
		requestTemplate.convertAndSend(expireEEEE);
		
		match = (MatchMessage) poll(responseTemplate);
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);
		ExpireMatchMessage expireAAAEEE = new ExpireMatchMessage(match.matchId,true);
		requestTemplate.convertAndSend(expireAAAEEE);
		requestTemplate.convertAndSend(matchEEEE);
		match = (MatchMessage) poll(responseTemplate);
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		
		Assert.assertEquals("EEE",match.requestIds[0]);
		Assert.assertEquals("E",match.requestIds[1]);	
		*/
	}

	public void testStateMessage() {
		requestTemplate.convertAndSend(new StateRequestMessage());
	}
	
	@Test
	public void testTrivial() {
		assert(true);
	}

}
