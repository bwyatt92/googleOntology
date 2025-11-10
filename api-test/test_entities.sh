#!/bin/bash

# Test script to check entities and their types

NIAGARA_URL="http://localhost/googleOntology/v1"
NIAGARA_USER="curl"
NIAGARA_PASS="Admin12345!"

echo "=========================================="
echo "Fetching Entities from Niagara"
echo "=========================================="
echo ""

# Get all entities
curl -s -u ${NIAGARA_USER}:${NIAGARA_PASS} ${NIAGARA_URL}/entities | python3 -m json.tool

echo ""
echo "=========================================="
echo "Summary by Entity Type"
echo "=========================================="
echo ""

# Get entities and count by type
curl -s -u ${NIAGARA_USER}:${NIAGARA_PASS} ${NIAGARA_URL}/entities | \
  python3 -c "
import sys, json
data = json.load(sys.stdin)
entities = data.get('entities', [])

# Count by type
type_counts = {}
for e in entities:
    etype = e.get('entityType', 'UNKNOWN')
    type_counts[etype] = type_counts.get(etype, 0) + 1

print('Entity Type Distribution:')
for etype, count in sorted(type_counts.items()):
    print(f'  {etype}: {count}')

print(f'\nTotal: {len(entities)} entities')

# Show RTUs specifically
rtus = [e for e in entities if e.get('entityType') == 'RTU']
if rtus:
    print(f'\nRTUs found ({len(rtus)}):')
    for rtu in rtus:
        print(f'  - {rtu.get(\"name\")} (id: {rtu.get(\"id\")}, points: {rtu.get(\"numPoints\", 0)})')
else:
    print('\nNo RTUs found.')
    print('\nEntities that might be RTUs (containing \"rtu\" in name):')
    for e in entities:
        if 'rtu' in e.get('name', '').lower():
            print(f'  - {e.get(\"name\")} -> {e.get(\"entityType\")}')
"
