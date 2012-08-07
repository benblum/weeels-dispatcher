package org.weeels.dispatcher.repository;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.weeels.dispatcher.domain.Rider;

@Repository
public interface RiderRepository extends PagingAndSortingRepository<Rider, String> {

    List<Rider> findAll();
}
