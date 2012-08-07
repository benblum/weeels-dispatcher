package org.weeels.dispatcher.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Itinerary {
	
	private Location destination;
	
	public Itinerary() {
		
	}
	
	public Itinerary(Itinerary other) {
		this.stops = new ArrayList<Stop>(other.stops);
	}

	private List<Stop> stops = new ArrayList<Stop>();

	public List<Stop> getStops() {
        return this.stops;
    }

	public void setStops(List<Stop> stops) {
        this.stops = stops;
        destination = stops.get(stops.size()-1).getLocation();
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Itinerary fromJsonToItinerary(String json) {
        return new JSONDeserializer<Itinerary>().use(null, Itinerary.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Itinerary> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Itinerary> fromJsonArrayToItinerarys(String json) {
        return new JSONDeserializer<List<Itinerary>>().use(null, ArrayList.class).use("values", Itinerary.class).deserialize(json);
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public void setDestination(Location destination) {
		this.destination = destination;
	}
	
	public Location getDestination() {
		return destination;
	}
	
	public void addStop(Stop stop) {
		stops.add(stop);
		destination = stop.getLocation();
	}

	public Stop getStop(int i) {
		return stops.get(i);
	}
}
