package org.weeels.dispatcher.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Document
public @Data @NoArgsConstructor class Hub {
	@Id
	private String Id;
	private String name;
	private String address;
	private Location location;
	
	public Hub(String name, String address, Location location) {
		this.name = name;
		this.address = address;
		this.location = location;
	}

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Hub fromJsonToHub(String json) {
        return new JSONDeserializer<Hub>().use(null, Hub.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Hub> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Hub> fromJsonArrayToHubs(String json) {
        return new JSONDeserializer<List<Hub>>().use(null, ArrayList.class).use("values", Hub.class).deserialize(json);
    }
}
