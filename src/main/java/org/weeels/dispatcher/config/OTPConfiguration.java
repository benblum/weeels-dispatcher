package org.weeels.dispatcher.config;

import org.opentripplanner.routing.algorithm.GenericAStar;
import org.opentripplanner.routing.impl.GraphServiceImpl;
import org.opentripplanner.routing.impl.RetryingPathServiceImpl;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.services.PathService;
import org.opentripplanner.routing.services.SPTService;
import org.opentripplanner.routing.services.StreetVertexIndexService;
import org.opentripplanner.common.geometry.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OTPConfiguration {
	@Bean
	GraphService graphService() {
		GraphServiceImpl graphService = new GraphServiceImpl();
		graphService.setPath("/Users/bblum/weeels/OTPdata/graphs/laguardia-streets/");
		return graphService;
	}
	
	@Bean
	StreetVertexIndexService streetVertexIndexService() {
		StreetVertexIndexServiceImpl indexService = new StreetVertexIndexServiceImpl(graphService().getGraph(), distanceLibrary());
		return indexService;
	}
	
	@Bean
	DistanceLibrary distanceLibrary() {
		return SphericalDistanceLibrary.getInstance();
	}
	
	@Bean
	SPTService stpService() {
		return new GenericAStar();
	}
	
	@Bean
	PathService pathService() {
		return new RetryingPathServiceImpl();
	}
}
