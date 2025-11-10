//
// Copyright (c) 2025
// Licensed under the MIT License
//

package googleOntology.model;

import java.util.*;

/**
 * OntologyIndex stores all matched entities and their DBO-mapped points.
 */
public final class OntologyIndex
{
  /** Constructor */
  public OntologyIndex()
  {
    this.entityMap = new HashMap();
  }

  /** Return number of entities in index */
  public int numEntities()
  {
    return entityMap.size();
  }

  /** Get entity for given ID or null if not found */
  public GoogleOntologyEntity getEntity(String id)
  {
    return (GoogleOntologyEntity)entityMap.get(id);
  }

  /** Get collection of all entities */
  public Collection getEntities()
  {
    return entityMap.values();
  }

  /** Clear all entries from index */
  public void clear()
  {
    entityMap.clear();
  }

  /** Add a new entity to index */
  public void addEntity(GoogleOntologyEntity entity)
  {
    entityMap.put(entity.getId(), entity);
  }

////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////

  private final HashMap entityMap;  // entity.id : GoogleOntologyEntity
}
