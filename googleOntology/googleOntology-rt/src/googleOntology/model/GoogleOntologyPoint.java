//
// Copyright (c) 2025
// Licensed under the MIT License
//

package googleOntology.model;

import javax.baja.sys.*;

/**
 * GoogleOntologyPoint represents a point matched to a DBO field.
 */
public final class GoogleOntologyPoint
{
  /**
   * Constructor
   */
  public GoogleOntologyPoint(
    String dboField,
    String originalName,
    String addr,
    String units,
    String enumMapping,
    int confidence)
  {
    this.dboField = dboField;
    this.originalName = originalName;
    this.addr = addr;
    this.units = units;
    this.enumMapping = enumMapping;
    this.confidence = confidence;
  }

  /** Get DBO field name */
  public String getDboField() { return dboField; }

  /** Get original point name */
  public String getOriginalName() { return originalName; }

  /** Get point address */
  public String getAddr() { return addr; }

  /** Get units (may be null) */
  public String getUnits() { return units; }

  /** Get enum mapping (may be null) */
  public String getEnumMapping() { return enumMapping; }

  /** Get confidence score (0-100) */
  public int getConfidence() { return confidence; }

  /** Set minimum value from facets */
  public void setMinValue(Double min) { this.minValue = min; }

  /** Get minimum value */
  public Double getMinValue() { return minValue; }

  /** Set maximum value from facets */
  public void setMaxValue(Double max) { this.maxValue = max; }

  /** Get maximum value */
  public Double getMaxValue() { return maxValue; }

  /** Set precision from facets */
  public void setPrecision(Integer precision) { this.precision = precision; }

  /** Get precision */
  public Integer getPrecision() { return precision; }

////////////////////////////////////////////////////////////////
// Public
////////////////////////////////////////////////////////////////

  /** Reference to actual Niagara component */
  public BComponent comp;

////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////

  private final String dboField;
  private final String originalName;
  private final String addr;
  private final String units;
  private final String enumMapping;
  private final int confidence;
  private Double minValue;
  private Double maxValue;
  private Integer precision;
}
