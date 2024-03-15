package com.orange.datagen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.util.Map;

/**
 *
 */
public class JsonGeneratorTest {

  @Test
  public void genEmpty() {
    String template = "{}";
    assertEquals(template, getRandom(template, 1, 0));
  }

  @Test
  public void genNullElements() {
    String template = "{\"element#1\":null,\"element#2\":null,\"element#3\":{}}";
    assertEquals(template, getRandom(template, 1, 0));
  }

  @Test
  public void genPrimitives() {
    String template = "{\"boolean\":false, \"int\":\"int(1,10)\", \"long\":\"long(" +
            "229999999999,889999999999)\", \"double\":\"double(" +
            Double.MIN_VALUE + "," + Double.MAX_VALUE + ")\"}";
    String generated = getRandom(template, 1, 0);
    JsonObject jsonObject = new JsonParser().parse(generated).getAsJsonObject();
    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
      switch(entry.getKey()) {
        case "boolean":
          assertTrue(entry.getValue().getAsBoolean() || !entry.getValue().getAsBoolean());
          break;
        case "int":
          assertTrue(entry.getValue().getAsInt() >= 1 && entry.getValue().getAsInt() <= 10);
          break;
        case "long":
          // TODO(rajkumar): check if the value is long
          break;
        case "double":
          // TODO(rajkumar): check if the value is double
          break;
        default:
      }
    }
  }

  @Test
  public void genJsonObjectEventCount() {
    String innerTemplate1 = "{\"state\":\"california\",\"shortcut\":\"ca\"," +
            "\"cities\":{\"capitol\":\"sacramento\"," +
            "\"tier1\":[\"los angeles\",\"san francisco\",\"san diego\"]," +
            "\"tier2\":[\"san jose\",\"redding\",\"santa barbara\",\"fresno\"]}}";
    String innerTemplate2 = "{\"state\":\"florida\",\"shortcut\":\"fl\"," +
            "\"cities\":{\"capitol\":\"Tallahassee\"," +
            "\"tier1\":[\"miami\",\"orlando\",\"jacksonville\",\"tampa\"]," +
            "\"tier2\":[\"clearwater\",\"st.petersberg\",\"west palm beach\",\"for myers\"]}}";
    String template = "[" + innerTemplate1 + "," + innerTemplate2 + "]";
    assertEquals(template, getRandom(template, 1, 1));
  }

  private String getRandom(String template, int eventCount, int nestedArraySize) {
    JsonGenerator jsonGenerator = new JsonGenerator();
    return jsonGenerator.generate(template, eventCount, nestedArraySize).toString();
  }
}
