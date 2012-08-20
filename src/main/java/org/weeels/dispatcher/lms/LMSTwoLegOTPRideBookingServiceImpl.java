package org.weeels.dispatcher.lms;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.RideBooking.BookingStatus;
import org.weeels.dispatcher.domain.RideProposal;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.repository.RideBookingRepository;
import org.weeels.dispatcher.service.BasicRideBookingServiceImpl;

@Service
@Qualifier("LMSTwoLegOTP")
public class LMSTwoLegOTPRideBookingServiceImpl extends BasicRideBookingServiceImpl {
	@Autowired
	protected RideBookingRepository rideBookingRepository;
	
	private static int MAX_RIDERS_PER_CAB = 3;
	
	@Override
	protected List<RideBooking> findPotentials(RideRequest rideRequest) {	
		int maxRiders = MAX_RIDERS_PER_CAB - rideRequest.getNumPassengers();
		List<RideBooking> totalBookings = rideBookingRepository.find(
				rideRequest, RideBooking.BookingStatus.OPEN, maxRiders);
		ListIterator<RideBooking> it = totalBookings.listIterator();
		while(it.hasNext()) {
			List<ObjectId> reqIds = new LinkedList<ObjectId>();
			reqIds.add(new ObjectId(it.next().getRideRequests().get(0).getId()));
			reqIds.add(new ObjectId(rideRequest.getId()));				
			if(rideBookingRepository.findOneByStatusAndAllRideRequestsId(BookingStatus.CANCELED, reqIds) != null)
				it.remove();
		}
		return totalBookings;
	}
	
	@Override
	public RideBooking bookRide(RideProposal rideProposal) {
		RideBooking rideBooking = super.bookRide(rideProposal);
		if(rideBooking == null)
			return null;
		else if(rideBooking.getRideRequests().size() == 1)
			return rideBooking;
		else
			return rideBookingRepository.findAndClose(rideBooking);
	}
}
