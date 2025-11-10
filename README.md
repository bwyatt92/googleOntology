# Google Ontology N4

A Niagara 4 module that automatically crawls your station and fuzzy-matches points to the Google Digital Buildings Ontology (DBO). Exposes matched points via a REST API with full facet information (units, enums, ranges).

## Features

- **Automatic Station Crawling** - Scans all points in the station
- **Fuzzy Matching** - Intelligently maps point names to DBO fields using:
  - Token-based matching (camelCase, snake_case, spaces, etc.)
  - Units validation (°F, CFM, PSI, etc.)
  - Semantic analysis (descriptors, measurements, point types)
  - Configurable confidence threshold
- **Facet Extraction** - Captures and exposes:
  - Units
  - Enum/Boolean mappings
  - Numeric ranges (min/max)
  - Precision
- **HTTP REST API** - Jasper-inspired JSON endpoints for easy integration
- **DBO Entity Types** - Automatically infers entity types (AHU, VAV, FCU, etc.)

## Architecture

### Core Components

1. **BGoogleOntologyService** - Main service that:
   - Crawls the station on startup/rebuild
   - Performs fuzzy matching to DBO fields
   - Builds an index of entities and their mapped points
   - Configurable minimum confidence threshold

2. **BGoogleOntologyServlet** - HTTP servlet providing REST API

3. **FuzzyMatcher** - Intelligent DBO field matching:
   - Tokenizes point names
   - Matches to DBO subfields (descriptor, component, measurement, pointtype)
   - Validates units against measurement types
   - Calculates confidence scores (0-100%)

4. **DboFieldLibrary** - Embedded DBO ontology knowledge:
   - Point types (sensor, setpoint, status, command, etc.)
   - Measurements (temperature, pressure, flowrate, etc.)
   - Descriptors (discharge, supply, return, zone, etc.)
   - Components (air, water, fan, damper, valve, etc.)
   - Entity type inference

## Installation

### Prerequisites

- Niagara 4.11 or newer
- Java Development Kit (JDK) 8 or 11 compatible with your Niagara version
- (Optional) Gradle 7.0+ for command-line building

### Build Methods

You can build this module using either **Gradle** (recommended) or **Niagara Workbench**.

#### Method 1: Build with Gradle (Recommended)

This module uses **Tridium's official Niagara Gradle plugins** (same as Tridium's own modules).

1. **Configure Niagara Path:**

   Edit `googleOntology/gradle.properties`:
   ```properties
   # Windows (use forward slashes):
   niagara_home=C:/Users/YOUR_USERNAME/Niagara4.14

   # Linux/macOS:
   # niagara_home=/opt/Niagara-4.14
   ```

2. **Build from root directory:**
   ```bash
   cd googleOntology
   ./gradlew build  # Linux/macOS
   # or
   gradlew.bat build  # Windows
   ```

3. **Install module:**
   ```bash
   # Copy JAR to station
   cp googleOntology-rt/build/module-jars/googleOntology-rt.jar \
      ~/.niagara/modules/googleOntology/
   ```

4. **Restart station** to load the new module

See **[GRADLE_BUILD_GUIDE.md](googleOntology/GRADLE_BUILD_GUIDE.md)** for detailed instructions.

#### Method 2: Build with Niagara Workbench

1. **Copy module to Niagara modules directory:**
   ```bash
   cp -r googleOntology <NIAGARA_HOME>/modules/
   ```

2. **Build using Niagara Workbench:**
   - Open Niagara Workbench
   - Go to **Tools > Module Manager**
   - Find `googleOntology` in the list
   - Click **Build Module**

3. **Restart station** to load the new module

### Setup

1. **Add service to station:**
   - In Workbench, navigate to your station
   - Go to **Services** folder
   - Right-click > **New > googleOntology > GoogleOntologyService**
   - Name it (e.g., "GoogleOntologyService")

2. **Configure service properties:**
   - `minConfidence` - Minimum confidence score to include a mapping (default: 50)
   - The servlet is automatically configured

3. **Rebuild index:**
   - Right-click the service > **Actions > rebuildIndex**
   - Check the station logs for progress

4. **Enable HTTP access:**
   - Go to **Services > WebService > Servlets**
   - The GoogleOntologyServlet should automatically appear

## API Documentation

