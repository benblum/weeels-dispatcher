package org.weeels.dispatcher.lms.message;

import lombok.*;

public @Data @NoArgsConstructor class StateMessage {
	public RideRequestMessage[] requests;
	public MatchMessage[] matches;
}
