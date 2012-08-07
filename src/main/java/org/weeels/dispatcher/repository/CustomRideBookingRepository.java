package org.weeels.dispatcher.repository;

import java.util.List;

import org.springframework.data.mongodb.core.geo.Point;
import org.weeels.dispatcher.domain.Location;
import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.RideBooking.BookingStatus;
import org.weeels.dispatcher.domain.RideRequest;

public interface CustomRideBookingRepository {
	public RideBooking unlock(RideBooking rideBooking, RideRequest rideRequest);
	public void unlock(RideRequest rideRequest);
	public RideBooking lock(RideBooking rideBooking, RideRequest rideRequest) throws RideBookingLockException;
	public List<RideBooking> findAndLock(RideRequest rideRequest,
			BookingStatus status, int maxRiders, Location destination,
			double radius);
	
	public RideBooking findOneByRideRequestsId(String id);
	public RideBooking findOneByStatusAndAllRideRequestsId(BookingStatus status,
			List<String> rideRequestIds);
	public List<RideBooking> findByStatusAndAnyRideRequestsId(BookingStatus status,
			List<String> rideRequestIds);
	public RideBooking findOneByAnyStatusAndRideRequestsId(List<BookingStatus> status,
			String id);
	List<RideBooking> findByAnyStatus(List<BookingStatus> viableRideBooking);
	
}
