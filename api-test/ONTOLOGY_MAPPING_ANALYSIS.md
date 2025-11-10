# KODE OS to Google Ontology Mapping Analysis

## Overview

This document analyzes the ontology structure found in your KODE OS buildings and proposes mappings to Google's Digital Buildings Ontology.

## Current KODE OS Structure

### Buildings
- **Office** - 93 devices, 200+ points
- **Warehouse** - 93 devices, 200+ points

### Device Types Found

| KODE Canonical Type | Count | Description |
|---------------------|-------|-------------|
| `vav` | 50 | Variable Air Volume units |
| `fcu` | 40 | Fan Coil Units |
| `ahu` | 5 | Air Handling Units |
| `loopSystem` | 1 | Control loop system |
| `N/A` | 90 | Devices without canonical type |

### Point Structure

**Points lack detailed ontology metadata:**
- Most points show `ontology: { type: "N/A", canonicalType: "N/A" }`
- Point identification relies on naming conventions
- Points are categorized by `kind`: Number (342) or Bool (59)

## Point Naming Patterns Observed

### AHU (Air Handling Unit) Points

**Supply Fan Points:**
- `S_SFan` (Bool) - Supply fan status/command
- `Discharge Fan Status` (Bool)
- `Discharge Fan Cmd` (Bool)

**Temperature Points:**
- `SP_MWU_SAT` (Number) - Setpoint for supply air temperature
- `SP_HtgSAT` (Number) - Heating setpoint
- `T_HtgSAT` (Number) - Temperature reading
- `Eff Heating Temp Setpt` (Number)

**Valve/Damper Commands:**
- `HtgVlv_out` (Number) - Heating valve output
- `Heat PID` (Number) - PID controller output

**Mode/Enable:**
- `MWU` (Bool) - Make-up mode
- `Enable` (Bool)
- `Device Mode` (Bool)
- `Occ_Mode` (Bool) - Occupancy mode

### VAV (Variable Air Volume) Points

**Temperature Control:**
- Temperature setpoints (Number)
- Zone temperature (Number)

**Airflow:**
- Damper position (Number)
- Airflow readings (Number)

**Control Logic:**
- `NumericSwitch`, `NumericSwitch1` - Logic components
- `Ramp`, `Ramp1` - Ramping functions
- `GreaterThan`, `LessThan` - Comparison logic
- `Minimum`, `Maximum` - Min/max functions
- `Add`, `Subtract`, `Divide` - Math operations

**Commands:**
- `Electric Heat Stage Cmd` (Bool)
- `Occupancy Cmd` (Bool)

### FCU (Fan Coil Unit) Points

**Fan Control:**
- `Discharge Fan Status` (Bool)
- `Discharge Fan Cmd` (Bool)

**Temperature:**
- Cooling/heating PIDs
- Temperature setpoints

**Logic:**
- Similar logic components as VAV (NumericSwitch, Ramp, etc.)

## Google Digital Buildings Ontology

### Standard Types

Google ontology uses standardized type definitions:

**For VAV:**
```
VAV = [
  "zone_air_temperature_sensor",
  "supply_air_damper_percentage_command",
  "heating_request_count",
  "cooling_request_count",
  etc.
]
```

**For AHU:**
```
AHU = [
  "supply_fan_run_command",
  "supply_fan_run_status",
  "supply_air_temperature_sensor",
  "supply_air_temperature_setpoint",
  "heating_valve_percentage_command",
  etc.
]
```

### Field Naming Convention

Google uses underscored, descriptive names:
- `zone_air_temperature_sensor`
- `supply_air_damper_percentage_command`
- `discharge_fan_run_status`

## Proposed Mapping Strategy

### Phase 1: Device Type Mapping

| KODE Type | Google Type | Notes |
|-----------|-------------|-------|
| `ahu` | `AHU_*` | Map to appropriate AHU type (SFSS, SFVSC, etc.) |
| `vav` | `VAV_*` | Map to VAV type based on features (reheat, cooling, etc.) |
| `fcu` | `FCU_*` | Map to FCU type |
| `loopSystem` | Custom or `CTRL` | May need custom type |

### Phase 2: Point Mapping Rules

#### Pattern-Based Mapping

Create rules based on point name patterns:

**Temperature Sensors:**
- If name contains `_SAT` or `Supply.*Temp` → `supply_air_temperature_sensor`
- If name contains `Zone.*Temp` → `zone_air_temperature_sensor`
- If name contains `Return.*Temp` → `return_air_temperature_sensor`

