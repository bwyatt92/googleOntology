# Niagara → KODE Labs Integration Guide

## Overview

This guide shows how to sync DBO-mapped devices and points from your Niagara Google Ontology module to KODE Labs using the Public API.

## Workflow

```
┌─────────────────┐
│ Niagara Station │  Google Ontology module fuzzy-matches points
│ Google Ontology │  to DBO fields (e.g., "DAT" → "discharge_air_temperature_sensor")
└────────┬────────┘
         │
         │ HTTP API (/v1/entities, /v1/entity/{id}/fields)
         ▼
┌─────────────────┐
│ Python Script   │  Fetches entities and points
│ niagara_to_kode │  Transforms to KODE Labs format
└────────┬────────┘
         │
         │ KODE Labs Public API (POST /devices or /devices/batch)
         ▼
┌─────────────────┐
│ KODE Labs       │  Devices and points created
│ Building        │  (Ontology tagging via UI for now)
└─────────────────┘
```

## Prerequisites

1. **Niagara Station** with Google Ontology module installed and running
2. **KODE Labs Service Account** with credentials configured
3. **Building and Datasource IDs** from KODE Labs
4. **Python 3.7+** with required packages

## Setup

### 1. Configure the Script

Edit `niagara_to_kode.py` and update these settings:

```python
# Niagara Station
NIAGARA_HOST = "localhost:8080"  # Your station host:port
NIAGARA_USER = "admin"           # Your username
NIAGARA_PASS = "admin"           # Your password

# KODE Labs
KODE_BUILDING_ID = "6903c1a86c822602dddb80d1"      # Your building ID
KODE_DATASOURCE_ID = "690d074c5e543310004e2c5a"    # Your datasource ID

# Filtering
MIN_CONFIDENCE = 70  # Only sync points with confidence >= this
ENTITY_TYPE_FILTER = ["AHU", "VAV", "FCU", "CH", "BLR"]  # Types to sync
```

### 2. Get KODE Labs Access Token

```bash
# If you have access_token.txt
export ACCESS_TOKEN=$(cat access_token.txt)

# Or generate new token (if you have get_token.py)
export ACCESS_TOKEN=$(python3 get_token.py)
```

### 3. Verify Niagara API is Accessible

```bash
# Test connectivity
curl -u admin:admin http://localhost:8080/googleOntology/v1/entities | python3 -m json.tool
```

## Usage

### Dry Run (Recommended First Step)

Preview what would be created without actually making changes:

```bash
python3 niagara_to_kode.py --dry-run
```

This will:
- Fetch entities from Niagara
- Transform to KODE Labs format
- Show preview of what would be created
- Display sample device payload

### Batch Mode (Recommended)

Create all devices in one API call:

```bash
python3 niagara_to_kode.py --batch
```

**Pros:**
- Faster (single API call)
- More efficient

**Cons:**
- If one device fails, entire batch may fail
- Less granular error reporting

### One-by-One Mode

Create devices individually:

```bash
python3 niagara_to_kode.py --one-by-one
```

**Pros:**
- Better error handling
- Can see which specific devices fail
- Can recover from partial failures

**Cons:**
- Slower
- More API calls

### Include All Entity Types

By default, only HVAC equipment (AHU, VAV, etc.) is synced. To include everything:

```bash
python3 niagara_to_kode.py --all
```

## What Gets Synced

### Entities (Devices)
- **Niagara Entity** → **KODE Labs Device**
- ID: `niagara_{entity_id}`
- Name: Entity name from Niagara
- Type: Inferred from entity type

### Points
- **DBO Field** → **Point Name** (for now - canonical type when API supports it)
- Only points with `confidence >= MIN_CONFIDENCE`
- Includes units if available
- Kind inferred from DBO field (Bool, Str, Number)

### Example Transformation

**Niagara Entity:**
```json
{
  "id": "4f4ab",
  "name": "AHU1",
  "entityType": "AHU",
  "numPoints": 9
}
```

**Niagara Fields:**
```json
{
  "fields": [
    {
      "dboField": "discharge_air_temperature_sensor",
      "originalName": "Discharge Air Temp",
      "addr": "DAT",
      "confidence": 95,
      "units": "°F"
    }
  ]
}
```

