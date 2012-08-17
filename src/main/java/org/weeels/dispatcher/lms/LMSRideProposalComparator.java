package org.weeels.dispatcher.lms;

import org.springframework.stereotype.Component;
import org.weeels.dispatcher.domain.*;

//@Component
public class LMSRideProposalComparator extends RideProposalComparator {
	
	public LMSRideProposalComparator() {
	}
	
	public static final double MAX_DIST = 1.5;
	public static final double BEST_DIST = 0.5;
	
	// Assume we're only comparing eligible itineraries
	@Override
	public int compare(RideProposal p1, RideProposal p2) {
		// Make up some kind of score combining order in line with time saved etc. 
		
	//	double destSep1 = geoDistance(request.getDropoff(), p1.getDestination());
	//	double destSep2 = geoDistance(request.getDropoff(), p2.getDestination());
		if(p1.getRideBookingToUpdate() == null || p2.getRideBookingToUpdate() == null) {
			if(p1.getRideBookingToUpdate() != null)
				return -1;
			else if(p2.getRideBookingToUpdate() != null)
				return 1;
			else 
				return 0;
		}
			
		int timeDiff = (int)(p1.getRideBookingToUpdate().getRideRequests().get(0).getRequestTime() 
				- p2.getRideBookingToUpdate().getRideRequests().get(0).getRequestTime() );
		
		return timeDiff;
	}
	
	private double geoDistance(Location loc1, Location loc2) {
		double x = (loc1.getLon() - loc2.getLon()) * 52.34;
		double y = (loc1.getLat() - loc2.getLat()) * 69.12;
		//System.out.println(Math.sqrt(x*x+y*y));
		return Math.sqrt(x * x + y * y);
	}
}
