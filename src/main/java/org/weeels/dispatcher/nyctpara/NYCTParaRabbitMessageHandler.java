package org.weeels.dispatcher.nyctpara;

import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.weeels.dispatcher.domain.*;
import org.weeels.dispatcher.domain.RideRequest.RequestStatus;
import org.weeels.dispatcher.nyctpara.message.NYCTParatransitRideRequestMessage;
import org.weeels.dispatcher.repository.*;
import org.weeels.dispatcher.service.RideBookingService;

public class NYCTParaRabbitMessageHandler {
	private static final Logger logger = Logger.getLogger(NYCTParaRabbitMessageHandler.class);
	@Autowired
	private RideRequestRepository rideRequestRepository;
	@Autowired
	private RideBookingRepository rideBookingRepository;
	@Autowired
	private RiderRepository riderRepository;
	@Autowired
	@Qualifier("NYCTPara")
	private RideBookingService rideBookingService;
	@Autowired
	private RabbitTemplate paraResponseTemplate;

	public void handleMessage(NYCTParatransitRideRequestMessage msg) {
		try {
			logger.info("Received request: " + msg.getOriginAddress());
			// Add a dummy rider specific to this request.
			Rider rider = new Rider();
			rider.setName(msg.getClientId());
			riderRepository.save(rider);
			RideRequest rideRequest = msg.toRideRequest(rider);
			rideRequestRepository.save(rideRequest);
			RideBooking rideBooking = openAndBookProposal(rideRequest);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private RideBooking openAndBookProposal(RideRequest request) {
		List<RideProposal> rideProposals = rideBookingService.openProposals(request, 1);
		RideProposal bestProposal = rideProposals.get(0); // Best ranked
		//logger.info("Trying to book " + bestProposal.getRideBookingToUpdate() + " for " + request.getId());
		return rideBookingService.bookRide(bestProposal);	
	}
	
}
