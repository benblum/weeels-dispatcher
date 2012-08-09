package org.weeels.dispatcher.repository;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.geo.Distance;
import org.springframework.data.mongodb.core.geo.Metrics;
import org.springframework.data.mongodb.core.geo.Point;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.Location;
import org.weeels.dispatcher.domain.RideBooking.BookingStatus;
import org.weeels.dispatcher.domain.RideRequest;

import com.mongodb.WriteResult;

public class RideBookingRepositoryImpl implements CustomRideBookingRepository {
	@Autowired
	MongoOperations mongoTemplate;

	//TODO: make transactional
	@Override
	public RideBooking unlock(RideBooking rideBooking, RideRequest rideRequest) {
		RideBooking retVal= mongoTemplate.findAndModify(new Query(Criteria.where("_id").is(new ObjectId(rideBooking.getId()))
				.and("lockedBy").is(new ObjectId(rideRequest.getId())))
				, new Update().set("lockedBy",null), new FindAndModifyOptions().returnNew(true), RideBooking.class);
		return retVal;
	}

	// TODO: make transactional
	@Override
	public RideBooking lock(RideBooking rideBooking, RideRequest rideRequest) throws RideBookingLockException {
		RideBooking retVal= mongoTemplate.findAndModify(new Query(Criteria.where("_id").is(new ObjectId(rideBooking.getId()))
				.and("lockedBy").is(null))
				, new Update().set("lockedBy",new ObjectId(rideRequest.getId())), new FindAndModifyOptions().returnNew(true), RideBooking.class);
		return retVal;
		
	}

	@Override
	public List<RideBooking> findAndLock(RideRequest rideRequest, 
			BookingStatus status, int maxRiders, Location destination, double radius) {
		Criteria good = Criteria.where("numPassengers").lte(maxRiders)
				.and("status").is(status.name())
				.and("itinerary.destination").nearSphere(new Point(destination.getLon(), destination.getLat())).maxDistance(radius)
				.and("lockedBy").is(null);
		mongoTemplate.updateMulti(new Query(good), 
				new Update().set("lockedBy",new ObjectId(rideRequest.getId())), RideBooking.class);
		good = Criteria.where("numPassengers").lte(maxRiders)
				.and("status").is(status.name())
				.and("itinerary.destination").nearSphere(new Point(destination.getLon(), destination.getLat())).maxDistance(radius)
				.and("lockedBy").is(new ObjectId(rideRequest.getId()));
		List<RideBooking> retVal = mongoTemplate.find(new Query(good), RideBooking.class);
		return retVal;
	}
	
	@Override
	public List<RideBooking> findAndLock(RideRequest rideRequest, 
			BookingStatus status, int maxRiders, long timeRadius, 
			Location origin, Location destination, double radius) {
		Criteria good = Criteria.where("numPassengers").lte(maxRiders)
				.and("status").is(status.name())
				.and("itinerary.origin").nearSphere(new Point(destination.getLon(), destination.getLat())).maxDistance(radius)
				.and("itinerary.destination").nearSphere(new Point(destination.getLon(), destination.getLat())).maxDistance(radius)
				.and("requestTime").gte(rideRequest.getRequestTime()-timeRadius).and("requestTime").lte(rideRequest.getRequestTime()+timeRadius)
				.and("lockedBy").is(null);
		mongoTemplate.updateMulti(new Query(good), 
				new Update().set("lockedBy",new ObjectId(rideRequest.getId())), RideBooking.class);
		good = Criteria.where("numPassengers").lte(maxRiders)
				.and("status").is(status.name())
				.and("itinerary.origin").nearSphere(new Point(destination.getLon(), destination.getLat())).maxDistance(radius)
				.and("itinerary.destination").nearSphere(new Point(destination.getLon(), destination.getLat())).maxDistance(radius)
				.and("requestTime").gte(rideRequest.getRequestTime()-timeRadius).and("requestTime").lte(rideRequest.getRequestTime()+timeRadius)
				.and("lockedBy").is(new ObjectId(rideRequest.getId()));
		List<RideBooking> retVal = mongoTemplate.find(new Query(good), RideBooking.class);
		return retVal;
	}
	
	@Override
	public void unlock(RideRequest rideRequest) {
		mongoTemplate.updateMulti(new Query(Criteria.where("lockedBy").is(new ObjectId(rideRequest.getId()))), 
				new Update().set("lockedBy",null), RideBooking.class);
	}

	@Override
	public RideBooking findOneByRideRequestsId(String id) {
		return mongoTemplate.findOne(new Query(Criteria.where("rideRequests.$id").is(new ObjectId(id))), RideBooking.class);
	}
	
	@Override
	public RideBooking findOneByStatusAndAllRideRequestsId(BookingStatus status, List<ObjectId> rideRequestIds) {
		return mongoTemplate.findOne(new Query(Criteria.where("status").is(status)
				.and("rideRequests.$id").all(rideRequestIds)), RideBooking.class);
	}

	@Override
	public List<RideBooking> findByStatusAndAnyRideRequestsId(BookingStatus status, List<ObjectId> rideRequestIds) {
		return mongoTemplate.find(new Query(Criteria.where("status").is(status)
				.and("rideRequests.$id").in(rideRequestIds)), RideBooking.class);
	}
	
	@Override
	public RideBooking findOneByAnyStatusAndRideRequestsId(
			List<BookingStatus> status, String id) {
		return mongoTemplate.findOne(new Query(Criteria.where("status").in(status)
				.and("rideRequests.$id").is(new ObjectId(id))), RideBooking.class);
	}
	
	@Override
	public List<RideBooking> findByAnyStatus(List<BookingStatus> status) {
		return mongoTemplate.find(new Query(Criteria.where("status").in(status)), RideBooking.class);
	}

}
