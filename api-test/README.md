# API Testing Suite

This directory contains test scripts for:
1. **KODE OS Public API** - Testing connectivity and data extraction
2. **Niagara Google Ontology ML Training** - Testing and using the ML learning system

## Prerequisites

1. Service Account created in KODE OS
2. RSA key pair generated for Private Key JWT authentication
3. Python 3.7+ installed

## Setup

### 1. Install Python Dependencies

```bash
pip install -r requirements.txt
```

### 2. Configure Your Credentials

Edit `kode_api_test.py` and update these lines:

```python
CLIENT_ID = "your-client-id-here"  # Your service account client ID
PRIVATE_KEY_PATH = "path/to/your/private-key.pem"  # Path to your private key file
```

### 3. Prepare Your Private Key

Make sure your private key is in PEM format. If you generated it using the KODE OS interface,
it should already be in the correct format. The file should look like:

```
-----BEGIN PRIVATE KEY-----
MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC...
...
-----END PRIVATE KEY-----
```

## Running the Test

```bash
python kode_api_test.py
```

## What the Test Does

The test script will:

1. **Authenticate** - Use Private Key JWT to get an access token
2. **List Buildings** - Retrieve all buildings your service account has access to
3. **List Devices** - Get devices for the first building
4. **List Points** - Get points for the first device

## Expected Output

If successful, you should see:

```
======================================================================
KODE OS Public API Test Script
======================================================================

======================================================================
TEST 1: Authentication
======================================================================
🔐 Authenticating with KODE OS API...
   Fetching OAuth2 discovery endpoints...
   Token endpoint: https://api.kodelabs.com/oauth2/v1/token
   Creating JWT assertion...
   Requesting access token...
✅ Successfully authenticated! Token expires at 2025-11-06 12:34:56

======================================================================
TEST 2: List Buildings
======================================================================
✅ Found 5 building(s)

Buildings:
  • Building A (ID: 507f1f77bcf86cd799439011)
  • Building B (ID: 507f191e810c19729de860ea)
  ...

======================================================================
✅ ALL TESTS PASSED!
======================================================================

API connectivity is working correctly. You can now integrate with Niagara!
```

## Troubleshooting

### Authentication Errors

- **401 Unauthorized**: Check that your CLIENT_ID is correct
- **Invalid JWT**: Verify your private key file is correct and matches the public key registered in KODE OS
- **404 Not Found**: Verify the base URL is correct

### Rate Limits

The API has the following rate limits per environment:
- 5 requests per second
- 100 requests per minute
- 500 requests per hour

If you hit rate limits, you'll receive a 429 status code.

### Connection Errors

- Verify you have internet connectivity
- Check that you can reach `https://api.kodelabs.com`
- Ensure no firewall is blocking the connection

## Part 2: Niagara Google Ontology ML Training

### Overview

The Google Ontology module includes **Machine Learning** that learns from corrections to improve matching accuracy.

### Quick Start

**Test the ML training endpoints:**

```bash
# Python (full workflow with sample data)
python3 test_ml_training.py full

# Bash (using curl)
./test_ml_training.sh
```

**Interactive mode to add your own corrections:**

```bash
python3 test_ml_training.py interactive
```

### ML Training Workflow

1. **Submit Corrections** - Add training examples
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

2. **Train Model** - Requires 5+ corrections
   ```bash
   curl -X POST http://localhost:8080/googleOntology/v1/learning/train \
     -u admin:admin
   ```

3. **Rebuild Index** - In Workbench: Right-click service > Actions > rebuildIndex

4. **Verify** - Check improved confidence scores
   ```bash
   curl -u admin:admin http://localhost:8080/googleOntology/v1/mappings
   ```

### Integration: KODE Labs → ML Training → Better Matching

```
KODE Labs Deployed Sites
         ↓
    Extract point → ontology mappings via API
         ↓
    Submit as ML training corrections
         ↓
    Train Niagara Google Ontology model
         ↓
    Better fuzzy matching on new sites
         ↓
    Export to KODE Labs with pre-tagged ontology
```

See **`ML_TRAINING_GUIDE.md`** for complete documentation.

## Next Steps

### For KODE OS API Integration
1. Review the `KodeAPIClient` class implementation
2. Integrate the authentication logic into your Niagara module
3. Use the client methods as a reference for implementing Java equivalents

### For ML Training
1. Run `python3 test_ml_training.py full` to verify endpoints work
2. Add real corrections from your station
3. Extract KODE Labs data to bootstrap training (see ML_TRAINING_GUIDE.md)
4. Train and rebuild to see improved confidence scores

## Documentation

- **`ML_TRAINING_GUIDE.md`** - Complete ML training system documentation
- **`../README.md`** - Main Google Ontology module documentation
- **`../EXAMPLES.md`** - API usage examples
- KODE OS Public API PDF: `C:\Users\gpg-mchristian\Documents\Beau\2025_Kode\KODE OS Public API.pdf`

## Key Endpoints

### KODE OS Public API
- `GET /oauth2/v1/.well-known/oauth-authorization-server` - OAuth2 discovery
- `POST /oauth2/v1/token` - Get access token
- `GET /kodeos/api/v1/buildings` - List buildings
- `GET /kodeos/api/v1/buildings/{buildingId}/devices` - List devices
- `GET /kodeos/api/v1/buildings/{buildingId}/points` - List points

### Niagara Google Ontology API
- `POST /googleOntology/v1/learning/correct` - Submit correction
- `POST /googleOntology/v1/learning/train` - Train ML model
- `GET /googleOntology/v1/learning/stats` - Get ML statistics
- `GET /googleOntology/v1/mappings` - View all point mappings with confidence scores
