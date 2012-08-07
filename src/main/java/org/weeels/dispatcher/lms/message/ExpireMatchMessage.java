package org.weeels.dispatcher.lms.message;

import lombok.*;

public @Data @NoArgsConstructor @AllArgsConstructor class ExpireMatchMessage {
	protected String matchId;
	protected boolean canceled;
}
