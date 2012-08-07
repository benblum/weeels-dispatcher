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
import lombok.*;

@Document
public @Data class RideRequest {
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
}
