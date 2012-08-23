package org.weeels.dispatcher.lms;

import org.springframework.stereotype.Component;
import org.weeels.dispatcher.domain.*;

@Component
public class LMSOTPRideProposalComparator extends RideProposalComparator {
	
	public LMSOTPRideProposalComparator() {
	}
	
	public static final double FRACTION_THRESHOLD = 2.0;
	public static final double KILL_PENALTY = 1E6;
	
	
	// Assume we're only comparing eligible itineraries
	// Low scores are better
	@Override
	public int compare(RideProposal p1, RideProposal p2) {
		System.out.println("******TEST******");
		double score1 = p1.getItinerary().getDuration() - p1.getRideRequest().getSoloDuration();
//		System.out.println("P1: " + p1.getItinerary().getDurationFor(p1.getRideRequest()) + " versus " + p1.getRideRequest().getSoloDuration());
		if(p1.getItinerary().getDurationFor(p1.getRideRequest()) > FRACTION_THRESHOLD * p1.getRideRequest().getSoloDuration())
			score1 += KILL_PENALTY;
		if(p1.getRideBookingToUpdate() != null) {
			RideRequest r1 = p1.getRideBookingToUpdate().getRideRequests().get(0);
			score1 -= r1.getSoloDuration();
//			System.out.println("P1: " + p1.getItinerary().getDurationFor(r1) + " versus " + r1.getSoloDuration());
			
			if(p1.getItinerary().getDurationFor(r1) > FRACTION_THRESHOLD * r1.getSoloDuration())
				score1 += KILL_PENALTY;
		}
		double score2 = p2.getItinerary().getDuration() - p2.getRideRequest().getSoloDuration();
//		System.out.println("P2: " + p2.getItinerary().getDurationFor(p2.getRideRequest()) + " versus " + p2.getRideRequest().getSoloDuration());
		if((double)p2.getItinerary().getDurationFor(p1.getRideRequest()) > FRACTION_THRESHOLD * (double)p2.getRideRequest().getSoloDuration())
			score2 += KILL_PENALTY;
		if(p2.getRideBookingToUpdate() != null) {
			RideRequest r2 = p2.getRideBookingToUpdate().getRideRequests().get(0);
			score2 -= r2.getSoloDuration();
//			System.out.println("P2: " + p2.getItinerary().getDurationFor(r2) + " versus " + r2.getSoloDuration());
			
			if(p2.getItinerary().getDurationFor(r2) > FRACTION_THRESHOLD * r2.getSoloDuration())
				score2 += KILL_PENALTY;
		}
		
//		System.out.println("For " + p1.getRideBookingToUpdate() + " and " + p1.getRideRequest().getRider().getName() + ": " + score1);
//		System.out.println("For " + p2.getRideBookingToUpdate() + " and " + p2.getRideRequest().getRider().getName() + ": " + score2);
		return (int) (100.0 * (score1 - score2));
	}

}
