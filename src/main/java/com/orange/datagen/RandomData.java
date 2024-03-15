
package com.orange.datagen;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 */
public final class RandomData {

  private static final Logger logger = LoggerFactory.getLogger(RandomData.class);

  private static final int DECIMAL_PLACES = 4;
  static SimpleDateFormat dateFormatISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  static SimpleDateFormat dateFormatYMD = new SimpleDateFormat("yyyy/MM/dd");
  static SimpleDateFormat dateFormatDash = new SimpleDateFormat("yyyy-MM-dd");
  static SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private static Random rand = new Random();
  private static RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

  private RandomData() {
    //not called
  }

  public static String stripQuotes(String s) {
    return s.replaceAll("'", "").replaceAll("\"", "").trim();
  }

  public static String stripBackslash(String s) {
    return s.replaceAll("\\\\", "").trim();
  }

  public static boolean getRandomBoolean() {
    return rand.nextBoolean();
  }

  public static int getRandomInt() {
    return getRandomInt(("0," + Integer.MAX_VALUE).split(","));
  }

  public static int getRandomInt(String[] args) {
    int min = 0;
    int max = Integer.MAX_VALUE;
    if (args.length == 0) {
      min = 0;
      max = Integer.MAX_VALUE;
    } else if (args.length == 1) {
      //min only
      min = Integer.parseInt(args[0].trim());
      max = Integer.MAX_VALUE;
    } else if (args.length == 2) {
      min = Integer.parseInt(args[0].trim());
      max = Integer.parseInt(args[1].trim());
    }
    return getRand().nextInt(min, max);
  }

  public static long getRandomLong() {
    return getRandomLong(("0," + Long.MAX_VALUE).split(","));
  }

  public static long getRandomLong(String[] args) {
    long min = 0;
    long max = Long.MAX_VALUE;
    if (args.length == 0) {
      min = 0;
      max = Long.MAX_VALUE;
    } else if (args.length == 1) {
      //min only
      min = Long.parseLong(args[0].trim());
      max = Long.MAX_VALUE;
    } else if (args.length == 2) {
      min = Long.parseLong(args[0].trim());
      max = Long.parseLong(args[1].trim());
    }
    return getRand().nextLong(min, max);
  }

  public static double getRandomDouble() {
    return getRandomDouble(("0," + Double.MAX_VALUE).split(","));
  }

  public static double getRandomDouble(String[] args) {
    double min = 0;
    double max = Double.MAX_VALUE;
    if (args.length == 0) {
      min = 0;
      max = Double.MAX_VALUE;
    } else if (args.length == 1) {
      //min only
      min = Double.parseDouble(args[0].trim());
      max = Double.MAX_VALUE;
    } else if (args.length == 2) {
      min = Double.parseDouble(args[0].trim());
      max = Double.parseDouble(args[1].trim());
    }

    double range = max - min;
    double scaled = rand.nextDouble() * range;
    double shifted = scaled + min;

    return Precision.round(shifted, DECIMAL_PLACES);
  }

