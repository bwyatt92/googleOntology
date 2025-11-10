//
// Copyright (c) 2025
// Licensed under the MIT License
//

package googleOntology.learning;

/**
 * MLOntologyLearnerDemo - Shows real ML in action!
 *
 * This demonstrates how K-Nearest Neighbors (KNN) can learn complex patterns
 * that simple fuzzy matching might miss. KNN finds the 5 most similar
 * examples and predicts based on their labels.
 */
public class MLOntologyLearnerDemo
{
  public static void main(String[] args)
  {
    System.out.println("=== ML Ontology Learner Demo ===\n");

    MLOntologyLearner learner = new MLOntologyLearner();

    System.out.println("Training with real-world examples...\n");

    // Add training data (simulating user corrections over time)
    // AHU points
    learner.addExample("DAT", "AHU", "discharge_air_temperature_sensor");
    learner.addExample("Discharge Air Temp", "AHU", "discharge_air_temperature_sensor");
    learner.addExample("SupplyAirTemp", "AHU", "discharge_air_temperature_sensor");
    learner.addExample("RAT", "AHU", "return_air_temperature_sensor");
    learner.addExample("Return Temp", "AHU", "return_air_temperature_sensor");
    learner.addExample("MAT", "AHU", "mixed_air_temperature_sensor");
    learner.addExample("OAT", "AHU", "outside_air_temperature_sensor");
    learner.addExample("Supply Fan Speed", "AHU", "supply_fan_speed_command");
    learner.addExample("SF_Speed", "AHU", "supply_fan_speed_command");
    learner.addExample("Supply Fan Status", "AHU", "supply_fan_run_status");

    // VAV points
    learner.addExample("Zone Temp", "VAV", "zone_air_temperature_sensor");
    learner.addExample("Space Temp", "VAV", "zone_air_temperature_sensor");
    learner.addExample("Room Temperature", "VAV", "zone_air_temperature_sensor");
    learner.addExample("Zone Temp SP", "VAV", "zone_air_temperature_setpoint");
    learner.addExample("Damper Position", "VAV", "supply_air_damper_command");
    learner.addExample("DMP_Cmd", "VAV", "supply_air_damper_command");
    learner.addExample("Reheat Valve", "VAV", "reheat_valve_command");

    // Chiller points
    learner.addExample("CHWS_Temp", "CHILLER", "chilled_water_supply_temperature_sensor");
    learner.addExample("CHW Supply Temp", "CHILLER", "chilled_water_supply_temperature_sensor");
    learner.addExample("CHWR_Temp", "CHILLER", "chilled_water_return_temperature_sensor");

    System.out.println("\n--- Training Model ---\n");
    boolean success = learner.train();

    if (!success) {
      System.out.println("Training failed!");
      return;
    }

    System.out.println("\n=== Testing Predictions ===\n");

    // Test on new, unseen point names
    testPrediction(learner, "DischgAirTemp", "AHU");
    testPrediction(learner, "SAT", "AHU");  // Should predict discharge_air (supply = discharge)
    testPrediction(learner, "ReturnAirTemp", "AHU");
    testPrediction(learner, "FanSpeedCmd", "AHU");
    testPrediction(learner, "RoomTemp", "VAV");
    testPrediction(learner, "ZoneTempSP", "VAV");
    testPrediction(learner, "DamperCmd", "VAV");
    testPrediction(learner, "CHWST", "CHILLER");

    System.out.println("\n" + learner.getStats());

    System.out.println("\n=== Why This Is Real ML ===");
    System.out.println("✓ Feature engineering: 21 extracted features");
    System.out.println("✓ K-Nearest Neighbors classifier (Smile library)");
    System.out.println("✓ Generalizes to unseen examples");
    System.out.println("✓ Multi-dimensional pattern recognition");
    System.out.println("✓ Instance-based learning (finds similar examples)");
    System.out.println("\n✓ This runs IN NIAGARA - no cloud needed!");
  }

  private static void testPrediction(MLOntologyLearner learner, String pointName, String equipType)
  {
    MLOntologyLearner.Prediction pred = learner.predict(pointName, equipType);

    System.out.println("Point: '" + pointName + "' (" + equipType + ")");
    System.out.println("  → Predicted: " + pred.dboField);
    System.out.println("  → Confidence: " + pred.confidence + "%");
    System.out.println("  → " + pred.reasoning + "\n");
  }
}
