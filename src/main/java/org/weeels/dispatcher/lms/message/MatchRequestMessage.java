package org.weeels.dispatcher.lms.message;

public class MatchRequestMessage {
	public String[] requestIds;
	
	public MatchRequestMessage() {
	}

	public MatchRequestMessage(String requestId0, String requestId1) {
		this.requestIds = new String[2];
		this.requestIds[0] = requestId0;
		this.requestIds[1] = requestId1;
	}
}