**KODE Labs Device Payload:**
```json
{
  "id": "niagara_4f4ab",
  "name": "AHU1",
  "displayName": "AHU1",
  "points": [
    {
      "kind": "Number",
      "name": "Discharge Air Temp",
      "pointId": "4f4ab_DAT",
      "unit": "degF"
    }
  ]
}
```

## Current Limitations

### Ontology Tagging

**Issue:** The KODE Labs Public API doesn't currently support setting ontology (canonical types) programmatically.

**Workaround:** Devices and points are created, but ontology must be assigned via the KODE OS UI using batch update features.

**Future:** When KODE Labs provides an ontology endpoint, we can include:
```json
{
  "ontology": {
    "canonicalType": "discharge-air-temperature-sensor"
  }
}
```

### What We Have Ready

The script already:
- Knows the DBO field → KODE canonical type mapping
- Structures data to be ready for ontology tagging
- Can be easily updated when API supports it

## Troubleshooting

### "ACCESS_TOKEN not set"

Make sure you export the token:
```bash
export ACCESS_TOKEN=$(cat access_token.txt)
```

Check it's set:
```bash
echo $ACCESS_TOKEN
```

### "Connection refused" to Niagara

- Verify Niagara station is running
- Check host/port in configuration
- Test with curl: `curl -u admin:admin http://localhost:8080/googleOntology/v1/about`

### "401 Unauthorized" to KODE Labs

- Token expired (1 hour lifetime)
- Regenerate token
- Check credentials in `config.json`

### Devices Created but No Points

- Check `MIN_CONFIDENCE` setting
- Lower it to include more points: `MIN_CONFIDENCE = 50`
- Check `/v1/mappings` endpoint to see actual confidence scores

### Batch Creation Fails

- Try `--one-by-one` mode instead
- Check individual device payloads
- Look at error messages for specific issues

## Advanced Usage

### Custom Filtering

Edit the script to add custom filters:

```python
# Only sync specific entities by name
entities = [e for e in entities if 'AHU' in e['name']]

# Only sync points with specific DBO fields
def transform_entity_to_kode_device(...):
    # ... inside the function ...
    if 'temperature' not in field['dboField']:
        continue  # Skip non-temperature points
```

### Logging

Add verbose logging:

```python
import logging
logging.basicConfig(level=logging.DEBUG)
```

### Export to File

Save the transformed devices to a file before syncing:

```python
# After transformation step
with open('kode_devices.json', 'w') as f:
    json.dump({"devices": kode_devices}, f, indent=2)
```

## Next Steps

### 1. Initial Sync

1. Run with `--dry-run` to preview
2. Review the devices and points that would be created
3. Adjust `MIN_CONFIDENCE` if needed
4. Run with `--batch` to create devices

### 2. Verify in KODE Labs

1. Log into KODE OS
2. Navigate to your building
3. Check that devices were created
4. Verify points are present

### 3. Assign Ontology (Manual for Now)

1. In KODE OS UI, use batch update feature
2. Assign canonical types to devices and points
3. Based on the DBO field names in the point names

### 4. Future: Automated Ontology

When KODE Labs API supports ontology assignment:
1. Update the script to include ontology in payload
2. Re-run to update existing devices
3. Or sync new devices with ontology already applied

## Integration with ML Training

The DBO mappings from Niagara are high-confidence. You can:

1. **Export Niagara mappings** for ML training
   ```bash
   curl -u admin:admin http://localhost:8080/googleOntology/v1/mappings > niagara_mappings.json
   ```

2. **Use as training data** for the ML model
   ```bash
   python3 test_ml_training.py interactive
   # Or write script to convert niagara_mappings.json to corrections
   ```

3. **Continuous improvement:**
   - Niagara fuzzy matching gets better with ML
   - Future syncs have higher confidence scores
   - Less manual ontology assignment needed

## Summary

Current workflow:
1. ✅ Niagara fuzzy-matches points to DBO
2. ✅ Python script syncs devices/points to KODE Labs
3. ⏳ Manual ontology assignment in KODE OS UI
4. ⏳ Waiting for ontology API endpoint

Future workflow (when ontology API available):
1. ✅ Niagara fuzzy-matches points to DBO
2. ✅ Python script syncs with ontology included
3. ✅ Devices appear in KODE Labs fully tagged
4. ✅ No manual steps needed!
