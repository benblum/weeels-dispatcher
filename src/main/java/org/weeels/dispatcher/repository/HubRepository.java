package org.weeels.dispatcher.repository;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.weeels.dispatcher.domain.Hub;

@Repository
public interface HubRepository extends PagingAndSortingRepository<Hub, String> {

    List<Hub> findAll();
    Hub findOneByName(String name);
}
