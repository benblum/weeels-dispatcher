package org.weeels.dispatcher.lms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.weeels.dispatcher.lms.message.ExpireMatchMessage;
import org.weeels.dispatcher.lms.message.ExpireRequestMessage;
import org.weeels.dispatcher.lms.message.MatchMessage;
import org.weeels.dispatcher.lms.message.MatchRequestMessage;
import org.weeels.dispatcher.lms.message.RideRequestMessage;
import org.weeels.dispatcher.lms.message.RideRequestResponseMessage;
import org.weeels.dispatcher.lms.message.StateMessage;
import org.weeels.dispatcher.lms.message.StateRequestMessage;
import org.weeels.dispatcher.domain.*;
import org.weeels.dispatcher.repository.*;
import org.weeels.dispatcher.service.RideBookingService;

public class RabbitMessageHandler {
	private static final Logger logger = Logger.getLogger(RabbitMessageHandler.class);
	@Autowired
	private RideRequestRepository rideRequestRepository;
	@Autowired
	private RideBookingRepository rideBookingRepository;
	@Autowired
	private RiderRepository riderRepository;
	@Autowired
	private RideBookingService rideBookingService;
	@Autowired
	private RabbitTemplate responseTemplate;

	public void handleMessage(RideRequestMessage msg) {
		try {
			logger.info("Received request: " + msg.formattedAddressDropoff);
			// Add a dummy rider specific to this request.
			Rider rider = new Rider();
			rider.setName(msg.name);
			rider.setEmail(msg.getEmail());
			riderRepository.save(rider);
			RideRequest rideRequest = msg.toRideRequest(rider);
			rideRequest = rideRequestRepository.save(rideRequest);
			responseTemplate.convertAndSend(new RideRequestResponseMessage(rideRequest));
			RideBooking rideBooking = openAndBookProposal(rideRequest);
			if(rideBooking.getRideRequests().size() > 1)
				responseTemplate.convertAndSend(new MatchMessage(rideBooking));
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
	
	/*
	 * 
	 */
	public void handleMessage(ExpireMatchMessage msg) {
		try {
			if(msg.getMatchId() == null) {
				logger.warn("Match ID is null.");
				return;
			}
			RideBooking rideBooking = rideBookingRepository.findOne(msg.getMatchId());
			if(rideBooking == null) {
				logger.warn("Match not found: " + msg.getMatchId());
				return;
			} 
			if(msg.isCanceled()) {
				//logger.info("Canceling match " + rideBooking.getId());
				rideBookingService.cancelRideBooking(rideBooking);
				for(RideRequest request : rideBooking.getRideRequests()) {
					RideBooking newBooking = openAndBookProposal(request);
					if(newBooking.getRideRequests().size() > 1)
						responseTemplate.convertAndSend(new MatchMessage(newBooking));
				}
			} else {
				logger.info("Match departing" + rideBooking.getId());
				rideBookingService.finishRideBooking(rideBooking);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private List<RideBooking.BookingStatus> viableRideBooking() {
		List<RideBooking.BookingStatus> viable = new LinkedList<RideBooking.BookingStatus>();
		viable.add(RideBooking.BookingStatus.OPEN);
		viable.add(RideBooking.BookingStatus.CLOSED);
		return viable;
	}
	
	public void handleMessage(ExpireRequestMessage msg) {
		try {
			if(msg.getRequestId() == null) {
				logger.warn("Request id is null.");
				return;
			}
			RideRequest rideRequest = rideRequestRepository.findOne(msg.getRequestId());
			if(rideRequest == null) {
				logger.warn("Request not found: " + msg.getRequestId());
				return;
			}
			logger.info("Expiring request for " + rideRequest.getRider().getName());
			RideBooking rideBooking = rideBookingRepository.findOneByAnyStatusAndRideRequestsId(viableRideBooking(), rideRequest.getId());
			if(rideBooking != null) {
				if(rideBooking.getRideRequests().size() == 1) {
					rideBookingService.cancelRideBooking(rideBooking);
				} else if(rideBooking.getRideRequests().size() == 2) {
					// If someone's shared ride has become solo, search for new shares
					rideBookingService.cancelRideBooking(rideBooking);
					System.out.println("Removing: " + rideRequest.getRider().getName() + " from " + rideBooking.getRideRequests().size());
					for(int i=0; i < rideBooking.getRideRequests().size(); i++)
						if(rideBooking.getRideRequests().get(i).getId().equals(rideRequest.getId()))
							rideBooking.getRideRequests().remove(i);
					RideRequest otherRequest = rideBooking.getRideRequests().get(0);
					RideBooking newBooking = openAndBookProposal(otherRequest);
					if(newBooking.getRideRequests().size() > 1)
						responseTemplate.convertAndSend(new MatchMessage(newBooking));
				} else {
					// Not used (yet), since we only ever match two people
					rideBookingService.cancelRideRequestFromBooking(rideBooking, rideRequest);
				}
			}
			if(msg.isCanceled()) {
				logger.info("Canceling request " + rideRequest.getId());					
			} else {
				logger.info("Rider departing " + rideRequest.getId());
			}
			rideBookingService.cancelUnbookedRideRequest(rideRequest);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void handleMessage(StateRequestMessage msg) {
		try {
			logger.info("Sending state");
			List<RideBooking> rideBookings = rideBookingRepository.findByAnyStatus(viableRideBooking());
			ArrayList<RideRequestResponseMessage> requests = new ArrayList<RideRequestResponseMessage>();
			ArrayList<MatchMessage> matches = new ArrayList<MatchMessage>();
			for(RideBooking rideBooking : rideBookings) {
				for(RideRequest rideRequest : rideBooking.getRideRequests())
					requests.add(new RideRequestResponseMessage(rideRequest));
				if(rideBooking.getRideRequests().size() == 2)
					matches.add(new MatchMessage(rideBooking));
			}
			StateMessage response = new StateMessage();
			response.requests = new RideRequestResponseMessage[requests.size()];
			response.matches = new MatchMessage[matches.size()];
			requests.toArray(response.requests);
			matches.toArray(response.matches);
			responseTemplate.convertAndSend(response);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void handleMessage(MatchRequestMessage msg) {
		try {
			if(msg.requestIds[0] == null || msg.requestIds[1] == null) {
				logger.warn("Request ID is null.");
				return;
			}				
			logger.info("Match requested between " + msg.requestIds[0] + " and " + msg.requestIds[1]);
			
			List<String> requestIds = new LinkedList<String>();
			requestIds.add(msg.requestIds[0]);
			requestIds.add(msg.requestIds[1]);
			List<RideBooking> rideBookings = rideBookingRepository.findByStatusAndAnyRideRequestsId(RideBooking.BookingStatus.OPEN, requestIds);
			
			if(rideBookings.size() != 2) {
				logger.warn("Requests are not matchable.");
				return;
			}
			RideProposal proposal = rideBookingService.openSharedProposal(rideBookings.get(0), rideBookings.get(1).getRideRequests().get(0));
			if(proposal == null) {
				logger.warn("Failed to propose new ride; perhaps booking was locked by another search.");
				return;
			}
			rideBookingService.cancelRideBooking(rideBookings.get(1));
			RideBooking rideBooking = rideBookingService.bookRide(proposal);
			responseTemplate.convertAndSend(new MatchMessage(rideBooking));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
