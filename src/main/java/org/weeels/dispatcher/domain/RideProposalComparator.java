package org.weeels.dispatcher.domain;

import java.util.Comparator;

public abstract class RideProposalComparator implements Comparator<RideProposal> {
	public abstract int compare(RideProposal p1, RideProposal p2);
}
