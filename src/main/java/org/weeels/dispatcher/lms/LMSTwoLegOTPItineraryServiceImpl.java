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
import org.opentripplanner.routing.impl.TravelingSalesmanPathService;
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
@Qualifier("LMSTwoLegOTP")
public class LMSTwoLegOTPItineraryServiceImpl extends BasicItineraryServiceImpl {
	@Autowired 
	private TravelingSalesmanPathService tspPathService;
	@Autowired
	private GraphService graphService;
	@Autowired
	private Hub laGuardia;
	
	RoutingRequest newRoutingRequest(String from, String to, Date date) {
		RoutingRequest routingRequestTemplate = new RoutingRequest();
		routingRequestTemplate.setFrom(from);
		routingRequestTemplate.setTo(to);
		routingRequestTemplate.setDateTime(date);
		routingRequestTemplate.setModes(new TraverseModeSet(TraverseMode.CAR));
		routingRequestTemplate.setShowIntermediateStops(true);
		routingRequestTemplate.setIntermediatePlacesOrdered(true);
		return routingRequestTemplate;
	}


	@Override
	public Itinerary soloItinerary(RideRequest rideRequest) {
		String laGuardiaOTP = laGuardia.getLocation().toOTPString();
		
		RoutingRequest routingRequest = newRoutingRequest(laGuardiaOTP, rideRequest.getDropoffLocation().toOTPString(), new Date(rideRequest.getPickupByTime()));
	  List<GraphPath> gps = tspPathService.getPaths(routingRequest);
	  if(gps == null || gps.size() == 0)
			return null;
	  GraphPath gp = gps.get(0);
	  Itinerary itinerary = new Itinerary();
	  itinerary.addStop(makeNewPickupStop(rideRequest));
	  itinerary.addStop(makeNewDropoffStop(rideRequest, gp.getDuration()));
	  rideRequest.setSoloDuration(gp.getDuration());
	  System.out.println("Solo duration: " + gp.getDuration() + " stops: " + gp.states.size());
	  
	  // TODO: Add gp's Origin and end vertex and distance info. 
	  return itinerary;
	}
	
	@Override
	public Itinerary sharedItinerary(Itinerary source, RideRequest rideRequest) {
		String laGuardiaOTP = laGuardia.getLocation().toOTPString();
		String requestDestination = rideRequest.getDropoffLocation().toOTPString();
		String sourceDestination = source.getDestination().toOTPString();
		Date pickupDate = new Date(rideRequest.getPickupByTime());
		System.out.println("Searching for shared ride for " + sourceDestination + " and " + requestDestination);
		
		RoutingRequest routingRequest = newRoutingRequest(laGuardiaOTP, requestDestination, pickupDate);
		List<GraphPath> gps = tspPathService.getPaths(routingRequest);
		if(gps == null || gps.size() == 0)
			return null;
		GraphPath gp = gps.get(0);
		rideRequest.setSoloDuration(gp.getDuration());
		System.out.println("Shared solo duration: " + gp.getDuration() + " stops: " + gp.states.size());
		
		RoutingRequest routingRequest2 = newRoutingRequest(laGuardiaOTP, requestDestination, pickupDate);
		List<String> middle = new LinkedList<String>();
		middle.add(sourceDestination);
		routingRequest2.setIntermediatePlaces(middle);
		List<GraphPath> itineraryFirst = tspPathService.getPaths(routingRequest2);
		GraphPath gpItineraryFirst = null;
		if(itineraryFirst != null && itineraryFirst.size() > 0)
			gpItineraryFirst = itineraryFirst.get(0);
		System.out.println("Itinerary first duration: " + gpItineraryFirst.getDuration() + " stops: " + gpItineraryFirst.states.size());
		
		middle.clear();
		middle.add(requestDestination);
		RoutingRequest routingRequest3 = newRoutingRequest(laGuardiaOTP, sourceDestination, pickupDate);
	
		routingRequest3.setIntermediatePlaces(middle);
		List<GraphPath> requestFirst = tspPathService.getPaths(routingRequest3);
		GraphPath gpRequestFirst = null;
		if(requestFirst != null && requestFirst.size() > 0)
			gpRequestFirst = requestFirst.get(0);
		System.out.println("Request first duration: " + gpRequestFirst.getDuration() + " stops: " + gpRequestFirst.states.size());
		 
		
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
