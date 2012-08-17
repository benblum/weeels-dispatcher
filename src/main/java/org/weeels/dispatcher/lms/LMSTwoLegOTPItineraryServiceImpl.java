package org.weeels.dispatcher.lms;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.joda.time.DateTime;
import org.opentripplanner.common.model.NamedPlace;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.edgetype.PatternEdge;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTransitLink;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.services.PathService;
import org.opentripplanner.routing.services.StreetVertexIndexService;
import org.opentripplanner.routing.spt.GraphPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.weeels.dispatcher.domain.Hub;
import org.weeels.dispatcher.domain.Itinerary;
import org.weeels.dispatcher.domain.Location;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.domain.Stop;
import org.weeels.dispatcher.service.BasicItineraryServiceImpl;
import org.weeels.dispatcher.service.ItineraryService;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;

@Service
@Qualifier("LMSTwoLeg")
public class LMSTwoLegOTPItineraryServiceImpl extends BasicItineraryServiceImpl {
	@Autowired 
	private PathService pathService;
	@Autowired
	private StreetVertexIndexService indexService;
	@Autowired
	private Hub laGuardia;
	
	private RoutingRequest routingRequestTemplate = new RoutingRequest();
	private String laGuardiaLabel;
	
	
	@PostConstruct
	void initRoutingRequestTemplate() {
		routingRequestTemplate.setModes(new TraverseModeSet(TraverseMode.CAR));
		laGuardiaLabel = indexService.getClosestVertex(laGuardia.getLocation().toCoordinate(),
				  "LaGuardia", routingRequestTemplate).getLabel();
	}


	@Override
	public Itinerary soloItinerary(RideRequest rideRequest) {
	  Vertex destination = indexService.getClosestVertex(rideRequest.getDropoffLocation().toCoordinate(),
			  rideRequest.getInputAddressDropoff(),
			  routingRequestTemplate);
	  if(destination == null)
		  return null;
	  routingRequestTemplate.setFrom(laGuardiaLabel);
	  routingRequestTemplate.setTo(destination.getLabel());
	  routingRequestTemplate.setDateTime(new Date(rideRequest.getPickupByTime()));
	  List<GraphPath> gps = pathService.getPaths(routingRequestTemplate);
	  if(gps == null || gps.size() == 0)
			return null;
	  GraphPath gp = gps.get(0);
	  Itinerary itinerary = new Itinerary();
	  itinerary.setDestinationLabel(destination.getLabel());
	  itinerary.setOriginLabel(laGuardiaLabel);
	  itinerary.addStop(makeNewPickupStop(rideRequest));
	  itinerary.addStop(makeNewDropoffStop(rideRequest, gp.getDuration()));
	  rideRequest.setSoloDuration(gp.getDuration());
	  // TODO: Add gp's Origin and end vertex and distance info. 
	  return itinerary;
	}
	
	@Override
	public Itinerary sharedItinerary(Itinerary source, RideRequest rideRequest) {
		Vertex requestDestination = indexService.getClosestVertex(rideRequest.getDropoffLocation().toCoordinate(),
				rideRequest.getInputAddressDropoff(),
				routingRequestTemplate);
		if(requestDestination == null)
			return null;
		
		  routingRequestTemplate.setFrom(laGuardiaLabel);
		  routingRequestTemplate.setTo(requestDestination.getLabel());
		  routingRequestTemplate.setDateTime(new Date(rideRequest.getPickupByTime()));
		  List<GraphPath> gps = pathService.getPaths(routingRequestTemplate);
		  if(gps == null || gps.size() == 0)
				return null;
		  GraphPath gp = gps.get(0);
		  rideRequest.setSoloDuration(gp.getDuration());
		
		routingRequestTemplate.setFrom(laGuardiaLabel);
		List<String> middle = new LinkedList<String>();
		middle.add(source.getDestinationLabel());
		routingRequestTemplate.setIntermediatePlaces(middle);
		routingRequestTemplate.setTo(requestDestination.getLabel());
		routingRequestTemplate.setDateTime(new Date(rideRequest.getPickupByTime()));
		List<GraphPath> itineraryFirst = pathService.getPaths(routingRequestTemplate);
		if(itineraryFirst == null || itineraryFirst.size() == 0)
			return null;
		GraphPath gpItineraryFirst = itineraryFirst.get(0);
		
		middle.clear();
		middle.add(requestDestination.getLabel());
		routingRequestTemplate.setIntermediatePlaces(middle);
		routingRequestTemplate.setTo(source.getDestinationLabel());
		List<GraphPath> requestFirst = pathService.getPaths(routingRequestTemplate);
		if(requestFirst == null || requestFirst.size() == 0)
			return null;
		GraphPath gpRequestFirst = requestFirst.get(0);
		
		Stop newStop = makeNewDropoffStop(rideRequest, 0);
		Itinerary itinerary = new Itinerary(source);
		if(gpItineraryFirst.getDuration() < gpRequestFirst.getDuration()) {
			newStop.setTime(itinerary.getStartTime() + gpItineraryFirst.getDuration());
			itinerary.insertStop(2, newStop);
		} else {
			itinerary.getStop(1).setTime(itinerary.getStartTime() + gpRequestFirst.getDuration());
			newStop.setTime(itinerary.getStartTime() + soloItinerary(rideRequest).getDuration());
			itinerary.insertStop(1, newStop);
		}
		return itinerary;
	}


	@Override
	public Itinerary sharedItinerary(List<RideRequest> rideRequests) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean possibleShare(Itinerary source, RideRequest rideRequest) {
		// TODO Auto-generated method stub
		return false;
	}

}
