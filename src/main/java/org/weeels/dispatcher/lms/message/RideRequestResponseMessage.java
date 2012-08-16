package org.weeels.dispatcher.lms.message;

import org.weeels.dispatcher.domain.Location;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.domain.RideRequest.LuggageSize;
import org.weeels.dispatcher.domain.Rider;
import lombok.*;

public @Data @NoArgsConstructor @AllArgsConstructor class RideRequestResponseMessage {
	public String requestId;
	public String formattedAddressPickup;
	public String inputAddressPickup;
	public String formattedAddressDropoff;
	public String inputAddressDropoff;
	public double latPickup;
	public double lonPickup;
	public double latDropoff;
	public double lonDropoff;
	public int partySize;
	public long requestTime;
	public RideRequest.LuggageSize luggage;
	public String name;
	public String email;
	public String neighborhood;	
	
	public RideRequestResponseMessage(RideRequest request) {
		this.requestId = request.getId();
		this.formattedAddressPickup = request.getFormattedAddressPickup();
		this.inputAddressPickup = request.getInputAddressPickup();
		this.formattedAddressDropoff = request.getFormattedAddressDropoff();
		this.inputAddressDropoff = request.getInputAddressDropoff();
		this.latDropoff = request.getDropoffLocation().getLat();
		this.lonDropoff = request.getDropoffLocation().getLon();
		this.latPickup = request.getPickupLocation().getLat();
		this.lonPickup = request.getPickupLocation().getLon();
		this.partySize = request.getNumPassengers();
		this.requestTime = request.getRequestTime();
		this.name = request.getRider().getName();
		this.email = request.getRider().getEmail();
		this.luggage = request.getLuggage();
		this.neighborhood = request.getNeighborhood();
	}
	
	public RideRequest toRideRequest(Rider rider) {
		RideRequest request = new RideRequest();
		request.setId(requestId);
		request.setFormattedAddressDropoff(formattedAddressDropoff);
		request.setFormattedAddressPickup(formattedAddressPickup);
		request.setInputAddressDropoff(inputAddressDropoff);
		request.setInputAddressPickup(inputAddressPickup);
		request.setNumPassengers(partySize);
		request.setRequestTime(requestTime);
		request.setDropoffLocation(new Location(lonDropoff, latDropoff));
		request.setPickupLocation(new Location(lonPickup, latPickup));
		request.setLuggage(luggage);
		request.setRider(rider);
		request.setNeighborhood(neighborhood);
		return request;
	}
}
