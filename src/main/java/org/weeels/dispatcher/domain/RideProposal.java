package org.weeels.dispatcher.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document
public @Data @NoArgsConstructor class RideProposal {

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
}
