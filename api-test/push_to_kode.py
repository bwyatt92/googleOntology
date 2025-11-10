#!/usr/bin/env python3
"""
Push Niagara Devices to KODE OS
Demonstrates automatic synchronization from Niagara to KODE OS
"""

import json
import sys
from kode_api_test import KodeAPIClient
from google_to_kode_mapper import GoogleToKodeMapper


def load_config():
    """Load KODE OS API configuration"""
    with open('config.json', 'r') as f:
        return json.load(f)


def load_niagara_devices():
    """Load sample Niagara device structure"""
    with open('niagara_sample_devices.json', 'r') as f:
        return json.load(f)


def get_or_create_datasource(client, building_id, datasource_name="NiagaraSync"):
    """
    Get or create a datasource for Niagara integration

    Args:
        client: KodeAPIClient instance
        building_id: Building ID in KODE OS
        datasource_name: Name for the datasource

    Returns:
        datasource_id or None if error
    """
    print(f"\n  Looking for datasource '{datasource_name}'...")

    # List existing datasources
    try:
        response = client.list_datasources(building_id)
        datasources = response.get('data', [])

        # Look for existing datasource
        for ds in datasources:
            if ds.get('name') == datasource_name:
                ds_id = ds.get('_id')
                print(f"   ✅ Found existing datasource: {ds_id}")
                return ds_id

        print(f"      Datasource '{datasource_name}' not found.")
        print(f"      You'll need to create it manually in KODE OS first.")
        print(f"      Or provide an existing datasource ID.")

        return None

    except Exception as e:
        print(f"     Error checking datasources: {e}")
        return None


def create_devices_in_kode(client, building_id, datasource_id, devices, mapper):
    """
    Create devices in KODE OS using batch API

    Args:
        client: KodeAPIClient instance
        building_id: Building ID
        datasource_id: Datasource ID
        devices: List of devices in Google format
        mapper: GoogleToKodeMapper instance

    Returns:
        True if successful, False otherwise
    """
    print(f"\n Creating devices in KODE OS...")
    print(f"   Building: {building_id}")
    print(f"   Datasource: {datasource_id}")
    print(f"   Devices to create: {len(devices)}")
    print()

    success_count = 0
    error_count = 0

    # Create devices one by one (easier to debug)
    for device in devices:
        device_name = device['name']
        print(f"   Creating device: {device_name}")

        try:
            # Map device to KODE format
            kode_device = mapper.map_device(device)

            # DEBUG: Print the payload
            print(f"      DEBUG: Payload being sent:")
            import json
            print(json.dumps(kode_device, indent=2))

            # Call KODE OS API to create device
            result = client.create_device(building_id, datasource_id, kode_device)

            print(f"         Created successfully!")
            print(f"         Points: {len(kode_device['points'])}")
            success_count += 1

        except Exception as e:
            print(f"      Error: {e}")
            # Print more details about the error
            if hasattr(e, 'response') and hasattr(e.response, 'text'):
                print(f"      Response: {e.response.text}")
            error_count += 1

    print()
    print("=" * 80)
    print(f"Results: {success_count} successful, {error_count} errors")
    print("=" * 80)

    return error_count == 0


def generate_mapping_preview(devices, mapper):
    """
    Generate a preview of how devices will be mapped

    Args:
        devices: List of devices in Google format
        mapper: GoogleToKodeMapper instance

    Returns:
        Preview report string
    """
    report = []
    report.append("\n" + "=" * 80)
    report.append("MAPPING PREVIEW")
    report.append("=" * 80)

    for device in devices:
        device_report = mapper.get_mapping_report(device)
        report.append(device_report)

    return "\n".join(report)


def main():
    print("=" * 80)
    print("Push Niagara Devices to KODE OS")
    print("=" * 80)

    # Load configuration
    print("\n Loading configuration...")
    config = load_config()
    print(f"   KODE OS: {config.get('base_url')}")

    # Load Niagara devices
    print("\n Loading Niagara devices...")
    niagara_data = load_niagara_devices()
    devices = niagara_data['devices']
    print(f"   Found {len(devices)} devices")
    for dev in devices:
        print(f"      • {dev['name']} ({dev['type']}) - {len(dev['points'])} points")

    # Initialize mapper
    mapper = GoogleToKodeMapper()

    # Show mapping preview
    print(generate_mapping_preview(devices, mapper))

    # Ask for confirmation
    print("\n" + "=" * 80)
    response = input("Do you want to push these devices to KODE OS? (yes/no): ")

    if response.lower() not in ['yes', 'y']:
        print("Aborted.")
        return

    # Initialize KODE API client
    print("\n Authenticating with KODE OS...")
    client = KodeAPIClient(
        config['client_id'],
        config['private_key_path'],
        config.get('key_id'),
        config.get('base_url', 'https://api.kodelabs.com')
    )
    client.authenticate()
    print("     Authenticated!")

    # Get buildings
    print("\n  Fetching buildings...")
    buildings_response = client.list_buildings()
    buildings = buildings_response.get('data', [])

    if not buildings:
        print("     No buildings found!")
        return

    # Show available buildings
    print(f"   Found {len(buildings)} building(s):")
    for i, building in enumerate(buildings):
        print(f"      {i+1}. {building.get('name')} (ID: {building.get('_id')})")

    # Select building
    if len(buildings) == 1:
        selected_building = buildings[0]
        print(f"\n   Using: {selected_building.get('name')}")
    else:
        building_idx = int(input(f"\nSelect building (1-{len(buildings)}): ")) - 1
        selected_building = buildings[building_idx]

    building_id = selected_building.get('_id')

    # Get or create datasource
    datasource_id = get_or_create_datasource(client, building_id)

    if not datasource_id:
        print("\n   You need a datasource to create devices.")
        print("\nOptions:")
        print("  1. Create a datasource in KODE OS (API type)")
        print("  2. Provide an existing datasource ID")
        print()
        datasource_id = input("Enter datasource ID (or 'exit' to quit): ").strip()

        if datasource_id.lower() == 'exit':
            return

    # Create devices
    success = create_devices_in_kode(client, building_id, datasource_id,
                                    devices, mapper)

    if success:
        print("\n  All devices created successfully!")
        print("\n  You can now view them in KODE OS:")
        print(f"   {config.get('base_url')}")
    else:
        print("\n  Some devices failed to create. Check errors above.")

    # Verify by reading back
    print("\n  Verifying devices...")
    devices_response = client.list_devices(building_id, limit=200)
    created_devices = devices_response.get('data', [])

    print(f"   Total devices in building: {len(created_devices)}")

    # Find our newly created devices
    our_device_names = {d['name'] for d in devices}
    found_devices = [d for d in created_devices if d.get('name') in our_device_names]

    print(f"   Found our devices: {len(found_devices)}")
    for dev in found_devices:
        print(f"       {dev.get('name')} (ID: {dev.get('_id')})")

    print("\n" + "=" * 80)
    print("  DEMONSTRATION COMPLETE!")
    print("=" * 80)
    print("\nWhat we just did:")
    print("  1.    Loaded Niagara device structure (Google ontology format)")
    print("  2.    Mapped Google field names → KODE OS point names")
    print("  3.    Authenticated with KODE OS API")
    print("  4.    Created devices and points in KODE OS")
    print("  5.    Verified devices were created")
    print("\nThis same process can be integrated into Niagara!")


if __name__ == "__main__":
    main()
