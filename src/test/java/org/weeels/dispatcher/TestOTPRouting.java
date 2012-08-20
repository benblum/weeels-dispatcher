package org.weeels.dispatcher;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.services.PathService;
import org.opentripplanner.routing.services.StreetVertexIndexService;
import org.opentripplanner.routing.spt.GraphPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.weeels.dispatcher.domain.Hub;

import com.vividsolutions.jts.geom.Coordinate;

@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
public class TestOTPRouting {
	@Autowired 
	private PathService pathService;
	@Autowired
	private Hub laGuardia;
	
	private RoutingRequest routingRequestTemplate = new RoutingRequest();
	
	
	@Before
	public void initRoutingRequestTemplate() {
		routingRequestTemplate.setModes(new TraverseModeSet(TraverseMode.CAR));
	}
	
	@Test
	public void makeOneRoute() {
		String destination = "40.7735,-73.9171";
		routingRequestTemplate.setFrom(laGuardia.getLocation().toOTPString());
		routingRequestTemplate.setTo(destination);
				
		routingRequestTemplate.setDateTime(new Date(System.currentTimeMillis()));
		List<GraphPath> gps = pathService.getPaths(routingRequestTemplate);
		GraphPath gp = gps.get(0);
		System.out.println("Duration: " + gp.getDuration());
	}
}
