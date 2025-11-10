#!/usr/bin/env python3
"""
Analyze KODE OS Ontology Structure
Retrieves and analyzes the ontology mapping for buildings, devices, and points
"""

import json
from collections import defaultdict
from kode_api_test import KodeAPIClient


def load_config():
    """Load configuration from config.json"""
    with open('config.json', 'r') as f:
        return json.load(f)


def analyze_device_ontology(devices):
    """Analyze device ontology structure"""

    # Count devices by canonical type
    canonical_types = defaultdict(int)
    device_samples = defaultdict(list)

    for device in devices:
        ontology = device.get('ontology', {})
        canonical_type = ontology.get('canonicalType', 'N/A')
        canonical_types[canonical_type] += 1

        # Store sample for each type (max 2)
        if len(device_samples[canonical_type]) < 2:
            device_samples[canonical_type].append({
                'name': device.get('name'),
                'id': device.get('_id'),
                'ontology': ontology
            })

    return canonical_types, device_samples


def analyze_point_ontology(points):
    """Analyze point ontology structure"""

    # Count points by type and kind
    point_types = defaultdict(int)
    point_kinds = defaultdict(int)
    canonical_types = defaultdict(int)
    point_samples = defaultdict(list)

    for point in points:
        ontology = point.get('ontology', {})
        point_type = ontology.get('type', 'N/A')
        canonical_type = ontology.get('canonicalType', 'N/A')
        kind = point.get('kind', 'N/A')

        point_types[point_type] += 1
        point_kinds[kind] += 1
        canonical_types[canonical_type] += 1

        # Store sample for each type (max 2)
        key = f"{canonical_type}:{point_type}:{kind}"
        if len(point_samples[key]) < 2:
            point_samples[key].append({
                'name': point.get('name'),
                'id': point.get('_id'),
                'kind': kind,
                'ontology': ontology,
                'unit': point.get('unit', 'N/A'),
                'writable': point.get('writable', False)
            })

    return point_types, point_kinds, canonical_types, point_samples


def get_all_points_for_building(client, building_id, limit=200):
    """Get all points for a building (paginated)"""
    all_points = []
    page = 1

    print(f"   Fetching points (page {page})...", end='', flush=True)

    while True:
        result = client.list_points(building_id, page=page, limit=limit)
        points = result.get('data', [])

        if not points:
            break

        all_points.extend(points)

        # Check if there are more pages
        total = result.get('total', 0)
        if len(all_points) >= total:
            break

        page += 1
        print(f" {page}...", end='', flush=True)

    print(f" Done! ({len(all_points)} total)")
    return all_points


def get_all_devices_for_building(client, building_id, limit=200):
    """Get all devices for a building (paginated)"""
    all_devices = []
    page = 1

    print(f"   Fetching devices (page {page})...", end='', flush=True)

    while True:
        result = client.list_devices(building_id, page=page, limit=limit)
        devices = result.get('data', [])

        if not devices:
            break

        all_devices.extend(devices)

        # Check if there are more pages
        total = result.get('total', 0)
        if len(all_devices) >= total:
            break

        page += 1
        print(f" {page}...", end='', flush=True)

    print(f" Done! ({len(all_devices)} total)")
    return all_devices


def main():
    print("=" * 80)
    print("KODE OS Ontology Analysis")
    print("=" * 80)
    print()

    # Load config and initialize client
    config = load_config()
    client = KodeAPIClient(
        config['client_id'],
        config['private_key_path'],
        config.get('key_id'),
        config.get('base_url', 'https://api.kodelabs.com')
    )

    # Authenticate
    print("🔐 Authenticating...")
    client.authenticate()
    print()

    # Get buildings
    print("📋 Fetching buildings...")
    buildings_response = client.list_buildings()
    buildings = buildings_response.get('data', [])
    print(f"   Found {len(buildings)} building(s)")
    print()

    # Analyze each building
    for building in buildings:
        building_name = building.get('name', 'Unknown')
        building_id = building.get('_id')

        print("=" * 80)
        print(f"Building: {building_name}")
        print("=" * 80)
        print()

        # Get all devices
        print("📦 Analyzing Devices...")
        devices = get_all_devices_for_building(client, building_id)

        canonical_types, device_samples = analyze_device_ontology(devices)

        print(f"\n   Device Canonical Types ({len(canonical_types)} types):")
        for ctype, count in sorted(canonical_types.items(), key=lambda x: x[1], reverse=True):
            print(f"      • {ctype}: {count} device(s)")

        print(f"\n   Sample Devices by Type:")
        for ctype, samples in sorted(device_samples.items()):
            if ctype == 'N/A':
                continue
            print(f"\n      📍 {ctype}:")
            for sample in samples:
                print(f"         - {sample['name']} (ID: {sample['id']})")
                if sample['ontology']:
                    print(f"           Ontology: {json.dumps(sample['ontology'], indent=13)}")

        # Get all points
        print(f"\n📊 Analyzing Points...")
        points = get_all_points_for_building(client, building_id)

        point_types, point_kinds, canonical_types, point_samples = analyze_point_ontology(points)

        print(f"\n   Point Canonical Types ({len(canonical_types)} types):")
        for ctype, count in sorted(canonical_types.items(), key=lambda x: x[1], reverse=True)[:20]:
            print(f"      • {ctype}: {count} point(s)")

        if len(canonical_types) > 20:
            print(f"      ... and {len(canonical_types) - 20} more types")

        print(f"\n   Point Ontology Types ({len(point_types)} types):")
        for ptype, count in sorted(point_types.items(), key=lambda x: x[1], reverse=True)[:20]:
            print(f"      • {ptype}: {count} point(s)")

        if len(point_types) > 20:
            print(f"      ... and {len(point_types) - 20} more types")

        print(f"\n   Point Kinds:")
        for kind, count in sorted(point_kinds.items(), key=lambda x: x[1], reverse=True):
            print(f"      • {kind}: {count} point(s)")

        print(f"\n   Sample Points by Type (Top 10):")

        # Show top 10 most common point type combinations
        sorted_samples = sorted(point_samples.items(),
                              key=lambda x: len(x[1]),
                              reverse=True)[:10]

        for key, samples in sorted_samples:
            canonical, ptype, kind = key.split(':', 2)
            print(f"\n      📍 {canonical} | {ptype} | {kind}:")
            for sample in samples[:1]:  # Just show one sample
                print(f"         - {sample['name']}")
                print(f"           Unit: {sample['unit']}, Writable: {sample['writable']}")
                if sample['ontology']:
                    ontology_str = json.dumps(sample['ontology'], indent=13)
                    # Indent properly
                    lines = ontology_str.split('\n')
                    for line in lines:
                        print(f"           {line}")

        print()

    # Generate summary
    print("=" * 80)
    print("📊 Summary")
    print("=" * 80)
    print()
    print("✅ Analysis complete!")
    print()
    print("Key Findings:")
    print("  • Buildings analyzed:", len(buildings))
    print("  • This data shows the ontology structure used in KODE OS")
    print("  • Use this information to map to Google ontology format")
    print()
    print("Next Steps:")
    print("  1. Compare KODE canonical types with Google ontology types")
    print("  2. Create mapping rules for device types")
    print("  3. Create mapping rules for point types")
    print("  4. Implement transformation logic in Niagara module")


if __name__ == "__main__":
    main()
