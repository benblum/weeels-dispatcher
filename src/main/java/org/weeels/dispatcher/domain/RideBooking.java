package org.weeels.dispatcher.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document
public @Data @NoArgsConstructor class RideBooking {
	public enum BookingStatus {
		OPEN, CLOSED, CANCELED, FINISHED;
	}
	
    @DBRef
    private List<RideRequest> rideRequests = new ArrayList<RideRequest>();

    private Itinerary itinerary;
    
    private BookingStatus status;
    
	@Id
    private String id;
	
	private int numPassengers;
	private long pickupTime;

	public RideBooking(RideProposal rideProposal) {
		super();
		rideRequests.add(rideProposal.getRideRequest());
		this.itinerary = rideProposal.getItinerary();
		numPassengers = rideProposal.getRideRequest().getNumPassengers();
		pickupTime = rideProposal.getRideRequest().getRequestTime();
	}
	
	public void addRideRequest(RideRequest rideRequest) {
		rideRequests.add(rideRequest);
		numPassengers += rideRequest.getNumPassengers();
		pickupTime = Math.min(pickupTime,  rideRequest.getRequestTime());
	}
	
	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static RideBooking fromJsonToRideBooking(String json) {
        return new JSONDeserializer<RideBooking>().use(null, RideBooking.class).deserialize(json);
    }

	public static String toJsonArray(Collection<RideBooking> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<RideBooking> fromJsonArrayToRideBookings(String json) {
        return new JSONDeserializer<List<RideBooking>>().use(null, ArrayList.class).use("values", RideBooking.class).deserialize(json);
    }
	
	public int hashCode() {
		return id.hashCode();
	}
	
	public boolean equals(Object o) {
		return id.equals(((RideBooking)o).getId());
	}
}
