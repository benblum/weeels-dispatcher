package org.weeels.dispatcher.lms.message;

import lombok.*;

public @Data @NoArgsConstructor class StateMessage {
	public RideRequestResponseMessage[] requests;
	public MatchMessage[] matches;
}
