package org.weeels.dispatcher.repository;

import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.RideRequest;

public interface CustomRideProposalRepository {
	public void deleteByRideRequestId(String id);

	public void deleteByRideBookingId(String id);
}
