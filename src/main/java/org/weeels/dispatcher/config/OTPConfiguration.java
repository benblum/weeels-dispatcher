package org.weeels.dispatcher.config;

import org.opentripplanner.routing.algorithm.GenericAStar;
import org.opentripplanner.routing.graph.Graph.LoadLevel;
import org.opentripplanner.routing.impl.GraphServiceImpl;
import org.opentripplanner.routing.impl.RetryingPathServiceImpl;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;
import org.opentripplanner.routing.impl.TravelingSalesmanPathService;
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
		graphService.setLoadLevel(LoadLevel.NO_HIERARCHIES);
		graphService.setPath("/Users/bblum/weeels/OTPdata/graphs/greater-nyc-streets/");
		return graphService;
	}
	
	@Bean
	SPTService sptService() {
		return new GenericAStar();
	}
	
	@Bean
	PathService pathService() {
		RetryingPathServiceImpl pathService = new RetryingPathServiceImpl();
		pathService.graphService = graphService();
		pathService.sptService = sptService();
		return pathService;
	}
	
	@Bean
	TravelingSalesmanPathService tspPathService() {
		TravelingSalesmanPathService tspPathService = new TravelingSalesmanPathService();
		tspPathService.setChainedPathService(pathService());
		return tspPathService;
	}
}
