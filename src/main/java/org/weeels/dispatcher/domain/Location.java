package org.weeels.dispatcher.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Location {
	public static final Location LAGUARDIA = new Location(40.770739, -73.865199);

    public Location(double lon, double lat) {
		super();
		this.lon = lon;
		this.lat = lat;
	}

	private double lon, lat;

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Location fromJsonToLocation(String json) {
        return new JSONDeserializer<Location>().use(null, Location.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Location> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Location> fromJsonArrayToLocations(String json) {
        return new JSONDeserializer<List<Location>>().use(null, ArrayList.class).use("values", Location.class).deserialize(json);
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
