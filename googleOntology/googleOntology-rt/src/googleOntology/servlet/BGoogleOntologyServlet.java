//
// Copyright (c) 2025
// Licensed under the MIT License
//

package googleOntology.servlet;

import java.io.*;
import java.util.*;
import javax.baja.status.*;
import javax.baja.sys.*;
import javax.baja.web.*;
import javax.servlet.http.*;
import googleOntology.model.*;
import googleOntology.service.*;
import googleOntology.util.*;
import googleOntology.integration.*;

/**
 * BGoogleOntologyServlet provides REST API for DBO-mapped points.
 */
public final class BGoogleOntologyServlet extends BWebServlet
{
  /*-
  class BGoogleOntologyServlet
  {
  }
  -*/
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $googleOntology.servlet.BGoogleOntologyServlet(2979106560)1.0$ @*/
/* Generated Mon Jan 01 12:00:00 EST 2025 by Slot-o-Matic 2000 (c) Tridium, Inc. 2000 */

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////

  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BGoogleOntologyServlet.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/

////////////////////////////////////////////////////////////////
// Constructor
////////////////////////////////////////////////////////////////

  /** Constructor. */
  public BGoogleOntologyServlet()
  {
    super.setServletName("googleOntology");
    super.setFlags(getSlot("servletName"), Flags.READONLY | Flags.SUMMARY);
  }

  /** Set backing index */
  public void setIndex(OntologyIndex index) { this.index = index; }

  /** Set backing service (for ML learner access) */
  public void setService(BGoogleOntologyService service) { this.service = service; }

////////////////////////////////////////////////////////////////
// Servlet
////////////////////////////////////////////////////////////////

  public void doPost(WebOp op) throws IOException
  {
    try
    {
      // NOTE: getPathInfo removes 'googleOntology' prefix from path already
      HttpServletRequest req = op.getRequest();
      String[] path = GoogleOntologyUtil.splitPath(req.getPathInfo());

      // sanity check path is long enough
      if (path.length < 2)
      {
        GoogleOntologyUtil.sendNotFound(op);
        return;
      }

      // key off version
      if (path[0].equals("v1"))
      {
        if (path[1].equals("about"))
        {
          JsonWriter w = startRes(op);
          doAbout(w);
          endRes(w);
          return;
        }
        if (path[1].equals("entities"))
        {
          JsonWriter w = startRes(op);
          doEntities(w);
          endRes(w);
          return;
        }
        if (path[1].equals("entity") && path.length >= 4)
        {
          String entityId = path[2];
          String action = path[3];

          if (action.equals("fields"))
          {
            JsonWriter w = startRes(op);
            doEntityFields(entityId, w);
            endRes(w);
            return;
          }
          if (action.equals("values"))
          {
            JsonWriter w = startRes(op);
            doEntityValues(entityId, w);
            endRes(w);
            return;
          }
        }
        if (path[1].equals("mappings"))
        {
          JsonWriter w = startRes(op);
          doMappings(w);
          endRes(w);
          return;
        }
        if (path[1].equals("learning"))
        {
          if (path.length >= 3)
          {
            String action = path[2];
            if (action.equals("correct"))
            {
              doLearningCorrect(op);
              return;
            }
            if (action.equals("train"))
            {
              JsonWriter w = startRes(op);
              doLearningTrain(w);
              endRes(w);
              return;
            }
            if (action.equals("stats"))
            {
              JsonWriter w = startRes(op);
              doLearningStats(w);
              endRes(w);
              return;
            }
          }
        }
        if (path[1].equals("kodeos"))
        {
          if (path.length >= 3)
          {
            String action = path[2];
            if (action.equals("discover"))
            {
              doKodeOsDiscover(op);
              return;
            }
          }
        }
      }

      // if we get here then 404
      GoogleOntologyUtil.sendNotFound(op);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      GoogleOntologyUtil.sendErr(op, 500, "Unexpected error", ex);
    }
  }

  public void doGet(WebOp op) throws IOException
  {
    // Support both POST and GET
    doPost(op);
  }

