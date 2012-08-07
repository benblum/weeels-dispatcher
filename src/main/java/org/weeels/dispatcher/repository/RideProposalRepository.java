package org.weeels.dispatcher.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.weeels.dispatcher.domain.RideProposal;

@Repository
public interface RideProposalRepository extends CustomRideProposalRepository, PagingAndSortingRepository<RideProposal, String> {
    List<RideProposal> findAll();

}
