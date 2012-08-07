package org.weeels.dispatcher.lms;

import java.util.List;

import org.springframework.stereotype.Service;
import org.weeels.dispatcher.domain.Itinerary;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.domain.Stop;
import org.weeels.dispatcher.service.ItineraryService;

@Service
public class LMSItineraryServiceImpl implements ItineraryService {

	@Override
	public Itinerary soloItinerary(RideRequest rideRequest) {
		Itinerary itinerary = new Itinerary();
		Stop stop = new Stop(rideRequest.getPickUpLocation(), rideRequest.getInputAddressPickup());
		stop.getRideRequestsToPickUp().add(rideRequest);
		itinerary.addStop(stop);
		stop = new Stop(rideRequest.getDropOffLocation(), rideRequest.getInputAddressDropoff());
		stop.getRideRequestsToDropOff().add(rideRequest);
		itinerary.addStop(stop);
		return itinerary;
	}

	@Override
	public Itinerary sharedItinerary(Itinerary source, RideRequest rideRequest) {
		Itinerary itinerary = new Itinerary(source);
		itinerary.getStop(0).getRideRequestsToPickUp().add(rideRequest);
		Stop stop = new Stop(rideRequest.getDropOffLocation(), rideRequest.getInputAddressDropoff());
		stop.getRideRequestsToDropOff().add(rideRequest);
		itinerary.addStop(stop);
		// do some triangle inequality crap to figure out where this dropoff should go
		return itinerary;
	}
	
	@Override
	public Itinerary sharedItinerary(List<RideRequest> rideRequests) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean possibleShare(Itinerary source, RideRequest rideRequest) {
		// TODO Auto-generated method stub
		return false;
	}

}
