package org.weeels.dispatcher.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.RideProposal;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.domain.Rider;

@RequestMapping("/database")
@Controller
public class DatabaseController {
	@Autowired
	MongoOperations mongoTemplate;
	
	@RequestMapping(params = "init", produces = "text/html") 
	public String initDatabase() {
		if(mongoTemplate.collectionExists(Rider.class))
			mongoTemplate.dropCollection(Rider.class);
		if(mongoTemplate.collectionExists(RideRequest.class))
			mongoTemplate.dropCollection(RideRequest.class);
		if(mongoTemplate.collectionExists(RideProposal.class))
			mongoTemplate.dropCollection(RideProposal.class);
		if(mongoTemplate.collectionExists(RideBooking.class))
			mongoTemplate.dropCollection(RideBooking.class);
		mongoTemplate.createCollection(Rider.class);
		mongoTemplate.createCollection(RideRequest.class);
		mongoTemplate.createCollection(RideBooking.class);
        mongoTemplate.indexOps(RideBooking.class).ensureIndex(new GeospatialIndex("itinerary.destination"));
        mongoTemplate.createCollection(RideProposal.class);
		mongoTemplate.indexOps(RideProposal.class).ensureIndex(new GeospatialIndex("itinerary.destination"));
		return "database/success";
	}
    
}
