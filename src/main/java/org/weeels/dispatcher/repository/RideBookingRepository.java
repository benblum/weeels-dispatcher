package org.weeels.dispatcher.repository;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.RideBooking.BookingStatus;
import org.weeels.dispatcher.domain.RideRequest;

@Repository
public interface RideBookingRepository extends CustomRideBookingRepository, PagingAndSortingRepository<RideBooking, String> {

    List<RideBooking> findAll();

	List<RideBooking> findAllByStatus(BookingStatus status);
    
//    RideBooking findByRideRequestsId(String id);
}
