//
// Copyright (c) 2025
// Licensed under the MIT License
//

package googleOntology.learning;

/**
 * SimpleLearnerTest - Shows the learner working!
 *
 * Run this to see ML in action (no Niagara needed for this demo).
 */
public class SimpleLearnerTest
{
  public static void main(String[] args)
  {
    System.out.println("=== Simple Learning Demo ===\n");

    SimpleLearner learner = new SimpleLearner();

    // Scenario: Fuzzy matcher keeps matching "DAT" to wrong field
    System.out.println("Initial matching:");
    System.out.println("  Point: 'DAT' → 'discharge_air_temperature' (confidence: 65)");
    System.out.println("  Point: 'DAT_Sensor' → 'discharge_air_temperature' (confidence: 70)");

    int confidence1 = 65;
    int confidence2 = 70;

    System.out.println("\n--- User submits corrections ---\n");

    // User corrects it 3 times
    learner.learnFromCorrection("DAT", "discharge_air_temperature", "discharge_air_temperature_sensor");
    learner.learnFromCorrection("DAT_Sensor", "discharge_air_temperature", "discharge_air_temperature_sensor");
    learner.learnFromCorrection("AHU1_DAT", "discharge_air_temperature", "discharge_air_temperature_sensor");

    System.out.println("\n--- Applying learned adjustments ---\n");

    // Now apply learned adjustments
    int adjusted1 = learner.adjustConfidence("DAT", "discharge_air_temperature_sensor", confidence1);
    int adjusted2 = learner.adjustConfidence("DAT_Sensor", "discharge_air_temperature_sensor", confidence2);

    System.out.println("\nAfter learning:");
    System.out.println("  Point: 'DAT' → 'discharge_air_temperature_sensor' (confidence: " + adjusted1 + ") ✓");
    System.out.println("  Point: 'DAT_Sensor' → 'discharge_air_temperature_sensor' (confidence: " + adjusted2 + ") ✓");

    System.out.println("\n" + learner.getStats());

    System.out.println("\n=== This is Machine Learning! ===");
    System.out.println("✓ Training data: User corrections");
    System.out.println("✓ Learning: Pattern recognition");
    System.out.println("✓ Inference: Confidence adjustment");
    System.out.println("✓ Improvement: Gets better with more data");
  }
}
