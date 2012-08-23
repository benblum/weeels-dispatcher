package org.weeels.dispatcher.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.weeels.dispatcher.domain.Itinerary;
import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.RideProposal;
import org.weeels.dispatcher.domain.RideProposalComparator;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.domain.RideBooking.BookingStatus;
import org.weeels.dispatcher.domain.RideRequest.RequestStatus;
import org.weeels.dispatcher.lms.LMSRideBookingServiceImpl;
import org.weeels.dispatcher.repository.RideBookingRepository;
import org.weeels.dispatcher.repository.RideProposalRepository;
import org.weeels.dispatcher.repository.RideRequestRepository;

public abstract class BasicRideBookingServiceImpl implements RideBookingService {
	@Autowired
	protected RideProposalRepository rideProposalRepository;
	@Autowired
	protected RideBookingRepository rideBookingRepository;
	@Autowired
	protected RideRequestRepository rideRequestRepository;
	@Autowired
	protected ItineraryService itineraryService;
	@Autowired
	protected RideProposalComparator rideProposalComparator;
	
	protected static final Logger logger = Logger.getLogger(LMSRideBookingServiceImpl.class);
	
	protected abstract List<RideBooking> findPotentials(RideRequest rideRequest); 
	
	
	/*
	 * Get list of BookedRides from bookedRideRepository that could potentially be shares. Make
	 * RideProposals from each of these. Evaluate using the rideProposalComparator (in the future,
	 * a unique one associated with the RideRequest; for now the standard bean). Send back a list
	 * of at most numProposals proposals. 
	 */
	@Override
	public List<RideProposal> openProposals(RideRequest rideRequest, int numProposals) {
		// Gets potential shares via maximum riders, no previous cancelation, etc
		List<RideBooking> potentialRideBookings = null;
		try {
			List<RideProposal> rideProposals = new ArrayList<RideProposal>();
			rideProposals.add(openSoloProposal(rideRequest));
			potentialRideBookings = findPotentials(rideRequest);
			potentialRideBookings = rideBookingRepository.lock(potentialRideBookings, rideRequest);
			for(RideBooking rideBookingToUpdate : potentialRideBookings) {
				rideProposals.add(makeSharedProposal(rideBookingToUpdate, rideRequest));
			}
			// TODO: make this efficient, maybe with a pre-screening by geometry
			Collections.sort(rideProposals, rideProposalComparator);
			List<RideProposal> rideProposalsToOpen;
			if(rideProposals.size() > numProposals)
				rideProposalsToOpen = rideProposals.subList(0, numProposals);
			else
				rideProposalsToOpen = rideProposals;
			rideProposalRepository.save(rideProposalsToOpen);
			return rideProposalsToOpen;
		} catch(Exception e) {
			logger.error(e.getMessage());
			return null;
		} finally {
			// Assume just one thread per rideRequest...
			rideBookingRepository.unlock(rideRequest);
		}
	}
	
	protected RideProposal makeSharedProposal(RideBooking rideBooking, RideRequest rideRequest) {
		Itinerary derived = itineraryService.sharedItinerary(rideBooking.getItinerary(), rideRequest);
		RideProposal proposal = new RideProposal(derived, rideRequest, rideBooking);
		return proposal;
	}
	
