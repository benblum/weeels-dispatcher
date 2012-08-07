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
public class RideRequest {
	public enum RequestStatus {
		OPEN, BOOKED, CANCELED, FINISHED
	}
	
	public enum LuggageSize {
		low, med, high
	}
	
	@Id
    private String id;

    @DBRef
    protected Rider rider;
    protected int numPassengers;
    protected Location pickUpLocation;
    protected Location dropOffLocation;	
    protected String formattedAddressPickup;
	protected String inputAddressPickup;
	protected String formattedAddressDropoff;
	protected String inputAddressDropoff;
	protected String neighborhood;
	protected LuggageSize luggage;

	protected RequestStatus status;

//    @Temporal(TemporalType.TIMESTAMP)
//    @DateTimeFormat(style = "M-")
    protected long requestTime;

//    @DBRef
//    protected RideBooking rideBooking;
    

    public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static RideRequest fromJsonToRideRequest(String json) {
        return new JSONDeserializer<RideRequest>().use(null, RideRequest.class).deserialize(json);
    }

	public static String toJsonArray(Collection<RideRequest> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<RideRequest> fromJsonArrayToRideRequests(String json) {
        return new JSONDeserializer<List<RideRequest>>().use(null, ArrayList.class).use("values", RideRequest.class).deserialize(json);
    }

	public Rider getRider() {
        return this.rider;
    }

	public void setRider(Rider rider) {
        this.rider = rider;
    }

	public int getNumPassengers() {
        return this.numPassengers;
    }

	public void setNumPassengers(int numPassengers) {
        this.numPassengers = numPassengers;
    }

	public Location getPickUpLocation() {
        return this.pickUpLocation;
    }

	public void setPickUpLocation(Location pickUpLocation) {
        this.pickUpLocation = pickUpLocation;
    }

	public Location getDropOffLocation() {
        return this.dropOffLocation;
    }

	public void setDropOffLocation(Location dropOffLocation) {
        this.dropOffLocation = dropOffLocation;
    }

	public long getRequestTime() {
        return this.requestTime;
    }

	public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public String getId() {
        return this.id;
    }

	public void setId(String id) {
        this.id = id;
    }

	public String getFormattedAddressPickup() {
		return formattedAddressPickup;
	}

	public void setFormattedAddressPickup(String formattedAddressPickup) {
		this.formattedAddressPickup = formattedAddressPickup;
	}

	public String getInputAddressPickup() {
		return inputAddressPickup;
	}

	public void setInputAddressPickup(String inputAddressPickup) {
		this.inputAddressPickup = inputAddressPickup;
	}

	public String getFormattedAddressDropoff() {
		return formattedAddressDropoff;
	}

	public void setFormattedAddressDropoff(String formattedAddressDropoff) {
		this.formattedAddressDropoff = formattedAddressDropoff;
	}

	public String getInputAddressDropoff() {
		return inputAddressDropoff;
	}

	public void setInputAddressDropoff(String inputAddressDropoff) {
		this.inputAddressDropoff = inputAddressDropoff;
	}

	public RequestStatus getStatus() {
		return status;
	}

	public void setStatus(RequestStatus status) {
		this.status = status;
	}
	
	public String getNeighborhood() {
		return neighborhood;
	}

	public void setNeighborhood(String neighborhood) {
		this.neighborhood = neighborhood;
	}

	public LuggageSize getLuggage() {
		return luggage;
	}

	public void setLuggage(LuggageSize luggage) {
		this.luggage = luggage;
	}
}
