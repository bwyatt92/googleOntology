//
// Copyright (c) 2025
// Licensed under the MIT License
//

package googleOntology.service;

import javax.baja.control.*;
import javax.baja.log.*;
import javax.baja.status.*;
import javax.baja.sys.*;
import javax.baja.util.*;
import googleOntology.matcher.*;
import googleOntology.model.*;
import googleOntology.servlet.*;
import googleOntology.util.*;
import googleOntology.learning.*;
import googleOntology.integration.*;

/**
 * BGoogleOntologyService crawls the station and maps points to Google Digital Buildings Ontology.
 */
public final class BGoogleOntologyService extends BAbstractService
{
  /*-
  class BGoogleOntologyService
  {
    properties
    {
      servlet: BGoogleOntologyServlet
        default{[ new BGoogleOntologyServlet() ]}

      minConfidence: int
        default {[ 50 ]}
        flags { summary }

      kodeBaseUrl: String
        default {[ "https://api.kodelabs.com/kodeos/api/v1" ]}
        flags { summary }

      kodeAccessToken: String
        default {[ "" ]}
        flags { summary }

      kodeBuildingId: String
        default {[ "" ]}
        flags { summary }

      kodeDatasourceId: String
        default {[ "" ]}
        flags { summary }
    }

    actions
    {
      rebuildIndex()
      syncToKodeLabs()
    }
  }
  -*/
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $googleOntology.service.BGoogleOntologyService(2979106560)1.0$ @*/
/* Generated Mon Jan 01 12:00:00 EST 2025 by Slot-o-Matic 2000 (c) Tridium, Inc. 2000 */

////////////////////////////////////////////////////////////////
// Property "servlet"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the <code>servlet</code> property.
   * @see googleOntology.service.BGoogleOntologyService#getServlet
   * @see googleOntology.service.BGoogleOntologyService#setServlet
   */
  public static final Property servlet = newProperty(0, new BGoogleOntologyServlet(), null);

  /**
   * Get the <code>servlet</code> property.
   * @see googleOntology.service.BGoogleOntologyService#servlet
   */
  public BGoogleOntologyServlet getServlet() { return (BGoogleOntologyServlet)get(servlet); }

  /**
   * Set the <code>servlet</code> property.
   * @see googleOntology.service.BGoogleOntologyService#servlet
   */
  public void setServlet(BGoogleOntologyServlet v) { set(servlet, v, null); }

////////////////////////////////////////////////////////////////
// Property "minConfidence"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the <code>minConfidence</code> property.
   * @see googleOntology.service.BGoogleOntologyService#getMinConfidence
   * @see googleOntology.service.BGoogleOntologyService#setMinConfidence
   */
  public static final Property minConfidence = newProperty(0, 50, null);

  /**
   * Get the <code>minConfidence</code> property.
   * @see googleOntology.service.BGoogleOntologyService#minConfidence
   */
  public int getMinConfidence() { return getInt(minConfidence); }

  /**
   * Set the <code>minConfidence</code> property.
   * @see googleOntology.service.BGoogleOntologyService#minConfidence
   */
  public void setMinConfidence(int v) { setInt(minConfidence, v, null); }

////////////////////////////////////////////////////////////////
// Property "kodeBaseUrl"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the <code>kodeBaseUrl</code> property.
   * @see googleOntology.service.BGoogleOntologyService#getKodeBaseUrl
   * @see googleOntology.service.BGoogleOntologyService#setKodeBaseUrl
   */
  public static final Property kodeBaseUrl = newProperty(0, "https://api.kodelabs.com/kodeos/api/v1", null);

  /**
   * Get the <code>kodeBaseUrl</code> property.
   * @see googleOntology.service.BGoogleOntologyService#kodeBaseUrl
   */
  public String getKodeBaseUrl() { return getString(kodeBaseUrl); }

  /**
   * Set the <code>kodeBaseUrl</code> property.
   * @see googleOntology.service.BGoogleOntologyService#kodeBaseUrl
   */
  public void setKodeBaseUrl(String v) { setString(kodeBaseUrl, v, null); }

////////////////////////////////////////////////////////////////
// Property "kodeAccessToken"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the <code>kodeAccessToken</code> property.
   * @see googleOntology.service.BGoogleOntologyService#getKodeAccessToken
   * @see googleOntology.service.BGoogleOntologyService#setKodeAccessToken
   */
  public static final Property kodeAccessToken = newProperty(0, "", null);

