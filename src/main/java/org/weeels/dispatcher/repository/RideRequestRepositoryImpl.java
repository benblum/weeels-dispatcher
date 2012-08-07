package org.weeels.dispatcher.repository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.weeels.dispatcher.domain.RideProposal;
import org.weeels.dispatcher.domain.RideRequest;

public class RideRequestRepositoryImpl implements CustomRideRequestRepository {
	@Autowired
	MongoOperations mongoTemplate;
/*
	@Override
	public RideProposal findOneRideProposal(String id, int index) {
		RideRequest r = mongoTemplate.findById(id, RideRequest.class);
		return r.getRideProposals().get(index);
	}
	
	@Override
	public List<RideProposal> findAllRideProposals(String requestId) {
		return mongoTemplate.findById(requestId, RideRequest.class).getRideProposals();
	}

	@Override
	public int findAndPushRideProposal(String id, RideProposal rideProposal) {
		rideProposal.setIndex(mongoTemplate.findById(id, RideRequest.class).getRideProposals().size());
		return mongoTemplate.findAndModify(new Query(Criteria.where("id").is(id)), 
				new Update().push("rideProposal",rideProposal), 
				RideRequest.class).getRideProposals().size()-1;
	}

	@Override
	public void findAndPopRideProposal(String id) {
		mongoTemplate.findAndModify(new Query(Criteria.where("id").is(id)), 
				new Update().pop("rideProposals", Update.Position.LAST), 
				RideRequest.class);
	}
	*/
}
