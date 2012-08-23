package org.weeels.dispatcher.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.geo.Point;
import org.weeels.dispatcher.domain.Location;
import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.RideBooking.BookingStatus;
import org.weeels.dispatcher.domain.RideRequest;

public interface CustomRideBookingRepository {
	public RideBooking unlock(RideBooking rideBooking, RideRequest rideRequest);
	public void unlock(RideRequest rideRequest);
	public RideBooking lock(RideBooking rideBooking, RideRequest rideRequest);
	public List<RideBooking> lock(List<RideBooking> rideBookings,
			RideRequest rideRequest);
/*	
  	public List<RideBooking> findAndLock(RideRequest rideRequest, BookingStatus status,
			int maxRiders);
	public List<RideBooking> findAndLock(RideRequest rideRequest,
			BookingStatus status, int maxRiders, Location destination,
			double radius);
	public List<RideBooking> findAndLock(RideRequest rideRequest, 
			BookingStatus status, int maxRiders, long timeRadius, 
			Location origin, Location destination, double radius);
*/
	public List<RideBooking> find(RideRequest rideRequest, BookingStatus status,
			int maxRiders);
	public List<RideBooking> find(RideRequest rideRequest,
			BookingStatus status, int maxRiders, Location destination,
			double radius);
	public List<RideBooking> find(RideRequest rideRequest, 
			BookingStatus status, int maxRiders, long timeRadius, 
			Location origin, Location destination, double radius);
	public List<RideBooking> find(RideRequest rideRequest, 
			BookingStatus status, int maxRiders, long timeRadius, 
			Location origin, double radius);
	public RideBooking findOneByRideRequestsId(String id);
	public RideBooking findOneByStatusAndAllRideRequestsId(BookingStatus status,
			List<ObjectId> rideRequestIds);
	public List<RideBooking> findByStatusAndAnyRideRequestsId(BookingStatus status,
			List<ObjectId> rideRequestIds);
	public RideBooking findOneByAnyStatusAndRideRequestsId(List<BookingStatus> status,
			String id);
	public List<RideBooking> findByAnyStatus(List<BookingStatus> viableRideBooking);
	public RideBooking findAndClose(RideBooking rideBooking);
	 
}
