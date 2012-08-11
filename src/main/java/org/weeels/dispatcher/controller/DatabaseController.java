package org.weeels.dispatcher.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.weeels.dispatcher.config.DatabaseConfiguration;
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
		DatabaseConfiguration.dropDatabase(mongoTemplate);
		DatabaseConfiguration.initializeDatabase(mongoTemplate);
		return "database/success";
	}
    
}
