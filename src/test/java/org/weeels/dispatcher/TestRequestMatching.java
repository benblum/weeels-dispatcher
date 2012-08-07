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
		RideRequestMessage msgA = new RideRequestMessage("AA", "LaGuardia","LaGuardia","337 Bedford Ave, Brooklyn NY 11211", 
				"337 bedford", 40.712346,-73.962665,40.712346,-73.962665, 1, 12334560, 1, "Bob");
		requestTemplate.convertAndSend(msgA);
		RideRequestMessage msgB = new RideRequestMessage("BB", "LaGuardia","LaGuardia","2960 Broadway, New York NY 10013", 
				"Columbia University", 40.712346,-73.962665,40.80801,-73.963169, 1, 12334562, 1, "Bob");
		RideRequestMessage msgC = new RideRequestMessage("CC", "LaGuardia","LaGuardia","496 Broadway, Brooklyn NY 11211", 
				"496 broadway, brooklyn", 40.712346,-73.962665,40.705595,-73.950734, 1, 12334561, 1, "Bob");
		requestTemplate.convertAndSend(msgB);
		requestTemplate.convertAndSend(msgC);
		//MatchMessage match = (MatchMessage)poll(responseTemplate);		
		//System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		
	}
	
	public void testAll() {
		amqpAdmin.purgeQueue(RabbitConfiguration.requestQueueName, true);
		amqpAdmin.purgeQueue(RabbitConfiguration.responseQueueName, true);
		rideRequestRepository.deleteAll();
		riderRepository.deleteAll();
		rideBookingRepository.deleteAll();
		rideProposalRepository.deleteAll();
		mongoTemplate.indexOps(RideBooking.class).ensureIndex(new GeospatialIndex("itinerary.destination"));
		mongoTemplate.indexOps(RideProposal.class).ensureIndex(new GeospatialIndex("itinerary.destination"));
		
		RideRequestMessage msgA = new RideRequestMessage("AA", "LaGuardia","LaGuardia","337 Bedford Ave, Brooklyn NY 11211", 
				"337 bedford", 40.712346,-73.962665,40.712346,-73.962665, 1, 12334560, 1, "Bob");
		RideRequestMessage msgB = new RideRequestMessage("BB", "LaGuardia","LaGuardia","2960 Broadway, New York NY 10013", 
				"Columbia University", 40.712346,-73.962665,40.80801,-73.963169, 1, 12334562, 1, "Bob");
		RideRequestMessage msgC = new RideRequestMessage("CC", "LaGuardia","LaGuardia","496 Broadway, Brooklyn NY 11211", 
				"496 broadway, brooklyn", 40.712346,-73.962665,40.705595,-73.950734, 1, 12334561, 1, "Bob");
		RideRequestMessage msgD = new RideRequestMessage("DD", "LaGuardia","LaGuardia","496 Broadway, Brooklyn NY 11211", 
				"496 broadway, brooklyn", 40.712346,-73.962665,40.705595,-73.950734, 1, 12334561, 1, "Bob");
		RideRequestMessage msgE = new RideRequestMessage("EE", "LaGuardia","LaGuardia","337 Bedford Ave, Brooklyn NY 11211", 
				"337 bedford", 40.712346,-73.962665,40.712346,-73.962665, 1, 12334560, 1, "Bob");
		
		requestTemplate.convertAndSend(msgA);
		requestTemplate.convertAndSend(msgB);
		requestTemplate.convertAndSend(msgC);
		requestTemplate.convertAndSend(msgD);
		requestTemplate.convertAndSend(msgE);
		
		MatchMessage match = (MatchMessage)poll(responseTemplate);
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		Assert.assertEquals("AA",match.requestIds[0]);
		Assert.assertEquals("CC",match.requestIds[1]);
		match = (MatchMessage) poll(responseTemplate);
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		Assert.assertEquals("DD",match.requestIds[0]);
		Assert.assertEquals("EE",match.requestIds[1]);
	
		RideRequestMessage requestA = new RideRequestMessage("A", "LaGuardia","LaGuardia","337 Bedford Ave, Brooklyn NY 11211", 
				"337 bedford", 40.712346,-73.962665,40.712346,-73.962665, 1, 12334560, 1, "Bob");
		RideRequestMessage requestB = new RideRequestMessage("B", "LaGuardia","LaGuardia","2960 Broadway, New York NY 10013", 
				"Columbia University", 40.712346,-73.962665,40.80801,-73.963169, 1, 12334562, 1, "Bob");
		RideRequestMessage requestC = new RideRequestMessage("C", "LaGuardia","LaGuardia","496 Broadway, Brooklyn NY 11211", 
				"496 broadway, brooklyn", 40.712346,-73.962665,40.705595,-73.950734, 1, 12334561, 1, "Bob");
		RideRequestMessage requestD = new RideRequestMessage("D", "LaGuardia","LaGuardia","496 Broadway, Brooklyn NY 11211", 
				"496 broadway, brooklyn", 40.712346,-73.962665,40.705595,-73.950734, 1, 12334561, 1, "Bob");
		ExpireRequestMessage expireC = new ExpireRequestMessage("C", true);
		RideRequestMessage requestE = new RideRequestMessage("E", "LaGuardia","LaGuardia","337 Bedford Ave, Brooklyn NY 11211", 
				"337 bedford", 40.712346,-73.962665,40.712346,-73.962665, 1, 12334560, 1, "Bob");
		
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
				"337 bedford", 40.712346,-73.962665,40.712346,-73.962665, 1, 12334560, 1, "Bob");
		 requestB = new RideRequestMessage("BBB", "LaGuardia","LaGuardia","2960 Broadway, New York NY 10013", 
				"Columbia University", 40.712346,-73.962665,40.80801,-73.963169, 1, 12334562, 1, "Bob");
		 requestC = new RideRequestMessage("CCC", "LaGuardia","LaGuardia","496 Broadway, Brooklyn NY 11211", 
				"496 broadway, brooklyn", 40.712346,-73.962665,40.705595,-73.950734, 1, 12334561, 1, "Bob");
		 requestD = new RideRequestMessage("DDD", "LaGuardia","LaGuardia","496 Broadway, Brooklyn NY 11211", 
				"496 broadway, brooklyn", 40.712346,-73.962665,40.705595,-73.950734, 1, 12334561, 1, "Bob");
		 requestE = new RideRequestMessage("EEE", "LaGuardia","LaGuardia","337 Bedford Ave, Brooklyn NY 11211", 
				"337 bedford", 40.712346,-73.962665,40.712346,-73.962665, 1, 12334560, 1, "Bob");
		
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
	}

	public void testStateMessage() {
		requestTemplate.convertAndSend(new StateRequestMessage());
	}
	
	@Test
	public void testTrivial() {
		assert(true);
	}

}
