package com.orange.datagen;

import com.orange.datagen.util.Pair;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 *
 */
public final class JsonGenerator {

  private static final Logger logger = LoggerFactory.getLogger(JsonGenerator.class);
  private static int sequence = 1;
  private static ZonedDateTime zonedTimeSequenceStart = null;
  private static ZonedDateTime zonedTimeSequenceEnd = null;

  // Generate a pair for a primitive data type
  protected static Pair genPrimitives(JsonElement jsonElement, int arraySize) {
    if (jsonElement.getAsJsonPrimitive().isBoolean()) {
      return new Pair(RandomData.getRandomBoolean(), Boolean.class);
    } else if (jsonElement.getAsJsonPrimitive().isString()) {
      return evalFunction(jsonElement.toString(), arraySize);
    } else if (jsonElement.getAsJsonPrimitive().isNumber()) {
      return new Pair(RandomData.getRandomInt(), Integer.class);
    } else if (jsonElement.isJsonNull()) {
      return new Pair(null, null);
    }
    return new Pair(jsonElement.toString(), String.class);
  }

  private static ZonedDateTime addSubDateTime(ZonedDateTime dateTime, String part, int offset) {
    switch(part) {
      case "millisecond":
        return dateTime.plusNanos(offset);
      case "second":
        return dateTime.plusSeconds(offset);
      case "minute":
        return  dateTime.plusMinutes(offset);
      case "hour":
        return dateTime.plusHours(offset);
      case "day":
        return dateTime.plusDays(offset);
      case "month":
        return dateTime.plusMonths(offset);
      case "year":
        return dateTime.plusYears(offset);
      default:
    }
    return dateTime;
  }

