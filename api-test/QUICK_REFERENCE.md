# Quick Reference - KODE OS to Google Ontology Mapping

## Your KODE OS Environment

- **2 Buildings**: Office, Warehouse
- **186 Devices**: 50 VAVs, 40 FCUs, 5 AHUs, 1 Loop System, 90 uncategorized
- **400+ Points**: Mostly lacking ontology metadata

## Key Finding: Use Pattern-Based Mapping

Since points lack ontology metadata, map using **name patterns**:

## Common Point Patterns

### Temperature
| KODE Pattern | Example | Google Field | Type |
|--------------|---------|--------------|------|
| `SP_.*SAT` | SP_MWU_SAT | supply_air_temperature_setpoint | Number |
| `T_.*SAT` | T_HtgSAT | supply_air_temperature_sensor | Number |
| `.*Temp.*Setpt` | Eff Heating Temp Setpt | *_temperature_setpoint | Number |

### Fans
| KODE Pattern | Example | Google Field | Type |
|--------------|---------|--------------|------|
| `S_SFan` | S_SFan | supply_fan_run_command | Bool |
| `Discharge.*Fan.*Status` | Discharge Fan Status | discharge_fan_run_status | Bool |
| `Discharge.*Fan.*Cmd` | Discharge Fan Cmd | discharge_fan_run_command | Bool |

### Valves
| KODE Pattern | Example | Google Field | Type |
|--------------|---------|--------------|------|
| `HtgVlv.*` | HtgVlv_out | heating_valve_percentage_command | Number |
| `ClgVlv.*` | ClgVlv_out | cooling_valve_percentage_command | Number |

### Modes
| KODE Pattern | Example | Google Field | Type |
|--------------|---------|--------------|------|
| `Occ.*Mode` | Occ_Mode | occupancy_mode | Bool |
| `Occupancy.*Cmd` | Occupancy Cmd | occupancy_command | Bool |

### PIDs/Control
| KODE Pattern | Example | Google Field | Type |
|--------------|---------|--------------|------|
| `.*PID.*` | Heat PID | *_percentage_command | Number |
| `Cooling_PI.*` | Cooling_PI1 | cooling_percentage_command | Number |

## Internal Logic Points (Skip These)

Don't map these - they're internal control logic:
- `NumericSwitch`, `NumericSwitch1`
- `Ramp`, `Ramp1`
- `Add`, `Subtract`, `Multiply`, `Divide`
- `Minimum`, `Maximum`
- `GreaterThan`, `LessThan`
- `And`, `Or`, `Not`

## Device Type Mappings

| KODE Type | Google Type | Example |
|-----------|-------------|---------|
| `ahu` | AHU_SFSS, AHU_SFVSC, etc. | Office/AHU_3 |
| `vav` | VAV_SD_DSP, VAV_RH_DSP, etc. | Office/VAV_101 |
| `fcu` | FCU_DFSS_DFVSC, etc. | Warehouse/FCU_207 |

## Mapping Rules

### Rule 1: Check Writable Flag
```
if point.writable == true:
    use "_command" or "_setpoint" suffix
else:
    use "_sensor" or "_status" suffix
```

### Rule 2: Check Point Kind
```
if point.kind == "Bool":
    likely: _command, _status, _mode
elif point.kind == "Number":
    likely: _sensor, _setpoint, _percentage_command
```

### Rule 3: Use Device Context
```
AHU + "Enable" → supply_fan_enable
VAV + "Enable" → zone_enable
```

### Rule 4: Confidence Scoring
```
Exact pattern match (S_SFan) → 95% confidence
Partial match (.*Fan.*Cmd) → 85% confidence
Generic name (Enable) → 60% confidence
```

## Example: Mapping an AHU

### Input (KODE OS)
```json
{
  "name": "AHU_3",
  "canonicalType": "ahu",
  "points": [
    {"name": "S_SFan", "kind": "Bool", "writable": true},
    {"name": "SP_MWU_SAT", "kind": "Number", "writable": true},
    {"name": "HtgVlv_out", "kind": "Number", "writable": true}
  ]
}
```

### Output (Google Ontology)
```
Office/AHU_3:
  type: AHU_SFSS
  uses:
    - supply_fan_run_command          (S_SFan) [95%]
    - supply_air_temperature_setpoint (SP_MWU_SAT) [90%]
    - heating_valve_percentage_command (HtgVlv_out) [95%]
```

## Files to Review

1. **ONTOLOGY_FINDINGS_SUMMARY.md** - Complete analysis
2. **ONTOLOGY_MAPPING_ANALYSIS.md** - Detailed mapping strategy
3. **ontology_analysis_full.txt** - Raw data
4. **API_INTEGRATION_PLAN.md** - Implementation roadmap

## Ready for Implementation?

✅ Yes! The patterns are clear and consistent enough to automate.

Next: Create mapping configuration file and build the mapper!
