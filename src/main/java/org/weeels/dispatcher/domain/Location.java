package org.weeels.dispatcher.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.*;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public @Data @AllArgsConstructor @NoArgsConstructor class Location {
	private double lon, lat;

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
