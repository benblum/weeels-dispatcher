package org.weeels.dispatcher.lms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.geo.Point;
import org.springframework.stereotype.Service;
import org.weeels.dispatcher.domain.Itinerary;
import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.RideBooking.BookingStatus;
import org.weeels.dispatcher.domain.RideProposal;
import org.weeels.dispatcher.domain.RideProposalComparator;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.domain.RideRequest.RequestStatus;
import org.weeels.dispatcher.repository.RideBookingLockException;
import org.weeels.dispatcher.repository.RideBookingRepository;
import org.weeels.dispatcher.repository.RideProposalRepository;
import org.weeels.dispatcher.repository.RideRequestRepository;
import org.weeels.dispatcher.service.BasicRideBookingServiceImpl;
import org.weeels.dispatcher.service.ItineraryService;
import org.weeels.dispatcher.service.RideBookingService;

@Service
public class LMSRideBookingServiceImpl extends BasicRideBookingServiceImpl {
	@Autowired
	private RideProposalRepository rideProposalRepository;
	@Autowired
	private RideBookingRepository rideBookingRepository;
	@Autowired
	private RideRequestRepository rideRequestRepository;
	@Autowired
	private ItineraryService itineraryService;
	@Autowired
	private RideProposalComparator rideProposalComparator;
	
	private static final Logger logger = Logger.getLogger(LMSRideBookingServiceImpl.class);
	
	private static int MAX_RIDERS_PER_CAB = 3;
	private static double MAX_DROPOFF_SEPARATION = 1.5;
	private static int MAX_POTENTIAL_SHARES = 5;
	private static double RADIUS_OF_EARTH = 3959;

	/*
	 * Get list of BookedRides from bookedRideRepository that could potentially be shares. Make
	 * RideProposals from each of these. Evaluate using the rideProposalComparator (in the future,
	 * a unique one associated with the RideRequest; for now the standard bean). Send back a list
	 * of at most numProposals proposals. 
	 */
	@Override
	public List<RideProposal> openProposals(RideRequest rideRequest, int numProposals) {
		int maxRiders = MAX_RIDERS_PER_CAB - rideRequest.getNumPassengers();
		// Gets potential shares via maximum riders, no previous cancelation, etc
		List<RideBooking> potentialRideBookings = null;
		try {
			potentialRideBookings = rideBookingRepository.findAndLock(
					rideRequest, RideBooking.BookingStatus.OPEN, maxRiders, rideRequest.getDropOffLocation(), MAX_DROPOFF_SEPARATION / RADIUS_OF_EARTH);
			
			List<RideProposal> rideProposals = new ArrayList<RideProposal>();
			rideProposals.add(openSoloProposal(rideRequest));
			for(RideBooking rideBookingToUpdate : potentialRideBookings) {
				List<ObjectId> reqIds = new LinkedList<ObjectId>();
				reqIds.add(new ObjectId(rideBookingToUpdate.getRideRequests().get(0).getId()));
				reqIds.add(new ObjectId(rideRequest.getId()));				
				if(rideBookingRepository.findOneByStatusAndAllRideRequestsId(BookingStatus.CANCELED, reqIds) == null)
					rideProposals.add(makeSharedProposal(rideBookingToUpdate, rideRequest));
			}
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
	
	private RideProposal makeSharedProposal(RideBooking rideBooking, RideRequest rideRequest) {
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
			RideProposal rideProposal = makeSharedProposal(rideBooking, rideRequest);
			rideProposalRepository.save(rideProposal);
			return rideProposal;
		} catch(RideBookingLockException e) {
			logger.error(e.getMessage());
			return null;
		} catch(Exception e) {
			logger.error(e.getMessage());
			return null;
		} finally {
			if(rideBooking.getLockedBy() != null)
				rideBookingRepository.unlock(rideBooking, rideRequest);
		}
	}


	@Override
	public RideProposal openSoloProposal(RideRequest rideRequest) {
		Itinerary itinerary = itineraryService.soloItinerary(rideRequest);
		return rideProposalRepository.save(new RideProposal(itinerary, rideRequest, null));
	}

	/*
	 * Books a ride from a proposal. If this proposal depends on updating a booked ride which has
	 * since changed, this will fail and return null. 
	 */
	@Override
	public RideBooking bookRide(RideProposal rideProposal) {
		RideBooking rideBooking = rideProposal.getRideBookingToUpdate();
		if(rideBooking != null) {
			try {
				rideBookingRepository.lock(rideBooking, rideProposal.getRideRequest());
				if(rideProposalRepository.findOne(rideProposal.getId()) == null) {
					// Ride proposal has been eliminated (e.g. by ride booked by others)
					logger.warn(rideProposal + " failed: " + rideBooking + " has changed.");	
					return null;
				}
				
				rideBooking.addRideRequest(rideProposal.getRideRequest());
				rideBooking.setItinerary(rideProposal.getItinerary());
				rideBooking.setStatus(BookingStatus.CLOSED);
				rideBookingRepository.save(rideBooking);
				logger.info("Made shared ride for " + rideBooking.getRideRequests().get(0).getRider().getName() + " and " + rideBooking.getRideRequests().get(1).getRider().getName());
				rideProposalRepository.deleteByRideBookingId(rideBooking.getId());
				// Some notification to other RideProposals that they've been deleted would
				// probably be nice in the future...
			} catch(RideBookingLockException e) {
				logger.error(e.getMessage());
			} catch(Exception e) {
				logger.error(e.getMessage());
			} finally {
				if(rideBooking.getLockedBy() != null)
					rideBookingRepository.unlock(rideBooking, rideProposal.getRideRequest());
			}
		} else {
			rideBooking = new RideBooking(rideProposal);
			rideBooking.setStatus(BookingStatus.OPEN);
			rideBookingRepository.save(rideBooking); // TODO: derive BookingStatus from Proposal
			logger.info("Made solo ride for " + rideBooking.getRideRequests().get(0).getId());
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
		try {
			RideRequest dummy = rideBooking.getRideRequests().get(0);
			rideBookingRepository.lock(rideBooking, dummy);
			rideProposalRepository.deleteByRideBookingId(rideBooking.getId());
			
			rideBooking.setStatus(status);
			rideBookingRepository.save(rideBooking);
			rideBookingRepository.unlock(rideBooking, dummy );
			
			logger.info("Deleting ride for " + rideBooking.getRideRequests().get(0).getId());
			return true;
		} catch(RideBookingLockException e) {
			logger.error(e.getMessage());
			return false;
		} 
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
		try {
			rideRequest.setStatus(RequestStatus.CANCELED);
			rideBookingRepository.lock(rideBooking, rideRequest);
			for(int i=0; i < rideBooking.getRideRequests().size(); i++)
				if(rideBooking.getRideRequests().get(i).getId().equals(rideRequest.getId()))
					rideBooking.getRideRequests().remove(i);
			rideBooking.setItinerary(itineraryService.sharedItinerary(rideBooking.getRideRequests()));
			rideBookingRepository.save(rideBooking);
			rideRequestRepository.save(rideRequest);
			return true;
		} catch(RideBookingLockException e) {
			logger.error(e.getMessage());
			return false;
		} catch(Exception e) {
			logger.error(e.getMessage());
			return false;
		} finally {
			if(rideBooking.getLockedBy().equals(rideRequest.getId()))
				rideBookingRepository.unlock(rideBooking, rideRequest);
		}
	}
}
