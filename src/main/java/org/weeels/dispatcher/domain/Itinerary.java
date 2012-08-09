package org.weeels.dispatcher.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;


public @Data @NoArgsConstructor class Itinerary {
	
	private Location destination;
	private Location origin;
	private List<Stop> stops = new ArrayList<Stop>();
	
	public Itinerary(Itinerary other) {
		setStops(other.stops);
	}

	public void addStop(Stop stop) {
		stops.add(stop);
		if(stops.size() == 1)
			origin = stop.getLocation();
		destination = stop.getLocation();
	}

	public Stop getStop(int i) {
		return stops.get(i);
	}
	
	public void setStops(List<Stop> stops) {
        this.stops = stops;
        origin = stops.get(0).getLocation();
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

}