**Temperature Setpoints:**
- If name contains `SP_.*SAT` or `Setpoint.*Supply` → `supply_air_temperature_setpoint`
- If name contains `Htg.*Setpt` → `heating_air_temperature_setpoint`
- If name contains `Clg.*Setpt` → `cooling_air_temperature_setpoint`

**Fan Commands/Status:**
- If name contains `S_SFan` or `Supply.*Fan.*Cmd` → `supply_fan_run_command`
- If name contains `Supply.*Fan.*Status` → `supply_fan_run_status`
- If name contains `Discharge.*Fan.*Cmd` → `discharge_fan_run_command`

**Valve/Damper Commands:**
- If name contains `HtgVlv` → `heating_valve_percentage_command`
- If name contains `ClgVlv` → `cooling_valve_percentage_command`
- If name contains `Damper` → `*_damper_percentage_command`

**Occupancy:**
- If name contains `Occ.*Mode` or `Occupancy.*Cmd` → `occupancy_mode`

#### Writable vs Sensor Mapping

```
if point.writable:
    suffix = "_command" or "_setpoint"
else:
    suffix = "_sensor" or "_status"
```

### Phase 3: Handle Unmapped Points

Points that don't match patterns:
1. **Control Logic Points** (NumericSwitch, Ramp, Add, etc.)
   - These are internal control logic
   - May not need Google ontology mapping
   - Could be excluded or mapped to custom types

2. **Generic Points**
   - Create mapping to closest Google field type
   - Document as "UNMAPPED" for manual review

## Implementation Plan

### 1. Create Mapping Configuration

JSON file with mapping rules:

```json
{
  "device_mappings": {
    "ahu": {
      "google_type": "AHU_SFSS",
      "point_patterns": {
        "S_SFan": "supply_fan_run_command",
        "SP_.*SAT": "supply_air_temperature_setpoint",
        "HtgVlv_out": "heating_valve_percentage_command"
      }
    },
    "vav": {
      "google_type": "VAV_SD_DSP",
      "point_patterns": {
        "Zone.*Temp": "zone_air_temperature_sensor",
        "Damper.*Cmd": "supply_air_damper_percentage_command"
      }
    }
  }
}
```

### 2. Create Mapping Service in Niagara

Java class that:
- Loads mapping configuration
- Applies regex patterns to point names
- Generates Google ontology compliant names
- Handles edge cases

### 3. Validation and Testing

- Test mappings against sample devices
- Generate reports of unmapped points
- Manual review and adjustment

## Challenges

### 1. Missing Ontology Data in KODE OS

**Problem:** Points in KODE OS don't have detailed ontology types set.

**Solution:**
- Rely on naming conventions (which are fairly consistent)
- Use device context (AHU points vs VAV points)
- Implement pattern matching with confidence scores

### 2. Internal Control Logic Points

**Problem:** Many points are internal logic (NumericSwitch, Add, Ramp, etc.)

**Solution:**
- Flag these as "internal control"
- Don't map to Google ontology (they're not physical points)
- Optionally filter them out

### 3. Ambiguous Names

**Problem:** Some names like "Enable" or "Device Mode" are generic

**Solution:**
- Use device type context
- Look at point's writable property
- Look at point's kind (Number vs Bool)
- Assign confidence score to mapping

## Next Steps

1. **Create mapping configuration file** with regex patterns
2. **Implement mapper in Python** (prototype)
3. **Test mapping on sample devices**
4. **Review and refine mappings**
5. **Port to Java for Niagara integration**
6. **Add UI for viewing/editing mappings**

## Example Mapping Output

### Before (KODE OS):
```
Device: AHU_3 (ahu)
  - S_SFan (Bool, Writable)
  - SP_MWU_SAT (Number, Writable)
  - HtgVlv_out (Number, Writable)
```

### After (Google Ontology):
```
Device: Office/AHU_3
Type: AHU_SFSS
Points:
  - supply_fan_run_command
  - supply_air_temperature_setpoint
  - heating_valve_percentage_command
```

## Resources

- **Google Digital Buildings Ontology:** https://github.com/google/digitalbuildings
- **KODE OS API Documentation:** `KODE OS Public API.pdf`
- **Current Niagara Module:** `googleOntology-rt/`