  private JsonWriter startRes(WebOp op) throws IOException
  {
    HttpServletResponse res = op.getResponse();
    res.setStatus(200);
    res.setHeader("Content-Type", "application/json");

    JsonWriter json = new JsonWriter(res.getOutputStream());
    return json;
  }

  private void endRes(JsonWriter json) throws IOException
  {
    json.flush().close();
  }

////////////////////////////////////////////////////////////////
// Endpoint /v1/about
////////////////////////////////////////////////////////////////

  /** Service /v1/about request. */
  private void doAbout(JsonWriter json) throws IOException
  {
    json.write('{');

    // required fields
    json.writeKey("name").writeVal(Sys.getStation().getStationName()).write(',');
    json.writeKey("vendor").writeVal("Tridium").write(',');
    json.writeKey("model").writeVal("Niagara 4").write(',');
    json.writeKey("version").writeVal(BComponent.TYPE.getVendorVersion().toString()).write(',');

    // module info
    json.writeKey("moduleName").writeVal("googleOntology").write(',');
    json.writeKey("moduleVersion").writeVal("1.0.0").write(',');

    // ontology info
    json.writeKey("ontologyType").writeVal("Google Digital Buildings").write(',');
    json.writeKey("numEntities").writeVal(index != null ? index.numEntities() : 0);

    json.write('}');
  }

////////////////////////////////////////////////////////////////
// Endpoint /v1/entities
////////////////////////////////////////////////////////////////

  /** Service /v1/entities request. */
  private void doEntities(JsonWriter json) throws IOException
  {
    if (index == null)
    {
      json.write('{');
      json.writeKey("entities").write('[').write(']');
      json.write('}');
      return;
    }

    json.write('{');
    json.writeKey("entities").write('[');

    Iterator iter = index.getEntities().iterator();
    int num = 0;

    while (iter.hasNext())
    {
      GoogleOntologyEntity e = (GoogleOntologyEntity)iter.next();

      // prefix trailing commas
      if (num > 0) json.write(',');

      json.write('{');
      json.writeKey("id").writeVal(e.getId()).write(',');
      json.writeKey("name").writeVal(e.getName()).write(',');
      json.writeKey("path").writeVal(e.getPath()).write(',');
      json.writeKey("entityType").writeVal(e.getEntityType()).write(',');
      json.writeKey("numPoints").writeVal(e.getPoints().size());
      json.write('}');
      num++;
    }
    json.write(']');
    json.write('}');
  }

////////////////////////////////////////////////////////////////
// Endpoint /v1/entity/{id}/fields
////////////////////////////////////////////////////////////////

  /** Service /v1/entity/{id}/fields request. */
  private void doEntityFields(String entityId, JsonWriter json) throws IOException
  {
    if (index == null)
      throw new IOException("Index not initialized");

    GoogleOntologyEntity entity = index.getEntity(entityId);
    if (entity == null)
      throw new IOException("Entity not found: " + entityId);

    json.write('{');
    json.writeKey("entityId").writeVal(entity.getId()).write(',');
    json.writeKey("entityName").writeVal(entity.getName()).write(',');
    json.writeKey("entityType").writeVal(entity.getEntityType()).write(',');
    json.writeKey("fields").write('[');

    Iterator iter = entity.getPoints().iterator();
    int num = 0;

    while (iter.hasNext())
    {
      GoogleOntologyPoint p = (GoogleOntologyPoint)iter.next();

      // prefix trailing commas
      if (num > 0) json.write(',');

      json.write('{');
      json.writeKey("dboField").writeVal(p.getDboField()).write(',');
      json.writeKey("originalName").writeVal(p.getOriginalName()).write(',');
      json.writeKey("addr").writeVal(p.getAddr()).write(',');
      json.writeKey("confidence").writeVal(p.getConfidence());

      // Add units if present
      if (p.getUnits() != null)
      {
        json.write(',');
        json.writeKey("units").writeVal(p.getUnits());
      }

      // Add enum mapping if present
      if (p.getEnumMapping() != null)
      {
        json.write(',');
        json.writeKey("enumMapping").writeVal(p.getEnumMapping());
      }

      // Add facets object
      boolean hasFacets = false;
      if (p.getMinValue() != null || p.getMaxValue() != null || p.getPrecision() != null)
      {
        json.write(',');
        json.writeKey("facets").write('{');

        if (p.getMinValue() != null)
        {
          json.writeKey("min").writeVal(p.getMinValue().doubleValue());
          hasFacets = true;
        }

        if (p.getMaxValue() != null)
        {
          if (hasFacets) json.write(',');
          json.writeKey("max").writeVal(p.getMaxValue().doubleValue());
          hasFacets = true;
        }

        if (p.getPrecision() != null)
        {
          if (hasFacets) json.write(',');
          json.writeKey("precision").writeVal(p.getPrecision().intValue());
        }

        json.write('}');
      }

      json.write('}');
      num++;
    }

    json.write(']');
    json.write('}');
  }

////////////////////////////////////////////////////////////////
// Endpoint /v1/entity/{id}/values
////////////////////////////////////////////////////////////////

