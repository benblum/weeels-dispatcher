package org.weeels.dispatcher.nyctpara.message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.weeels.dispatcher.domain.Location;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.domain.RideRequest.LuggageSize;
import org.weeels.dispatcher.domain.Rider;
import lombok.*;

public @Data @NoArgsConstructor @AllArgsConstructor class NYCTParatransitRideRequestMessage {
	private static final double GPS_SCALE = 1E5;
	public String TripDate;
	public String ClientId;
	public String RequestTime;
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
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy hh:mm");
		RideRequest request = new RideRequest();
		request.setInputAddressPickup(OriginHouseNumber + " " + OriginAddress + " " + OriginCity + ", NY " + OriginZip);
		request.setInputAddressDropoff(DestinationHouseNumber + " " + DestinationAddress + " " + DestinationCity + ", NY " + DestinationZip);
		request.setFormattedAddressDropoff(request.getInputAddressDropoff());
		request.setFormattedAddressPickup(request.getInputAddressPickup());
		request.setNumPassengers(1);
		try {
			request.setRequestTime(formatter.parse(TripDate + " " + RequestTime).getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		request.setDropOffLocation(new Location(DestinationGridX/GPS_SCALE, DestinationGridY/GPS_SCALE));
		request.setPickUpLocation(new Location(OriginGridX/GPS_SCALE, OriginGridY/GPS_SCALE));
		request.setLuggage(RideRequest.LuggageSize.low);
		request.setRider(rider);
		return request;
	}
}
