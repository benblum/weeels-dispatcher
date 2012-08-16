package org.weeels.dispatcher.domain;
import lombok.*;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.DBRef;

public @Data @NoArgsConstructor class Stop {
	private int index;
	private Location location;
	private String address;
	private long time;
	
	@DBRef
    private List<RideRequest> rideRequestsToPickUp = new ArrayList<RideRequest>();

    @DBRef
    private List<RideRequest> rideRequestsToDropOff = new ArrayList<RideRequest>();

    @DBRef
    private Hub hub;
    
    public Stop(Hub hub) {
    	this.location = hub.getLocation();
    	this.address = hub.getAddress();
    	this.hub = hub;
    }
    
	public Stop(Location location, String address) {
		this.location = location;
		this.address = address;
		this.hub = null;
	}

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Stop fromJsonToStop(String json) {
        return new JSONDeserializer<Stop>().use(null, Stop.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Stop> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Stop> fromJsonArrayToStops(String json) {
        return new JSONDeserializer<List<Stop>>().use(null, ArrayList.class).use("values", Stop.class).deserialize(json);
    }

	// TODO: this will have to be made more rigorous, some kind of merging strategy
	public boolean sameLocation(Stop stop) {
		if(hub != null && stop.getHub() != null && hub.getId().equals(stop.getHub().getId()))
			return true;
		else if(Location.geoDistance(location, stop.getLocation()) < .01)
			return true;

		return false;
	}
	
	public int getPassengerDelta() {
		int delta = 0;
		for(RideRequest r: rideRequestsToPickUp)
			delta += r.getNumPassengers();
		for(RideRequest r: rideRequestsToDropOff)
			delta -= r.getNumPassengers();
		return delta;
	}
	
	public int getRideRequestDelta() {
		return rideRequestsToPickUp.size() - rideRequestsToDropOff.size();
	}
}
