package org.weeels.dispatcher.lms.message;

import lombok.*;
import org.weeels.dispatcher.domain.RideBooking;

public @Data @NoArgsConstructor class MatchMessage {
	public String matchId;
	public String[] requestIds;
	
	public MatchMessage(RideBooking ride) {
		matchId = ride.getId();
		requestIds = new String[2];
		requestIds[0] = ride.getRideRequests().get(0).getId().toString();
		requestIds[1] = ride.getRideRequests().get(1).getId().toString();
	}
}
