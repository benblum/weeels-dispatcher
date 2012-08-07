package org.weeels.dispatcher.controller;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.stereotype.Component;
import org.weeels.dispatcher.domain.RideBooking;
import org.weeels.dispatcher.domain.Itinerary;
import org.weeels.dispatcher.domain.Location;
import org.weeels.dispatcher.domain.RideProposal;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.domain.Rider;
import org.weeels.dispatcher.domain.Stop;
import org.weeels.dispatcher.repository.RideBookingRepository;
import org.weeels.dispatcher.repository.RideRequestRepository;
import org.weeels.dispatcher.repository.RiderRepository;

/**
 * A central place to register application converters and formatters. 
 */
@Component
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

	@Autowired
    RideBookingRepository rideBookingRepository;

	@Autowired
    RideRequestRepository rideRequestRepository;

	@Autowired
    RiderRepository riderRepository;
	
	@Autowired
	MongoOperations mongoTemplate;

	@Override
	protected void installFormatters(FormatterRegistry registry) {
		super.installFormatters(registry);
		// Register application converters and formatters
	}
	
    public Converter<Stop, String> getStopToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<org.weeels.dispatcher.domain.Stop, java.lang.String>() {
            public String convert(Stop stop) {
                return new StringBuilder().append(stop.getAddress()).toString();
            }
        };
    }
    
    public Converter<RideRequest, String> getRideRequestToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<org.weeels.dispatcher.domain.RideRequest, java.lang.String>() {
            public String convert(RideRequest rideRequest) {
                return new StringBuilder().append(rideRequest.getRider().getName()).append(" ").append(rideRequest.getRequestTime()).toString();
            }
        };
    }
    
    public Converter<Location, String> getLocationToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<org.weeels.dispatcher.domain.Location, java.lang.String>() {
            public String convert(Location location) {
                return new StringBuilder("Lat: ").append(location.getLat()).append(" Lon: ").append(location.getLon()).toString();
            }
        };
    }
    
    public Converter<Itinerary, String> getItineraryToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<org.weeels.dispatcher.domain.Itinerary, java.lang.String>() {
            public String convert(Itinerary itinerary) {
            	StringBuilder builder = new StringBuilder();
            	Iterator<Stop> it = itinerary.getStops().iterator();
            	if(it.hasNext())
            		builder.append(it.next().getAddress());
            	while(it.hasNext()) {
            		builder.append(" --> ").append(it.next().getAddress());
            	}
                return builder.toString();
            }
        };
    }
   
    /*
    public Converter<String, RideProposal> getIdToRideProposalConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, org.weeels.dispatcher.domain.RideProposal>() {
            public org.weeels.dispatcher.domain.RideProposal convert(java.lang.String id) {
                return rideRequestRepository.findOneRideProposal(id);
            }
        };
    }
    */

	public Converter<RideBooking, String> getRideBookingToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<org.weeels.dispatcher.domain.RideBooking, java.lang.String>() {
            public String convert(RideBooking rideBooking) {
                return new StringBuilder().append(rideBooking.getItinerary().getDestination()).toString();
            }
        };
    }

	public Converter<String, RideBooking> getIdToRideBookingConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, org.weeels.dispatcher.domain.RideBooking>() {
            public org.weeels.dispatcher.domain.RideBooking convert(java.lang.String id) {
                return rideBookingRepository.findOne(id);
            }
        };
    }

	/*
	public Converter<String, Location> getIdToLocationConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, org.weeels.dispatcher.domain.Location>() {
            public org.weeels.dispatcher.domain.Location convert(java.lang.String id) {
                return locationRepository.findOne(id);
            }
        };
    }
    */

	public Converter<RideProposal, String> getRideProposalToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<org.weeels.dispatcher.domain.RideProposal, java.lang.String>() {
            public String convert(RideProposal rideProposal) {
                return new StringBuilder().append(rideProposal.getFare()).toString();
            }
        };
    }

	public Converter<String, RideRequest> getIdToRideRequestConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, org.weeels.dispatcher.domain.RideRequest>() {
            public org.weeels.dispatcher.domain.RideRequest convert(java.lang.String id) {
                return rideRequestRepository.findOne(id);
            }
        };
    }

	public Converter<Rider, String> getRiderToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<org.weeels.dispatcher.domain.Rider, java.lang.String>() {
            public String convert(Rider rider) {
                return new StringBuilder().append(rider.getName()).toString();
            }
        };
    }

	public Converter<String, Rider> getIdToRiderConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, org.weeels.dispatcher.domain.Rider>() {
            public org.weeels.dispatcher.domain.Rider convert(java.lang.String id) {
                return riderRepository.findOne(id);
            }
        };
    }
/*
	public Converter<String, Stop> getIdToStopConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, org.weeels.dispatcher.domain.Stop>() {
            public org.weeels.dispatcher.domain.Stop convert(java.lang.String id) {
                return stopRepository.findOne(id);
            }
        };
    }
    */

	public void installLabelConverters(FormatterRegistry registry) {
        registry.addConverter(getRideBookingToStringConverter());
        registry.addConverter(getIdToRideBookingConverter());
        registry.addConverter(getItineraryToStringConverter());
        registry.addConverter(getLocationToStringConverter());
        registry.addConverter(getRideProposalToStringConverter());
        registry.addConverter(getRideRequestToStringConverter());
        registry.addConverter(getIdToRideRequestConverter());
        registry.addConverter(getRiderToStringConverter());
        registry.addConverter(getIdToRiderConverter());
        registry.addConverter(getStopToStringConverter());
    }

	public void afterPropertiesSet() {
        super.afterPropertiesSet();
        installLabelConverters(getObject());
    }
}
