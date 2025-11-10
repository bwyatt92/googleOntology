# Project Memory: Niagara Google Ontology Module - KODE Labs Integration

**Last Updated:** 2025-11-10
**Status:** ✅ KODE Labs Sync Working!

---

## Project Overview

Building a Niagara 4 module that automatically maps BMS points to Google Digital Buildings Ontology (DBO) using fuzzy matching and machine learning, with **direct integration to KODE Labs Public API** for device synchronization (eliminating need for external Python middleware).

## Current Status: ✅ Working!

### Successfully Completed
1. ✅ **Core Module Functionality**
   - Fuzzy matching of point names to DBO fields
   - ML training system using K-Nearest Neighbors (21 features)
   - HTTP servlet serving REST API at `/googleOntology/v1/`
   - Entity and point indexing from Niagara station
   - Confidence scoring for point mappings
   - RTU entity type recognition added

2. ✅ **KODE Labs Direct Integration**
   - `KodeLabsClient.java` created for direct HTTP communication
   - Configuration properties added to `BGoogleOntologyService`
   - `syncToKodeLabs()` action working
   - Network permissions configured for HTTPS client connections
   - Cloudflare compatibility headers added
   - **Successfully syncing devices to KODE Labs!**

### Last Successful Sync
```
INFO [12:28:24 10-Nov-25 EST][googleOntology] GoogleOntology reindex complete
  [7 ms, 19 entities, 118 points scanned, 55 points matched]
INFO [12:28:29 10-Nov-25 EST][googleOntology] Starting KODE Labs sync...
INFO [12:28:29 10-Nov-25 EST][googleOntology]   Building ID: 6903c1a86c822602dddb80d1
INFO [12:28:29 10-Nov-25 EST][googleOntology]   Datasource ID: 690d074c5e543310004e2c5a
INFO [12:28:29 10-Nov-25 EST][googleOntology]   Min Confidence: 50
INFO [12:28:29 10-Nov-25 EST][googleOntology] Syncing 9 devices with 55 total points...
✅ SUCCESS!
```

---

## Key Files Reference

### Java Source (Niagara Module - Active Development)
```
/mnt/c/Users/gpg-mchristian/Niagara4.14/vykon/googleOntology-rt/
├── src/googleOntology/
│   ├── service/BGoogleOntologyService.java       # Main service with syncToKodeLabs action
│   ├── integration/KodeLabsClient.java           # KODE Labs HTTP client
│   ├── integration/KodeOsAdapter.java            # Legacy adapter (not used anymore)
│   ├── matcher/DboFieldLibrary.java              # Entity type inference (line 92: RTU added)
│   ├── matcher/FuzzyMatcher.java                 # Fuzzy point matching
│   ├── learning/MLOntologyLearner.java           # Machine learning training
│   ├── model/OntologyIndex.java                  # In-memory entity/point index
│   ├── model/GoogleOntologyEntity.java           # Entity data model
│   ├── model/GoogleOntologyPoint.java            # Point data model
│   ├── servlet/BGoogleOntologyServlet.java       # REST API servlet
│   └── util/GoogleOntologyUtil.java              # Helper utilities
├── module-permissions.xml                         # Network permissions (HTTPS client)
├── module-include.xml                             # Type registry
└── niagara-module.xml                             # Module metadata
```

### Source Repository (Version Control)
```
/mnt/c/Users/gpg-mchristian/google-ontology-n4/googleOntology/googleOntology-rt/src/
(Mirror of above - always sync changes to both locations)
```

### Test Scripts
```
/mnt/c/Users/gpg-mchristian/google-ontology-n4/api-test/
├── test_kode_token.sh           # Test KODE Labs API access and token
├── test_entities.sh             # Check entity types in Niagara
├── test_ml_training.sh          # Test ML training endpoints
├── niagara_to_kode.py          # Original Python middleware (deprecated)
├── get_token.py                # Generate KODE Labs access token
└── access_token.txt            # Stored access token (expires hourly)
```

---

## Access Configuration

### Niagara Station
- **URL:** http://localhost (port 80, not 8080)
- **Username:** curl
- **Password:** Admin12345!
- **Environment:** Windows with WSL
- **Station Location:** Running in Niagara 4.15.1.16

### KODE Labs Configuration
- **Building ID:** 6903c1a86c822602dddb80d1
- **Datasource ID:** 690d074c5e543310004e2c5a
- **API Base URL:** https://api.kodelabs.com/kodeos/api/v1
- **Token Lifetime:** 1 hour (regenerate with `get_token.py`)
- **Protection:** Behind Cloudflare (requires proper User-Agent)

