//
// Copyright (c) 2025
// Licensed under the MIT License
//

package googleOntology.matcher;

import java.util.*;

/**
 * DboFieldLibrary contains common DBO subfields and entity types.
 */
public final class DboFieldLibrary
{
  /** Initialize library */
  public static void init()
  {
    if (initialized) return;

    // Point types (required suffix)
    pointTypes.add("sensor");
    pointTypes.add("setpoint");
    pointTypes.add("command");
    pointTypes.add("status");
    pointTypes.add("alarm");
    pointTypes.add("count");
    pointTypes.add("accumulator");
    pointTypes.add("mode");
    pointTypes.add("enable");

    // Measurements
    measurements.add("temperature");
    measurements.add("pressure");
    measurements.add("flowrate");
    measurements.add("flowvolume");
    measurements.add("power");
    measurements.add("current");
    measurements.add("voltage");
    measurements.add("speed");
    measurements.add("frequency");
    measurements.add("percentage");
    measurements.add("concentration");

    // Measurement descriptors
    measurementDescriptors.add("differential");
    measurementDescriptors.add("relative");
    measurementDescriptors.add("static");
    measurementDescriptors.add("total");
    measurementDescriptors.add("effective");

    // Descriptors (location/function)
    descriptors.add("discharge");
    descriptors.add("supply");
    descriptors.add("return");
    descriptors.add("exhaust");
    descriptors.add("outside");
    descriptors.add("zone");
    descriptors.add("mixed");
    descriptors.add("entering");
    descriptors.add("leaving");
    descriptors.add("primary");
    descriptors.add("secondary");
    descriptors.add("chilled");
    descriptors.add("hot");
    descriptors.add("condensing");
    descriptors.add("evaporator");
    descriptors.add("cooling");
    descriptors.add("heating");
    descriptors.add("building");
    descriptors.add("space");

    // Components
    components.add("air");
    components.add("water");
    components.add("fan");
    components.add("pump");
    components.add("valve");
    components.add("damper");
    components.add("filter");
    components.add("coil");
    components.add("compressor");
    components.add("economizer");

    // Aggregations
    aggregations.add("min");
    aggregations.add("max");
    aggregations.add("average");
    aggregations.add("mean");

    // Entity types
    entityTypes.put("ahu", "AHU");
    entityTypes.put("rtu", "RTU");
    entityTypes.put("vav", "VAV");
    entityTypes.put("fcu", "FCU");
    entityTypes.put("chws", "CHWS");
    entityTypes.put("hws", "HWS");
    entityTypes.put("boiler", "BLR");
    entityTypes.put("chiller", "CH");
    entityTypes.put("cooling", "CT");
    entityTypes.put("tower", "CT");
    entityTypes.put("fan", "FAN");
    entityTypes.put("pump", "PMP");

    // Common HVAC abbreviations
    abbreviations.put("dat", "discharge_air_temperature");
    abbreviations.put("rat", "return_air_temperature");
    abbreviations.put("mat", "mixed_air_temperature");
    abbreviations.put("oat", "outside_air_temperature");
    abbreviations.put("sat", "supply_air_temperature");
    abbreviations.put("chwst", "chilled_water_supply_temperature");
    abbreviations.put("chwrt", "chilled_water_return_temperature");
    abbreviations.put("hwst", "hot_water_supply_temperature");
    abbreviations.put("hwrt", "hot_water_return_temperature");
    abbreviations.put("znt", "zone_air_temperature");
    abbreviations.put("sp", "setpoint");
    abbreviations.put("cmd", "command");
    abbreviations.put("sts", "status");
    abbreviations.put("dpr", "damper");
    abbreviations.put("vlv", "valve");
    abbreviations.put("sf", "supply_fan");
    abbreviations.put("rf", "return_fan");
    abbreviations.put("ef", "exhaust_fan");

    initialized = true;
  }

  /** Check if word is a known point type */
  public static boolean isPointType(String word)
  {
    return pointTypes.contains(word.toLowerCase());
  }

  /** Check if word is a known measurement */
  public static boolean isMeasurement(String word)
  {
    return measurements.contains(word.toLowerCase());
  }

  /** Check if word is a known descriptor */
  public static boolean isDescriptor(String word)
  {
    return descriptors.contains(word.toLowerCase());
  }

  /** Check if word is a known component */
  public static boolean isComponent(String word)
  {
    return components.contains(word.toLowerCase());
  }

  /** Get all point types */
  public static Set getPointTypes() { return pointTypes; }

  /** Get all measurements */
  public static Set getMeasurements() { return measurements; }

  /** Get all descriptors */
  public static Set getDescriptors() { return descriptors; }

  /** Expand abbreviation if known */
  public static String expandAbbreviation(String word)
  {
    String lower = word.toLowerCase();
    if (abbreviations.containsKey(lower))
      return (String)abbreviations.get(lower);
    return null;
  }

  /** Infer entity type from name */
  public static String inferEntityType(String name)
  {
    String lower = name.toLowerCase();
    for (Iterator it = entityTypes.keySet().iterator(); it.hasNext();)
    {
      String key = (String)it.next();
      if (lower.contains(key))
        return (String)entityTypes.get(key);
    }
    return "EQUIPMENT";  // default
  }

////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////

  private static boolean initialized = false;
  private static final HashSet pointTypes = new HashSet();
  private static final HashSet measurements = new HashSet();
  private static final HashSet measurementDescriptors = new HashSet();
  private static final HashSet descriptors = new HashSet();
  private static final HashSet components = new HashSet();
  private static final HashSet aggregations = new HashSet();
  private static final HashMap entityTypes = new HashMap();
  private static final HashMap abbreviations = new HashMap();  // HVAC abbreviations
}
