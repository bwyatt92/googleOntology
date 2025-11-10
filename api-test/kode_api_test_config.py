#!/usr/bin/env python3
"""
KODE OS Public API Test Script (Config File Version)
Tests authentication and basic API connectivity using Private Key JWT
Reads configuration from config.json file
"""

import json
import sys
from pathlib import Path
from kode_api_test import KodeAPIClient
import requests


def load_config(config_path="config.json"):
    """Load configuration from JSON file"""
    config_file = Path(config_path)

    if not config_file.exists():
        print(f"❌ Configuration file not found: {config_path}")
        print()
        print("Please create a config.json file based on config.example.json:")
        print("  1. Copy config.example.json to config.json")
        print("  2. Update the values with your service account credentials")
        print()
        print("Example:")
        print('  {')
        print('    "client_id": "your-service-account-client-id",')
        print('    "private_key_path": "path/to/your/private-key.pem",')
        print('    "base_url": "https://dsrus.kodelabs.com"')
        print('  }')
        return None

    try:
        with open(config_file, 'r') as f:
            config = json.load(f)

        # Validate required fields
        required_fields = ['client_id', 'private_key_path']
        missing_fields = [field for field in required_fields if field not in config]

        if missing_fields:
            print(f"❌ Missing required configuration fields: {', '.join(missing_fields)}")
            return None

        # Set default base_url if not provided
        if 'base_url' not in config:
            config['base_url'] = "https://api.kodelabs.com"

        return config

    except json.JSONDecodeError as e:
        print(f"❌ Error parsing config.json: {e}")
        return None


def main():
    """Main test function"""
    print("=" * 70)
    print("KODE OS Public API Test Script")
    print("=" * 70)
    print()

    # Load configuration
    config = load_config()
    if not config:
        sys.exit(1)

    CLIENT_ID = config['client_id']
    KEY_ID = config.get('key_id')
    PRIVATE_KEY_PATH = config['private_key_path']
    BASE_URL = config.get('base_url', 'https://api.kodelabs.com')

    print(f"📋 Configuration loaded:")
    print(f"   Client ID: {CLIENT_ID}")
    print(f"   Key ID: {KEY_ID if KEY_ID else '⚠️  NOT SET (Required!)'}")
    print(f"   Private Key: {PRIVATE_KEY_PATH}")
    print(f"   Base URL: {BASE_URL}")
    print()

    if not KEY_ID or KEY_ID == "YOUR_KEY_ID_HERE":
        print("⚠️  WARNING: key_id is not set in config.json!")
        print("   The Key ID (kid) is REQUIRED for authentication.")
        print("   Find it in your KODE OS Service Account details under Credentials.")
        print()

    try:
        # Initialize client
        client = KodeAPIClient(CLIENT_ID, PRIVATE_KEY_PATH, KEY_ID, BASE_URL)

        # Test 1: Authentication
        print("=" * 70)
        print("TEST 1: Authentication")
        print("=" * 70)
        client.authenticate()

        # Test 2: List Buildings
        print("\n" + "=" * 70)
        print("TEST 2: List Buildings")
        print("=" * 70)
        buildings = client.list_buildings()
        print(f"✅ Found {len(buildings.get('data', []))} building(s)")

        if buildings.get('data'):
            print("\nBuildings:")
            for building in buildings['data'][:10]:  # Show first 10
                print(f"  • {building.get('name', 'N/A')} (ID: {building.get('_id', 'N/A')})")

            # Test 3: List Devices for first building
            first_building = buildings['data'][0]
            first_building_id = first_building['_id']
            print("\n" + "=" * 70)
            print(f"TEST 3: List Devices for Building: {first_building.get('name', 'N/A')}")
            print("=" * 70)
            devices = client.list_devices(first_building_id)
            print(f"✅ Found {len(devices.get('data', []))} device(s)")

            if devices.get('data'):
                print("\nDevices (first 10):")
                for device in devices['data'][:10]:
                    canonical_type = device.get('ontology', {}).get('canonicalType', 'N/A')
                    print(f"  • {device.get('name', 'N/A')} (Type: {canonical_type}, ID: {device.get('_id', 'N/A')})")

                # Test 4: List Points for first device
                first_device = devices['data'][0]
                first_device_id = first_device['_id']
                print("\n" + "=" * 70)
                print(f"TEST 4: List Points for Device: {first_device.get('name', 'N/A')}")
                print("=" * 70)
                points = client.list_points(first_building_id, device_id=first_device_id, limit=10)
                print(f"✅ Found {len(points.get('data', []))} point(s)")

                if points.get('data'):
                    print("\nPoints (first 10):")
                    for point in points['data'][:10]:
                        point_type = point.get('ontology', {}).get('type', 'N/A')
                        kind = point.get('kind', 'N/A')
                        print(f"  • {point.get('name', 'N/A')} (Type: {point_type}, Kind: {kind})")
            else:
                print("\n⚠️  No devices found in the first building")

        else:
            print("\n⚠️  No buildings found. Check your service account permissions.")

        # Summary
        print("\n" + "=" * 70)
        print("✅ ALL TESTS PASSED!")
        print("=" * 70)
        print("\n🎉 API connectivity is working correctly!")
        print("   You can now integrate this with your Niagara module.")

    except FileNotFoundError as e:
        print(f"\n❌ ERROR: Private key file not found")
        print(f"   {e}")
        print(f"\n   Please check that the private_key_path in config.json is correct")
    except requests.exceptions.HTTPError as e:
        print(f"\n❌ HTTP ERROR: {e}")
        if hasattr(e.response, 'text'):
            print(f"   Response: {e.response.text}")
        print(f"\n   Common issues:")
        print(f"   - Check that your client_id is correct")
        print(f"   - Verify your private key matches the public key in KODE OS")
        print(f"   - Ensure your service account has proper permissions")
    except Exception as e:
        print(f"\n❌ ERROR: {e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()
