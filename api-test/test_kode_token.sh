#!/bin/bash

# Test KODE Labs API token

if [ -z "$ACCESS_TOKEN" ]; then
    echo "ERROR: ACCESS_TOKEN environment variable not set"
    echo "Run: export ACCESS_TOKEN=\$(cat access_token.txt)"
    exit 1
fi

BUILDING_ID="6903c1a86c822602dddb80d1"
DATASOURCE_ID="690d074c5e543310004e2c5a"

echo "=========================================="
echo "Testing KODE Labs API Access"
echo "=========================================="
echo ""

# Test 1: Get building info
echo "[1/3] Testing building access..."
RESPONSE=$(curl -s -w "\n%{http_code}" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Accept: application/json" \
  -H "User-Agent: Niagara-GoogleOntology/1.0" \
  "https://api.kodelabs.com/kodeos/api/v1/buildings/${BUILDING_ID}")

HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    echo "✓ Building access OK (HTTP $HTTP_CODE)"
else
    echo "✗ Building access FAILED (HTTP $HTTP_CODE)"
    echo "Response: $BODY"
fi
echo ""

# Test 2: List devices in datasource
echo "[2/3] Testing datasource access..."
RESPONSE=$(curl -s -w "\n%{http_code}" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Accept: application/json" \
  -H "User-Agent: Niagara-GoogleOntology/1.0" \
  "https://api.kodelabs.com/kodeos/api/v1/buildings/${BUILDING_ID}/integrations/datasources/${DATASOURCE_ID}/devices?limit=1")

HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    echo "✓ Datasource access OK (HTTP $HTTP_CODE)"
else
    echo "✗ Datasource access FAILED (HTTP $HTTP_CODE)"
    echo "Response: $BODY"
fi
echo ""

# Test 3: Test batch endpoint with minimal payload
echo "[3/3] Testing batch device creation..."
TEST_PAYLOAD='{
  "devices": [{
    "id": "test_device_001",
    "name": "Test Device",
    "displayName": "Test Device",
    "points": [{
      "kind": "Number",
      "name": "Test Point",
      "pointId": "test_001"
    }]
  }]
}'

RESPONSE=$(curl -s -w "\n%{http_code}" \
  -X POST \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "User-Agent: Niagara-GoogleOntology/1.0" \
  -d "$TEST_PAYLOAD" \
  "https://api.kodelabs.com/kodeos/api/v1/buildings/${BUILDING_ID}/integrations/datasources/${DATASOURCE_ID}/devices/batch")

HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ]; then
    echo "✓ Batch endpoint OK (HTTP $HTTP_CODE)"
    echo "Response: $BODY"
elif [ "$HTTP_CODE" = "403" ]; then
    echo "✗ Batch endpoint FORBIDDEN (HTTP $HTTP_CODE)"
    echo "Response: $BODY"
    echo ""
    echo "Possible causes:"
    echo "  - Token expired (tokens expire after 1 hour)"
    echo "  - Insufficient permissions for this datasource"
    echo "  - IP address blocked by Cloudflare"
else
    echo "✗ Batch endpoint FAILED (HTTP $HTTP_CODE)"
    echo "Response: $BODY"
fi
echo ""

echo "=========================================="
echo "Testing Complete"
echo "=========================================="