### BGoogleOntologyService Properties
```
kodeBaseUrl: "https://api.kodelabs.com/kodeos/api/v1"
kodeAccessToken: "<paste your token here>"
kodeBuildingId: "6903c1a86c822602dddb80d1"
kodeDatasourceId: "690d074c5e543310004e2c5a"
minConfidence: 50
```

---

## How to Use

### 1. Build and Deploy Module
```bash
# In IntelliJ IDEA
# Build → Build Module 'googleOntology-rt'
# Module JAR is automatically placed in Niagara modules folder
```

### 2. Configure KODE Labs Credentials
```bash
# Generate fresh token (expires after 1 hour)
cd /mnt/c/Users/gpg-mchristian/google-ontology-n4/api-test
python3 get_token.py > access_token.txt

# In Niagara Workbench:
# Navigate to: Services/GoogleOntologyService
# Set properties:
#   - kodeAccessToken: <paste token from access_token.txt>
#   - kodeBuildingId: 6903c1a86c822602dddb80d1
#   - kodeDatasourceId: 690d074c5e543310004e2c5a
```

### 3. Rebuild Index and Sync
```
# In Niagara Workbench:
# Right-click GoogleOntologyService
# Actions → rebuildIndex()      # Scan station and match points
# Actions → syncToKodeLabs()    # Push devices to KODE Labs
```

### 4. Test from Command Line
```bash
# Test Niagara API
curl http://localhost/googleOntology/v1/entities -u curl:Admin12345! | python3 -m json.tool

# Get specific entity fields
curl http://localhost/googleOntology/v1/entity/{ENTITY_ID}/fields -u curl:Admin12345! | python3 -m json.tool

# Check entity types
cd /mnt/c/Users/gpg-mchristian/google-ontology-n4/api-test
./test_entities.sh

# Test KODE Labs token
export ACCESS_TOKEN=$(cat access_token.txt)
./test_kode_token.sh
```

---

## Key Implementation Details

### Entity Type Inference
**File:** `DboFieldLibrary.java` (lines 169-179)
**Method:** `inferEntityType(String name)`
**Logic:** Case-insensitive keyword matching in entity name
```java
// Supported entity types (line 91-102):
entityTypes.put("ahu", "AHU");
entityTypes.put("rtu", "RTU");        // ✅ Added for RTU support
entityTypes.put("vav", "VAV");
entityTypes.put("fcu", "FCU");
entityTypes.put("chws", "CHWS");
entityTypes.put("hws", "HWS");
entityTypes.put("boiler", "BLR");
entityTypes.put("chiller", "CH");
entityTypes.put("cooling", "CT");
entityTypes.put("tower", "CT");
entityTypes.put("fan", "FAN");
entityTypes.put("pump", "PMP");
// Default: "EQUIPMENT"
```

### KODE Labs Sync Logic
**File:** `KodeLabsClient.java`
**Key Method:** `syncDevices(OntologyIndex index)`
**Behavior:**
- Skips entities with type "EQUIPMENT" (generic/uncategorized)
- Only includes points with `confidence >= minConfidence` (default 50)
- Transforms to KODE Labs format:
  - Device ID: `niagara_{entityId}`
  - Point ID: `{entityId}_{pointAddr}`
  - Kind: Inferred from DBO field name (Bool, Str, Number)
  - Units: Preserved if present
- Uses batch endpoint for efficiency: `/devices/batch`

### Network Permissions
**File:** `module-permissions.xml`
```xml
<req-permission>
  <name>NETWORK_COMMUNICATION</name>
  <purposeKey>HTTP client to sync devices to KODE Labs API</purposeKey>
  <parameters>
    <parameter name="hosts" value="api.kodelabs.com"/>
    <parameter name="ports" value="443"/>
    <parameter name="type" value="client"/>
  </parameters>
</req-permission>
```

### HTTP Headers for Cloudflare Compatibility
**File:** `KodeLabsClient.java` (lines 175-181)
```java
conn.setRequestProperty("Authorization", "Bearer " + accessToken);
conn.setRequestProperty("Content-Type", "application/json");
conn.setRequestProperty("Accept", "application/json");
conn.setRequestProperty("User-Agent", "Niagara-GoogleOntology/1.0");  // Required for Cloudflare
conn.setConnectTimeout(30000);  // 30 second timeout
conn.setReadTimeout(30000);
```

---

## Machine Learning Training (Optional Enhancement)

### ML Training Endpoints
- `POST /v1/learning/correct` - Submit point name corrections
- `POST /v1/learning/train` - Train KNN model (requires ≥5 examples)
- `GET /v1/learning/stats` - Get training statistics

### How ML Improves Matching
1. Fuzzy matcher provides baseline confidence
2. If ML model is trained, it predicts DBO field with confidence
3. If ML confidence > fuzzy confidence, ML prediction wins
4. See: `BGoogleOntologyService.java` lines 246-259

