package org.weeels.dispatcher.service;

import java.util.List;

import org.weeels.dispatcher.domain.*;

public interface ItineraryService {
	public Itinerary soloItinerary(RideRequest rideRequest);
	public Itinerary sharedItinerary(Itinerary source, RideRequest rideRequest);
	public Itinerary sharedItinerary(List<RideRequest> rideRequests);
	public boolean possibleShare(Itinerary source, RideRequest rideRequest);
}
