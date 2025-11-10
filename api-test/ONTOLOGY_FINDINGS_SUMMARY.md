# KODE OS Ontology Analysis - Summary of Findings

## Executive Summary

Your KODE OS environment contains **2 buildings** with **186 devices total** and hundreds of points. The ontology structure shows:

✅ **Device-level ontology is defined** - Devices have canonical types (ahu, vav, fcu, loopSystem)
❌ **Point-level ontology is NOT defined** - Points lack ontology metadata
✅ **Naming conventions are consistent** - Point names follow patterns that can be parsed

## Building Inventory

### Office Building
- **93 devices**
  - 10 VAV (Variable Air Volume)
  - 1 AHU (Air Handling Unit)
  - 82 uncategorized devices

### Warehouse Building
- **93 devices**
  - 40 VAV (Variable Air Volume)
  - 40 FCU (Fan Coil Unit)
  - 4 AHU (Air Handling Unit)
  - 1 Loop System
  - 8 uncategorized devices

## Key Findings

### 1. Device Ontology Status: ✅ GOOD

Devices have canonical types that can be mapped to Google ontology:

| KODE Type | Count | Google Mapping | Confidence |
|-----------|-------|----------------|------------|
| `vav` | 50 | `VAV_*` types | HIGH |
| `ahu` | 5 | `AHU_*` types | HIGH |
| `fcu` | 40 | `FCU_*` types | HIGH |
| `loopSystem` | 1 | Custom/CTRL | MEDIUM |

### 2. Point Ontology Status: ❌ NEEDS WORK

**Problem:** All points show:
```json
{
  "ontology": {
    "type": "N/A",
    "canonicalType": "N/A"
  }
}
```

**Impact:**
- Cannot directly map points using ontology fields
- Must rely on name pattern matching
- Requires robust regex-based mapping logic

### 3. Point Naming Patterns: ✅ PARSEABLE

Despite missing ontology metadata, point names follow patterns:

#### AHU Common Points (from AHU_3)

**Fan Control:**
- `S_SFan` - Supply fan (Bool, Writable)
- `Discharge Fan Status` - Fan status (Bool)
- `Discharge Fan Cmd` - Fan command (Bool)

**Temperature:**
- `SP_MWU_SAT` - Supply air temp setpoint (Number)
- `SP_HtgSAT` - Heating setpoint (Number)
- `T_HtgSAT` - Temperature reading (Number)
- `Eff Heating Temp Setpt` - Effective heating setpoint (Number)

**Valves:**
- `HtgVlv_out` - Heating valve output (Number)
- `Heat PID` - PID controller (Number)

**Modes:**
- `MWU` - Make-up mode (Bool)
- `Enable` - Enable flag (Bool)
- `Device Mode` - Operating mode (Bool)

#### VAV Common Points (from VAV_101)

