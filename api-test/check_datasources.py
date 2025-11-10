#!/usr/bin/env python3
"""
Check Datasources in KODE OS
Shows all datasources for all buildings
"""

import json
from kode_api_test import KodeAPIClient


def main():
    print("=" * 80)
    print("KODE OS Datasources Checker")
    print("=" * 80)

    # Load configuration
    with open('config.json', 'r') as f:
        config = json.load(f)

    # Initialize client
    client = KodeAPIClient(
        config['client_id'],
        config['private_key_path'],
        config.get('key_id'),
        config.get('base_url', 'https://api.kodelabs.com')
    )

    # Authenticate
    print("\n🔐 Authenticating...")
    client.authenticate()

    # Get all buildings
    print("\n🏢 Fetching buildings...")
    buildings_response = client.list_buildings()
    buildings = buildings_response.get('data', [])

    print(f"   Found {len(buildings)} building(s)\n")

    # Check datasources for each building
    for building in buildings:
        building_id = building.get('_id')
        building_name = building.get('name')

        print("=" * 80)
        print(f"Building: {building_name}")
        print(f"Building ID: {building_id}")
        print("-" * 80)

        # Get datasources
        datasources_response = client.list_datasources(building_id)
        datasources = datasources_response.get('data', [])

        if datasources:
            print(f"Datasources: {len(datasources)}")
            for ds in datasources:
                print(f"\n  📦 Name: {ds.get('name')}")
                print(f"     ID: {ds.get('_id')}")
                print(f"     Type: {ds.get('apiType', 'N/A')}")
        else:
            print("❌ No datasources found!")
            print("   You need to create a datasource in KODE OS UI first.")
            print("   See CREATE_DATASOURCE_GUIDE.md for instructions.")

        print()

    print("=" * 80)
    print("✅ Check complete!")
    print("=" * 80)


if __name__ == "__main__":
    main()