  /**
   * Get the <code>kodeAccessToken</code> property.
   * @see googleOntology.service.BGoogleOntologyService#kodeAccessToken
   */
  public String getKodeAccessToken() { return getString(kodeAccessToken); }

  /**
   * Set the <code>kodeAccessToken</code> property.
   * @see googleOntology.service.BGoogleOntologyService#kodeAccessToken
   */
  public void setKodeAccessToken(String v) { setString(kodeAccessToken, v, null); }

////////////////////////////////////////////////////////////////
// Property "kodeBuildingId"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the <code>kodeBuildingId</code> property.
   * @see googleOntology.service.BGoogleOntologyService#getKodeBuildingId
   * @see googleOntology.service.BGoogleOntologyService#setKodeBuildingId
   */
  public static final Property kodeBuildingId = newProperty(0, "", null);

  /**
   * Get the <code>kodeBuildingId</code> property.
   * @see googleOntology.service.BGoogleOntologyService#kodeBuildingId
   */
  public String getKodeBuildingId() { return getString(kodeBuildingId); }

  /**
   * Set the <code>kodeBuildingId</code> property.
   * @see googleOntology.service.BGoogleOntologyService#kodeBuildingId
   */
  public void setKodeBuildingId(String v) { setString(kodeBuildingId, v, null); }

////////////////////////////////////////////////////////////////
// Property "kodeDatasourceId"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the <code>kodeDatasourceId</code> property.
   * @see googleOntology.service.BGoogleOntologyService#getKodeDatasourceId
   * @see googleOntology.service.BGoogleOntologyService#setKodeDatasourceId
   */
  public static final Property kodeDatasourceId = newProperty(0, "", null);

  /**
   * Get the <code>kodeDatasourceId</code> property.
   * @see googleOntology.service.BGoogleOntologyService#kodeDatasourceId
   */
  public String getKodeDatasourceId() { return getString(kodeDatasourceId); }

  /**
   * Set the <code>kodeDatasourceId</code> property.
   * @see googleOntology.service.BGoogleOntologyService#kodeDatasourceId
   */
  public void setKodeDatasourceId(String v) { setString(kodeDatasourceId, v, null); }

////////////////////////////////////////////////////////////////
// Action "rebuildIndex"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the <code>rebuildIndex</code> action.
   * @see googleOntology.service.BGoogleOntologyService#rebuildIndex()
   */
  public static final Action rebuildIndex = newAction(0, null);

  /**
   * Invoke the <code>rebuildIndex</code> action.
   * @see googleOntology.service.BGoogleOntologyService#rebuildIndex
   */
  public void rebuildIndex() { invoke(rebuildIndex, null, null); }

////////////////////////////////////////////////////////////////
// Action "syncToKodeLabs"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the <code>syncToKodeLabs</code> action.
   * @see googleOntology.service.BGoogleOntologyService#syncToKodeLabs()
   */
  public static final Action syncToKodeLabs = newAction(0, null);

  /**
   * Invoke the <code>syncToKodeLabs</code> action.
   * @see googleOntology.service.BGoogleOntologyService#syncToKodeLabs
   */
  public void syncToKodeLabs() { invoke(syncToKodeLabs, null, null); }

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////

  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BGoogleOntologyService.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/

////////////////////////////////////////////////////////////////
// BAbstractService
////////////////////////////////////////////////////////////////

  public Type[] getServiceTypes()
  {
    Type[] t = { getType() };
    return t;
  }

  public void serviceStarted() throws Exception
  {
    DboFieldLibrary.init();
  }

  public void atSteadyState()
  {
    doRebuildIndex();
    LOG.message("GoogleOntologyService ready [version=" + moduleVer() + "]");
  }

