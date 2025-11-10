#!/usr/bin/env python3
"""
Niagara to KODE Labs Integration Script

Fetches DBO-mapped devices/points from Niagara Google Ontology module
and creates them in KODE Labs with ontology tags using the Public API.
"""
import requests
import json
import os
import sys
from datetime import datetime

# ============================================================================
# CONFIGURATION
# ============================================================================

# Niagara Station
NIAGARA_HOST = "localhost:8080"
NIAGARA_USER = "admin"
NIAGARA_PASS = "admin"
NIAGARA_BASE_URL = f"http://{NIAGARA_HOST}/googleOntology/v1"

# KODE Labs
KODE_BUILDING_ID = "6903c1a86c822602dddb80d1"
KODE_DATASOURCE_ID = "690d074c5e543310004e2c5a"
KODE_BASE_URL = "https://api.kodelabs.com/kodeos/api/v1"
KODE_ACCESS_TOKEN = os.getenv('ACCESS_TOKEN')

# Mapping configuration
MIN_CONFIDENCE = 70  # Only include points with confidence >= this
ENTITY_TYPE_FILTER = ["AHU", "VAV", "FCU", "CH", "BLR"]  # Filter to specific types

# ============================================================================
# NIAGARA API CLIENT
# ============================================================================

class NiagaraClient:
    """Client for Niagara Google Ontology API"""

    def __init__(self, base_url, username, password):
        self.base_url = base_url
        self.auth = (username, password)

    def get_entities(self):
        """Get all entities from Niagara"""
        url = f"{self.base_url}/entities"
        response = requests.get(url, auth=self.auth)
        response.raise_for_status()
        return response.json()['entities']

    def get_entity_fields(self, entity_id):
        """Get fields (points) for an entity"""
        url = f"{self.base_url}/entity/{entity_id}/fields"
        response = requests.get(url, auth=self.auth)
        response.raise_for_status()
        return response.json()

# ============================================================================
# KODE LABS API CLIENT
# ============================================================================

class KodeLabsClient:
    """Client for KODE Labs Public API"""

    def __init__(self, base_url, access_token, building_id, datasource_id):
        self.base_url = base_url
        self.building_id = building_id
        self.datasource_id = datasource_id
        self.headers = {
            "Authorization": f"Bearer {access_token}",
            "Content-Type": "application/json",
            "accept": "application/json"
        }

    def create_device(self, device_data):
        """Create a single device"""
        url = f"{self.base_url}/buildings/{self.building_id}/integrations/datasources/{self.datasource_id}/devices"

        response = requests.post(url, headers=self.headers, json=device_data)
        return response

    def create_devices_batch(self, devices_list):
        """Create multiple devices in batch"""
        url = f"{self.base_url}/buildings/{self.building_id}/integrations/datasources/{self.datasource_id}/devices/batch"

        payload = {"devices": devices_list}
        response = requests.post(url, headers=self.headers, json=payload)
        return response

# ============================================================================
# DATA TRANSFORMATION
# ============================================================================

def map_dbo_to_kode_canonical(dbo_field):
    """
    Map Google DBO field to KODE Labs canonical type

    For now, we'll just use the DBO field as-is since they're similar.
    In the future, this could have custom mappings if needed.
    """
    # Direct mapping - DBO and KODE use similar naming
    return dbo_field.replace("_", "-")

def transform_entity_to_kode_device(niagara_entity, niagara_fields, min_confidence=70):
    """
    Transform a Niagara entity to KODE Labs device format

    Returns device object ready for KODE Labs API, or None if filtered out
    """
    entity_id = niagara_entity['id']
    entity_name = niagara_entity['name']
    entity_type = niagara_entity['entityType']

    # Filter out generic equipment
    if entity_type == "EQUIPMENT":
        return None

    # Build device object
    device = {
        "id": f"niagara_{entity_id}",
        "name": entity_name,
        "displayName": entity_name,
        "points": []
    }

    # Add points
    for field in niagara_fields['fields']:
        confidence = field.get('confidence', 0)

        # Skip low-confidence matches
        if confidence < min_confidence:
            continue

        point = {
            "kind": infer_kind_from_dbo(field['dboField']),
            "name": field['originalName'],
            "pointId": f"{entity_id}_{field['addr']}",
        }

        # Add units if present
        if field.get('units'):
            point['unit'] = field['units']

        # Note: We can't add ontology via public API (yet)
        # But we're structuring the data to be ready when we can

        device['points'].append(point)

    # Skip devices with no points
    if len(device['points']) == 0:
        return None

    return device

def infer_kind_from_dbo(dbo_field):
    """Infer point kind (Bool, Str, Number) from DBO field name"""
    lower = dbo_field.lower()

    # Boolean indicators
    if any(x in lower for x in ['status', 'enable', 'alarm', 'fault', 'run']):
        return "Bool"

    # String/Enum indicators
    if any(x in lower for x in ['mode', 'state']):
        return "Str"

    # Default to Number
    return "Number"

# ============================================================================
# MAIN WORKFLOW
# ============================================================================

