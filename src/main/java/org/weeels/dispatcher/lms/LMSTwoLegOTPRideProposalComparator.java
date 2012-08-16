package org.weeels.dispatcher.lms;

import org.springframework.stereotype.Component;
import org.weeels.dispatcher.domain.*;

@Component
public class LMSTwoLegOTPRideProposalComparator extends RideProposalComparator {
	
	public LMSTwoLegOTPRideProposalComparator() {
	}
	
	public static final double MAX_DIST = 1.5;
	public static final double BEST_DIST = 0.5;
	
	private double pseudoFareSavings(Itinerary itinerary, RideRequest request) {
		return itinerary.getSoloDurationFor(request) 
				+ .5 * itinerary.getSharedDurationFor(request) 
				- request.getSoloDuration();
	}
	
	// Assume we're only comparing eligible itineraries
	@Override
	public int compare(RideProposal p1, RideProposal p2) {
		double savings1 = pseudoFareSavings(p1.getItinerary(), p1.getRideRequest());
		if(p1.getRideBookingToUpdate() != null)
			savings1 += pseudoFareSavings(p1.getItinerary(), p1.getRideBookingToUpdate().getRideRequests().get(0));
		double savings2 = pseudoFareSavings(p2.getItinerary(), p2.getRideRequest());
		if(p2.getRideBookingToUpdate() != null)
			savings2 += pseudoFareSavings(p2.getItinerary(), p2.getRideBookingToUpdate().getRideRequests().get(0));
		
		return (int) (100.0 * (savings1 - savings2));
	}

}
