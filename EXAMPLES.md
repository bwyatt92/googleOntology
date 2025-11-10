# API Examples

Example curl commands for testing the Google Ontology N4 API.

Replace:
- `<station-host>` with your station's hostname/IP
- `username:password` with your Niagara credentials

## Get Module Info

```bash
curl http://<station-host>/googleOntology/v1/about \
  -u username:password
```

## List All Entities

```bash
curl http://<station-host>/googleOntology/v1/entities \
  -u username:password
```

## Get Entity Fields (DBO Mappings)

```bash
# Replace {entityId} with an actual entity ID from /entities response
curl http://<station-host>/googleOntology/v1/entity/{entityId}/fields \
  -u username:password
```

Example:
```bash
curl http://<station-host>/googleOntology/v1/entity/54d/fields \
  -u username:password
```

## Get Current Values

```bash
curl http://<station-host>/googleOntology/v1/entity/{entityId}/values \
  -u username:password
```

Example:
```bash
curl http://<station-host>/googleOntology/v1/entity/54d/values \
  -u username:password
```

## Get All Mappings with Confidence Scores

```bash
curl http://<station-host>/googleOntology/v1/mappings \
  -u username:password
```

## Pretty Print JSON (with jq)

If you have `jq` installed, pipe the output for better formatting:

```bash
curl http://<station-host>/googleOntology/v1/entities \
  -u username:password | jq .
```

## Example Response - Entity with Fields

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
      "dboField": "zone_air_temperature_setpoint",
      "originalName": "Zone Temp SP",
      "addr": "ZoneTempSP",
      "confidence": 90,
      "units": "°F",
      "facets": {
        "min": 55.0,
        "max": 85.0,
        "precision": 1
      }
    },
    {
      "dboField": "damper_position_command",
      "originalName": "Damper Cmd",
      "addr": "DamperCmd",
      "confidence": 85,
      "units": "%",
      "facets": {
        "min": 0.0,
        "max": 100.0,
        "precision": 0
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

## Example Response - Current Values

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
      "dboField": "zone_air_temperature_setpoint",
      "addr": "ZoneTempSP",
      "val": 72.0,
      "status": "ok",
      "units": "°F"
    },
    {
      "dboField": "damper_position_command",
      "addr": "DamperCmd",
      "val": 45.0,
      "status": "ok",
      "units": "%"
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

## Integration Examples

### Python

```python
import requests
from requests.auth import HTTPBasicAuth

station_host = "192.168.1.100"
username = "admin"
password = "password"

# Get all entities
response = requests.get(
    f"http://{station_host}/googleOntology/v1/entities",
    auth=HTTPBasicAuth(username, password)
)

entities = response.json()["entities"]

# Get fields for first entity
if entities:
    entity_id = entities[0]["id"]
    response = requests.get(
        f"http://{station_host}/googleOntology/v1/entity/{entity_id}/fields",
        auth=HTTPBasicAuth(username, password)
    )
    fields = response.json()["fields"]

    for field in fields:
        print(f"{field['dboField']} <- {field['originalName']} ({field['confidence']}%)")
```

### JavaScript (Node.js)

```javascript
const axios = require('axios');

const stationHost = '192.168.1.100';
const auth = {
  username: 'admin',
  password: 'password'
};

async function getEntities() {
  const response = await axios.get(
    `http://${stationHost}/googleOntology/v1/entities`,
    { auth }
  );
  return response.data.entities;
}

async function getEntityValues(entityId) {
  const response = await axios.get(
    `http://${stationHost}/googleOntology/v1/entity/${entityId}/values`,
    { auth }
  );
  return response.data.values;
}

// Usage
(async () => {
  const entities = await getEntities();
  console.log(`Found ${entities.length} entities`);

  if (entities.length > 0) {
    const values = await getEntityValues(entities[0].id);
    console.log('Current values:', values);
  }
})();
```

## Filtering and Processing

### Find High-Confidence Matches Only

```bash
# Get all mappings and filter for confidence >= 90
curl http://<station-host>/googleOntology/v1/mappings \
  -u username:password | \
  jq '.mappings[] | select(.confidence >= 90)'
```

### List All Temperature Sensors

```bash
curl http://<station-host>/googleOntology/v1/mappings \
  -u username:password | \
  jq '.mappings[] | select(.dboField | contains("temperature_sensor"))'
```

### Count Points by Entity

```bash
curl http://<station-host>/googleOntology/v1/entities \
  -u username:password | \
  jq '.entities[] | {name: .name, points: .numPoints}'
```