  public static Object getNextRandomValue(String[] args) {
    List<Object> values = new ArrayList<>();
    for (String s : args) {
      try {
        if (s.contains("\"") || s.contains("'")) {
          values.add(stripQuotes(s).trim());
        } else {
          if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false")) {
            values.add(Boolean.parseBoolean(s));
          } else if (s.contains(".")) {
            values.add(Double.parseDouble(s));
          } else {
            values.add(Long.parseLong(s));
          }
        }
      } catch (Throwable t) {
        // error parsing, just assume string then
        // logger.warn("parsing error, just assuming string");
        values.add(stripQuotes(s));
      }
    }
    return values.get(getRand().nextInt(0, values.size() - 1));
  }

  public static String getNextRandomAlpha() {
    return getNextRandomAlpha(20);
  }

  public static String getNextRandomAlpha(int length) {
    return RandomStringUtils.randomAlphabetic(length);
  }

  public static String getNextRandomAlphaNumeric() {
    return getNextRandomAlphaNumeric(20);
  }

  public static String getNextRandomAlphaNumeric(int length) {
    return RandomStringUtils.randomAlphanumeric(length);
  }

  private static String getDefaultDateByFormat(SimpleDateFormat df) {
    if (df.equals(dateFormatISO)) {
      return "1970-01-01T00:00:00.000Z,2018-12-31T23:59:59.000Z";
    } else if (df.equals(dateFormatYMD)) {
      return "1970/01/01,2018/12/31";
    } else if (df.equals(dateFormatDash)) {
      return "2018-05-01,2018-12-31";
    }
    return "";
  }

  public static String getRandomDateTime(SimpleDateFormat dateFormat) {
    return getRandomDateTime(getDefaultDateByFormat(dateFormat).split(","), dateFormat);
  }

  public static String getRandomDateTime(String[] args, SimpleDateFormat dateFormat) {
    Date min = new Date();
    Date max = new Date();
    try {
      if (args.length == 0) {
        String[] defaults = getDefaultDateByFormat(dateFormat).split(",");
        min = dateFormat.parse(defaults[0]);
        max = dateFormat.parse(defaults[1]);
      } else if (args.length == 1) { // only lower boundery
        min = dateFormat.parse(stripQuotes(args[0]).trim());
      } else if (args.length == 2) {
        min = dateFormat.parse(stripQuotes(args[0]).trim());
        max = dateFormat.parse(stripQuotes(args[1]).trim());
      } else { // return current date;
        return getRandomDateTime(dateFormat);
      }
    } catch (ParseException e) {
      logger.error(e.getMessage());
    }
    GregorianCalendar gc = new GregorianCalendar();
    GregorianCalendar minCal = new GregorianCalendar();
    minCal.setTime(min);
    GregorianCalendar maxCal = new GregorianCalendar();
    maxCal.setTime(max);

    int year = getRand().nextInt(minCal.get(GregorianCalendar.YEAR), maxCal.get(GregorianCalendar.YEAR));
    gc.set(GregorianCalendar.YEAR, year);

    int month = -1;
    if (minCal.get(GregorianCalendar.YEAR) == maxCal.get(GregorianCalendar.YEAR)) {
      month = getRand().nextInt(minCal.get(GregorianCalendar.MONTH), maxCal.get(GregorianCalendar.MONTH));
    } else if (year == minCal.get(GregorianCalendar.YEAR)) {
      month = getRand().nextInt(minCal.get(GregorianCalendar.MONTH), gc.getActualMaximum(GregorianCalendar.MONTH));
    } else if (year == maxCal.get(GregorianCalendar.YEAR)) {
      month = getRand().nextInt(gc.getActualMinimum(GregorianCalendar.MONTH), maxCal.get(GregorianCalendar.MONTH));
    } else {
      month = getRand().nextInt(gc.getActualMinimum(GregorianCalendar.MONTH),
          gc.getActualMaximum(GregorianCalendar.MONTH));
    }
    gc.set(GregorianCalendar.MONTH, month);

    int day = -1;
    if (minCal.get(GregorianCalendar.YEAR) == maxCal.get(GregorianCalendar.YEAR) &&
        minCal.get(GregorianCalendar.MONTH) == maxCal.get(GregorianCalendar.MONTH)) {
      day = getRand().nextInt(minCal.get(GregorianCalendar.DAY_OF_MONTH), maxCal.get(GregorianCalendar.DAY_OF_MONTH));
    } else if (year == minCal.get(GregorianCalendar.YEAR) && month == minCal.get(GregorianCalendar.MONTH)) {
      day = getRand().nextInt(minCal.get(GregorianCalendar.DAY_OF_MONTH),
          gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
    } else if (year == maxCal.get(GregorianCalendar.YEAR) && month == maxCal.get(GregorianCalendar.MONTH)) {
      day = getRand().nextInt(gc.getActualMinimum(GregorianCalendar.DAY_OF_MONTH),
          maxCal.get(GregorianCalendar.DAY_OF_MONTH));
    } else {
      day = getRand().nextInt(1, gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
    }
    gc.set(GregorianCalendar.DAY_OF_MONTH, day);

    //generate a random time too
    int minHour = gc.getActualMinimum(GregorianCalendar.HOUR_OF_DAY);
    int minMin = gc.getActualMinimum(GregorianCalendar.MINUTE);
    int minSec = gc.getActualMinimum(GregorianCalendar.SECOND);
    int maxHour = gc.getActualMaximum(GregorianCalendar.HOUR_OF_DAY);
    int maxMin = gc.getActualMaximum(GregorianCalendar.MINUTE);
    int maxSec = gc.getActualMaximum(GregorianCalendar.SECOND);

    if (minCal.get(GregorianCalendar.YEAR) == gc.get(GregorianCalendar.YEAR) &&
        minCal.get(GregorianCalendar.MONTH) == gc.get(GregorianCalendar.MONTH) &&
        minCal.get(GregorianCalendar.DAY_OF_MONTH) == gc.get(GregorianCalendar.DAY_OF_MONTH)) {
      //same day as min.  Must be after the min hour, min, sec
      minHour = minCal.get(GregorianCalendar.HOUR_OF_DAY);
    }
    if (maxCal.get(GregorianCalendar.YEAR) == gc.get(GregorianCalendar.YEAR) &&
        maxCal.get(GregorianCalendar.MONTH) == gc.get(GregorianCalendar.MONTH) &&
        maxCal.get(GregorianCalendar.DAY_OF_MONTH) == gc.get(GregorianCalendar.DAY_OF_MONTH)) {
      //same day as max. Must be before max hour, min, sec
      maxHour = maxCal.get(GregorianCalendar.HOUR_OF_DAY);
    }

    int hour = getRand().nextInt(minHour, maxHour);
    gc.set(GregorianCalendar.HOUR_OF_DAY, hour);
    if (minHour == maxHour) {
      minMin = minCal.get(GregorianCalendar.MINUTE);
      maxMin = maxCal.get(GregorianCalendar.MINUTE);
    } else if (hour == minHour) {
      minMin = minCal.get(GregorianCalendar.MINUTE);
    } else if (hour == maxHour) {
      maxMin = maxCal.get(GregorianCalendar.MINUTE);
    }

    int minute = getRand().nextInt(minMin, maxMin);
    gc.set(GregorianCalendar.MINUTE, minute);

    if (minHour == maxHour && minMin == maxMin) {
      minSec = minCal.get(GregorianCalendar.SECOND);
      maxSec = maxCal.get(GregorianCalendar.SECOND);
    } else if (hour == minHour && minute == minMin) {
      minSec = minCal.get(GregorianCalendar.SECOND);
    } else if (hour == maxHour && minute == maxMin) {
      maxSec = maxCal.get(GregorianCalendar.SECOND);
    }
    int sec = getRand().nextInt(minSec, maxSec);
    gc.set(GregorianCalendar.SECOND, sec);

    //clear MS because we don't care about that much precision
    gc.set(GregorianCalendar.MILLISECOND, 0);
    return dateFormat.format(gc.getTime());
  }

  private static RandomDataGenerator getRand() {
    return randomDataGenerator;
  }

}