All endpoints support both GET and POST requests.

### Base URL

```
http://<station-host>/googleOntology/v1/
```

### Authentication

Use HTTP Basic Authentication with your Niagara station credentials.

### Endpoints

#### 1. About - Get module and station info

```bash
GET /googleOntology/v1/about
```

**Response:**
```json
{
  "name": "MyStation",
  "vendor": "Tridium",
  "model": "Niagara 4",
  "version": "4.11.0.156",
  "moduleName": "googleOntology",
  "moduleVersion": "1.0.0",
  "ontologyType": "Google Digital Buildings",
  "numEntities": 15
}
```

#### 2. Entities - List all mapped entities

```bash
GET /googleOntology/v1/entities
```

**Response:**
```json
{
  "entities": [
    {
      "id": "54d",
      "name": "AHU-1",
      "path": "/Drivers/BACnet/AHU-1/points",
      "entityType": "AHU",
      "numPoints": 25
    },
    {
      "id": "620",
      "name": "VAV-101",
      "path": "/Drivers/BACnet/VAV-101/points",
      "entityType": "VAV",
      "numPoints": 8
    }
  ]
}
```

#### 3. Entity Fields - Get DBO-mapped fields for an entity

```bash
GET /googleOntology/v1/entity/{entityId}/fields
```

**Response:**
```json
{
  "entityId": "620",
  "entityName": "VAV-101",
  "entityType": "VAV",
  "fields": [
    {
      "dboField": "discharge_air_temperature_sensor",
      "originalName": "Discharge Air Temp",
      "addr": "DischargeTemp",
      "confidence": 95,
      "units": "°F",
      "facets": {
        "min": -40.0,
        "max": 250.0,
        "precision": 1
      }
    },
    {
      "dboField": "zone_air_temperature_sensor",
      "originalName": "Zone Temp",
      "addr": "ZoneTemp",
      "confidence": 92,
      "units": "°F",
      "facets": {
        "min": 0.0,
        "max": 150.0,
        "precision": 1
      }
    },
    {
      "dboField": "fan_run_status",
      "originalName": "Fan Status",
      "addr": "FanStatus",
      "confidence": 88,
      "enumMapping": "false=Off,true=On"
    }
  ]
}
```

#### 4. Entity Values - Get current values with DBO field names

```bash
GET /googleOntology/v1/entity/{entityId}/values
```

**Response:**
```json
{
  "entityId": "620",
  "entityName": "VAV-101",
  "entityType": "VAV",
  "values": [
    {
      "dboField": "discharge_air_temperature_sensor",
      "addr": "DischargeTemp",
      "val": 72.5,
      "status": "ok",
      "units": "°F"
    },
    {
      "dboField": "zone_air_temperature_sensor",
      "addr": "ZoneTemp",
      "val": 71.0,
      "status": "ok",
      "units": "°F"
    },
    {
      "dboField": "fan_run_status",
      "addr": "FanStatus",
      "val": 1,
      "status": "ok"
    }
  ]
}
```

#### 5. Mappings - Get all fuzzy match mappings with confidence scores

```bash
GET /googleOntology/v1/mappings
```

**Response:**
```json
{
  "mappings": [
    {
      "entityId": "620",
      "entityName": "VAV-101",
      "originalName": "Discharge Air Temp",
      "dboField": "discharge_air_temperature_sensor",
      "confidence": 95
    },
    {
      "entityId": "620",
      "entityName": "VAV-101",
      "originalName": "Zone Temp",
      "dboField": "zone_air_temperature_sensor",
      "confidence": 92
    }
  ]
}
```

## Fuzzy Matching Examples

### Example 1: Temperature Sensor

**Input:** "VAV-1-DischargeTempSensor"
- Tokens: ["VAV", "Discharge", "Temp", "Sensor"]
- Matched descriptors: `discharge`
- Matched measurement: `temperature` (from "Temp")
- Matched point type: `sensor`
- **Output:** `discharge_air_temperature_sensor`
- **Confidence:** ~90%

### Example 2: Setpoint with Units

**Input:** "Supply Air Temp SP" (Units: "°F")
- Tokens: ["Supply", "Air", "Temp", "SP"]
- Matched descriptors: `supply`
- Matched component: `air`
- Matched measurement: `temperature` (inferred from units)
- Matched point type: `setpoint` (from "SP")
- **Output:** `supply_air_temperature_setpoint`
- **Confidence:** ~95% (boosted by units match)

