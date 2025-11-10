#!/usr/bin/env python3
"""
Google Ontology to KODE OS Mapper
Converts Google Digital Buildings ontology format to KODE OS format
"""

import re
from typing import Dict, List, Tuple


class GoogleToKodeMapper:
    """Maps Google ontology device types and field names to KODE OS format"""

    # Device type mappings: Google → KODE canonical type
    DEVICE_TYPE_MAPPING = {
        'AHU_SFSS': 'ahu',
        'AHU_SFVSC': 'ahu',
        'AHU': 'ahu',
        'VAV_SD_DSP': 'vav',
        'VAV_RH_DSP': 'vav',
        'VAV': 'vav',
        'FCU_DFSS_DFVSC': 'fcu',
        'FCU': 'fcu',
    }

    # Field name mapping rules: Google ontology → KODE OS naming
    FIELD_MAPPING_RULES = [
        # Supply Fan
        (r'supply_fan_run_command', 'Supply_Fan_Cmd'),
        (r'supply_fan_run_status', 'Supply_Fan_Status'),
        (r'supply_fan_speed_percentage_command', 'Supply_Fan_Speed_Cmd'),

        # Discharge Fan
        (r'discharge_fan_run_command', 'Discharge_Fan_Cmd'),
        (r'discharge_fan_run_status', 'Discharge_Fan_Status'),
        (r'discharge_fan_speed_percentage_command', 'Discharge_Fan_Speed_Cmd'),

        # Return Fan
        (r'return_fan_run_command', 'Return_Fan_Cmd'),
        (r'return_fan_run_status', 'Return_Fan_Status'),

        # Supply Air Temperature
        (r'supply_air_temperature_sensor', 'Supply_Air_Temp'),
        (r'supply_air_temperature_setpoint', 'Supply_Air_Temp_Sp'),

        # Zone Temperature
        (r'zone_air_temperature_sensor', 'Zone_Temp'),
        (r'zone_air_temperature_setpoint', 'Zone_Temp_Sp'),
        (r'zone_air_cooling_temperature_setpoint', 'Zone_Cooling_Sp'),
        (r'zone_air_heating_temperature_setpoint', 'Zone_Heating_Sp'),

        # Return Air Temperature
        (r'return_air_temperature_sensor', 'Return_Air_Temp'),
        (r'return_air_temperature_setpoint', 'Return_Air_Temp_Sp'),

        # Mixed Air Temperature
        (r'mixed_air_temperature_sensor', 'Mixed_Air_Temp'),

        # Discharge Air Temperature
        (r'discharge_air_temperature_sensor', 'Discharge_Air_Temp'),
        (r'discharge_air_temperature_setpoint', 'Discharge_Air_Temp_Sp'),

        # Outside Air Temperature
        (r'outside_air_temperature_sensor', 'Outside_Air_Temp'),

        # Valves
        (r'heating_valve_percentage_command', 'Heating_Valve_Cmd'),
        (r'cooling_valve_percentage_command', 'Cooling_Valve_Cmd'),
        (r'reheat_valve_percentage_command', 'Reheat_Valve_Cmd'),

        # Dampers
        (r'supply_air_damper_percentage_command', 'Damper_Cmd'),
        (r'outside_air_damper_percentage_command', 'OA_Damper_Cmd'),
        (r'return_air_damper_percentage_command', 'RA_Damper_Cmd'),
        (r'exhaust_air_damper_percentage_command', 'EA_Damper_Cmd'),

        # Airflow
        (r'supply_air_flow_sensor', 'Supply_Airflow'),
        (r'supply_air_flow_setpoint', 'Supply_Airflow_Sp'),
        (r'zone_air_flow_sensor', 'Zone_Airflow'),

        # Pressure
        (r'supply_air_static_pressure_sensor', 'Supply_Static_Pressure'),
        (r'supply_air_static_pressure_setpoint', 'Supply_Static_Pressure_Sp'),

        # Modes
        (r'occupancy_mode', 'Occupancy_Mode'),
        (r'occupancy_command', 'Occupancy_Cmd'),
        (r'run_mode', 'Run_Mode'),
        (r'run_command', 'Run_Cmd'),

        # Generic catch-all (convert underscores to title case)
        (r'(.+)', lambda m: self._to_kode_name(m.group(1))),
    ]

    @staticmethod
    def _to_kode_name(google_name: str) -> str:
        """
        Convert Google ontology underscore naming to KODE OS title case
        Example: 'zone_air_temperature_sensor' → 'Zone_Air_Temperature_Sensor'
        """
        # Split by underscore and title case each word
        words = google_name.split('_')
        return '_'.join(word.capitalize() for word in words)

    def map_device_type(self, google_type: str) -> str:
        """
        Map Google device type to KODE canonical type

        Args:
            google_type: Google ontology type (e.g., 'AHU_SFSS', 'VAV_SD_DSP')

        Returns:
            KODE canonical type (e.g., 'ahu', 'vav', 'fcu')
        """
        # Try exact match first
        if google_type in self.DEVICE_TYPE_MAPPING:
            return self.DEVICE_TYPE_MAPPING[google_type]

        # Try prefix match (e.g., VAV_RH_DSP → VAV)
        for gtype, ktype in self.DEVICE_TYPE_MAPPING.items():
            if google_type.startswith(gtype + '_'):
                return ktype

        # Default to lowercase of first word
        return google_type.split('_')[0].lower()

    def map_field_name(self, google_field: str) -> str:
        """
        Map Google field name to KODE OS point name

        Args:
            google_field: Google ontology field (e.g., 'supply_fan_run_command')

        Returns:
            KODE OS point name (e.g., 'Supply_Fan_Cmd')
        """
        # Try each mapping rule in order
        for pattern, replacement in self.FIELD_MAPPING_RULES:
            if isinstance(replacement, str):
                # Simple string replacement
                match = re.fullmatch(pattern, google_field)
                if match:
                    return replacement
            else:
                # Callable replacement (e.g., lambda)
                match = re.fullmatch(pattern, google_field)
                if match:
                    return replacement(match)

        # Fallback: just convert to title case
        return self._to_kode_name(google_field)

    def map_device(self, google_device: Dict) -> Dict:
        """
        Map a complete device from Google format to KODE OS format

        Args:
            google_device: Device in Google ontology format

        Returns:
            Device in KODE OS API format
        """
        kode_device = {
            'id': google_device['id'],
            'name': google_device['name'],
            'displayName': google_device.get('description', google_device['name']),
            'path': google_device.get('location', ''),
            'points': []
        }

        # Map each point
        for point in google_device.get('points', []):
            kode_point = self.map_point(point)
            kode_device['points'].append(kode_point)

        return kode_device

    def map_point(self, google_point: Dict) -> Dict:
        """
        Map a point from Google format to KODE OS format

        Args:
            google_point: Point in Google ontology format

        Returns:
            Point in KODE OS API format
        """
        kode_name = self.map_field_name(google_point['name'])

        # Map kind
        kind = google_point.get('kind', 'Number')
        if kind == 'Bool':
            kind = 'Bool'
        elif kind == 'Number':
            kind = 'Number'
        else:
            kind = 'Str'

        # Generate sourceId from Niagara path
        source_id = google_point.get('niagara_path', '')
        if not source_id:
            source_id = google_point.get('id', kode_name.replace(' ', '_').lower())

        # Generate pointId - unique identifier for the point
        # Use the Google point name or generate from KODE name
        point_id = google_point.get('id', google_point.get('name', kode_name.replace(' ', '_').lower()))

        kode_point = {
            'name': kode_name,
            'pointId': point_id,
            'sourceId': source_id,
            'kind': kind,
            'writable': google_point.get('writable', False)
        }

        # Add unit if present
        if google_point.get('unit'):
            kode_point['unit'] = google_point['unit']

        # Add enum if present (for enumerated values)
        if google_point.get('enum'):
            kode_point['pointEnum'] = google_point['enum']
        elif kind == 'Bool':
            # Boolean points need pointEnum in KODE OS
            kode_point['pointEnum'] = ['false', 'true']

        return kode_point

    def create_kode_payload(self, google_devices: List[Dict], building_id: str,
                           datasource_id: str) -> Dict:
        """
        Create a complete KODE OS API payload for batch device creation

        Args:
            google_devices: List of devices in Google format
            building_id: KODE OS building ID
            datasource_id: KODE OS datasource ID

        Returns:
            Payload ready for KODE OS batch API
        """
        kode_devices = []

        for google_device in google_devices:
            kode_device = self.map_device(google_device)
            kode_devices.append(kode_device)

        return {
            'devices': kode_devices
        }

    def get_mapping_report(self, google_device: Dict) -> str:
        """
        Generate a human-readable mapping report

        Args:
            google_device: Device in Google format

        Returns:
            Formatted report string
        """
        report = []
        report.append("=" * 80)
        report.append(f"Device: {google_device['name']}")
        report.append(f"Google Type: {google_device.get('type', 'N/A')}")
        report.append(f"KODE Type: {self.map_device_type(google_device.get('type', 'unknown'))}")
        report.append("=" * 80)
        report.append("")
        report.append("Point Mappings:")
        report.append("-" * 80)

        for point in google_device.get('points', []):
            google_name = point['name']
            kode_name = self.map_field_name(google_name)
            kind = point.get('kind', 'Number')
            writable = 'Write' if point.get('writable', False) else 'Read'

            report.append(f"  {google_name:<45} → {kode_name:<30} ({kind}, {writable})")

        report.append("")
        return "\n".join(report)


# Example usage
if __name__ == "__main__":
    mapper = GoogleToKodeMapper()

    # Test device type mapping
    print("Device Type Mappings:")
    print("-" * 40)
    test_types = ['AHU_SFSS', 'VAV_SD_DSP', 'FCU_DFSS_DFVSC', 'VAV_RH_DSP']
    for gtype in test_types:
        ktype = mapper.map_device_type(gtype)
        print(f"  {gtype:<20} → {ktype}")

    print("\n" + "=" * 80)
    print("Field Name Mappings:")
    print("=" * 80)

    # Test field mappings
    test_fields = [
        'supply_fan_run_command',
        'zone_air_temperature_sensor',
        'heating_valve_percentage_command',
        'supply_air_damper_percentage_command',
        'occupancy_mode',
    ]

    for field in test_fields:
        kode_name = mapper.map_field_name(field)
        print(f"  {field:<45} → {kode_name}")
