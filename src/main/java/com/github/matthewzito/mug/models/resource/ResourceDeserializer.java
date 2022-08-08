package com.github.matthewzito.mug.models.resource;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ResourceDeserializer implements JsonDeserializer<Resource> {
  @Override
  public Resource deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject jsonObject = json.getAsJsonObject();

    return new Resource(
        jsonObject.get("id").getAsString(),
        jsonObject.get("data").getAsString());
  }
}
