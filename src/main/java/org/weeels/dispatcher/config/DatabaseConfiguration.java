package org.weeels.dispatcher.config;

import static java.lang.System.getenv;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.weeels.dispatcher.domain.Hub;
import org.weeels.dispatcher.domain.Location;
import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.RideProposal;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.domain.Rider;
import org.weeels.dispatcher.repository.HubRepository;

import com.mongodb.MongoURI;

@Configuration
public class DatabaseConfiguration {

	public static void initializeDatabase(MongoOperations mongoTemplate) {
		if(!mongoTemplate.collectionExists(Rider.class))
			mongoTemplate.createCollection(Rider.class);
		if(!mongoTemplate.collectionExists(Hub.class))
			mongoTemplate.createCollection(Hub.class);
		if(!mongoTemplate.collectionExists(RideRequest.class))
			mongoTemplate.createCollection(RideRequest.class);
		if(!mongoTemplate.collectionExists(RideBooking.class))
			mongoTemplate.createCollection(RideBooking.class);
        mongoTemplate.indexOps(RideBooking.class).ensureIndex(new GeospatialIndex("itinerary.origin"));
        mongoTemplate.indexOps(RideBooking.class).ensureIndex(new GeospatialIndex("itinerary.destination"));
        if(!mongoTemplate.collectionExists(RideProposal.class))
    		mongoTemplate.createCollection(RideProposal.class);
		mongoTemplate.indexOps(RideProposal.class).ensureIndex(new GeospatialIndex("itinerary.origin"));
		mongoTemplate.indexOps(RideProposal.class).ensureIndex(new GeospatialIndex("itinerary.destination"));
	}
	
	public static void dropDatabase(MongoOperations mongoTemplate) {
		mongoTemplate.dropCollection(Rider.class);
		mongoTemplate.dropCollection(RideRequest.class);
		mongoTemplate.dropCollection(RideBooking.class);
		mongoTemplate.dropCollection(RideProposal.class);
	}
	
	@Bean
    public MongoDbFactory mongoDbFactory() throws Exception {
        MongoURI mongoURI = new MongoURI(getEnvOrFake("MONGOLAB_URI"));
        return new SimpleMongoDbFactory(mongoURI);
    }
	
	@Bean
	public MongoOperations mongoTemplate() throws Exception {
		MongoOperations mongoTemplate = new MongoTemplate(mongoDbFactory());
		initializeDatabase(mongoTemplate);
		return mongoTemplate;
	}
	
    private static String getEnvOrFake(String name) {
        String env = getenv(name);
        if (env == null) {
        	env = "mongodb://127.0.0.1:27017/dispatcher";
        }
        return env;
    }
}