**Control Logic** (Internal - likely don't need mapping):
- `NumericSwitch`, `NumericSwitch1`
- `Ramp`, `Ramp1`
- `Add`, `Subtract`, `Divide`, `Multiply`
- `Minimum`, `Maximum`
- `GreaterThan`, `LessThan`

**Commands:**
- `Occ_Mode` - Occupancy mode (Bool)
- `Electric Heat Stage Cmd` - Heating command (Bool)

#### FCU Common Points (from FCU_207)

**Fan Control:**
- `Discharge Fan Status` (Bool)
- `Discharge Fan Cmd` (Bool)
- `Occupancy Cmd` (Bool)

**Control:**
- `Cooling_PI1` - Cooling PID (Number)
- Similar logic components as VAV

### 4. Point Characteristics

**By Kind:**
- **Number:** ~342 points (75%) - Analog values, setpoints, sensor readings
- **Bool:** ~59 points (25%) - Binary states, commands, enables

**By Writability:**
- **Writable points** - Commands, setpoints (need `_command` or `_setpoint` suffix)
- **Read-only points** - Sensors, status (need `_sensor` or `_status` suffix)

## Mapping Strategy

### Approach 1: Pattern-Based Mapping (Recommended)

Use regex patterns on point names to infer Google ontology fields:

```
Pattern: "S_SFan" + Writable → "supply_fan_run_command"
Pattern: "SP_.*SAT" + Number → "supply_air_temperature_setpoint"
Pattern: "HtgVlv" + Number → "heating_valve_percentage_command"
```

**Pros:**
- Works with current data structure
- Flexible and extensible
- Can handle variations

**Cons:**
- Requires maintaining pattern rules
- May misclassify ambiguous names
- Need confidence scoring

### Approach 2: Enhance KODE OS Ontology (Future)

Work with KODE team to add proper ontology metadata to points:

```json
{
  "name": "S_SFan",
  "ontology": {
    "type": "supply_fan_run_command",
    "canonicalType": "fan_command"
  }
}
```

**Pros:**
- Direct, accurate mapping
- No guessing
- Industry standard

**Cons:**
- Requires KODE OS changes
- Not available today
- Migration needed

## Google Ontology Field Examples

Based on observed points, you'll likely need these Google fields:

### For AHUs:
- `supply_fan_run_command`
- `supply_fan_run_status`
- `supply_air_temperature_sensor`
- `supply_air_temperature_setpoint`
- `heating_valve_percentage_command`
- `heating_air_temperature_setpoint`
- `discharge_fan_run_command`
- `discharge_fan_run_status`

### For VAVs:
- `zone_air_temperature_sensor`
- `zone_air_temperature_setpoint`
- `supply_air_damper_percentage_command`
- `supply_air_flow_sensor`
- `reheat_valve_percentage_command`
- `occupancy_mode`

### For FCUs:
- `discharge_fan_run_command`
- `discharge_fan_run_status`
- `zone_air_temperature_sensor`
- `zone_air_cooling_temperature_setpoint`
- `zone_air_heating_temperature_setpoint`
- `cooling_valve_percentage_command`
- `heating_valve_percentage_command`

## Challenges & Solutions

### Challenge 1: Internal Control Logic Points

**Problem:** Many points are internal logic (NumericSwitch, Ramp, Add, etc.)

**Solution:**
- Create filter to identify control logic points
- Skip mapping these (they're not physical points)
- Document as "internal" type

### Challenge 2: Ambiguous Names

**Problem:** Names like "Enable" or "Device Mode" are not specific

**Solution:**
- Use device type context (AHU Enable vs VAV Enable)
- Check point kind (Bool vs Number)
- Look at writable flag
- Assign confidence score to each mapping

### Challenge 3: Missing Units

**Problem:** Most points show `unit: "N/A"`

**Solution:**
- Infer from point name (Temp → °F/°C, Vlv → %)
- Use Google ontology defaults
- Allow manual override in configuration

## Recommended Next Steps

### Phase 1: Create Mapping Engine (Python Prototype)
1. ✅ API connectivity working
2. ✅ Ontology structure analyzed
3. ⏭️ **Create mapping rules configuration**
4. ⏭️ **Build pattern-based mapper**
5. ⏭️ **Test on sample devices**
6. ⏭️ **Generate mapping report with confidence scores**

### Phase 2: Integrate with Niagara
1. Port mapping logic to Java
2. Create BKodeApiService component
3. Add mapping configuration UI
4. Implement sync engine
5. Add error handling and logging

### Phase 3: Refinement
1. Review unmapped points
2. Add custom mapping rules
3. Handle edge cases
4. User testing and feedback

## Sample Mapping Output

Here's what a mapped device would look like:

### Input (KODE OS):
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

### Output (Google Ontology):
```
Office/AHU_3
├── type: AHU_SFSS
├── points:
│   ├── supply_fan_run_command (S_SFan) [confidence: 95%]
│   ├── supply_air_temperature_setpoint (SP_MWU_SAT) [confidence: 90%]
│   └── heating_valve_percentage_command (HtgVlv_out) [confidence: 95%]
```

## Files Created

1. **`analyze_ontology.py`** - High-level ontology analysis script
2. **`analyze_device_points.py`** - Detailed device/point analysis
3. **`ontology_analysis_full.txt`** - Complete analysis output
4. **`ONTOLOGY_MAPPING_ANALYSIS.md`** - Mapping strategy document
5. **`ONTOLOGY_FINDINGS_SUMMARY.md`** - This summary

## Conclusion

✅ **Feasibility:** Mapping KODE OS to Google ontology is FEASIBLE
✅ **Approach:** Pattern-based mapping using point names
✅ **Confidence:** HIGH for common HVAC points, MEDIUM for generic names
⚠️ **Limitation:** Some points will require manual review
🚀 **Ready:** Can proceed to Phase 1 implementation

The ontology mapping can be automated for most points using pattern matching on point names, with manual review needed for ambiguous cases.
