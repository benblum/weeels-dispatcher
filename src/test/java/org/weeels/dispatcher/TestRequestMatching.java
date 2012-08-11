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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.weeels.dispatcher.domain.Location;
import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.RideProposal;
import org.weeels.dispatcher.domain.RideRequest.LuggageSize;
import org.weeels.dispatcher.domain.Stop;
import org.weeels.dispatcher.lms.LMSRabbitConfiguration;
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
	private RabbitTemplate lmsRequestTemplate;
	@Autowired
	private RabbitTemplate lmsResponseTemplate;
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
	
	Stop BedfordHouse = new Stop(new Location(-73.962665, 40.712346), "Bedford House");
	Stop Downtown = new Stop(new Location(-73.963169,40.80801), "Downtown");
	Stop BroadwayHouse = new Stop(new Location(-73.950734,40.705595), "Broadway House");

	
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
		amqpAdmin.purgeQueue(LMSRabbitConfiguration.requestQueueName, true);
		amqpAdmin.purgeQueue(LMSRabbitConfiguration.responseQueueName, true);
		rideRequestRepository.deleteAll();
		riderRepository.deleteAll();
		rideBookingRepository.deleteAll();
		rideProposalRepository.deleteAll();
		
		RideRequestMessage msg = new RideRequestMessage();
		msg.setEmail("benblum@gmail.com");
		msg.setInputAddressDropoff("");
		msg.setFormattedAddressDropoff("");
		msg.setLuggage(LuggageSize.low);
		msg.setNeighborhood("Manhattan");
		msg.setPartySize(1);
		msg.setRequestTime(0);
		
		msg.setName("AA");
		msg.setLatDropoff(BedfordHouse.getLocation().getLat());
		msg.setLonDropoff(BedfordHouse.getLocation().getLon());
		msg.setRequestTime(12334560);
		lmsRequestTemplate.convertAndSend(msg);
		
		msg.setName("BB");
		msg.setLatDropoff(Downtown.getLocation().getLat());
		msg.setLonDropoff(Downtown.getLocation().getLon());
		msg.setRequestTime(12334562);
		lmsRequestTemplate.convertAndSend(msg);
		
		msg.setName("CC");
		msg.setLatDropoff(BroadwayHouse.getLocation().getLat());
		msg.setLonDropoff(BroadwayHouse.getLocation().getLon());
		msg.setRequestTime(12334561);
		lmsRequestTemplate.convertAndSend(msg);
		MatchMessage match = (MatchMessage)poll(lmsResponseTemplate);		
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		
	}
	
	public void testAll() {
		amqpAdmin.purgeQueue(LMSRabbitConfiguration.requestQueueName, true);
		amqpAdmin.purgeQueue(LMSRabbitConfiguration.responseQueueName, true);
		rideRequestRepository.deleteAll();
		riderRepository.deleteAll();
		rideBookingRepository.deleteAll();
		rideProposalRepository.deleteAll();
		mongoTemplate.indexOps(RideBooking.class).ensureIndex(new GeospatialIndex("itinerary.destination"));
		mongoTemplate.indexOps(RideProposal.class).ensureIndex(new GeospatialIndex("itinerary.destination"));
		
		RideRequestMessage msg = new RideRequestMessage();
		msg.setEmail("benblum@gmail.com");
		msg.setInputAddressDropoff("");
		msg.setFormattedAddressDropoff("");
		msg.setLuggage(LuggageSize.low);
		msg.setNeighborhood("Manhattan");
		msg.setPartySize(1);
		msg.setRequestTime(0);
		
		msg.setName("AA");
		msg.setLatDropoff(BedfordHouse.getLocation().getLat());
		msg.setLonDropoff(BedfordHouse.getLocation().getLon());
		msg.setRequestTime(12334560);
		lmsRequestTemplate.convertAndSend(msg);
		
		msg.setName("BB");
		msg.setLatDropoff(Downtown.getLocation().getLat());
		msg.setLonDropoff(Downtown.getLocation().getLon());
		msg.setRequestTime(12334562);
		lmsRequestTemplate.convertAndSend(msg);
		
		msg.setName("CC");
		msg.setLatDropoff(BroadwayHouse.getLocation().getLat());
		msg.setLonDropoff(BroadwayHouse.getLocation().getLon());
		msg.setRequestTime(12334561);
		lmsRequestTemplate.convertAndSend(msg);
		
		msg.setName("DD");
		msg.setLatDropoff(BroadwayHouse.getLocation().getLat());
		msg.setLonDropoff(BroadwayHouse.getLocation().getLon());
		msg.setRequestTime(12334562);
		lmsRequestTemplate.convertAndSend(msg);
		
		msg.setName("EE");
		msg.setLatDropoff(BedfordHouse.getLocation().getLat());
		msg.setLonDropoff(BedfordHouse.getLocation().getLon());
		msg.setRequestTime(12334561);
		
		RideBooking booking;
		RideRequestResponseMessage response = (RideRequestResponseMessage)poll(lmsResponseTemplate);
		assert(response.getName().equals("AA"));
		response = (RideRequestResponseMessage)poll(lmsResponseTemplate);
		assert(response.getName().equals("BB"));
		response = (RideRequestResponseMessage)poll(lmsResponseTemplate);
		assert(response.getName().equals("CC"));
		MatchMessage match = (MatchMessage)poll(lmsResponseTemplate);
		booking = rideBookingRepository.findOne(match.getMatchId());
		System.out.println("Received match: "+booking.getRideRequests().get(0).getRider().getName() + 
				" and "+booking.getRideRequests().get(1).getRider().getName());	
		Assert.assertEquals("AA",booking.getRideRequests().get(0).getRider().getName());
		Assert.assertEquals("CC",booking.getRideRequests().get(1).getRider().getName());
		
		response = (RideRequestResponseMessage)poll(lmsResponseTemplate);
		assert(response.getName().equals("DD"));
		response = (RideRequestResponseMessage)poll(lmsResponseTemplate);
		assert(response.getName().equals("EE"));
		match = (MatchMessage) poll(lmsResponseTemplate);
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
		
		lmsRequestTemplate.convertAndSend(requestA);
		lmsRequestTemplate.convertAndSend(requestB);
		lmsRequestTemplate.convertAndSend(requestC);
		lmsRequestTemplate.convertAndSend(requestD);
		lmsRequestTemplate.convertAndSend(expireC);
		lmsRequestTemplate.convertAndSend(requestE);
		
		
		match = (MatchMessage)poll(lmsResponseTemplate);		
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		Assert.assertEquals("BB",match.requestIds[0]);
		Assert.assertEquals("B",match.requestIds[1]);
		match = (MatchMessage) poll(lmsResponseTemplate);
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		Assert.assertEquals("A",match.requestIds[0]);
		Assert.assertEquals("C",match.requestIds[1]);
		match = (MatchMessage) poll(lmsResponseTemplate);
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
		
		lmsRequestTemplate.convertAndSend(requestA);
		
		 match = (MatchMessage)poll(lmsResponseTemplate);		
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		Assert.assertEquals("E",match.requestIds[0]);
		Assert.assertEquals("AAA",match.requestIds[1]);
		
		ExpireMatchMessage expireAAAE = new ExpireMatchMessage(match.matchId,true);
		lmsRequestTemplate.convertAndSend(requestB);
		lmsRequestTemplate.convertAndSend(requestC);
		lmsRequestTemplate.convertAndSend(requestD);
		lmsRequestTemplate.convertAndSend(requestE);
		lmsRequestTemplate.convertAndSend(expireAAAE);
		match = (MatchMessage)poll(lmsResponseTemplate);
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		Assert.assertEquals("CCC",match.requestIds[0]);
		Assert.assertEquals("DDD",match.requestIds[1]);
		match = (MatchMessage)poll(lmsResponseTemplate);
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		Assert.assertEquals("EEE",match.requestIds[0]);
		Assert.assertEquals("E",match.requestIds[1]);
		
		ExpireMatchMessage expireEEEE = new ExpireMatchMessage(match.matchId,true);
		MatchRequestMessage matchEEEE = new MatchRequestMessage(match.requestIds[0],match.requestIds[1]);
		
		lmsRequestTemplate.convertAndSend(expireEEEE);
		
		match = (MatchMessage) poll(lmsResponseTemplate);
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);
		ExpireMatchMessage expireAAAEEE = new ExpireMatchMessage(match.matchId,true);
		lmsRequestTemplate.convertAndSend(expireAAAEEE);
		lmsRequestTemplate.convertAndSend(matchEEEE);
		match = (MatchMessage) poll(lmsResponseTemplate);
		System.out.println("Received match: "+match.requestIds[0] + " and "+match.requestIds[1]);	
		
		Assert.assertEquals("EEE",match.requestIds[0]);
		Assert.assertEquals("E",match.requestIds[1]);	
		*/
	}

	public void testStateMessage() {
		lmsRequestTemplate.convertAndSend(new StateRequestMessage());
	}
	
	@Test
	public void testTrivial() {
		assert(true);
	}

}