  public void doRebuildIndex()
  {
    try
    {
      // start
      LOG.message("GoogleOntology reindex started...");
      BAbsTime t1 = BAbsTime.now();
      int numPoints = 0;
      int numMatched = 0;

      // clear index
      index.clear();

      // scan station for points
      BStation station = Sys.getStation();
      BComponent[] comps = station.getComponentSpace().getAllComponents();

      for (int i = 0; i < comps.length; i++)
      {
        BComponent c = comps[i];
        try
        {
          if (c instanceof BNumericPoint || c instanceof BBooleanPoint || c instanceof BEnumPoint)
          {
            numPoints++;

            // verify has parent entity
            GoogleOntologyEntity entity = getOrCreateEntity(c);
            if (entity == null)
            {
              if (LOG.isTraceOn())
                LOG.trace("Entity not found for point: " + c.getDisplayName(null));
              continue;
            }

            // Extract facet information
            String units = null;
            String enumMapping = null;
            Double minValue = null;
            Double maxValue = null;
            Integer precision = null;
            boolean isBoolean = false;

            BFacets f = (BFacets)c.get("facets");
            if (f != null)
            {
              // Get units
              units = f.gets("units", null);
              if (units != null && units.equals("null")) units = null;

              // Get enum range for enum/boolean points
              if (c instanceof BEnumPoint)
              {
                BEnumRange r = (BEnumRange)f.get("range");
                if (r != null)
                  enumMapping = GoogleOntologyUtil.parseEnumRange(r);
              }
              else if (c instanceof BBooleanPoint)
              {
                isBoolean = true;
                enumMapping = GoogleOntologyUtil.parseBooleanRange();
              }

              // Get numeric range
              if (c instanceof BNumericPoint)
              {
                // TODO: Get min/max from numeric point facets
                // Need to find correct Niagara 4.14 API for numeric ranges
                // BNumericRange doesn't exist - need to find the right class

                // For now, skip range extraction
                // BNumericRange range = (BNumericRange)f.get("range");
                // if (range != null)
                // {
                //   minValue = new Double(range.getMin());
                //   maxValue = new Double(range.getMax());
                // }

                // Get precision (if available)
                try
                {
                  Object precObj = f.get("precision");
                  if (precObj != null)
                  {
                    // Precision might be an Integer or BInteger
                    int prec = -1;
                    if (precObj instanceof Integer)
                      prec = ((Integer)precObj).intValue();
                    else if (precObj instanceof Number)
                      prec = ((Number)precObj).intValue();

                    if (prec >= 0)
                      precision = new Integer(prec);
                  }
                }
                catch (Exception ex) { /* ignore - precision not critical */ }
              }
            }

            // Perform fuzzy matching
            String pointName = c.getDisplayName(null);
            FuzzyMatcher.MatchResult fuzzyMatch = FuzzyMatcher.matchToDboField(
              pointName, units, isBoolean);

            FuzzyMatcher.MatchResult match = fuzzyMatch;  // default to fuzzy

            // Try ML prediction if model is trained
            if (mlLearner != null && mlLearner.isReady())
            {
              String entityType = entity.getEntityType();
              MLOntologyLearner.Prediction mlPred = mlLearner.predict(pointName, entityType);

              if (mlPred.dboField != null && mlPred.confidence > fuzzyMatch.confidence)
              {
                // ML has better prediction!
                match = new FuzzyMatcher.MatchResult(mlPred.dboField, mlPred.confidence);
                if (LOG.isTraceOn())
                  LOG.trace("ML prediction wins: " + pointName + " -> " + mlPred.dboField +
                           " (ML:" + mlPred.confidence + "% vs Fuzzy:" + fuzzyMatch.confidence + "%)");
              }
            }

            // Check if confidence meets threshold
            if (match.confidence < getMinConfidence())
            {
              if (LOG.isTraceOn())
                LOG.trace("Low confidence match (" + match.confidence + "%) for: " + pointName);
              continue;
            }

            // Create point with DBO mapping
            String addr = GoogleOntologyUtil.getPointAddr(entity.comp, c);
            GoogleOntologyPoint point = new GoogleOntologyPoint(
              match.dboField,
              pointName,
              addr,
              units,
              enumMapping,
              match.confidence
            );

            // Set additional facet info
            point.setMinValue(minValue);
            point.setMaxValue(maxValue);
            point.setPrecision(precision);
            point.comp = c;

            entity.addPoint(point);
            numMatched++;
          }
        }
        catch (Exception e)
        {
          // do not fail reindex for one component; log error and continue
          LOG.error("FAILED to index: " + c.getName(), e);
        }
      }

      // Update servlet references (internal property)
      getServlet().setIndex(index);
      getServlet().setService(this);

      // Also update ANY servlet instances in the station (e.g., in WebService)
      for (int j = 0; j < comps.length; j++)
      {
        if (comps[j] instanceof BGoogleOntologyServlet)
        {
          BGoogleOntologyServlet servlet = (BGoogleOntologyServlet)comps[j];
          servlet.setIndex(index);
          servlet.setService(this);
          if (LOG.isTraceOn())
            LOG.trace("Updated servlet: " + comps[j].getSlotPath());
        }
      }

      if (LOG.isTraceOn())
        LOG.trace("Total BComponents searched: " + comps.length);

      // complete
      BAbsTime t2 = BAbsTime.now();
      LOG.message("GoogleOntology reindex complete [" +
        t1.delta(t2) + ", " +
        index.numEntities() + " entities, " +
        numPoints + " points scanned, " +
        numMatched + " points matched]");
    }
    catch (Exception e)
    {
      LOG.error("GoogleOntology reindex FAILED", e);
    }
  }

////////////////////////////////////////////////////////////////
// Entities
////////////////////////////////////////////////////////////////