  /** Service /v1/entity/{id}/values request. */
  private void doEntityValues(String entityId, JsonWriter json) throws IOException
  {
    if (index == null)
      throw new IOException("Index not initialized");

    GoogleOntologyEntity entity = index.getEntity(entityId);
    if (entity == null)
      throw new IOException("Entity not found: " + entityId);

    json.write('{');
    json.writeKey("entityId").writeVal(entity.getId()).write(',');
    json.writeKey("entityName").writeVal(entity.getName()).write(',');
    json.writeKey("entityType").writeVal(entity.getEntityType()).write(',');
    json.writeKey("values").write('[');

    Iterator iter = entity.getPoints().iterator();
    int num = 0;

    while (iter.hasNext())
    {
      GoogleOntologyPoint p = (GoogleOntologyPoint)iter.next();
      Object val = null;
      String status = "unknown";

      // bump lease time
      p.comp.lease(1, leaseTime);

      // get point value
      BStatusValue pv = GoogleOntologyUtil.getPointValue(p.comp);
      if (pv != null)
      {
        val = pv.getStatus().isValid() ? pv : "na";
        status = pv.getStatus().flagsToString(null);
      }

      // prefix trailing commas
      if (num > 0) json.write(',');

      json.write('{');
      json.writeKey("dboField").writeVal(p.getDboField()).write(',');
      json.writeKey("addr").writeVal(p.getAddr()).write(',');
      json.writeKey("val").writeVal(val).write(',');
      json.writeKey("status").writeVal(status);

      if (p.getUnits() != null)
      {
        json.write(',');
        json.writeKey("units").writeVal(p.getUnits());
      }

      json.write('}');
      num++;
    }

    json.write(']');
    json.write('}');
  }

////////////////////////////////////////////////////////////////
// Endpoint /v1/mappings
////////////////////////////////////////////////////////////////

  /** Service /v1/mappings request - returns all mappings with confidence scores. */
  private void doMappings(JsonWriter json) throws IOException
  {
    if (index == null)
    {
      json.write('{');
      json.writeKey("mappings").write('[').write(']');
      json.write('}');
      return;
    }

    json.write('{');
    json.writeKey("mappings").write('[');

    Iterator entityIter = index.getEntities().iterator();
    int totalNum = 0;

    while (entityIter.hasNext())
    {
      GoogleOntologyEntity entity = (GoogleOntologyEntity)entityIter.next();
      Iterator pointIter = entity.getPoints().iterator();

      while (pointIter.hasNext())
      {
        GoogleOntologyPoint p = (GoogleOntologyPoint)pointIter.next();

        // prefix trailing commas
        if (totalNum > 0) json.write(',');

        json.write('{');
        json.writeKey("entityId").writeVal(entity.getId()).write(',');
        json.writeKey("entityName").writeVal(entity.getName()).write(',');
        json.writeKey("originalName").writeVal(p.getOriginalName()).write(',');
        json.writeKey("dboField").writeVal(p.getDboField()).write(',');
        json.writeKey("confidence").writeVal(p.getConfidence());
        json.write('}');
        totalNum++;
      }
    }

    json.write(']');
    json.write('}');
  }

////////////////////////////////////////////////////////////////
// ML Learning Endpoints
////////////////////////////////////////////////////////////////

