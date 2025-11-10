#!/usr/bin/env python3
"""
Test script for Google Ontology ML Training Endpoints

This tests the ML learning functionality:
1. Submit corrections (training examples)
2. Train the model
3. Check statistics
4. Test predictions
"""
import requests
import json
import sys

# Configuration
STATION_HOST = "localhost:8080"  # Change to your station
BASE_URL = f"http://{STATION_HOST}/googleOntology/v1"

# Credentials
USERNAME = "admin"  # Change to your station username
PASSWORD = "admin"  # Change to your station password

def test_learning_stats():
    """Get current ML learning statistics"""
    url = f"{BASE_URL}/learning/stats"

    print("\n" + "="*70)
    print("TEST: Get ML Learning Statistics")
    print("="*70)
    print(f"GET {url}")

    response = requests.get(url, auth=(USERNAME, PASSWORD))

    print(f"Status: {response.status_code}")
    print(f"Response:")
    print(json.dumps(response.json(), indent=2))

    return response.json()

def submit_correction(point_name, correct_match, equipment_type=None):
    """Submit a correction to the ML learner"""
    url = f"{BASE_URL}/learning/correct"

    payload = {
        "pointName": point_name,
        "correctMatch": correct_match
    }

    if equipment_type:
        payload["equipmentType"] = equipment_type

    print(f"\nSubmitting correction:")
    print(f"  Point: '{point_name}'")
    print(f"  Correct Match: '{correct_match}'")
    if equipment_type:
        print(f"  Equipment: '{equipment_type}'")

    response = requests.post(
        url,
        auth=(USERNAME, PASSWORD),
        headers={"Content-Type": "application/json"},
        data=json.dumps(payload)
    )

    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        print(f"✓ Correction added")
    else:
        print(f"✗ Error: {response.text}")

    return response.status_code == 200

def train_model():
    """Trigger ML model training"""
    url = f"{BASE_URL}/learning/train"

    print("\n" + "="*70)
    print("TEST: Train ML Model")
    print("="*70)
    print(f"POST {url}")

    response = requests.post(url, auth=(USERNAME, PASSWORD))

    print(f"Status: {response.status_code}")
    result = response.json()
    print(f"Response:")
    print(json.dumps(result, indent=2))

    if result.get("success"):
        print("\n✓ Model trained successfully!")
    else:
        print(f"\n✗ Training failed: {result.get('message')}")

    return result.get("success", False)

def add_sample_corrections():
    """Add sample training corrections"""
    print("\n" + "="*70)
    print("TEST: Submit Training Corrections")
    print("="*70)

    # Sample corrections based on common HVAC naming patterns
    corrections = [
        # AHU points
        ("AHU-1-DischargeTempSensor", "discharge_air_temperature_sensor", "AHU"),
        ("AHU-1-ReturnAirTemp", "return_air_temperature_sensor", "AHU"),
        ("AHU-1-SupplyAirTempSP", "supply_air_temperature_setpoint", "AHU"),
        ("AHU-1-FanStatus", "fan_run_status", "AHU"),
        ("AHU-1-FanSpeedCmd", "fan_speed_command", "AHU"),

        # VAV points
        ("VAV-101-ZoneTemp", "zone_air_temperature_sensor", "VAV"),
        ("VAV-101-DamperPosition", "damper_position_command", "VAV"),
        ("VAV-101-Airflow", "supply_air_flowrate_sensor", "VAV"),
        ("VAV-101-ZoneTempSP", "zone_air_temperature_setpoint", "VAV"),
        ("VAV-101-HeatingCmd", "heating_command", "VAV"),

        # Chiller points
        ("CH-1-ChilledWaterSupplyTemp", "chilled_water_supply_temperature_sensor", "CH"),
        ("CH-1-ChilledWaterReturnTemp", "chilled_water_return_temperature_sensor", "CH"),
        ("CH-1-RunStatus", "run_status", "CH"),
        ("CH-1-ChilledWaterFlowrate", "chilled_water_flowrate_sensor", "CH"),
        ("CH-1-CondenserWaterTemp", "condenser_water_supply_temperature_sensor", "CH"),
    ]

    success_count = 0
    for point_name, correct_match, equipment_type in corrections:
        if submit_correction(point_name, correct_match, equipment_type):
            success_count += 1

    print(f"\n✓ Added {success_count}/{len(corrections)} corrections")
    return success_count

