//
// Copyright (c) 2025
// Licensed under the MIT License
//

package googleOntology.learning;

import java.util.*;

/**
 * SimpleLearner - Baby's first ML in Niagara!
 *
 * This learns from user corrections to improve fuzzy matching.
 * Uses basic statistics (no fancy ML libraries needed).
 */
public class SimpleLearner
{
  /**
   * A correction from a user
   */
  public static class Correction {
    public final String originalName;
    public final String wrongMatch;
    public final String correctMatch;
    public final long timestamp;

    public Correction(String originalName, String wrongMatch, String correctMatch) {
      this.originalName = originalName;
      this.wrongMatch = wrongMatch;
      this.correctMatch = correctMatch;
      this.timestamp = System.currentTimeMillis();
    }
  }

  private final List corrections;
  private final Map confidenceAdjustments; // How much to boost certain patterns

  public SimpleLearner() {
    this.corrections = new ArrayList();
    this.confidenceAdjustments = new HashMap();
  }

  /**
   * Record a user correction
   */
  public void learnFromCorrection(String originalName, String wrongMatch, String correctMatch) {
    Correction c = new Correction(originalName, wrongMatch, correctMatch);
    corrections.add(c);

    // Simple learning: If we see the same correction multiple times,
    // boost confidence for that pattern
    String pattern = extractPattern(originalName, correctMatch);
    Integer count = (Integer)confidenceAdjustments.get(pattern);
    if (count == null) count = new Integer(0);
    confidenceAdjustments.put(pattern, new Integer(count.intValue() + 1));

    System.out.println("[SimpleLearner] Learned: '" + originalName + "' -> '" + correctMatch + "' (pattern: " + pattern + ")");
  }

  /**
   * Adjust confidence based on learned patterns
   */
  public int adjustConfidence(String originalName, String proposedMatch, int baseConfidence) {
    String pattern = extractPattern(originalName, proposedMatch);
    Integer boostCount = (Integer)confidenceAdjustments.get(pattern);

    if (boostCount != null) {
      // Each correction adds +5 confidence (max +20)
      int boost = Math.min(boostCount.intValue() * 5, 20);
      int adjusted = Math.min(baseConfidence + boost, 100);

      if (boost > 0) {
        System.out.println("[SimpleLearner] Boosting confidence: " + baseConfidence + " -> " + adjusted + " (learned from " + boostCount + " corrections)");
      }

      return adjusted;
    }

    return baseConfidence;
  }

  /**
   * Extract a simple pattern (e.g., "temp" -> "temperature")
   */
  private String extractPattern(String original, String match) {
    // Very simple: look for common tokens
    String origLower = original.toLowerCase();
    String matchLower = match.toLowerCase();

    // If original contains key words from match, that's our pattern
    if (matchLower.contains("temperature") && origLower.contains("temp"))
      return "temp->temperature";
    if (matchLower.contains("pressure") && origLower.contains("press"))
      return "press->pressure";
    if (matchLower.contains("setpoint") && origLower.contains("sp"))
      return "sp->setpoint";

    // Default: first 3 chars of each
    String origPrefix = origLower.length() >= 3 ? origLower.substring(0, 3) : origLower;
    String matchPrefix = matchLower.length() >= 3 ? matchLower.substring(0, 3) : matchLower;
    return origPrefix + "->" + matchPrefix;
  }

  /**
   * Get statistics about what we've learned
   */
  public String getStats() {
    return "Learned from " + corrections.size() + " corrections, " +
           confidenceAdjustments.size() + " patterns discovered";
  }

  /**
   * Get all corrections (for export/review)
   */
  public List getCorrections() {
    return new ArrayList(corrections);
  }
}
