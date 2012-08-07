package org.weeels.dispatcher.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class RideProposal {

    private double fare;
    
    @Id
    private String id;
    
    @DBRef
    @NotNull
    private RideRequest rideRequest;
    
    private Itinerary itinerary;

    @DBRef
    private RideBooking rideBookingToUpdate;

    public RideProposal(Itinerary derived, RideRequest rideRequest,
			RideBooking rideBooking) {
		this.itinerary = derived; 
		this.rideRequest = rideRequest;
		this.rideBookingToUpdate = rideBooking;
	}

	public RideProposal() {
	}

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static RideProposal fromJsonToRideProposal(String json) {
        return new JSONDeserializer<RideProposal>().use(null, RideProposal.class).deserialize(json);
    }

	public static String toJsonArray(Collection<RideProposal> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<RideProposal> fromJsonArrayToRideProposals(String json) {
        return new JSONDeserializer<List<RideProposal>>().use(null, ArrayList.class).use("values", RideProposal.class).deserialize(json);
    }

	public String getId() {
        return this.id;
    }

	public void setId(String id) {
        this.id = id;
    }

	public double getFare() {
        return this.fare;
    }

	public void setFare(double fare) {
        this.fare = fare;
    }

	public Itinerary getItinerary() {
        return this.itinerary;
    }

	public void setItinerary(Itinerary itinerary) {
        this.itinerary = itinerary;
    }

	public RideBooking getRideBookingToUpdate() {
        return this.rideBookingToUpdate;
    }

	public void setRideBookingToUpdate(RideBooking rideBookingToUpdate) {
        this.rideBookingToUpdate = rideBookingToUpdate;
    }

	public RideRequest getRideRequest() {
		return rideRequest;
	}

	public void setRideRequest(RideRequest rideRequest) {
		this.rideRequest = rideRequest;
	}
	
	public boolean equals(RideProposal other) {
		return other.getId().equals(this.id);
	}
}
