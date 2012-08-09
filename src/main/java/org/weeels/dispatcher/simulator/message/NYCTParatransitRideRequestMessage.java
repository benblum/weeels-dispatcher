package org.weeels.dispatcher.simulator.message;

import java.util.Date;

import org.weeels.dispatcher.domain.Location;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.domain.RideRequest.LuggageSize;
import org.weeels.dispatcher.domain.Rider;
import lombok.*;

public @Data @NoArgsConstructor @AllArgsConstructor class NYCTParatransitRideRequestMessage {
	private static final double GPS_SCALE = 1E5;
	public String ClientId;
	public Date RequestTime;
	public String Anchor;
	public String OriginHouseNumber;
	public String OriginAddress;
	public String OriginCity;
	public String OriginZip;
	public double OriginGridX;
	public double OriginGridY;
	public String DestinationHouseNumber;
	public String DestinationAddress;
	public String DestinationCity;
	public String DestinationZip;
	public double DestinationGridX;
	public double DestinationGridY;
	
	
	public RideRequest toRideRequest(Rider rider) {
		RideRequest request = new RideRequest();
		request.setInputAddressPickup(OriginHouseNumber + " " + OriginAddress + " " + OriginCity + ", NY " + OriginZip);
		request.setInputAddressDropoff(DestinationHouseNumber + " " + DestinationAddress + " " + DestinationCity + ", NY " + DestinationZip);
		request.setFormattedAddressDropoff(request.getInputAddressDropoff());
		request.setFormattedAddressPickup(request.getInputAddressPickup());
		request.setNumPassengers(1);
		request.setRequestTime(RequestTime.getTime());
		request.setDropOffLocation(new Location(DestinationGridX/GPS_SCALE, DestinationGridY/GPS_SCALE));
		request.setPickUpLocation(new Location(OriginGridX/GPS_SCALE, OriginGridY/GPS_SCALE));
		request.setLuggage(RideRequest.LuggageSize.low);
		request.setRider(rider);
		return request;
	}
}
