package org.weeels.dispatcher.repository;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.weeels.dispatcher.domain.RideRequest;

@Repository
public interface RideRequestRepository extends CustomRideRequestRepository, PagingAndSortingRepository<RideRequest, String> {
    List<RideRequest> findAll();
}
