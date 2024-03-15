package com.orange.datagen;

import com.orange.datagen.util.Pair;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 *
 */
public final class CsvGenerator {

  private static final Logger logger = LoggerFactory.getLogger(CsvGenerator.class);
  private static CsvGenerator instance;
  private String delimiter;

  private CsvGenerator(String delim) {
    delimiter = delim;
  }

  public static CsvGenerator getInstance(String delim) {
    if (instance == null) { //lazy init
      instance = new CsvGenerator(delim);
    }
    return instance;
  }

  public String generateHeader(String template) {
    try {
      JsonObject jsonObject = new JsonParser().parse(template).getAsJsonObject();
      StringBuilder header = new StringBuilder();
      for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
        header.append(delimiter).append(entry.getKey());
      }
      return header.substring(1); //strip heading comma
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public String generateBody(String template) {
    try {
      JsonObject jsonObject = new JsonParser().parse(template).getAsJsonObject();
      StringBuilder body = new StringBuilder();
      for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
        Pair primitive = JsonGenerator.genPrimitives(entry.getValue(), 0);
        body.append(delimiter).append(primitive.getFirst().toString());
      }
      return body.substring(1); //strip heading comma
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public static void main(String[] args) throws Exception {

    int eventCount = 5; // default event count
    String template = "";
    String delimiter = ","; //default comma, configure for pipe '|' etc.
    ArgumentParser ap = ArgumentParsers.newFor(JsonGenerator.class.getSimpleName()).build().defaultHelp(true);
    ap.addArgument("-t", "--template")
        .required(true).help("template for generating synthetic data");
    ap.addArgument("-n", "--number")
        .required(false).help("number of events to generate");
    ap.addArgument("-d", "--delimiter")
        .required(false).help("delimiter to separate column");
    Namespace ns = ap.parseArgs(args);

    File temp = new File(ns.getString("template"));
    if (temp.exists()) {
      template = new String(Files.readAllBytes(Paths.get(ns.getString("template"))));
    } else {
      URL url = Resources.getResource("./templates/template.json");
      template = Resources.toString(url, Charsets.UTF_8);
    }
    eventCount = ns.getString("number") != null ? Integer.parseInt(ns.getString("number")) : 5;
    delimiter = ns.getString("delimiter");
    CsvGenerator csvGenerator = new CsvGenerator(delimiter);
    logger.info(csvGenerator.generateHeader(template));
    for (int i = 1; i <= eventCount; i++) {
      logger.info(csvGenerator.generateBody(template));
    }
  }

}
