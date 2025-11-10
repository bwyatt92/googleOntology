# Google Ontology ML Training Guide

## Overview

The Google Ontology module includes a **Machine Learning system** that learns from corrections to improve point name → DBO field matching accuracy over time.

### How It Works

1. **Fuzzy Matcher** (default) - Rule-based matching using tokens, keywords, and units
2. **ML Learner** (optional) - K-Nearest Neighbors (KNN) model that learns from corrections
3. **Hybrid Approach** - ML predictions can augment fuzzy matching for better results

The ML system uses the **Smile library** (Statistical Machine Intelligence and Learning Engine) with a K-Nearest Neighbors classifier.

## ML Training Workflow

### Step 1: Submit Corrections

When the fuzzy matcher gets something wrong, submit a correction:

```bash
curl -X POST http://localhost:8080/googleOntology/v1/learning/correct \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "pointName": "AHU-1-DischargeTemp",
    "correctMatch": "discharge_air_temperature_sensor",
    "equipmentType": "AHU"
  }'
```

**Fields:**
- `pointName` (required) - The original point name from your system
- `correctMatch` (required) - The correct DBO field it should map to
- `equipmentType` (optional) - Equipment type (AHU, VAV, CH, etc.) for better features

### Step 2: Accumulate Examples

You need **at least 5 corrections** before you can train the model. More is better!

Check current status:
```bash
curl -u admin:admin http://localhost:8080/googleOntology/v1/learning/stats
```

Response:
```json
{
  "available": true,
  "stats": "ML Model: 8 training examples, 5 DBO fields, trained"
}
```

### Step 3: Train the Model

Once you have enough corrections, trigger training:

```bash
curl -X POST http://localhost:8080/googleOntology/v1/learning/train \
  -u admin:admin
```

Response:
```json
{
  "success": true,
  "message": "Model trained successfully"
}
```

### Step 4: Rebuild Index

After training, rebuild the index to use ML predictions:

In Niagara Workbench:
1. Navigate to your `GoogleOntologyService`
2. Right-click > **Actions** > **rebuildIndex**

The service will now use ML predictions to augment fuzzy matching!

## Using the Test Scripts

### Python Test Script (Recommended)

```bash
# Run full workflow (adds sample corrections, trains model, shows results)
python3 test_ml_training.py full

# Interactive mode - add your own corrections
python3 test_ml_training.py interactive

# Check current stats
python3 test_ml_training.py stats

# Train model
python3 test_ml_training.py train

# View mappings
python3 test_ml_training.py mappings
```

### Bash Test Script

```bash
# Run complete workflow
./test_ml_training.sh
```

## ML Feature Extraction

The ML model uses **21 features** extracted from point names and equipment types:

### String Features
- Name length
- Token count

### Keyword Features (binary: 0 or 1)
- Has temperature keywords (`temp`, `temperature`)
- Has pressure keywords (`press`, `pressure`)
- Has flow keywords (`flow`)
- Has fan/air keywords (`fan`, `supply`, `return`, `exhaust`)
- Has damper keywords (`damper`, `dmp`)
- Has valve keywords (`valve`, `vlv`)
- Has setpoint keywords (`setpoint`, `sp`, `set`)
- Has sensor keywords (`sensor`, `snsr`)
- Has command keywords (`command`, `cmd`)
- Has status keywords (`status`, `sts`)

### Position Features
- Is discharge/supply
- Is return
- Is mixed
- Is outside/outdoor

### Equipment Type Features
- Is AHU
- Is VAV
- Is Chiller/CHWS
- Is Boiler/HWS
- Is FCU/Fan Coil

## Training Data Sources

### Option 1: Manual Corrections

Review the `/v1/mappings` endpoint and submit corrections for low-confidence or incorrect matches.

### Option 2: Import from KODE Labs

Extract existing mappings from KODE Labs deployed sites:

```bash
# Get all points with ontology from KODE Labs
curl --location "https://api.kodelabs.com/kodeos/api/v1/buildings/BUILDING_ID/points?page=1&limit=200" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | \
  python3 -c "
import json, sys
data = json.load(sys.stdin)
for p in data.get('data', []):
    if 'ontology' in p and p['ontology'].get('canonicalType'):
        print(f\"{p['name']},{p['ontology']['canonicalType']},{p.get('device',{}).get('ontology',{}).get('canonicalType','')}\")
" > kode_training_data.csv

# Convert to correction format
python3 convert_kode_data_to_corrections.py kode_training_data.csv
```

