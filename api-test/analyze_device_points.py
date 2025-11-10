#!/usr/bin/env python3
"""
Detailed Device and Point Analysis
Retrieves full details for specific devices to understand point structure
"""

import json
from kode_api_test import KodeAPIClient


def load_config():
    """Load configuration from config.json"""
    with open('config.json', 'r') as f:
        return json.load(f)


def analyze_device_points(client, building_id, device_id, device_name):
    """Analyze all points for a specific device"""

    print(f"\n{'='*80}")
    print(f"Device: {device_name}")
    print(f"Device ID: {device_id}")
    print(f"{'='*80}\n")

    # Get all points for this device
    all_points = []
    page = 1

    while True:
        result = client.list_points(building_id, page=page, limit=200, device_id=device_id)
        points = result.get('data', [])

        if not points:
            break

        all_points.extend(points)

        total = result.get('total', 0)
        if len(all_points) >= total:
            break

        page += 1

    print(f"Total Points: {len(all_points)}\n")

    # Group by kind
    by_kind = {}
    for point in all_points:
        kind = point.get('kind', 'Unknown')
        if kind not in by_kind:
            by_kind[kind] = []
        by_kind[kind].append(point)

    # Display points by kind
    for kind, points in sorted(by_kind.items()):
        print(f"  {kind} Points ({len(points)}):")
        print(f"  {'-'*76}")

        for point in points[:5]:  # Show first 5 of each kind
            name = point.get('name', 'N/A')
            point_id = point.get('_id', 'N/A')
            unit = point.get('unit', 'N/A')
            writable = point.get('writable', False)
            status = point.get('status', 'N/A')

            ontology = point.get('ontology', {})
            ont_type = ontology.get('type', 'N/A')
            ont_canonical = ontology.get('canonicalType', 'N/A')

            # Get current value if available
            cur_val = point.get('cur', {})
            value = cur_val.get('val', 'N/A')

            print(f"    • {name}")
            print(f"      - ID: {point_id}")
            print(f"      - Value: {value} {unit if unit != 'N/A' else ''}")
            print(f"      - Writable: {writable}, Status: {status}")
            print(f"      - Ontology Type: {ont_type}, Canonical: {ont_canonical}")

            # Show full ontology if it has more than just type
            if len(ontology) > 1 or (len(ontology) == 1 and 'type' not in ontology):
                print(f"      - Full Ontology: {json.dumps(ontology, indent=8)}")

            print()

        if len(points) > 5:
            print(f"    ... and {len(points) - 5} more {kind} points\n")


def main():
    print("="*80)
    print("KODE OS Device Point Analysis")
    print("="*80)

    # Load config and initialize client
    config = load_config()
    client = KodeAPIClient(
        config['client_id'],
        config['private_key_path'],
        config.get('key_id'),
        config.get('base_url', 'https://api.kodelabs.com')
    )

    # Authenticate
    print("\n🔐 Authenticating...")
    client.authenticate()

    # Get buildings
    print("\n📋 Fetching buildings...")
    buildings_response = client.list_buildings()
    buildings = buildings_response.get('data', [])

    if not buildings:
        print("No buildings found!")
        return

    # For each building, analyze key devices
    for building in buildings:
        building_name = building.get('name', 'Unknown')
        building_id = building.get('_id')

        print(f"\n{'='*80}")
        print(f"Building: {building_name}")
        print(f"{'='*80}")

        # Get devices
        devices_response = client.list_devices(building_id, limit=200)
        devices = devices_response.get('data', [])

        # Group devices by canonical type
        by_type = {}
        for device in devices:
            canonical_type = device.get('ontology', {}).get('canonicalType', 'N/A')
            if canonical_type not in by_type:
                by_type[canonical_type] = []
            by_type[canonical_type].append(device)

        # Analyze one device of each type
        for device_type, device_list in sorted(by_type.items()):
            if device_type == 'N/A':
                continue  # Skip devices without canonical type

            # Analyze first device of this type
            device = device_list[0]
            device_name = device.get('name', 'Unknown')
            device_id = device.get('_id')

            analyze_device_points(client, building_id, device_id, f"{device_name} ({device_type})")

            # Only analyze first 3 types to avoid too much output
            if len([k for k in by_type.keys() if k != 'N/A']) > 3:
                analyzed_types = [k for k in by_type.keys() if k != 'N/A']
                if analyzed_types.index(device_type) >= 2:
                    remaining_types = len([k for k in by_type.keys() if k != 'N/A']) - 3
                    if remaining_types > 0:
                        print(f"\n  ... skipping {remaining_types} more device type(s) for brevity\n")
                    break

    print("\n" + "="*80)
    print("Analysis Complete!")
    print("="*80)
    print("\nKey Observations:")
    print("  • Check if points have meaningful names that indicate their function")
    print("  • Look for patterns in naming (e.g., Temperature, Setpoint, Command)")
    print("  • Note which points are writable (commands) vs. read-only (sensors)")
    print("  • Identify common point types per device type (VAV, AHU, FCU, etc.)")
    print("\nThis information will help create the Google ontology mapping!")


if __name__ == "__main__":
    main()
