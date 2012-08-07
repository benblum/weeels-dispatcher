package org.weeels.dispatcher.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class RideBooking {
	public enum BookingStatus {
		OPEN, CLOSED, CANCELED, FINISHED;
	}
	
    @DBRef
    private List<RideRequest> rideRequests = new ArrayList<RideRequest>();

    private Itinerary itinerary;
 
    private BookingStatus status;
    
    private String lockedBy;

	@Id
    private String id;
	
	private int numPassengers;

	public RideBooking(RideProposal rideProposal) {
		this.rideRequests = new ArrayList<RideRequest>();
		rideRequests.add(rideProposal.getRideRequest());
		this.itinerary = rideProposal.getItinerary();
		numPassengers = rideProposal.getRideRequest().getNumPassengers();
	}
	
	public RideBooking() {
	}

	public String getId() {
        return this.id;
    }

	public void setId(String id) {
        this.id = id;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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

	public List<RideRequest> getRideRequests() {
        return this.rideRequests;
    }

	public void setRideRequests(List<RideRequest> rideRequests) {
        this.rideRequests = rideRequests;
    }

	public Itinerary getItinerary() {
        return this.itinerary;
    }

	public void setItinerary(Itinerary itinerary) {
        this.itinerary = itinerary;
    }

	public BookingStatus getStatus() {
		return status;
	}

	public void setStatus(BookingStatus status) {
		this.status = status;
	}

	public void addRideRequest(RideRequest rideRequest) {
		rideRequests.add(rideRequest);
		numPassengers += rideRequest.getNumPassengers();
	}

	public String getLockedBy() {
		return lockedBy;
	}

	public void setLockedBy(String lockedBy) {
		this.lockedBy = lockedBy;
	}

	public int getNumPassengers() {
		return numPassengers;
	}

	public void setNumPassengers(int numPassengers) {
		this.numPassengers = numPassengers;
	}
}
