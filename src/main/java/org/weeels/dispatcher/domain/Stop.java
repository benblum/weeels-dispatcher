package org.weeels.dispatcher.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.mongodb.core.mapping.DBRef;

public class Stop {

	private int index;
    
	private Location location;
	
	private String address;

    public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	@DBRef
    private List<RideRequest> rideRequestsToPickUp = new ArrayList<RideRequest>();

    @DBRef
    private List<RideRequest> rideRequestsToDropOff = new ArrayList<RideRequest>();

	public Stop(Location location, String address) {
		this.location = location;
		this.address = address;
	}

	public Stop() {
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

	public Location getLocation() {
        return this.location;
    }

	public void setLocation(Location location) {
        this.location = location;
    }

	public List<RideRequest> getRideRequestsToPickUp() {
        return this.rideRequestsToPickUp;
    }

	public void setRideRequestsToPickUp(List<RideRequest> rideRequestsToPickUp) {
        this.rideRequestsToPickUp = rideRequestsToPickUp;
    }

	public List<RideRequest> getRideRequestsToDropOff() {
        return this.rideRequestsToDropOff;
    }

	public void setRideRequestsToDropOff(List<RideRequest> rideRequestsToDropOff) {
        this.rideRequestsToDropOff = rideRequestsToDropOff;
    }

	public int getIndex() {
        return this.index;
    }

	public void setIndex(int index) {
        this.index = index;
    }
	
	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
