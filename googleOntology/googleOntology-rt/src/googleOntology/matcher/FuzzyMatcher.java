//
// Copyright (c) 2025
// Licensed under the MIT License
//

package googleOntology.matcher;

import java.util.*;

/**
 * FuzzyMatcher implements fuzzy matching logic for DBO fields.
 */
public final class FuzzyMatcher
{
  /**
   * Match result containing DBO field and confidence score
   */
  public static class MatchResult
  {
    public String dboField;
    public int confidence;
    public String pointType;
    public String measurement;

    public MatchResult(String dboField, int confidence)
    {
      this.dboField = dboField;
      this.confidence = confidence;
    }
  }

  /**
   * Match a point name to a DBO field
   */
  public static MatchResult matchToDboField(String pointName, String units, boolean isBoolean)
  {
    // Initialize library
    DboFieldLibrary.init();

    // Check if entire point name is a known abbreviation (like "DAT", "RAT", etc.)
    String expanded = DboFieldLibrary.expandAbbreviation(pointName);
    if (expanded != null)
    {
      // Found exact abbreviation match! Use it with high confidence
      MatchResult result = new MatchResult(expanded + "_sensor", 90);
      return result;
    }

    // Tokenize the point name
    List tokens = tokenize(pointName);

    // Build DBO field components
    String pointType = null;
    String measurement = null;
    ArrayList descriptorList = new ArrayList();
    String component = null;

    int baseConfidence = 50;  // Start with base confidence

    // Classify tokens
    for (int i = 0; i < tokens.size(); i++)
    {
      String token = (String)tokens.get(i);
      String lower = token.toLowerCase();

      // Check point type
      if (pointType == null && DboFieldLibrary.isPointType(lower))
      {
        pointType = lower;
        baseConfidence += 15;
        continue;
      }

      // Check measurement
      if (measurement == null && DboFieldLibrary.isMeasurement(lower))
      {
        measurement = lower;
        baseConfidence += 15;
        continue;
      }

      // Check descriptor
      if (DboFieldLibrary.isDescriptor(lower))
      {
        descriptorList.add(lower);
        baseConfidence += 5;
        continue;
      }

      // Check component
      if (component == null && DboFieldLibrary.isComponent(lower))
      {
        component = lower;
        baseConfidence += 10;
      }
    }

    // Infer point type if not found
    if (pointType == null)
    {
      pointType = inferPointType(pointName, isBoolean);
      baseConfidence -= 10;  // Lower confidence for inferred
    }

    // Infer measurement from units if not found
    if (measurement == null && units != null)
    {
      measurement = inferMeasurementFromUnits(units);
      if (measurement != null)
        baseConfidence += 10;
    }

    // Validate units match measurement
    if (measurement != null && units != null)
    {
      if (validateUnitsForMeasurement(units, measurement))
        baseConfidence += 10;
      else
        baseConfidence -= 15;  // Penalize mismatched units
    }

    // Build DBO field name
    StringBuilder dboField = new StringBuilder();

    // Add descriptors
    for (int i = 0; i < descriptorList.size(); i++)
    {
      if (dboField.length() > 0) dboField.append('_');
      dboField.append(descriptorList.get(i));
    }

    // Add component
    if (component != null)
    {
      if (dboField.length() > 0) dboField.append('_');
      dboField.append(component);
    }

    // Add measurement
    if (measurement != null)
    {
      if (dboField.length() > 0) dboField.append('_');
      dboField.append(measurement);
    }

    // Add point type (required)
    if (dboField.length() > 0) dboField.append('_');
    dboField.append(pointType);

    // Cap confidence at 100
    if (baseConfidence > 100) baseConfidence = 100;
    if (baseConfidence < 0) baseConfidence = 0;

    MatchResult result = new MatchResult(dboField.toString(), baseConfidence);
    result.pointType = pointType;
    result.measurement = measurement;
    return result;
  }

  /**
   * Tokenize a point name into words
   */
  private static List tokenize(String name)
  {
    ArrayList tokens = new ArrayList();

    // Split by common delimiters
    String[] parts = name.split("[\\s_\\-\\.]+");

    for (int i = 0; i < parts.length; i++)
    {
      String part = parts[i];
      if (part.length() == 0) continue;

      // Split camelCase
      String[] camelTokens = part.split("(?<=[a-z])(?=[A-Z])");
      for (int j = 0; j < camelTokens.length; j++)
      {
        String token = camelTokens[j];
        if (token.length() > 0)
          tokens.add(token);
      }
    }

    return tokens;
  }

  /**
   * Infer point type from context
   */
  private static String inferPointType(String name, boolean isBoolean)
  {
    String lower = name.toLowerCase();

    if (isBoolean)
    {
      if (lower.contains("status") || lower.contains("state"))
        return "status";
      if (lower.contains("cmd") || lower.contains("command"))
        return "command";
      if (lower.contains("enable") || lower.contains("enabled"))
        return "enable";
      return "status";  // default for boolean
    }

    if (lower.contains("sp") || lower.contains("setpoint") || lower.contains("set"))
      return "setpoint";
    if (lower.contains("alarm") || lower.contains("alert"))
      return "alarm";

    return "sensor";  // default for numeric
  }

  /**
   * Infer measurement from units
   */
  private static String inferMeasurementFromUnits(String units)
  {
    String lower = units.toLowerCase();

    if (lower.contains("f") || lower.contains("c") || lower.contains("k"))
      return "temperature";
    if (lower.contains("psi") || lower.contains("pa") || lower.contains("bar"))
      return "pressure";
    if (lower.contains("cfm") || lower.contains("l/s") || lower.contains("m3/h"))
      return "flowrate";
    if (lower.contains("gal") || lower.contains("liter"))
      return "flowvolume";
    if (lower.contains("w") || lower.contains("kw") || lower.contains("mw"))
      return "power";
    if (lower.contains("amp") || lower.contains("ma"))
      return "current";
    if (lower.contains("volt") || lower.contains("v"))
      return "voltage";
    if (lower.contains("rpm"))
      return "speed";
    if (lower.contains("hz"))
      return "frequency";
    if (lower.contains("%") || lower.contains("percent"))
      return "percentage";

    return null;
  }

  /**
   * Validate that units match expected measurement
   */
  private static boolean validateUnitsForMeasurement(String units, String measurement)
  {
    String lower = units.toLowerCase();

    if (measurement.equals("temperature"))
      return lower.contains("f") || lower.contains("c") || lower.contains("k");
    if (measurement.equals("pressure"))
      return lower.contains("psi") || lower.contains("pa") || lower.contains("bar");
    if (measurement.equals("flowrate"))
      return lower.contains("cfm") || lower.contains("l/s") || lower.contains("m3");
    if (measurement.equals("power"))
      return lower.contains("w") || lower.contains("kw");
    if (measurement.equals("percentage"))
      return lower.contains("%") || lower.contains("percent");

    // Add more as needed
    return true;  // default to true if not validated
  }
}
