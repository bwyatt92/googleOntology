//
// Copyright (c) 2025
// Licensed under the MIT License
//

package googleOntology.integration;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.baja.sys.*;
import javax.baja.log.*;
import googleOntology.model.*;
import googleOntology.util.*;

/**
 * KodeLabsClient - HTTP client for KODE Labs Public API
 *
 * Handles authentication and device/point creation via KODE Labs Public API.
 */
public class KodeLabsClient
{
  private final String baseUrl;
  private final String accessToken;
  private final String buildingId;
  private final String datasourceId;
  private final int minConfidence;

  private static final Log LOG = Log.getLog("googleOntology");

  /**
   * Constructor
   */
  public KodeLabsClient(String baseUrl, String accessToken, String buildingId,
                        String datasourceId, int minConfidence)
  {
    this.baseUrl = baseUrl;
    this.accessToken = accessToken;
    this.buildingId = buildingId;
    this.datasourceId = datasourceId;
    this.minConfidence = minConfidence;
  }

  /**
   * Sync all entities to KODE Labs
   *
   * @return Result summary string
   */
  public String syncDevices(OntologyIndex index)
  {
    LOG.message("Starting KODE Labs sync...");
    LOG.message("  Building ID: " + buildingId);
    LOG.message("  Datasource ID: " + datasourceId);
    LOG.message("  Min Confidence: " + minConfidence);

    try
    {
      // Build devices payload
      StringBuffer devicesJson = new StringBuffer();
      devicesJson.append("{\"devices\":[");

      Iterator entityIter = index.getEntities().iterator();
      int deviceCount = 0;
      int totalPoints = 0;
      boolean firstDevice = true;

      while (entityIter.hasNext())
      {
        GoogleOntologyEntity entity = (GoogleOntologyEntity)entityIter.next();

        // Skip generic equipment
        if ("EQUIPMENT".equals(entity.getEntityType()))
        {
          LOG.trace("Skipping entity: " + entity.getName() + " (EQUIPMENT type)");
          continue;
        }

        // Build device JSON
        StringBuffer deviceJson = new StringBuffer();
        deviceJson.append("{");
        deviceJson.append("\"id\":\"niagara_").append(escapeJson(entity.getId())).append("\",");
        deviceJson.append("\"name\":\"").append(escapeJson(entity.getName())).append("\",");
        deviceJson.append("\"displayName\":\"").append(escapeJson(entity.getName())).append("\",");
        deviceJson.append("\"points\":[");

        // Add points
        Iterator pointIter = entity.getPoints().iterator();
        int pointCount = 0;
        boolean firstPoint = true;

        while (pointIter.hasNext())
        {
          GoogleOntologyPoint point = (GoogleOntologyPoint)pointIter.next();

          // Skip low confidence points
          if (point.getConfidence() < minConfidence)
          {
            continue;
          }

          if (!firstPoint) deviceJson.append(',');
          firstPoint = false;

          deviceJson.append("{");
          deviceJson.append("\"kind\":\"").append(inferKind(point)).append("\",");
          deviceJson.append("\"name\":\"").append(escapeJson(point.getOriginalName())).append("\",");
          deviceJson.append("\"pointId\":\"").append(escapeJson(entity.getId() + "_" + point.getAddr())).append("\"");

          // Add units if present
          if (point.getUnits() != null)
          {
            deviceJson.append(",\"unit\":\"").append(escapeJson(point.getUnits())).append("\"");
          }

          deviceJson.append("}");
          pointCount++;
        }

        deviceJson.append("]}");

        // Only include device if it has points
        if (pointCount > 0)
        {
          if (!firstDevice) devicesJson.append(',');
          firstDevice = false;

          devicesJson.append(deviceJson.toString());
          deviceCount++;
          totalPoints += pointCount;

          LOG.trace("Added device: " + entity.getName() + " (" + pointCount + " points)");
        }
      }

      devicesJson.append("]}");

      if (deviceCount == 0)
      {
        LOG.warning("No devices to sync (check minConfidence setting)");
        return "No devices found with confidence >= " + minConfidence;
      }

      LOG.message("Syncing " + deviceCount + " devices with " + totalPoints + " total points...");

      // Send to KODE Labs
      String result = postDevicesBatch(devicesJson.toString());

      LOG.message("Sync complete! " + result);
      return "Synced " + deviceCount + " devices, " + totalPoints + " points. " + result;
    }
    catch (Exception e)
    {
      LOG.error("KODE Labs sync failed", e);
      return "Sync failed: " + e.getMessage();
    }
  }

  /**
   * POST devices to KODE Labs batch endpoint
   */
  private String postDevicesBatch(String jsonPayload) throws Exception
  {
    String url = baseUrl + "/buildings/" + buildingId + "/integrations/datasources/" +
                 datasourceId + "/devices/batch";

    LOG.trace("POST " + url);
    LOG.trace("Payload: " + jsonPayload.substring(0, Math.min(500, jsonPayload.length())) + "...");

    HttpURLConnection conn = null;
    try
    {
      URL urlObj = new URL(url);
      conn = (HttpURLConnection)urlObj.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Authorization", "Bearer " + accessToken);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("User-Agent", "Niagara-GoogleOntology/1.0");
      conn.setDoOutput(true);
      conn.setConnectTimeout(30000);  // 30 second timeout
      conn.setReadTimeout(30000);

      // Write payload
      OutputStream os = conn.getOutputStream();
      os.write(jsonPayload.getBytes("UTF-8"));
      os.flush();
      os.close();

      // Read response
      int responseCode = conn.getResponseCode();
      LOG.trace("Response code: " + responseCode);

      if (responseCode >= 200 && responseCode < 300)
      {
        // Success
        InputStream is = conn.getInputStream();
        String response = readStream(is);
        is.close();

        LOG.trace("Response: " + response);
        return "Success (HTTP " + responseCode + ")";
      }
      else
      {
        // Error
        InputStream es = conn.getErrorStream();
        String error = es != null ? readStream(es) : "No error details";
        if (es != null) es.close();

        LOG.error("KODE Labs API error (" + responseCode + "): " + error);
        throw new Exception("HTTP " + responseCode + ": " + error);
      }
    }
    finally
    {
      if (conn != null) conn.disconnect();
    }
  }

  /**
   * Read input stream to string
   */
  private String readStream(InputStream is) throws IOException
  {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    StringBuffer response = new StringBuffer();
    String line;

    while ((line = reader.readLine()) != null)
    {
      response.append(line);
    }

    return response.toString();
  }

  /**
   * Infer point kind from DBO field
   */
  private String inferKind(GoogleOntologyPoint point)
  {
    String dboField = point.getDboField().toLowerCase();

    // Boolean indicators
    if (dboField.contains("status") || dboField.contains("enable") ||
        dboField.contains("alarm") || dboField.contains("fault") ||
        dboField.contains("run"))
    {
      return "Bool";
    }

    // String/Enum indicators
    if (dboField.contains("mode") || dboField.contains("state"))
    {
      return "Str";
    }

    // Default to Number
    return "Number";
  }

  /**
   * Escape JSON string
   */
  private String escapeJson(String str)
  {
    if (str == null) return "";

    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < str.length(); i++)
    {
      char c = str.charAt(i);
      switch (c)
      {
        case '"':  sb.append("\\\""); break;
        case '\\': sb.append("\\\\"); break;
        case '\n': sb.append("\\n"); break;
        case '\r': sb.append("\\r"); break;
        case '\t': sb.append("\\t"); break;
        default:   sb.append(c); break;
      }
    }
    return sb.toString();
  }
}
