package org.weeels.dispatcher.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;


public @Data @NoArgsConstructor class Itinerary {
	
	private Location destination;
	private String destinationLabel;
	private Location origin;
	private String originLabel;
	private List<Stop> stops = new ArrayList<Stop>();
	
	public Itinerary(Itinerary other) {
		setStops(other.stops);
	}
	
	public long getStartTime() {
		return stops.get(0).getTime();
	}
	
	public long getFinishTime() {
		return stops.get(stops.size()-1).getTime();
	}
	
	public long getDuration() {
		return getFinishTime() - getStartTime();
	}
	
	public long getDurationFor(RideRequest rideRequest) {
		long pickupTime = 0;
		for(Stop stop : stops) {
			if(stop.getRideRequestsToPickUp().contains(rideRequest))
				pickupTime = stop.getTime();
			else if(stop.getRideRequestsToDropOff().contains(rideRequest))
				return stop.getTime() - pickupTime;
		}
		return 0;
	}
	
	public long getSoloDurationFor(RideRequest rideRequest) {
		long lastTime = 0;
		long soloTime = 0;
		int numR = 0;
		boolean haveTheGuy = false;
		for(Stop stop : stops) {
			if(haveTheGuy) {
				if(numR == 1)
					soloTime += stop.getTime() - lastTime;
				if(stop.getRideRequestsToDropOff().contains(rideRequest))
					haveTheGuy = false;
			} else if(stop.getRideRequestsToPickUp().contains(rideRequest))
				haveTheGuy = true;
			
			numR += stop.getRideRequestDelta();
			lastTime = stop.getTime();
		}
		return soloTime;
	}
	
	public long getSharedDurationFor(RideRequest rideRequest) {
		long lastTime = 0;
		long sharedTime = 0;
		int numR = 0;
		boolean haveTheGuy = false;
		for(Stop stop : stops) {
			if(haveTheGuy) {
				if(numR > 1)
					sharedTime += stop.getTime() - lastTime;
				if(stop.getRideRequestsToDropOff().contains(rideRequest))
					haveTheGuy = false;
			} else if(stop.getRideRequestsToPickUp().contains(rideRequest))
				haveTheGuy = true;
			
			numR += stop.getRideRequestDelta();
			lastTime = stop.getTime();
		}
		return sharedTime;
	}

	public void addStop(Stop stop) {
		stops.add(stop);
		if(stops.size() == 1)
			origin = stop.getLocation();
		destination = stop.getLocation();
	}

	public Stop getStop(int i) {
		return stops.get(i);
	}
	
	public void setStops(List<Stop> stops) {
        this.stops = stops;
        origin = stops.get(0).getLocation();
        destination = stops.get(stops.size()-1).getLocation();
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Itinerary fromJsonToItinerary(String json) {
        return new JSONDeserializer<Itinerary>().use(null, Itinerary.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Itinerary> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Itinerary> fromJsonArrayToItinerarys(String json) {
        return new JSONDeserializer<List<Itinerary>>().use(null, ArrayList.class).use("values", Itinerary.class).deserialize(json);
    }

	public void insertStop(int i, Stop stop) {
		stops.add(i, stop);
	}

}