def test_mappings():
    """Get current mappings to see what was matched"""
    url = f"{BASE_URL}/mappings"

    print("\n" + "="*70)
    print("TEST: Check Current Mappings")
    print("="*70)
    print(f"GET {url}")

    response = requests.get(url, auth=(USERNAME, PASSWORD))

    if response.status_code == 200:
        data = response.json()
        mappings = data.get("mappings", [])

        print(f"\nFound {len(mappings)} mapped points")

        # Show first 5 as examples
        print("\nExample mappings:")
        for mapping in mappings[:5]:
            print(f"  {mapping['originalName']:40} → {mapping['dboField']:40} ({mapping['confidence']}%)")
    else:
        print(f"Error: {response.status_code}")

def full_training_workflow():
    """Run complete ML training workflow"""
    print("\n" + "="*70)
    print("GOOGLE ONTOLOGY ML TRAINING TEST")
    print("="*70)

    # Step 1: Check initial stats
    print("\n### Step 1: Check Initial ML Stats ###")
    initial_stats = test_learning_stats()

    # Step 2: Add training corrections
    print("\n### Step 2: Add Training Corrections ###")
    num_corrections = add_sample_corrections()

    if num_corrections < 5:
        print("\n⚠ Warning: Need at least 5 corrections for training")
        print("Add more corrections and try again")
        return False

    # Step 3: Train the model
    print("\n### Step 3: Train ML Model ###")
    success = train_model()

    if not success:
        print("\n✗ Training failed!")
        return False

    # Step 4: Check updated stats
    print("\n### Step 4: Check Updated ML Stats ###")
    final_stats = test_learning_stats()

    # Step 5: Check current mappings
    print("\n### Step 5: Review Current Mappings ###")
    test_mappings()

    print("\n" + "="*70)
    print("WORKFLOW COMPLETE!")
    print("="*70)
    print("\nNext steps:")
    print("  1. Add more corrections based on your station's naming patterns")
    print("  2. Retrain the model with more examples")
    print("  3. Rebuild the index to use ML predictions")
    print("  4. Check /v1/mappings to see improved confidence scores")

    return True

def interactive_corrections():
    """Interactive mode to add corrections"""
    print("\n" + "="*70)
    print("INTERACTIVE CORRECTION MODE")
    print("="*70)
    print("Add corrections to train the ML model")
    print("(Press Ctrl+C to exit)\n")

    corrections_added = 0

    try:
        while True:
            point_name = input("\nEnter point name (or 'done' to finish): ").strip()
            if point_name.lower() == 'done':
                break

            correct_match = input("Enter correct DBO field: ").strip()
            if not correct_match:
                print("DBO field required!")
                continue

            equipment_type = input("Enter equipment type (optional, e.g., AHU, VAV): ").strip()
            equipment_type = equipment_type if equipment_type else None

            if submit_correction(point_name, correct_match, equipment_type):
                corrections_added += 1
                print(f"✓ Total corrections: {corrections_added}")

    except KeyboardInterrupt:
        print("\n\nExiting interactive mode...")

    if corrections_added > 0:
        print(f"\n✓ Added {corrections_added} corrections")

        if corrections_added >= 5:
            print("\nYou have enough corrections to train the model!")
            train_now = input("Train model now? (y/n): ").strip().lower()

            if train_now == 'y':
                train_model()
                test_learning_stats()
        else:
            print(f"\n⚠ Need {5 - corrections_added} more corrections to train")

def main():
    """Main entry point"""
    if len(sys.argv) > 1:
        command = sys.argv[1]

        if command == "stats":
            test_learning_stats()
        elif command == "train":
            train_model()
        elif command == "mappings":
            test_mappings()
        elif command == "interactive":
            interactive_corrections()
        elif command == "full":
            full_training_workflow()
        else:
            print(f"Unknown command: {command}")
            print_usage()
    else:
        # Default: run full workflow
        full_training_workflow()

def print_usage():
    """Print usage information"""
    print("\nUsage: python test_ml_training.py [command]")
    print("\nCommands:")
    print("  stats        - Show ML learning statistics")
    print("  train        - Train the ML model")
    print("  mappings     - Show current point mappings")
    print("  interactive  - Add corrections interactively")
    print("  full         - Run complete training workflow (default)")
    print("\nExample:")
    print("  python test_ml_training.py stats")
    print("  python test_ml_training.py interactive")

if __name__ == "__main__":
    main()