### Training Example
```bash
# Add correction examples
curl -u curl:Admin12345! -X POST http://localhost/googleOntology/v1/learning/correct \
  -H "Content-Type: application/json" \
  -d '{
    "pointName": "RTU1 Discharge Air Temp",
    "equipmentType": "RTU",
    "correctMatch": "discharge_air_temperature_sensor"
  }'

# After 5+ examples, train the model
curl -u curl:Admin12345! -X POST http://localhost/googleOntology/v1/learning/train

# Check stats
curl -u curl:Admin12345! http://localhost/googleOntology/v1/learning/stats
```

---

## Troubleshooting

### Issue: HTTP 403 Error (Cloudflare Error 1010)
**Solution:** Token expired or missing User-Agent header
```bash
# Regenerate token
python3 get_token.py > access_token.txt
# Update kodeAccessToken property in service
# Rebuild module if User-Agent header was missing
```

### Issue: SecurityException - Socket Permission Denied
**Solution:** Module permissions not configured
- Check `module-permissions.xml` has NETWORK_COMMUNICATION permission
- Rebuild module
- Restart station (may prompt for permission approval)

### Issue: RTUs showing as "EQUIPMENT" type
**Solution:** Rebuild module with RTU entity type
- Verify `DboFieldLibrary.java` line 92 has: `entityTypes.put("rtu", "RTU");`
- Rebuild module
- Invoke `rebuildIndex()` action

### Issue: No devices synced (0 devices)
**Solution:** Check confidence threshold
- Lower `minConfidence` property (default 50)
- Check `/v1/mappings` endpoint to see actual confidence scores
- Verify entities are not all type "EQUIPMENT" (which are skipped)

### Issue: Station API not accessible
**Solution:** Verify station is running and port
- Check if station daemon is running (not just Workbench)
- Verify port (80 vs 8080)
- Test: `curl http://localhost/googleOntology/v1/about -u curl:Admin12345!`

---

## Development Workflow

### Making Changes
1. **Edit source in Niagara workspace:**
   ```
   /mnt/c/Users/gpg-mchristian/Niagara4.14/vykon/googleOntology-rt/src/
   ```

2. **Mirror changes to version control:**
   ```
   /mnt/c/Users/gpg-mchristian/google-ontology-n4/googleOntology/googleOntology-rt/src/
   ```

3. **Build in IntelliJ:**
   - Build → Build Module 'googleOntology-rt'

4. **Test in station:**
   - Restart station (if needed)
   - Approve any new permissions
   - Test via Workbench or API

### Important Files to Keep in Sync
- Always update BOTH locations (Niagara4.14/vykon AND google-ontology-n4)
- Key files: BGoogleOntologyService.java, KodeLabsClient.java, DboFieldLibrary.java, module-permissions.xml

---

## Known Limitations

1. **Ontology Assignment:** KODE Labs Public API doesn't support setting canonical types programmatically yet
   - Devices/points are created but ontology must be assigned via KODE OS UI
   - Ready for future API support (DBO field → KODE canonical type mapping exists)

2. **Entity Type Classification:** Based on simple keyword matching, not ML
   - ML training improves point matching only
   - Entity types determined by name pattern (e.g., "RTU1" → RTU)

3. **Token Expiration:** Access tokens expire after 1 hour
   - Must regenerate and update `kodeAccessToken` property
   - No automatic refresh implemented

4. **No Data Sync:** Only device structure is synced, not point values
   - This is a metadata/ontology sync only
   - Actual data integration would require separate mechanism

---

## Success Metrics

- ✅ 19 entities discovered
- ✅ 118 points scanned
- ✅ 55 points matched (confidence ≥ 50)
- ✅ 9 devices synced to KODE Labs
- ✅ 55 points created in KODE Labs
- ✅ Direct integration (no Python middleware needed)
- ✅ RTU entity type recognition working

---

## Future Enhancements

1. **Automatic Token Refresh:** Implement OAuth refresh token flow
2. **Ontology Tagging:** Add support when KODE Labs API provides endpoint
3. **ML Entity Classification:** Train model for entity type prediction
4. **Data Streaming:** Sync live point values (not just metadata)
5. **Batch Operations:** Update existing devices instead of create-only
6. **Error Recovery:** Retry logic for failed syncs
7. **Sync Scheduling:** Automatic periodic sync

---

## Contact & Resources

- **KODE Labs API Docs:** See `KODE OS Public API.pdf` in api-test folder
- **Google DBO:** Digital Buildings Ontology specification
- **Niagara Framework:** Tridium Niagara 4.x documentation
- **ML Library:** Smile (Statistical Machine Intelligence and Learning Engine)

---

**Last Successful Sync:** 2025-11-10 12:28:29 EST
**Build Version:** 1.0.0
**Status:** ✅ Production Ready
