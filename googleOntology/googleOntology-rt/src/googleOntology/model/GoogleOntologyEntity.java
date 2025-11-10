//
// Copyright (c) 2025
// Licensed under the MIT License
//

package googleOntology.model;

import java.util.*;
import javax.baja.sys.*;

/**
 * GoogleOntologyEntity represents an equipment/device with DBO-mapped points.
 */
public final class GoogleOntologyEntity
{
  /**
   * Constructor
   */
  public GoogleOntologyEntity(String id, String name, String path, String entityType)
  {
    this.id = id;
    this.name = name;
    this.path = path;
    this.entityType = entityType;
    this.points = new ArrayList();
  }

  /** Get entity ID */
  public String getId() { return id; }

  /** Get entity name */
  public String getName() { return name; }

  /** Get entity path */
  public String getPath() { return path; }

  /** Get DBO entity type (AHU, VAV, CHWS, etc.) */
  public String getEntityType() { return entityType; }

  /** Get point for given address or null if not found */
  public GoogleOntologyPoint getPoint(String addr)
  {
    for (int i=0; i<points.size(); i++)
    {
      GoogleOntologyPoint p = (GoogleOntologyPoint)points.get(i);
      if (p.getAddr().equals(addr)) return p;
    }
    return null;
  }

  /** Get list of points */
  public List getPoints()
  {
    return points;
  }

////////////////////////////////////////////////////////////////
// Public
////////////////////////////////////////////////////////////////

  /** Add a point to this entity */
  public void addPoint(GoogleOntologyPoint point)
  {
    points.add(point);
  }

  /** Reference to actual Niagara component */
  public BComponent comp;

////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////

  private final String id;
  private final String name;
  private final String path;
  private final String entityType;
  private final ArrayList points;
}