  private static Pair evalFunction(String value, int arraySize) {
    if (value.contains("(")) {
      String subValue = value.substring(value.indexOf("(") + 1, value.lastIndexOf(")"));
      String[] args = subValue.split(",");
      if (value.startsWith("\"random(") || value.startsWith("random(")) {
        return new Pair(RandomData.getNextRandomValue(args), String.class);
      } else if (value.startsWith("\"json(") || value.startsWith(("json("))) {
        String subJson = subValue.substring(0, subValue.lastIndexOf("}") + 1);
        String[] jsonArgs = subValue.substring(subValue.lastIndexOf("}") + 2).split(",");
        return new Pair(generateEvents(RandomData.stripBackslash(subJson), Long.parseLong(jsonArgs[0].trim()),
            Integer.parseInt(jsonArgs[1].trim())).get(0), String.class);
      } else if (value.startsWith("\"int(") || value.startsWith("int")) {
        return new Pair(RandomData.getRandomInt(args), Integer.class);
      } else if (value.startsWith("\"long(") || value.startsWith("long")) {
        return new Pair(RandomData.getRandomLong(args), Long.class);
      } else if (value.startsWith("\"double(") || value.startsWith("double")) {
        return new Pair(RandomData.getRandomDouble(args), Double.class);
      } else if (value.startsWith("\"datetime_seq(") || value.startsWith("datetime_seq(")) {
        // args[0] - start datetime or now-offset, args[1] - end datetime or now
        // args[2] - input date format
        // args[3] - timezone id, eg: "America/Los_Angeles"
        // args[4] - increment second/minute/hour/day/month/year
        int incrementOffset = 1; // default increment value
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(args[2]);

        // initialize start and end sequence datetime first
        if (zonedTimeSequenceStart == null) {
          zonedTimeSequenceStart = ZonedDateTime.now(ZoneId.of(args[3]));
          zonedTimeSequenceEnd = ZonedDateTime.now(ZoneId.of(args[3]));
        }
        if (args[0].startsWith("now")) { // now or now-5 or now-10
          String[] startOffset = args[0].split("-");
          if (startOffset.length == 2) {
            zonedTimeSequenceStart = addSubDateTime(ZonedDateTime.now(ZoneId.of(args[3])), args[3],
                -1 * Integer.parseInt(startOffset[1]));
          }
        }

        if (zonedTimeSequenceStart.isBefore(zonedTimeSequenceEnd)) {
          zonedTimeSequenceStart = addSubDateTime(zonedTimeSequenceEnd, args[4], incrementOffset);
        }
        return new Pair(zonedTimeSequenceStart.format(formatter), String.class);
      } else if (value.startsWith("\"datetime(") || value.startsWith("datetime(")) {
        // args[0] - now+/-offset
        // args[1] - input date format
        // args[2] - timezone id, eg: "America/Los_Angeles"
        // args[3] - increment second/minute/hour/day/month/year
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(args[2]));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(args[1]);
        if (args[0].startsWith("now")) { // now or now+5 or now-10
          if (args[0].contains("-")) {
            String[] startOffset = args[0].split("-");
            if (startOffset.length == 2) {
              zonedDateTime = addSubDateTime(ZonedDateTime.now(ZoneId.of(args[2])), args[3],
                  -1 * Integer.parseInt(startOffset[1]));
            }
          } else if (args[0].contains("+")) {
            String[] startOffset = args[0].split("\\+");
            if (startOffset.length == 2) {
              zonedDateTime = addSubDateTime(ZonedDateTime.now(ZoneId.of(args[2])), args[3],
                  Integer.parseInt(startOffset[1]));
            }
          }
        }

        return new Pair(zonedDateTime.format(formatter), String.class);
      } else if (value.startsWith("\"datetime:simple(")) {
        return new Pair(RandomData.getRandomDateTime(args, RandomData.datetimeFormat), String.class);
      } else if (value.startsWith("\"date(")) {
        return new Pair(RandomData.getRandomDateTime(args, RandomData.dateFormatDash), String.class);
      } else if (value.startsWith("\"alpha(") || value.startsWith("alpha(")) {
        return new Pair(RandomData.getNextRandomAlpha(Integer.parseInt(args[0])), String.class);
      } else if (value.startsWith("\"alphanumeric(") || value.startsWith("alphanumeric(")) {
        return new Pair(RandomData.getNextRandomAlphaNumeric(Integer.parseInt(args[0])), String.class);
      } else if (value.startsWith("\"qs(") || value.startsWith("qs(")) {
        int startIndex = value.indexOf("(") + 1;
        int endIndex = value.lastIndexOf(")");
        String p = value.substring(startIndex, endIndex);
        return new Pair("\"" + evalFunction(p, arraySize).getFirst() + "\"", String.class);
      } else if (value.startsWith("\"cat(") || value.startsWith("cat(")) {
        StringBuilder builder = new StringBuilder();
        int startIndex = value.indexOf("(") + 1;
        int endIndex = value.lastIndexOf(")");
        String p = value.substring(startIndex, endIndex);
        for (String param : splitArgs(p, ',')) {
          if (param.equals("SPACE")) { // HACK: for whitespace
            builder.append(" ");
          } else if (param.equals("COMMA")) { // for comma
            builder.append(", ");
          } else {
            builder.append(evalFunction(param, arraySize).getFirst());
          }
        }
        return new Pair(builder.toString(), String.class);
      } else if (value.startsWith("\"seq(")) {
        if (sequence == 1) {
          int startIndex = value.indexOf("(") + 1;
          int endIndex = value.lastIndexOf(")");
          sequence = Integer.parseInt(value.substring(startIndex, endIndex));
        }
        return new Pair(sequence++, Integer.class);
      } else if (value.startsWith("\"upper(") || value.startsWith("upper(")) {
        int startIndex = value.indexOf("(") + 1;
        int endIndex = value.lastIndexOf(")");
        String p = value.substring(startIndex, endIndex);
        return new Pair(evalFunction(p, arraySize).getFirst().toString().toUpperCase(), String.class);
      } else if (value.startsWith("\"lower(") || value.startsWith("lower(")) {
        int startIndex = value.indexOf("(") + 1;
        int endIndex = value.lastIndexOf(")");
        String p = value.substring(startIndex, endIndex);
        return new Pair(evalFunction(p, arraySize).getFirst().toString().toLowerCase(), String.class);
      } else {
        // return the passed value as is
        // logger.warn("invalid format, generated as is");
        return new Pair(RandomData.stripQuotes(value), String.class);
      }
    } else {
      switch (RandomData.stripQuotes(value)) {
        case "int":
          return new Pair(RandomData.getRandomInt(), Integer.class);
        case "long":
          return new Pair(RandomData.getRandomLong(), Long.class);
        case "double":
          return new Pair(RandomData.getRandomDouble(), Double.class);
        case "datetime":
          return new Pair(RandomData.getRandomDateTime(RandomData.dateFormatISO), String.class);
        case "date":
          return new Pair(RandomData.getRandomDateTime(RandomData.dateFormatDash), String.class);
        case "alpha":
          return new Pair(RandomData.getNextRandomAlpha(), String.class);
        case "alphanumeric":
          return new Pair(RandomData.getNextRandomAlphaNumeric(), String.class);
        case "uuid":
          return new Pair(UUID.randomUUID().toString(), String.class);
        case "seq":
          return new Pair(sequence++, Integer.class);
        default:
          return new Pair(RandomData.stripQuotes(value), String.class);
      }
    }
  }

  private static String[] splitArgs(String value, char delimiter) {
    List<String> result = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    int nest = 0;
    for (int i = 0; i < value.length(); ++i) {
      char ch = value.charAt(i);
      switch (ch) {
        case ',':
          if (nest == 0) {
            result.add(sb.toString().trim());
            sb.setLength(0);
            continue;
          }
          break;
        case '(':
          ++nest;
          break;
        case ')':
          --nest;
          break;
        default:
      }
      sb.append(ch);
    }
    result.add(sb.toString().trim());
    return result.toArray(new String[result.size()]);
  }

  public static Object generate(String template, int eventCount, int nestedArraySize) {
    try {
      JsonElement jsonElement = new JsonParser().parse(template);
      if (jsonElement.isJsonArray()) {
        return genRandomJsonArray(jsonElement.getAsJsonArray(), nestedArraySize);
      } else if (jsonElement.isJsonObject()) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= eventCount; i++) {
          builder.append(genRandomJsonObject(jsonObject, nestedArraySize));
          if (i < eventCount) {
            builder.append("\n");
          }
        }
        return builder;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public static List<String> generateEvents(String template, long eventCount, int nestedArraySize) {
    List<String> events = new ArrayList<>();
    try {
      JsonElement jsonElement = new JsonParser().parse(template);
      if (jsonElement.isJsonObject()) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (long i = 1; i <= eventCount; i++) {
          events.add(genRandomJsonObject(jsonObject, nestedArraySize).toString());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return events;
  }

  // Generate random json array to number of event count
  private static JsonArray genRandomJsonArray(JsonArray jsonArray, int arraySize) {
    JsonArray outArray = new JsonArray();
    for (int i = 1; i <= arraySize; i++) {
      for (JsonElement element : jsonArray) {
        if (element.isJsonObject()) {
          outArray.add(genRandomJsonObject(element.getAsJsonObject(), arraySize));
        } else if (element.isJsonPrimitive()) {
          Pair primitive = genPrimitives(element, arraySize);
          if (primitive.getSecond() == Integer.class || primitive.getSecond() == Long.class ||
              primitive.getSecond() == Double.class || primitive.getSecond() == Boolean.class) {
            outArray.add((Number) primitive.getFirst());
          } else if (primitive.getSecond() == String.class) {
            outArray.add(primitive.getFirst().toString());
          } else {
            // TODO: extend for other custom types
            // Throw error for now.
            logger.error("invalid type, not supported");
          }
        }
      }
    }
    return outArray;
  }

  // Generate a single json object
  private static JsonObject genRandomJsonObject(JsonObject jsonObject, int arraySize) {
    JsonObject out = new JsonObject();
    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
      if (entry.getValue().isJsonArray()) {
        out.add(entry.getKey(), genRandomJsonArray(entry.getValue().getAsJsonArray(), arraySize));
      } else if (entry.getValue().isJsonObject()) {
        out.add(entry.getKey(), genRandomJsonObject(entry.getValue().getAsJsonObject(), arraySize));
      } else if (entry.getValue().isJsonPrimitive()) {
        Pair primitive = genPrimitives(entry.getValue(), arraySize);
        if (primitive.getSecond() == Integer.class || primitive.getSecond() == Long.class ||
            primitive.getSecond() == Double.class || primitive.getSecond() == Boolean.class) {
          out.add(entry.getKey(), new Gson().toJsonTree(primitive.getFirst()));
        } else if (primitive.getSecond() == String.class) {
          out.addProperty(entry.getKey(), primitive.getFirst().toString());
        } else {
          // TODO: extend for other custom types
          // Throw error for now.
          logger.error("invalid type, not supported");
        }
      } else if (entry.getValue().isJsonNull()) {
        out.add(entry.getKey(), null);
      } else { // custom class type
        if (entry.getValue().isJsonObject()) {
          out.add(entry.getKey(), genRandomJsonObject(entry.getValue().getAsJsonObject(), arraySize));
        }
      }
    }
    return out;
  }

  public static class JsonGeneratorCallable implements Callable<List<String>> {
    private String template;
    private long eventCount;
    private int nestedArraySize;

    public JsonGeneratorCallable(String template, long eventCount, int nestedArraySize) {
      this.template = template;
      this.eventCount = eventCount;
      this.nestedArraySize = nestedArraySize;
    }

    @Override
    public List<String> call() {
      return generateEvents(template, eventCount, nestedArraySize);
    }

  }

  public static void main(String[] args) throws Exception {
    ArgumentParser ap = ArgumentParsers.newFor(JsonGenerator.class.getSimpleName()).build().defaultHelp(true);
    ap.addArgument("-t", "--template")
        .required(true).help("template for generating synthetic data");
    ap.addArgument("-n", "--number")
        .required(false).help("number of events to generate");
    ap.addArgument("-a", "--nested")
        .required(false).help("nested array size (if any)");
    ap.addArgument("-x", "--threads")
        .required(false).help("number of threads");
    Namespace ns = ap.parseArgs(args);

    int eventCount = 5; // default event count
    int nestedArraySize = 1; // default nested array size
    int threadCount = 2; // default thread count
    String template;
    File temp = new File(ns.getString("template"));
    if (temp.exists()) {
      template = new String(Files.readAllBytes(Paths.get(ns.getString("template"))));
    } else {
      URL url = Resources.getResource("./templates/template.json");
      template = Resources.toString(url, Charsets.UTF_8);
    }

    eventCount = ns.getString("number") != null ? Integer.parseInt(ns.getString("number")) : 5;
    nestedArraySize = ns.getString("nested") != null ? Integer.parseInt(ns.getString("nested")) : 0;
    threadCount = ns.getString("threads") != null ? Integer.parseInt(ns.getString("threads")) : 2;

    template = template.replace("DTM_RUNTIME", "2021-09-30T17:53:33.838+0000");
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    Set<Callable<List<String>>> callables = new HashSet<>();
    for (int i = 0; i < threadCount; i++) {
      callables.add(new JsonGeneratorCallable(template, eventCount, nestedArraySize));
    }

    List<Future<List<String>>> futureList = executorService.invokeAll(callables);
    futureList.stream().forEach(s -> {
      try {
        List<String> events = s.get();
        events.stream().forEach(e -> {
          logger.info(e);
        });
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    });

    System.exit(0);
  }

}
