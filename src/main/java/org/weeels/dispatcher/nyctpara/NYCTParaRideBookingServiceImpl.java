package org.weeels.dispatcher.nyctpara;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.repository.RideBookingRepository;
import org.weeels.dispatcher.service.BasicRideBookingServiceImpl;

@Service
@Qualifier("NYCTPara")
public class NYCTParaRideBookingServiceImpl extends BasicRideBookingServiceImpl {
	@Autowired
	protected RideBookingRepository rideBookingRepository;
	
	private static int MAX_RIDERS_PER_CAB = 3;
	private static double MAX_DROPOFF_SEPARATION = 1;
	private static double RADIUS_OF_EARTH = 3959;
	private static long TIME_DIFF = 10 * 60 * 1000;
	
	@Override
	protected List<RideBooking> findPotentials(RideRequest rideRequest) {
		int maxRiders = MAX_RIDERS_PER_CAB - rideRequest.getNumPassengers();
		
		List<RideBooking> totalBookings = rideBookingRepository.find(
				rideRequest, RideBooking.BookingStatus.OPEN, maxRiders, TIME_DIFF,
				rideRequest.getPickupLocation(), rideRequest.getDropoffLocation(), MAX_DROPOFF_SEPARATION / RADIUS_OF_EARTH);
		
		return totalBookings;
	}

}
