# Push Niagara Devices to KODE OS - Demo

## Overview

This demonstration shows how to **automatically replicate devices from Niagara to KODE OS** using the KODE OS Public API.

## What It Does

```
Niagara Station          →    Mapper     →    KODE OS
(Google Ontology)             (Transform)     (Auto-Create)

AHU_01                                        AHU_01 (ahu)
├─ supply_fan_run_command  →  Supply_Fan_Cmd
├─ supply_air_temperature  →  Supply_Air_Temp
└─ heating_valve_percentage → Heating_Valve_Cmd
```

## Files Created

1. **`niagara_sample_devices.json`** - Sample Niagara device structure (4 devices with Google ontology points)
2. **`google_to_kode_mapper.py`** - Mapper that converts Google ontology → KODE OS format
3. **`push_to_kode.py`** - Main script that pushes devices to KODE OS

## Prerequisites

1. ✅ KODE OS API access (configured in `config.json`)
2. ✅ A building in KODE OS
3. ⚠️  **A datasource in KODE OS** (needs to be created)

## Step 1: Create a Datasource in KODE OS

Before running the demo, you need a datasource in KODE OS:

1. Log in to your KODE OS instance (https://dsrus.kodelabs.com)
2. Go to your building
3. Create a new **Integration/Datasource**
   - Name: `NiagaraSync` (or any name)
   - Type: **API** or **KODE API**
4. Copy the datasource ID

## Step 2: Run the Demo

```bash
python3 push_to_kode.py
```

### What the Script Does:

1. **Loads Niagara devices** from `niagara_sample_devices.json`
2. **Shows mapping preview** - How Google ontology names map to KODE OS
3. **Asks for confirmation**
4. **Authenticates** with KODE OS
5. **Selects building**
6. **Creates devices** using the API
7. **Verifies** devices were created

### Expected Output:

```
================================================================================
Push Niagara Devices to KODE OS
================================================================================

📋 Loading configuration...
   KODE OS: https://dsrus.kodelabs.com

📦 Loading Niagara devices...
   Found 4 devices
      • AHU_01 (AHU_SFSS) - 8 points
      • VAV_101 (VAV_SD_DSP) - 6 points
      • VAV_102 (VAV_RH_DSP) - 6 points
      • FCU_201 (FCU_DFSS_DFVSC) - 7 points

================================================================================
MAPPING PREVIEW
================================================================================

Device: AHU_01
Google Type: AHU_SFSS
KODE Type: ahu
================================================================================

Point Mappings:
--------------------------------------------------------------------------------
  supply_fan_run_command                    → Supply_Fan_Cmd             (Bool, Write)
  supply_fan_run_status                     → Supply_Fan_Status          (Bool, Read)
  supply_air_temperature_sensor             → Supply_Air_Temp            (Number, Read)
  supply_air_temperature_setpoint           → Supply_Air_Temp_Sp         (Number, Write)
  heating_valve_percentage_command          → Heating_Valve_Cmd          (Number, Write)
  cooling_valve_percentage_command          → Cooling_Valve_Cmd          (Number, Write)
  return_air_temperature_sensor             → Return_Air_Temp            (Number, Read)
  mixed_air_temperature_sensor              → Mixed_Air_Temp             (Number, Read)

[... similar for VAV_101, VAV_102, FCU_201 ...]

================================================================================
Do you want to push these devices to KODE OS? (yes/no): yes

🔐 Authenticating with KODE OS...
   ✅ Authenticated!

🏢 Fetching buildings...
   Found 2 building(s):
      1. Office (ID: 690495f73e4a4f66f2bf195d)
      2. Warehouse (ID: 6903c1a86c822602dddb80d1)

   Using: Office

📦 Looking for datasource 'NiagaraSync'...
Enter datasource ID: 690cf2a7a117992939ae8d99

🔨 Creating devices in KODE OS...
   Building: 690495f73e4a4f66f2bf195d
   Datasource: 690cf2a7a117992939ae8d99
   Devices to create: 4

   📍 Creating device: AHU_01
      ✅ Created successfully!
         Points: 8

   📍 Creating device: VAV_101
      ✅ Created successfully!
         Points: 6

   📍 Creating device: VAV_102
      ✅ Created successfully!
         Points: 6

   📍 Creating device: FCU_201
      ✅ Created successfully!
         Points: 7

================================================================================
Results: 4 successful, 0 errors
================================================================================

🎉 All devices created successfully!

💡 You can now view them in KODE OS:
   https://dsrus.kodelabs.com

🔍 Verifying devices...
   Total devices in building: 97
   Found our devices: 4
      ✅ AHU_01 (ID: 690cf...)
      ✅ VAV_101 (ID: 690cf...)
      ✅ VAV_102 (ID: 690cf...)
      ✅ FCU_201 (ID: 690cf...)

================================================================================
✅ DEMONSTRATION COMPLETE!
================================================================================

What we just did:
  1. ✅ Loaded Niagara device structure (Google ontology format)
  2. ✅ Mapped Google field names → KODE OS point names
  3. ✅ Authenticated with KODE OS API
  4. ✅ Created devices and points in KODE OS
  5. ✅ Verified devices were created

This same process can be integrated into Niagara!
```

## Sample Devices Included

### AHU_01 (Air Handling Unit)
- Supply fan control (command & status)
- Supply air temperature (sensor & setpoint)
- Heating/cooling valves
- Return & mixed air temps

### VAV_101 (Variable Air Volume)
- Zone temperature (sensor & setpoint)
- Damper control
- Airflow (sensor & setpoint)
- Occupancy mode

### VAV_102 (VAV with Reheat)
- Zone temperature control
- Damper control
- Reheat valve
- Discharge air temp

### FCU_201 (Fan Coil Unit)
- Zone temperature control
- Fan control (command & status)
- Heating/cooling valves
- Separate heating/cooling setpoints

## Mapping Rules

The `GoogleToKodeMapper` class converts Google ontology names to KODE OS format:

| Google Ontology | KODE OS | Rule |
|----------------|---------|------|
| `supply_fan_run_command` | `Supply_Fan_Cmd` | Pattern match |
| `zone_air_temperature_sensor` | `Zone_Temp` | Pattern match |
| `heating_valve_percentage_command` | `Heating_Valve_Cmd` | Pattern match |
| Any other field | Title_Case | Fallback |

Device types also map:
- `AHU_SFSS` → `ahu`
- `VAV_SD_DSP` → `vav`
- `FCU_DFSS_DFVSC` → `fcu`

## Customizing the Sample Data

Edit `niagara_sample_devices.json` to match your actual Niagara devices:

```json
{
  "building": {
    "name": "YourBuilding",
    "id": "your-id"
  },
  "devices": [
    {
      "id": "your-device-id",
      "name": "Your_Device_Name",
      "type": "AHU_SFSS",
      "points": [
        {
          "name": "supply_fan_run_command",
          "value": false,
          "writable": true,
          "kind": "Bool"
        }
      ]
    }
  ]
}
```

## Next Steps

### Phase 1: Python Prototype ✅ COMPLETE
- [x] API connectivity
- [x] Google → KODE mapper
- [x] Device creation demo

### Phase 2: Java Integration (Next)
1. Create `BKodeApiService` in Niagara
2. Read devices from local Niagara station
3. Use BGoogleOntologyService for mappings
4. Push to KODE OS using API
5. Add scheduling (hourly, daily, manual)

### Phase 3: Production Features
1. Incremental sync (only changed devices)
2. Conflict resolution
3. Timeseries data sync
4. UI for configuration
5. Error handling & retry logic

## Troubleshooting

### "No datasource found"
- Create a datasource in KODE OS first
- Provide the datasource ID when prompted

### "401 Unauthorized"
- Check your `config.json` credentials
- Verify key_id is correct

### "Device already exists"
- The API will update existing devices
- Check the KODE OS UI to verify

### Rate Limits
- API limits: 5/sec, 100/min, 500/hour
- Script creates devices sequentially to avoid limits

## Benefits of This Approach

✅ **Automates manual work** - No need to manually create devices in KODE OS
✅ **Maintains consistency** - Google ontology names are preserved
✅ **Reduces errors** - Automated mapping eliminates typos
✅ **Scales easily** - Can sync hundreds of devices
✅ **Demonstrates integration** - Shows what the Niagara module will do

## Questions?

This is a proof of concept demonstrating automated device replication from Niagara to KODE OS. The same logic will be implemented in Java for the Niagara module!
