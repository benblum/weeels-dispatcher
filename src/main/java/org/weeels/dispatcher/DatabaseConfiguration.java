package org.weeels.dispatcher;

import static java.lang.System.getenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoURI;

@Configuration
public class DatabaseConfiguration {
	@Bean
    public MongoDbFactory mongoDbFactory() throws Exception {
        MongoURI mongoURI = new MongoURI(getEnvOrFake("MONGOLAB_URI"));
        return new SimpleMongoDbFactory(mongoURI);
    }
	
	@Bean
	public MongoOperations mongoTemplate() throws Exception {
		return new MongoTemplate(mongoDbFactory());
	}
	
    private static String getEnvOrFake(String name) {
        String env = getenv(name);
        if (env == null) {
        	env = "mongodb://127.0.0.1:27017/dispatcher";
        }
        return env;
    }
}