### Option 3: Export from Niagara Google Ontology

If you already have correctly tagged points in Niagara, export them:

```bash
curl -u admin:admin http://localhost:8080/googleOntology/v1/mappings | \
  python3 -c "
import json, sys
data = json.load(sys.stdin)
for m in data.get('mappings', []):
    if m['confidence'] > 85:  # Only high-confidence matches
        print(f\"{m['originalName']},{m['dboField']}\")
" > high_confidence_mappings.csv
```

## Best Practices

### 1. Quality Over Quantity
- Focus on **correct** corrections
- One wrong correction can hurt the model
- Review your corrections before training

### 2. Diverse Examples
- Include different equipment types (AHU, VAV, CH, BLR, etc.)
- Cover different point types (sensors, setpoints, commands, status)
- Include various naming conventions

### 3. Iterative Training
- Start with 10-20 high-quality corrections
- Train and test
- Add more corrections based on results
- Retrain periodically

### 4. Monitor Confidence Scores
- Check `/v1/mappings` regularly
- Low confidence (<70) might need correction
- Very high confidence (>95) is usually correct

### 5. Rebuild Index After Training
- Always rebuild after training
- This applies the ML predictions
- Check logs for improvement

## Integration with KODE Labs

Once your ML model is trained with KODE Labs data:

1. **Better Niagara → KODE Labs mapping**
   - Points are pre-tagged with correct ontology
   - Export to KODE OS DISCOVER format
   - Import into KODE Labs with ontology already applied

2. **Continuous Improvement Loop**
   ```
   KODE Labs Data → ML Training → Better Niagara Matching → Export to KODE Labs → Repeat
   ```

## API Reference

### POST /v1/learning/correct
Submit a correction for ML training

**Request:**
```json
{
  "pointName": "VAV-101-ZoneTemp",
  "correctMatch": "zone_air_temperature_sensor",
  "equipmentType": "VAV"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Correction added"
}
```

### POST /v1/learning/train
Trigger ML model training

**Response:**
```json
{
  "success": true,
  "message": "Model trained successfully"
}
```

Or if insufficient data:
```json
{
  "success": false,
  "message": "Training failed - need at least 5 examples"
}
```

### GET /v1/learning/stats
Get ML learning statistics

**Response:**
```json
{
  "available": true,
  "stats": "ML Model: 15 training examples, 8 DBO fields, trained"
}
```

## Troubleshooting

### "Training failed - need at least 5 examples"
- Add more corrections using `/v1/learning/correct`
- Check stats with `/v1/learning/stats`

### Model trained but no improvement
- Rebuild the index (Actions > rebuildIndex)
- Check that corrections are actually correct
- Need more diverse examples

### High-confidence matches are wrong
- Submit corrections for those specific points
- Retrain the model
- Adjust `minConfidence` threshold in service properties

### ML predictions not being used
- Verify model is trained (`/v1/learning/stats`)
- Rebuild index after training
- Check service logs for ML prediction messages

## Advanced: Model Customization

The ML model can be customized by editing:
- `MLOntologyLearner.java` - Change features, algorithm, or parameters
- `extractFeatures()` - Add domain-specific features
- `FuzzyMatcher.java` - Adjust how ML predictions are integrated

After editing, rebuild the module and restart the station.

## Example: Complete Workflow

```bash
# 1. Check current state
curl -u admin:admin http://localhost:8080/googleOntology/v1/learning/stats

# 2. Add corrections (do this 5+ times with different points)
curl -X POST http://localhost:8080/googleOntology/v1/learning/correct \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{"pointName":"AHU1-DAT","correctMatch":"discharge_air_temperature_sensor","equipmentType":"AHU"}'

# 3. Train model
curl -X POST http://localhost:8080/googleOntology/v1/learning/train -u admin:admin

# 4. Check stats
curl -u admin:admin http://localhost:8080/googleOntology/v1/learning/stats

# 5. Rebuild index in Workbench (Actions > rebuildIndex)

# 6. Verify improvement
curl -u admin:admin http://localhost:8080/googleOntology/v1/mappings
```

## Next Steps

1. **Run the test script** to verify ML training works
2. **Add real corrections** from your station
3. **Train the model** with your data
4. **Export KODE Labs data** to bootstrap training
5. **Integrate with KODE Labs** for seamless ontology tagging
