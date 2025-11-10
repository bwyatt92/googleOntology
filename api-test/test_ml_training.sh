#!/bin/bash
#
# Test script for Google Ontology ML Training using curl
#

STATION_HOST="localhost:8080"
BASE_URL="http://${STATION_HOST}/googleOntology/v1"
AUTH="admin:admin"  # Change to your credentials

echo "========================================================================"
echo "Google Ontology ML Training Test (curl)"
echo "========================================================================"

# Step 1: Check initial stats
echo -e "\n### Step 1: Check ML Statistics ###"
echo "GET ${BASE_URL}/learning/stats"
curl -s -u "$AUTH" "${BASE_URL}/learning/stats" | python3 -m json.tool

# Step 2: Add training corrections
echo -e "\n### Step 2: Submit Training Corrections ###"

corrections=(
    '{"pointName":"AHU-1-DischargeTempSensor","correctMatch":"discharge_air_temperature_sensor","equipmentType":"AHU"}'
    '{"pointName":"AHU-1-ReturnAirTemp","correctMatch":"return_air_temperature_sensor","equipmentType":"AHU"}'
    '{"pointName":"AHU-1-SupplyAirTempSP","correctMatch":"supply_air_temperature_setpoint","equipmentType":"AHU"}'
    '{"pointName":"AHU-1-FanStatus","correctMatch":"fan_run_status","equipmentType":"AHU"}'
    '{"pointName":"AHU-1-FanSpeedCmd","correctMatch":"fan_speed_command","equipmentType":"AHU"}'
    '{"pointName":"VAV-101-ZoneTemp","correctMatch":"zone_air_temperature_sensor","equipmentType":"VAV"}'
    '{"pointName":"VAV-101-DamperPosition","correctMatch":"damper_position_command","equipmentType":"VAV"}'
    '{"pointName":"VAV-101-Airflow","correctMatch":"supply_air_flowrate_sensor","equipmentType":"VAV"}'
)

count=0
for correction in "${corrections[@]}"; do
    echo -e "\nSubmitting correction $((count+1))..."
    curl -s -u "$AUTH" \
        -H "Content-Type: application/json" \
        -d "$correction" \
        "${BASE_URL}/learning/correct" | python3 -m json.tool
    count=$((count+1))
done

echo -e "\n✓ Submitted $count corrections"

# Step 3: Train the model
echo -e "\n### Step 3: Train ML Model ###"
echo "POST ${BASE_URL}/learning/train"
curl -s -u "$AUTH" -X POST "${BASE_URL}/learning/train" | python3 -m json.tool

# Step 4: Check updated stats
echo -e "\n### Step 4: Check Updated ML Stats ###"
curl -s -u "$AUTH" "${BASE_URL}/learning/stats" | python3 -m json.tool

# Step 5: Check mappings
echo -e "\n### Step 5: Check Current Mappings ###"
echo "GET ${BASE_URL}/mappings"
curl -s -u "$AUTH" "${BASE_URL}/mappings" | python3 -c "
import json
import sys
data = json.load(sys.stdin)
mappings = data.get('mappings', [])
print(f'Total mappings: {len(mappings)}')
print('\nExample mappings:')
for m in mappings[:10]:
    print(f\"  {m['originalName']:40} → {m['dboField']:40} ({m['confidence']}%)\")
"

echo -e "\n========================================================================"
echo "WORKFLOW COMPLETE!"
echo "========================================================================"
echo -e "\nML Training Endpoints:"
echo "  POST /v1/learning/correct - Submit a correction"
echo "  POST /v1/learning/train   - Train the model"
echo "  GET  /v1/learning/stats   - Get statistics"
