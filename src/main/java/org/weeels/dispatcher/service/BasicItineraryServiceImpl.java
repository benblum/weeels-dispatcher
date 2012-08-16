package org.weeels.dispatcher.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.weeels.dispatcher.domain.Itinerary;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.domain.Stop;
import org.weeels.dispatcher.service.ItineraryService;

@Service
public class BasicItineraryServiceImpl implements ItineraryService {

	@Override
	public Itinerary soloItinerary(RideRequest rideRequest) {
		Itinerary itinerary = new Itinerary();
		itinerary.addStop(makeNewPickupStop(rideRequest));
		itinerary.addStop(makeNewDropoffStop(rideRequest, 999));
		return itinerary;
	}
	
	protected Stop makeNewDropoffStop(RideRequest rideRequest, long duration) {
		Stop pickupStop;
		if(rideRequest.getDropoffHub() != null)
			pickupStop = new Stop(rideRequest.getDropoffHub());
		else
			pickupStop = new Stop(rideRequest.getDropoffLocation(), rideRequest.getInputAddressDropoff());
		pickupStop.getRideRequestsToDropOff().add(rideRequest);
		pickupStop.setTime(rideRequest.getPickupByTime()+duration);
		return pickupStop;
	}
	
	protected Stop makeNewPickupStop(RideRequest rideRequest) {
		Stop pickupStop;
		if(rideRequest.getPickupHub() != null)
			pickupStop = new Stop(rideRequest.getPickupHub());
		else
			pickupStop = new Stop(rideRequest.getPickupLocation(), rideRequest.getInputAddressPickup());
		pickupStop.getRideRequestsToPickUp().add(rideRequest);
		pickupStop.setTime(rideRequest.getPickupByTime());
		return pickupStop;
	}
	

	@Override
	public Itinerary sharedItinerary(Itinerary source, RideRequest rideRequest) {
		Itinerary itinerary = new Itinerary(source);
		Stop pickupStop = makeNewPickupStop(rideRequest);
		if(itinerary.getStop(0).sameLocation(pickupStop))
			itinerary.getStop(0).getRideRequestsToPickUp().add(rideRequest);
		else {
			if(itinerary.getStop(0).getRideRequestsToPickUp().get(0).getRequestTime() > rideRequest.getRequestTime())
				itinerary.insertStop(0, pickupStop);
			else
				itinerary.insertStop(1, pickupStop);
		}
		int n = itinerary.getStops().size();
		Stop dropoffStop = makeNewDropoffStop(rideRequest, 999);
		if(itinerary.getStop(n-1).sameLocation(dropoffStop)) {
			itinerary.getStop(n-1).getRideRequestsToDropOff().add(rideRequest);
		} else {
			if(itinerary.getStop(n-1).getRideRequestsToDropOff().get(0).getRequestTime() < rideRequest.getRequestTime())
				itinerary.insertStop(n, dropoffStop);
			else
				itinerary.insertStop(n-1, dropoffStop);
		}
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
