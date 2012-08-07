package org.weeels.dispatcher.lms.message;

public class ExpireMatchMessage {
	protected String matchId;
	protected boolean canceled;
	
	public ExpireMatchMessage() {
	}
	
	public ExpireMatchMessage(String matchId, boolean canceled) {
		this.matchId = matchId;
		this.canceled = canceled;
	}

	public String getMatchId() {
		return matchId;
	}

	public void setMatchId(String matchId) {
		this.matchId = matchId;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
	
	
}