  /**
   * Get or create entity for given point.
   */
  private GoogleOntologyEntity getOrCreateEntity(BComponent point)
  {
    // sanity check
    BComponent parent = (BComponent)point.getParent();
    if (parent == null) return null;

    // Use parent as entity (equipment folder, device, etc.)
    BComponent entityComp = findEntityComp(parent);
    if (entityComp == null) return null;

    // check cache
    String id = GoogleOntologyUtil.getEntityId(entityComp);
    GoogleOntologyEntity entity = index.getEntity(id);

    // add to cache if not found
    if (entity == null)
    {
      String name = entityComp.getDisplayName(null);
      String path = GoogleOntologyUtil.unescapeSlotPath(
        entityComp.getSlotPath().toString().substring(5));

      // filter out common stuff we likely never want
      if (path.startsWith("/Services/")) return null;

      // Infer entity type from name
      String entityType = DboFieldLibrary.inferEntityType(name);

      // index entity
      entity = new GoogleOntologyEntity(id, name, path, entityType);
      entity.comp = entityComp;
      index.addEntity(entity);
    }

    return entity;
  }

  /**
   * Find best component to use as entity.
   */
  private BComponent findEntityComp(BComponent orig)
  {
    // never walk if no parent
    BComplex p = orig.getParent();
    if (p == null) return orig;

    // For now, use parent directly
    // TODO: Add logic to walk up to proxy device if needed
    return orig;
  }

  /** Get module version string */
  private String moduleVer()
  {
    return "1.0.0";  // TODO: Read from module
  }

////////////////////////////////////////////////////////////////
// ML Learning
////////////////////////////////////////////////////////////////

  /** Add a correction for ML training */
  public void addCorrection(String pointName, String correctMatch, String equipmentType)
  {
    if (mlLearner == null)
      mlLearner = new MLOntologyLearner();

    mlLearner.addExample(pointName, equipmentType, correctMatch);
    LOG.message("ML correction added: " + pointName + " -> " + correctMatch);
  }

  /** Train the ML model */
  public boolean trainMLModel()
  {
    if (mlLearner == null)
    {
      LOG.warning("ML learner not initialized");
      return false;
    }

    boolean success = mlLearner.train();
    if (success)
      LOG.message("ML model trained successfully");
    else
      LOG.warning("ML training failed - need at least 5 examples");

    return success;
  }

  /** Get ML statistics */
  public String getMLStats()
  {
    if (mlLearner == null)
      return "ML learner not initialized";

    return mlLearner.getStats();
  }

////////////////////////////////////////////////////////////////
// KODE Labs Integration
////////////////////////////////////////////////////////////////

  /** Sync devices to KODE Labs */
  public void doSyncToKodeLabs()
  {
    try
    {
      // Validate configuration
      String baseUrl = getKodeBaseUrl();
      String accessToken = getKodeAccessToken();
      String buildingId = getKodeBuildingId();
      String datasourceId = getKodeDatasourceId();

      if (accessToken == null || accessToken.isEmpty())
      {
        LOG.error("KODE Labs sync failed: Access token not configured");
        return;
      }

      if (buildingId == null || buildingId.isEmpty())
      {
        LOG.error("KODE Labs sync failed: Building ID not configured");
        return;
      }

      if (datasourceId == null || datasourceId.isEmpty())
      {
        LOG.error("KODE Labs sync failed: Datasource ID not configured");
        return;
      }

      // Create client and sync
      KodeLabsClient client = new KodeLabsClient(
        baseUrl,
        accessToken,
        buildingId,
        datasourceId,
        getMinConfidence()
      );

      String result = client.syncDevices(index);
      LOG.message("KODE Labs sync result: " + result);
    }
    catch (Exception e)
    {
      LOG.error("KODE Labs sync failed", e);
    }
  }

////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////

  static final Log LOG = Log.getLog("googleOntology");

  private OntologyIndex index = new OntologyIndex();
  private MLOntologyLearner mlLearner;
}
