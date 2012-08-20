package org.weeels.dispatcher;

import static org.junit.Assert.*;
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
	@Qualifier("LMS")
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
	
	@Before
	private void clearQueues() {
		amqpAdmin.purgeQueue(LMSRabbitConfiguration.requestQueueName, true);
		amqpAdmin.purgeQueue(LMSRabbitConfiguration.responseQueueName, true);
		rideRequestRepository.deleteAll();
		riderRepository.deleteAll();
		rideBookingRepository.deleteAll();
		rideProposalRepository.deleteAll();
		mongoTemplate.indexOps(RideBooking.class).ensureIndex(new GeospatialIndex("itinerary.destination"));
		mongoTemplate.indexOps(RideProposal.class).ensureIndex(new GeospatialIndex("itinerary.destination"));
	}
	
	private void makeRequest(String name, Stop stop, long time) {
		RideRequestMessage msg = new RideRequestMessage();
		msg.setEmail("benblum@gmail.com");
		msg.setInputAddressDropoff("");
		msg.setFormattedAddressDropoff("");
		msg.setLuggage(LuggageSize.low);
		msg.setNeighborhood("New Yorkish");
		msg.setPartySize(1);
		msg.setRequestTime(0);
		msg.setName(name);
		msg.setLatDropoff(stop.getLocation().getLat());
		msg.setLonDropoff(stop.getLocation().getLon());
		msg.setRequestTime(time);
		lmsRequestTemplate.convertAndSend(msg);
		RideRequestResponseMessage response = (RideRequestResponseMessage)poll(lmsResponseTemplate);
		assertEquals(response.getName(),name);
	}
	
	private MatchMessage checkMatch(String name1, String name2) {
		MatchMessage match = (MatchMessage)poll(lmsResponseTemplate);
		RideBooking booking = rideBookingRepository.findOne(match.getMatchId());
		System.out.println("Received match: "+booking.getRideRequests().get(0).getRider().getName() + 
				" and "+booking.getRideRequests().get(1).getRider().getName());	
		assertEquals(name1,booking.getRideRequests().get(0).getRider().getName());
		assertEquals(name2,booking.getRideRequests().get(1).getRider().getName());
		return match;
	}
	
	@Test
	public void testAll() {
		makeRequest("A", BedfordHouse, 12334560);
		makeRequest("B", Downtown, 12334562);
		makeRequest("C", BroadwayHouse, 12334561);
		checkMatch("A", "C");
		makeRequest("D", BroadwayHouse, 12334562);
		makeRequest("E", BedfordHouse, 12334561);
		checkMatch("D","E");

		makeRequest("AA", BedfordHouse, 12334560);
		makeRequest("BB", Downtown, 12334562);
		checkMatch("B", "BB");
		makeRequest("CC", BroadwayHouse, 12334561);
		checkMatch("AA", "CC");
		makeRequest("DD", BroadwayHouse, 12334562);
		lmsRequestTemplate.convertAndSend(new ExpireRequestMessage("CC", true));
		checkMatch("DD","AA");
		makeRequest("EE", BedfordHouse, 12334561);
		
		makeRequest("AAA", BedfordHouse, 12334560);
		String matchId = checkMatch("EE", "AAA").getMatchId();
		makeRequest("BBB", Downtown, 12334562);
		makeRequest("CCC", BroadwayHouse, 12334561);
		makeRequest("DDD", BroadwayHouse, 12334562);
		checkMatch("CCC","DDD");
		lmsRequestTemplate.convertAndSend(new ExpireMatchMessage(matchId, true));
		makeRequest("EEE", BedfordHouse, 12334561);
		MatchMessage match = checkMatch("EEE", "EE");
		lmsRequestTemplate.convertAndSend(new ExpireMatchMessage(match.getMatchId(), true));
		matchId = checkMatch("AAA", "EE").getMatchId();
		lmsRequestTemplate.convertAndSend(new ExpireMatchMessage(matchId, true));
		lmsRequestTemplate.convertAndSend(new MatchRequestMessage(match.requestIds[0],match.requestIds[1]));
		checkMatch("EEE","EE");
	}

	@Test
	public void testStateMessage() {
		lmsRequestTemplate.convertAndSend(new StateRequestMessage());
	}
	
	@Test
	public void testTrivial() {
		assert(true);
	}

}
