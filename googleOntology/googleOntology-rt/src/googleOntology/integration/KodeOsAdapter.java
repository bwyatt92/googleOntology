//
// Copyright (c) 2025
// Licensed under the MIT License
//

package googleOntology.integration;

import java.util.*;
import javax.baja.status.*;
import javax.baja.sys.*;
import googleOntology.model.*;
import googleOntology.util.*;

/**
 * KodeOsAdapter - Converts GoogleOntology data to KODE OS format
 *
 * Transforms DBO-mapped entities/points into KODE OS DISCOVER action format.
 */
public class KodeOsAdapter
{
  /**
   * Convert OntologyIndex to KODE OS DISCOVER payload
   *
   * Returns JSON string in format:
   * {
   *   "actions": [{
   *     "id": "devices",
   *     "value": "[{\"id\":\"...\",\"name\":\"...\",\"points\":[...]}]"
   *   }]
   * }
   */
  public static String toKodeOsDiscoverPayload(OntologyIndex index)
  {
    StringBuffer devices = new StringBuffer();
    devices.append('[');

    Iterator entityIter = index.getEntities().iterator();
    boolean firstEntity = true;

    while (entityIter.hasNext())
    {
      GoogleOntologyEntity entity = (GoogleOntologyEntity)entityIter.next();

      if (!firstEntity) devices.append(',');
      firstEntity = false;

      // Device object
      devices.append('{');
      devices.append("\\\"id\\\":\\\"").append(escapeJson(entity.getId())).append("\\\",");
      devices.append("\\\"name\\\":\\\"").append(escapeJson(entity.getName())).append("\\\",");
      devices.append("\\\"type\\\":\\\"").append(escapeJson(entity.getEntityType())).append("\\\",");
      devices.append("\\\"points\\\":[");

      // Points array
      Iterator pointIter = entity.getPoints().iterator();
      boolean firstPoint = true;

      while (pointIter.hasNext())
      {
        GoogleOntologyPoint point = (GoogleOntologyPoint)pointIter.next();

        if (!firstPoint) devices.append(',');
        firstPoint = false;

        devices.append('{');
        devices.append("\\\"pointId\\\":\\\"").append(escapeJson(point.getAddr())).append("\\\",");
        devices.append("\\\"name\\\":\\\"").append(escapeJson(point.getDboField())).append("\\\",");
        devices.append("\\\"originalName\\\":\\\"").append(escapeJson(point.getOriginalName())).append("\\\",");
        devices.append("\\\"kind\\\":\\\"").append(inferKind(point)).append("\\\",");
        devices.append("\\\"confidence\\\":").append(point.getConfidence());

        // Add units if present
        if (point.getUnits() != null)
        {
          devices.append(",\\\"units\\\":\\\"").append(escapeJson(point.getUnits())).append("\\\"");
        }

        // Add facets if present
        if (point.getMinValue() != null || point.getMaxValue() != null)
        {
          devices.append(",\\\"facets\\\":{");
          boolean addedFacet = false;

          if (point.getMinValue() != null)
          {
            devices.append("\\\"min\\\":").append(point.getMinValue());
            addedFacet = true;
          }

          if (point.getMaxValue() != null)
          {
            if (addedFacet) devices.append(',');
            devices.append("\\\"max\\\":").append(point.getMaxValue());
          }

          devices.append('}');
        }

        devices.append('}');
      }

      devices.append("]}");
    }

    devices.append(']');

    // Wrap in KODE OS action format
    StringBuffer payload = new StringBuffer();
    payload.append('{');
    payload.append("\"actions\":[{");
    payload.append("\"id\":\"devices\",");
    payload.append("\"value\":\"").append(devices.toString()).append("\"");
    payload.append("}]");
    payload.append('}');

    return payload.toString();
  }

  /**
   * Convert to simpler format for TIMESERIES action
   * Returns array of point values with DBO field names
   */
  public static String toKodeOsTimeseriesPayload(GoogleOntologyEntity entity)
  {
    StringBuffer json = new StringBuffer();
    json.append('[');

    Iterator iter = entity.getPoints().iterator();
    boolean first = true;

    while (iter.hasNext())
    {
      GoogleOntologyPoint p = (GoogleOntologyPoint)iter.next();

      if (!first) json.append(',');
      first = false;

      json.append('{');
      json.append("\"pointId\":\"").append(escapeJson(p.getAddr())).append("\",");
      json.append("\"fieldName\":\"").append(escapeJson(p.getDboField())).append("\",");

      // Get current value
      if (p.comp != null)
      {
        BStatusValue val = GoogleOntologyUtil.getPointValue(p.comp);
        if (val != null && val.getStatus().isValid())
        {
          json.append("\"value\":").append(formatValue(val)).append(',');
          json.append("\"timestamp\":").append(System.currentTimeMillis());
        }
      }

      json.append('}');
    }

    json.append(']');
    return json.toString();
  }

  /**
   * Infer KODE OS "kind" from point type
   */
  private static String inferKind(GoogleOntologyPoint point)
  {
    if (point.comp == null) return "Num";

    String type = point.comp.getType().toString();

    if (type.contains("Boolean")) return "Bool";
    if (type.contains("Enum")) return "Str";
    if (type.contains("Numeric")) return "Num";
    if (type.contains("String")) return "Str";

    return "Num";  // default
  }

  /**
   * Escape JSON string
   */
  private static String escapeJson(String str)
  {
    if (str == null) return "";

    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < str.length(); i++)
    {
      char c = str.charAt(i);
      switch (c)
      {
        case '"':  sb.append("\\\\\\\""); break;
        case '\\': sb.append("\\\\\\\\"); break;
        case '\n': sb.append("\\\\n"); break;
        case '\r': sb.append("\\\\r"); break;
        case '\t': sb.append("\\\\t"); break;
        default:   sb.append(c); break;
      }
    }
    return sb.toString();
  }

  /**
   * Format point value for JSON
   */
  private static String formatValue(BStatusValue val)
  {
    // Handle BStatusBoolean
    if (val instanceof BStatusBoolean)
    {
      BStatusBoolean b = (BStatusBoolean)val;
      return b.getValue() ? "true" : "false";
    }

    // Handle BStatusNumeric
    if (val instanceof BStatusNumeric)
    {
      BStatusNumeric n = (BStatusNumeric)val;
      return String.valueOf(n.getValue());
    }

    // Handle BStatusEnum
    if (val instanceof BStatusEnum)
    {
      BStatusEnum e = (BStatusEnum)val;
      return "\"" + escapeJson(e.getValue().toString()) + "\"";
    }

    // Handle BStatusString or other string-like values
    return "\"" + escapeJson(val.toString()) + "\"";
  }
}
