package org.weeels.dispatcher.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document
public @Data class Rider {

    private String name;
    private String email;
	@Id
    private String id;

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Rider fromJsonToRider(String json) {
        return new JSONDeserializer<Rider>().use(null, Rider.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Rider> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Rider> fromJsonArrayToRiders(String json) {
        return new JSONDeserializer<List<Rider>>().use(null, ArrayList.class).use("values", Rider.class).deserialize(json);
    }
}