	/*
	 * Proper usage is
	 * 
	 * RideProposal rideProposal = openSharedProposal(rideBooking, rideRequest);
	 * while(rideProposal = null) {
	 *    // evaluate if rideBooking is still good 
	 *    rideBooking = rideBookingRepository.findById(rideBooking.getId());
	 *    if(good for rideRequest)
	 *      break;
	 *      rideProposal = openSharedProposal(rideBooking, rideRequest);
	 * }
	 * 
	 */
	@Override
	public RideProposal openSharedProposal(RideBooking rideBooking, RideRequest rideRequest) {
		try {
			rideBooking = rideBookingRepository.lock(rideBooking, rideRequest);
			if(rideBooking == null || rideBooking.getStatus() == BookingStatus.CLOSED) {
				logger.error("Failed to lock proposal");
				return null;
			}
			RideProposal rideProposal = makeSharedProposal(rideBooking, rideRequest);
			rideProposalRepository.save(rideProposal);
			rideBookingRepository.unlock(rideBooking, rideRequest);
			return rideProposal;
		} catch(Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}


	@Override
	public RideProposal openSoloProposal(RideRequest rideRequest) {
		Itinerary itinerary = itineraryService.soloItinerary(rideRequest);
		if(itinerary == null)
			return null;
		rideRequest.setSoloDuration(itinerary.getDuration());
		rideRequestRepository.save(rideRequest);
		return rideProposalRepository.save(new RideProposal(itinerary, rideRequest, null));
	}

	/*
	 * Books a ride from a proposal. If this proposal depends on updating a booked ride which has
	 * since changed, this will fail and return null. 
	 */
	@Override
	public RideBooking bookRide(RideProposal rideProposal) {
		RideBooking rideBooking = rideProposal.getRideBookingToUpdate();
		if(rideBooking == null) { // Solo ride
			rideBooking = new RideBooking(rideProposal);
			rideBooking.setStatus(BookingStatus.OPEN);
			rideBookingRepository.save(rideBooking); // TODO: derive BookingStatus from Proposal
			logger.info("Made solo ride for " + rideBooking.getRideRequests().get(0).getId());
		} else { // shared ride
			rideBooking = rideBookingRepository.lock(rideProposal.getRideBookingToUpdate(), rideProposal.getRideRequest());
			if(rideBooking == null) {
				logger.warn("Failed to lock rideBooking");
				return null;
			}
			if(rideProposalRepository.findOne(rideProposal.getId()) == null) {
				// Ride proposal has been eliminated (e.g. by ride booked by others)
				logger.warn(rideProposal + " failed: " + rideBooking + " has changed.");
				rideBookingRepository.unlock(rideBooking, rideProposal.getRideRequest());
				return null;
			}
			rideBooking.addRideRequest(rideProposal.getRideRequest());
			rideBooking.setItinerary(rideProposal.getItinerary());
			rideBookingRepository.save(rideBooking);
			logger.info("Made shared ride for " + rideBooking.getRideRequests().get(0).getRider().getName() + " and " + rideBooking.getRideRequests().get(1).getRider().getName());
			rideProposalRepository.deleteByRideBookingId(rideBooking.getId());
				// Some notification to other RideProposals that they've been deleted would
				// probably be nice in the future...
			
		} 
	
		rideProposal.getRideRequest().setStatus(RequestStatus.BOOKED);
		rideRequestRepository.save(rideProposal.getRideRequest());
		rideProposalRepository.deleteByRideRequestId(rideProposal.getRideRequest().getId());
		return rideBooking;
	}

	@Override
	public boolean cancelRideBooking(RideBooking rideBooking) {
		for(RideRequest rideRequest: rideBooking.getRideRequests())
			rideRequest.setStatus(RequestStatus.OPEN);
		rideRequestRepository.save(rideBooking.getRideRequests());
		return terminateRideBooking(rideBooking, BookingStatus.CANCELED);
	}

	@Override
	public boolean finishRideBooking(RideBooking rideBooking) {
		for(RideRequest rideRequest: rideBooking.getRideRequests())
			rideRequest.setStatus(RequestStatus.FINISHED);
		rideRequestRepository.save(rideBooking.getRideRequests());
		return terminateRideBooking(rideBooking, BookingStatus.FINISHED);
	}

	private boolean terminateRideBooking(RideBooking rideBooking, BookingStatus status) {
		RideRequest dummy = rideBooking.getRideRequests().get(0);
		rideBooking = rideBookingRepository.lock(rideBooking, dummy);
		if(rideBooking == null)
			return false;

		rideProposalRepository.deleteByRideBookingId(rideBooking.getId());	
		rideBooking.setStatus(status);
		rideBookingRepository.save(rideBooking);
		rideBookingRepository.unlock(rideBooking, dummy );
			
		logger.info("Deleting ride for " + rideBooking.getRideRequests().get(0).getId());
		return true; 
	}

	@Override
	public boolean cancelUnbookedRideRequest(RideRequest rideRequest) {
		rideRequest.setStatus(RequestStatus.CANCELED);
		rideProposalRepository.deleteByRideRequestId(rideRequest.getId());
		rideRequestRepository.save(rideRequest);
		return true;
	}

	// Don't need this one yet
	@Override
	public boolean cancelRideRequestFromBooking(RideBooking rideBooking, RideRequest rideRequest) {
		rideRequest.setStatus(RequestStatus.CANCELED);
		rideBooking = rideBookingRepository.lock(rideBooking, rideRequest);
		if(rideBooking == null)
			return false;
		for(int i=0; i < rideBooking.getRideRequests().size(); i++)
			if(rideBooking.getRideRequests().get(i).getId().equals(rideRequest.getId()))
				rideBooking.getRideRequests().remove(i);
		rideBooking.setItinerary(itineraryService.sharedItinerary(rideBooking.getRideRequests()));
		rideBookingRepository.save(rideBooking);
		rideBookingRepository.unlock(rideBooking, rideRequest);
		rideRequestRepository.save(rideRequest);
		return true;
	}
}
