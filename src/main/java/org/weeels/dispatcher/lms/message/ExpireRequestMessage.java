package org.weeels.dispatcher.lms.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public @Data @NoArgsConstructor @AllArgsConstructor class ExpireRequestMessage {
	private String requestId;
	private boolean canceled;
}
