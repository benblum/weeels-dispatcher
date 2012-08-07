package org.weeels.dispatcher.lms.message;

import org.weeels.dispatcher.domain.Location;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.domain.RideRequest.LuggageSize;
import org.weeels.dispatcher.domain.Rider;
import lombok.*;

public @Data @NoArgsConstructor @AllArgsConstructor class RideRequestMessage {
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
	
	public RideRequestMessage(RideRequest request) {
		this.requestId = request.getId();
		this.formattedAddressPickup = request.getFormattedAddressPickup();
		this.inputAddressPickup = request.getInputAddressPickup();
		this.formattedAddressDropoff = request.getFormattedAddressDropoff();
		this.inputAddressDropoff = request.getInputAddressDropoff();
		this.latDropoff = request.getDropOffLocation().getLat();
		this.lonDropoff = request.getDropOffLocation().getLon();
		this.latPickup = request.getPickUpLocation().getLat();
		this.lonPickup = request.getPickUpLocation().getLon();
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
		request.setDropOffLocation(new Location(lonDropoff, latDropoff));
		request.setPickUpLocation(new Location(lonPickup, latPickup));
		request.setLuggage(luggage);
		request.setRider(rider);
		request.setNeighborhood(neighborhood);
		return request;
	}
}