def sync_niagara_to_kode(dry_run=False, batch_mode=True, entity_filter=None):
    """
    Sync Niagara entities to KODE Labs

    Args:
        dry_run: If True, only print what would be done
        batch_mode: If True, use batch API; if False, create one at a time
        entity_filter: List of entity types to include (e.g., ["AHU", "VAV"])
    """
    print("="*70)
    print("NIAGARA → KODE LABS SYNC")
    print("="*70)

    # Initialize clients
    niagara = NiagaraClient(NIAGARA_BASE_URL, NIAGARA_USER, NIAGARA_PASS)
    kode = KodeLabsClient(KODE_BASE_URL, KODE_ACCESS_TOKEN, KODE_BUILDING_ID, KODE_DATASOURCE_ID)

    # Step 1: Get entities from Niagara
    print("\n[1/4] Fetching entities from Niagara...")
    entities = niagara.get_entities()
    print(f"✓ Found {len(entities)} entities")

    # Filter entities
    if entity_filter:
        entities = [e for e in entities if e['entityType'] in entity_filter]
        print(f"✓ Filtered to {len(entities)} entities of types: {entity_filter}")

    # Step 2: Transform to KODE format
    print("\n[2/4] Transforming entities to KODE Labs format...")
    kode_devices = []

    for entity in entities:
        entity_id = entity['id']
        entity_name = entity['name']
        entity_type = entity['entityType']

        print(f"\n  Processing: {entity_name} ({entity_type})")

        # Get fields for this entity
        try:
            fields_data = niagara.get_entity_fields(entity_id)
            num_fields = len(fields_data.get('fields', []))
            print(f"    - {num_fields} points found")

            # Transform to KODE device
            device = transform_entity_to_kode_device(entity, fields_data, MIN_CONFIDENCE)

            if device:
                kode_devices.append(device)
                print(f"    ✓ Transformed: {len(device['points'])} points (confidence >= {MIN_CONFIDENCE})")
            else:
                print(f"    ⊘ Skipped (no high-confidence points or filtered out)")

        except Exception as e:
            print(f"    ✗ Error: {e}")

    print(f"\n✓ Transformed {len(kode_devices)} devices ready for KODE Labs")

    # Step 3: Preview
    print("\n[3/4] Preview of devices to create:")
    for device in kode_devices[:5]:  # Show first 5
        print(f"  - {device['name']}: {len(device['points'])} points")
    if len(kode_devices) > 5:
        print(f"  ... and {len(kode_devices) - 5} more")

    if dry_run:
        print("\n[DRY RUN] Not creating devices in KODE Labs")
        print("\nSample device payload:")
        print(json.dumps(kode_devices[0] if kode_devices else {}, indent=2))
        return

    # Step 4: Create in KODE Labs
    print("\n[4/4] Creating devices in KODE Labs...")

    if batch_mode and len(kode_devices) > 0:
        print(f"Using batch mode ({len(kode_devices)} devices)...")
        try:
            response = kode.create_devices_batch(kode_devices)

            if response.status_code in [200, 201]:
                print(f"✓ Batch created successfully!")
                print(f"Response: {response.json()}")
            else:
                print(f"✗ Batch creation failed: {response.status_code}")
                print(f"Error: {response.text}")
        except Exception as e:
            print(f"✗ Error during batch creation: {e}")
    else:
        # One at a time
        success_count = 0
        for device in kode_devices:
            try:
                response = kode.create_device(device)

                if response.status_code in [200, 201]:
                    print(f"  ✓ Created: {device['name']}")
                    success_count += 1
                else:
                    print(f"  ✗ Failed: {device['name']} ({response.status_code})")
                    print(f"    Error: {response.text[:100]}")
            except Exception as e:
                print(f"  ✗ Error creating {device['name']}: {e}")

        print(f"\n✓ Created {success_count}/{len(kode_devices)} devices")

    print("\n" + "="*70)
    print("SYNC COMPLETE")
    print("="*70)

# ============================================================================
# CLI
# ============================================================================

def main():
    """Main entry point"""

    # Check for access token
    if not KODE_ACCESS_TOKEN:
        print("ERROR: ACCESS_TOKEN environment variable not set!")
        print("\nRun:")
        print("  export ACCESS_TOKEN=$(cat access_token.txt)")
        print("or")
        print("  export ACCESS_TOKEN=$(python3 get_token.py)")
        sys.exit(1)

    # Parse arguments
    dry_run = "--dry-run" in sys.argv
    batch_mode = "--batch" in sys.argv or len(sys.argv) == 1  # Default to batch
    one_at_a_time = "--one-by-one" in sys.argv

    if one_at_a_time:
        batch_mode = False

    # Entity type filter
    entity_filter = ENTITY_TYPE_FILTER
    if "--all" in sys.argv:
        entity_filter = None

    print(f"\nConfiguration:")
    print(f"  Niagara: {NIAGARA_BASE_URL}")
    print(f"  KODE Building: {KODE_BUILDING_ID}")
    print(f"  KODE Datasource: {KODE_DATASOURCE_ID}")
    print(f"  Min Confidence: {MIN_CONFIDENCE}")
    print(f"  Entity Filter: {entity_filter if entity_filter else 'All'}")
    print(f"  Mode: {'DRY RUN' if dry_run else ('BATCH' if batch_mode else 'ONE-BY-ONE')}")

    if not dry_run:
        confirm = input("\nProceed? (y/n): ").strip().lower()
        if confirm != 'y':
            print("Aborted.")
            sys.exit(0)

    # Run sync
    sync_niagara_to_kode(
        dry_run=dry_run,
        batch_mode=batch_mode,
        entity_filter=entity_filter
    )

def print_usage():
    """Print usage information"""
    print("\nUsage: python niagara_to_kode.py [options]")
    print("\nOptions:")
    print("  --dry-run      Don't actually create devices, just show what would be done")
    print("  --batch        Use batch API (default)")
    print("  --one-by-one   Create devices one at a time")
    print("  --all          Include all entity types (default filters to AHU, VAV, etc.)")
    print("\nExamples:")
    print("  python niagara_to_kode.py --dry-run")
    print("  python niagara_to_kode.py --batch")
    print("  python niagara_to_kode.py --one-by-one --all")

if __name__ == "__main__":
    if "--help" in sys.argv or "-h" in sys.argv:
        print_usage()
    else:
        main()
