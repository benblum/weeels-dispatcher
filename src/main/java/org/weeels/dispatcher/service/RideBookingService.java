package org.weeels.dispatcher.service;

import java.util.List;

import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.RideProposal;
import org.weeels.dispatcher.domain.RideRequest;

public interface RideBookingService {
	public List<RideProposal> openProposals(RideRequest rideRequest, int numProposals);
	public RideProposal openSharedProposal(RideBooking existingRideBooking, RideRequest rideRequest);
	public RideProposal openSoloProposal(RideRequest rideRequest);
	public RideBooking bookRide(RideProposal proposal);
	public boolean cancelUnbookedRideRequest(RideRequest rideRequest); // Just cancels request; no cascading.
	public boolean cancelRideBooking(RideBooking rideBooking); // Same lack of cascading
	public boolean finishRideBooking(RideBooking rideBooking);
	public boolean cancelRideRequestFromBooking(RideBooking rideBooking, RideRequest rideRequest);
}
