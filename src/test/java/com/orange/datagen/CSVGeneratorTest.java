package com.orange.datagen;

import org.junit.Test;
import org.junit.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Unit test for CSV generation.
 */
public class CSVGeneratorTest {

  /*@Test
  public void testCSV() {
    try {
      String template = new String(Files.readAllBytes(
              Paths.get("src/main/resources/templates/template.json")));
      CsvGenerator csvGenerator = CsvGenerator.getInstance("|");
      String header = csvGenerator.generateHeader(template);
      List<String> columns = Arrays.asList("boolean", "int", "long", "double", "datetime", "alpha", "alphanumeric",
          "sequence", "sequence_start", "int_range", "long_range", "double_range", "datetime_seq", "datetime_range",
          "concat_int", "concat_int_range", "concat_double", "uuid", "random", "nested");
      String expected = String.join("|", columns);
      Assert.assertEquals(expected, header);
    } catch (IOException e) {
      Assert.fail("Exception: " + e);
    }
  }*/

}