  /** POST /v1/learning/correct - Submit a correction for ML training */
  private void doLearningCorrect(WebOp op) throws IOException
  {
    if (service == null)
    {
      GoogleOntologyUtil.sendErr(op, 503, "Service not available", null);
      return;
    }

    try
    {
      // Parse JSON body
      HttpServletRequest req = op.getRequest();
      String body = GoogleOntologyUtil.readBody(req);

      // Simple JSON parsing (no external libraries needed)
      String pointName = extractJsonValue(body, "pointName");
      String correctMatch = extractJsonValue(body, "correctMatch");
      String equipmentType = extractJsonValue(body, "equipmentType");

      if (pointName == null || correctMatch == null)
      {
        GoogleOntologyUtil.sendErr(op, 400, "Missing required fields: pointName, correctMatch", null);
        return;
      }

      // Add correction to ML learner
      service.addCorrection(pointName, correctMatch, equipmentType);

      // Return success
      JsonWriter json = startRes(op);
      json.write('{');
      json.writeKey("success").writeVal(true).write(',');
      json.writeKey("message").writeVal("Correction added");
      json.write('}');
      endRes(json);
    }
    catch (Exception ex)
    {
      GoogleOntologyUtil.sendErr(op, 500, "Failed to add correction", ex);
    }
  }

  /** POST /v1/learning/train - Trigger ML model training */
  private void doLearningTrain(JsonWriter json) throws IOException
  {
    if (service == null)
    {
      json.write('{');
      json.writeKey("success").writeVal(false).write(',');
      json.writeKey("message").writeVal("Service not available");
      json.write('}');
      return;
    }

    boolean success = service.trainMLModel();

    json.write('{');
    json.writeKey("success").writeVal(success).write(',');
    json.writeKey("message").writeVal(success ? "Model trained successfully" : "Training failed - need at least 5 examples");
    json.write('}');
  }

  /** GET /v1/learning/stats - Get ML learning statistics */
  private void doLearningStats(JsonWriter json) throws IOException
  {
    if (service == null)
    {
      json.write('{');
      json.writeKey("available").writeVal(false);
      json.write('}');
      return;
    }

    String stats = service.getMLStats();

    json.write('{');
    json.writeKey("available").writeVal(true).write(',');
    json.writeKey("stats").writeVal(stats);
    json.write('}');
  }

  /** Extract JSON value (simple parser, no external deps) */
  private String extractJsonValue(String json, String key)
  {
    String search = "\"" + key + "\"";
    int idx = json.indexOf(search);
    if (idx == -1) return null;

    // Find opening quote after key
    int start = json.indexOf("\"", idx + search.length() + 1);
    if (start == -1) return null;
    start++; // skip quote

    // Find closing quote
    int end = json.indexOf("\"", start);
    if (end == -1) return null;

    return json.substring(start, end);
  }

////////////////////////////////////////////////////////////////
// KODE OS Integration Endpoints
////////////////////////////////////////////////////////////////

  /** GET /v1/kodeos/discover - Get data in KODE OS DISCOVER format */
  private void doKodeOsDiscover(WebOp op) throws IOException
  {
    if (index == null)
    {
      GoogleOntologyUtil.sendErr(op, 503, "Index not initialized", null);
      return;
    }

    try
    {
      // Convert to KODE OS format
      String payload = KodeOsAdapter.toKodeOsDiscoverPayload(index);

      // Return as JSON
      HttpServletResponse res = op.getResponse();
      res.setStatus(200);
      res.setHeader("Content-Type", "application/json");

      res.getOutputStream().write(payload.getBytes("UTF-8"));
      res.getOutputStream().flush();
      res.getOutputStream().close();
    }
    catch (Exception ex)
    {
      GoogleOntologyUtil.sendErr(op, 500, "Failed to generate KODE OS payload", ex);
    }
  }

////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////

  private OntologyIndex index;
  private BGoogleOntologyService service;
  private final long leaseTime = 120000;   // 2min in millis
}
