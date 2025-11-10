//
// Copyright (c) 2025
// Licensed under the MIT License
//

package googleOntology.learning;

import smile.classification.*;
import java.util.*;

/**
 * MLOntologyLearner - Real ML for ontology matching!
 *
 * Uses Smile library (K-Nearest Neighbors) to learn
 * optimal point name → DBO field mappings from corrections.
 *
 * This is a HUGE step up from SimpleLearner:
 * - Multi-dimensional feature extraction (21 features)
 * - K-Nearest Neighbors classifier (finds 5 most similar examples)
 * - Learns from user corrections
 * - Generalizes to new, unseen point names
 */
public class MLOntologyLearner
{
  /**
   * Training example
   */
  public static class TrainingExample {
    public final String pointName;
    public final String equipmentType;
    public final String dboField;
    public final long timestamp;

    public TrainingExample(String pointName, String equipmentType, String dboField) {
      this.pointName = pointName;
      this.equipmentType = equipmentType;
      this.dboField = dboField;
      this.timestamp = System.currentTimeMillis();
    }
  }

  private final List examples;
  private final Map dboFieldIndex;  // DBO field → numeric label
  private final Map reverseDboIndex; // numeric label → DBO field
  private KNN model;  // Using KNN - simpler API, no DataFrame needed!
  private double[][] trainX;
  private int[] trainY;
  private int nextLabel = 0;

  public MLOntologyLearner() {
    this.examples = new ArrayList();
    this.dboFieldIndex = new HashMap();
    this.reverseDboIndex = new HashMap();
  }

  /**
   * Add a training example (correction from user)
   */
  public void addExample(String pointName, String equipmentType, String correctDboField) {
    TrainingExample ex = new TrainingExample(pointName, equipmentType, correctDboField);
    examples.add(ex);

    // Assign numeric label to DBO field if new
    if (!dboFieldIndex.containsKey(correctDboField)) {
      Integer label = new Integer(nextLabel++);
      dboFieldIndex.put(correctDboField, label);
      reverseDboIndex.put(label, correctDboField);
    }

    System.out.println("[MLOntologyLearner] Added training example: '" + pointName +
                      "' (" + equipmentType + ") → '" + correctDboField + "'");
  }

  /**
   * Train the model on accumulated examples
   */
  public boolean train() {
    if (examples.size() < 5) {
      System.out.println("[MLOntologyLearner] Need at least 5 examples to train (have " +
                        examples.size() + ")");
      return false;
    }

    try {
      // Extract features and labels
      int n = examples.size();
      double[][] X = new double[n][];
      int[] y = new int[n];

      for (int i = 0; i < n; i++) {
        TrainingExample ex = (TrainingExample)examples.get(i);
        X[i] = extractFeatures(ex.pointName, ex.equipmentType);
        Integer label = (Integer)dboFieldIndex.get(ex.dboField);
        y[i] = label.intValue();
      }

      // Train KNN classifier (simpler API, no DataFrame needed!)
      // k=5 means look at 5 nearest neighbors
      this.trainX = X;
      this.trainY = y;
      model = KNN.fit(X, y, 5);

      System.out.println("[MLOntologyLearner] KNN model trained! " + n + " examples, " +
                        dboFieldIndex.size() + " classes (k=5 neighbors)");
      return true;

    } catch (Exception e) {
      System.err.println("[MLOntologyLearner] Training failed: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Predict DBO field for a new point
   */
  public Prediction predict(String pointName, String equipmentType) {
    if (model == null) {
      return new Prediction(null, 0, "Model not trained");
    }

    try {
      double[] features = extractFeatures(pointName, equipmentType);
      int labelPredicted = model.predict(features);

      String predictedField = (String)reverseDboIndex.get(new Integer(labelPredicted));

      // Get confidence (not all classifiers provide this easily, so we'll estimate)
      int confidence = 75; // Base confidence for ML predictions

      return new Prediction(predictedField, confidence, "ML prediction");

    } catch (Exception e) {
      return new Prediction(null, 0, "Prediction error: " + e.getMessage());
    }
  }

  /**
   * Extract features from point name and equipment type
   *
   * This is where the magic happens! Good features = good model.
   */
  private double[] extractFeatures(String pointName, String equipmentType) {
    String lower = pointName.toLowerCase();
    String equipLower = equipmentType != null ? equipmentType.toLowerCase() : "";

    // Feature vector (21 features)
    return new double[] {
      // Length features
      lower.length(),
      countTokens(lower),

      // Keyword features (binary)
      contains(lower, "temp", "temperature") ? 1 : 0,
      contains(lower, "press", "pressure") ? 1 : 0,
      contains(lower, "flow") ? 1 : 0,
      contains(lower, "fan", "supply", "return", "exhaust") ? 1 : 0,
      contains(lower, "damper", "dmp") ? 1 : 0,
      contains(lower, "valve", "vlv") ? 1 : 0,
      contains(lower, "setpoint", "sp", "set") ? 1 : 0,
      contains(lower, "sensor", "snsr") ? 1 : 0,
      contains(lower, "command", "cmd") ? 1 : 0,
      contains(lower, "status", "sts") ? 1 : 0,

      // Position indicators
      contains(lower, "discharge", "dat", "supply") ? 1 : 0,
      contains(lower, "return", "rat") ? 1 : 0,
      contains(lower, "mixed", "mat") ? 1 : 0,
      contains(lower, "outside", "oat", "outdoor") ? 1 : 0,

      // Equipment type features
      contains(equipLower, "ahu") ? 1 : 0,
      contains(equipLower, "vav") ? 1 : 0,
      contains(equipLower, "chiller", "chws") ? 1 : 0,
      contains(equipLower, "boiler", "hws") ? 1 : 0,
      contains(equipLower, "fcu", "fan_coil") ? 1 : 0,
    };
  }

  /**
   * Feature names (for DataFrame)
   */
  private String[] getFeatureNames() {
    return new String[] {
      "name_length", "token_count",
      "has_temp", "has_pressure", "has_flow", "has_fan",
      "has_damper", "has_valve", "has_setpoint", "has_sensor",
      "has_command", "has_status",
      "is_discharge", "is_return", "is_mixed", "is_outside",
      "equip_ahu", "equip_vav", "equip_chiller", "equip_boiler", "equip_fcu"
    };
  }

  /**
   * Count tokens in name
   */
  private int countTokens(String name) {
    int count = 1;
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (c == '_' || c == ' ' || c == '-' || Character.isUpperCase(c)) count++;
    }
    return count;
  }

  /**
   * Check if string contains any of the keywords
   */
  private boolean contains(String str, String... keywords) {
    for (int i = 0; i < keywords.length; i++) {
      if (str.contains(keywords[i])) return true;
    }
    return false;
  }

  /**
   * Prediction result
   */
  public static class Prediction {
    public final String dboField;
    public final int confidence;
    public final String reasoning;

    public Prediction(String dboField, int confidence, String reasoning) {
      this.dboField = dboField;
      this.confidence = confidence;
      this.reasoning = reasoning;
    }
  }

  /**
   * Get statistics
   */
  public String getStats() {
    return "ML Model: " + examples.size() + " training examples, " +
           dboFieldIndex.size() + " DBO fields, " +
           (model != null ? "trained" : "not trained");
  }

  /**
   * Check if model is ready
   */
  public boolean isReady() {
    return model != null;
  }
}