### Example 3: Boolean Status

**Input:** "Fan Status" (BooleanPoint)
- Tokens: ["Fan", "Status"]
- Matched component: `fan`
- Matched point type: `status` (inferred from boolean + "Status")
- **Output:** `fan_run_status`
- **Confidence:** ~85%

## Configuration

### Minimum Confidence Threshold

The `minConfidence` property (default: 50) controls which matches are included:

- **50-70** - Include more matches, may have some incorrect mappings
- **70-85** - Balanced approach (recommended)
- **85-100** - Only high-confidence matches, may miss some points

Adjust based on your naming conventions and tolerance for false positives.

### Rebuilding the Index

The index is automatically built when the service starts. To manually rebuild:

1. In Workbench, right-click the GoogleOntologyService
2. Select **Actions > rebuildIndex**
3. Monitor station logs for progress

Rebuild when:
- Adding/removing points
- Changing point names
- Adjusting minConfidence threshold

## Troubleshooting

### No entities appear

1. Check that points exist under equipment/devices in your station
2. Verify minConfidence isn't set too high
3. Check station logs for errors during reindex
4. Ensure points are BNumericPoint, BBooleanPoint, or BEnumPoint types

### Low confidence scores

- Point names don't match DBO conventions
- Try adjusting point display names to include DBO keywords
- Check the `/v1/mappings` endpoint to see actual confidence scores

### HTTP endpoints not accessible

1. Verify WebService is running
2. Check that servlet is registered in Services > WebService > Servlets
3. Ensure proper HTTP authentication
4. Check firewall/network settings

## Development

### Project Structure

```
googleOntology/
└── googleOntology-rt/
    ├── src/
    │   └── googleOntology/
    │       ├── matcher/          # Fuzzy matching logic
    │       │   ├── DboFieldLibrary.java
    │       │   └── FuzzyMatcher.java
    │       ├── model/            # Data models
    │       │   ├── GoogleOntologyEntity.java
    │       │   ├── GoogleOntologyPoint.java
    │       │   └── OntologyIndex.java
    │       ├── service/          # Main service
    │       │   └── BGoogleOntologyService.java
    │       ├── servlet/          # HTTP API
    │       │   └── BGoogleOntologyServlet.java
    │       └── util/             # Utilities
    │           ├── GoogleOntologyUtil.java
    │           └── JsonWriter.java
    ├── build.xml
    ├── module-include.xml
    ├── module.palette
    └── module.properties
```

### Extending the Fuzzy Matcher

To improve matching for your specific naming conventions:

1. Edit `DboFieldLibrary.java` to add domain-specific keywords
2. Modify `FuzzyMatcher.java` to add custom matching rules
3. Rebuild the module

### Adding New DBO Fields

Update `DboFieldLibrary.java`:
- Add new point types, measurements, descriptors, or components to their respective sets
- Update `inferEntityType()` to recognize new entity patterns

## Google Digital Buildings Ontology

This module implements fuzzy matching to the [Google Digital Buildings Ontology](https://github.com/google/digitalbuildings).

### DBO Field Structure

Fields follow this pattern:
```
(aggregation_)?(descriptor_)*(component_)?(measurement_)?<pointtype>
```

Examples:
- `discharge_air_temperature_sensor`
- `supply_water_flowrate_setpoint`
- `fan_run_status`
- `zone_air_temperature_setpoint`

### Supported Entity Types

- **AHU** - Air Handling Unit
- **VAV** - Variable Air Volume
- **FCU** - Fan Coil Unit
- **CHWS** - Chilled Water System
- **HWS** - Hot Water System
- **BLR** - Boiler
- **CH** - Chiller
- **CT** - Cooling Tower
- **FAN** - Fan
- **PMP** - Pump

## License

MIT License

## Credits

- Inspired by [Jasper N4](https://github.com/novant-io/jasper-n4) for HTTP server architecture
- Based on [Google Digital Buildings Ontology](https://github.com/google/digitalbuildings)

## Support

For issues and questions:
- Check station logs for detailed error messages
- Verify point naming conventions match DBO patterns
- Use `/v1/mappings` endpoint to debug confidence scores
# googleOntology
