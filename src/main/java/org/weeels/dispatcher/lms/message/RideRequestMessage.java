package org.weeels.dispatcher.lms.message;

import org.weeels.dispatcher.domain.Location;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.domain.RideRequest.LuggageSize;
import org.weeels.dispatcher.domain.Rider;

public class RideRequestMessage {
	
	
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
	
	public RideRequestMessage(String requestId, String formattedAddressPickup,
			String inputAddressPickup, String formattedAddressDropoff,
			String inputAddressDropoff, double latPickup, double lonPickup,
			double latDropoff, double lonDropoff, int partySize,
			long requestTime, LuggageSize luggage, String name, String email,
			String neighborhood) {
		super();
		this.requestId = requestId;
		this.formattedAddressPickup = formattedAddressPickup;
		this.inputAddressPickup = inputAddressPickup;
		this.formattedAddressDropoff = formattedAddressDropoff;
		this.inputAddressDropoff = inputAddressDropoff;
		this.latPickup = latPickup;
		this.lonPickup = lonPickup;
		this.latDropoff = latDropoff;
		this.lonDropoff = lonDropoff;
		this.partySize = partySize;
		this.requestTime = requestTime;
		this.luggage = luggage;
		this.name = name;
		this.email = email;
		this.neighborhood = neighborhood;
	}
	
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
	
	public RideRequestMessage() {
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

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
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

	public double getLatPickup() {
		return latPickup;
	}

	public void setLatPickup(double latPickup) {
		this.latPickup = latPickup;
	}

	public double getLonPickup() {
		return lonPickup;
	}

	public void setLonPickup(double lonPickup) {
		this.lonPickup = lonPickup;
	}

	public double getLatDropoff() {
		return latDropoff;
	}

	public void setLatDropoff(double latDropoff) {
		this.latDropoff = latDropoff;
	}

	public double getLonDropoff() {
		return lonDropoff;
	}

	public void setLonDropoff(double lonDropoff) {
		this.lonDropoff = lonDropoff;
	}

	public int getPartySize() {
		return partySize;
	}

	public void setPartySize(int partySize) {
		this.partySize = partySize;
	}

	public long getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(long requestTime) {
		this.requestTime = requestTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RideRequest.LuggageSize getLuggage() {
		return luggage;
	}

	public void setLuggage(RideRequest.LuggageSize luggage) {
		this.luggage = luggage;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNeighborhood() {
		return neighborhood;
	}

	public void setNeighborhood(String neighborhood) {
		this.neighborhood = neighborhood;
	}
}
