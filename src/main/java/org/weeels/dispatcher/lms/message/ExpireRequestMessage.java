package org.weeels.dispatcher.lms.message;

public class ExpireRequestMessage {
	private String requestId;
	private boolean canceled;

	public ExpireRequestMessage() {
	}

	public ExpireRequestMessage(String requestId, boolean canceled) {
		this.requestId = requestId;
		this.canceled = canceled;
	}
	
	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	
	
}
