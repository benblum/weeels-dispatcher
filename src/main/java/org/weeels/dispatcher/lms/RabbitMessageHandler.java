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
import org.weeels.dispatcher.lms.message.StateMessage;
import org.weeels.dispatcher.lms.message.StateRequestMessage;
import org.weeels.dispatcher.domain.*;
import org.weeels.dispatcher.domain.RideRequest.RequestStatus;
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
			logger.info("Received request: " + msg.requestId + " for " + msg.formattedAddressDropoff);
			// Add a dummy rider specific to this request.
			Rider rider = new Rider();
			rider.setName("LMS_"+msg.name);
			riderRepository.save(rider);
			RideRequest rideRequest = msg.toRideRequest(rider);
			rideRequestRepository.save(rideRequest);
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
			RideBooking rideBooking = rideBookingRepository.findOne(msg.getMatchId());
			if(rideBooking == null) {
				logger.warn("Match not found: " + msg.getMatchId());
			} else {
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
			RideRequest rideRequest = rideRequestRepository.findOne(msg.getRequestId());
			if(rideRequest == null) {
				logger.warn("Request not found: " + msg.getRequestId());
				return;
			}
			logger.info("Expiring request " + rideRequest.getId());
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
					System.out.println("Left: " + otherRequest.getId() + " from " + rideBooking.getRideRequests().size());
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
			rideBookingService.cancelUnbookedRideRequest(rideRequest); // TODO: handle potential new matches
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void handleMessage(StateRequestMessage msg) {
		try {
			logger.info("Sending state");
			List<RideBooking> rideBookings = rideBookingRepository.findByAnyStatus(viableRideBooking()); // TODO: just open, shared ones
			ArrayList<RideRequestMessage> requests = new ArrayList<RideRequestMessage>();
			ArrayList<MatchMessage> matches = new ArrayList<MatchMessage>();
			for(RideBooking rideBooking : rideBookings) {
				for(RideRequest rideRequest : rideBooking.getRideRequests())
					requests.add(new RideRequestMessage(rideRequest));
				if(rideBooking.getRideRequests().size() == 2)
					matches.add(new MatchMessage(rideBooking));
			}
			StateMessage response = new StateMessage();
			response.requests = new RideRequestMessage[requests.size()];
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
			logger.info("Match requested between " + msg.requestIds[0] + " and " + msg.requestIds[1]);
			List<String> requestIds = new LinkedList<String>();
			requestIds.add(msg.requestIds[0]);
			requestIds.add(msg.requestIds[1]);
			List<RideBooking> rideBookings = rideBookingRepository.findByStatusAndAnyRideRequestsId(RideBooking.BookingStatus.OPEN, requestIds);
			
			assert(rideBookings.size() == 2);
			RideProposal proposal = rideBookingService.openSharedProposal(rideBookings.get(0), rideBookings.get(1).getRideRequests().get(0));
			assert(proposal != null); // Someone else better not have locked it up. TODO: No asserts!
			rideBookingService.cancelRideBooking(rideBookings.get(1));
			RideBooking rideBooking = rideBookingService.bookRide(proposal);
			responseTemplate.convertAndSend(new MatchMessage(rideBooking));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
