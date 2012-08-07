package org.weeels.dispatcher.repository;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.RideProposal;
import org.weeels.dispatcher.domain.RideRequest;

public class RideProposalRepositoryImpl implements CustomRideProposalRepository {
	@Autowired
	MongoOperations mongoTemplate;

	@Override
	public void deleteByRideRequestId(String id) {
		mongoTemplate.remove(new Query(Criteria.where("rideRequest.$id").is(id)),RideProposal.class);
	}
	
	@Override
	public void deleteByRideBookingId(String id) {
		mongoTemplate.remove(new Query(Criteria.where("rideBookingToUpdate.$id").is(new ObjectId(id))),RideProposal.class);
	}

}
