#!/usr/bin/env python3
"""
KODE OS Public API Test Script
Tests authentication and basic API connectivity using Private Key JWT
"""

import json
import time
import uuid
from datetime import datetime, timedelta
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.backends import default_backend
import jwt
import requests


class KodeAPIClient:
    """Client for KODE OS Public API using Private Key JWT authentication"""

    def __init__(self, client_id, private_key_path, key_id=None, base_url="https://api.kodelabs.com"):
        """
        Initialize the KODE API client

        Args:
            client_id: Service account client ID
            private_key_path: Path to the private key PEM file
            key_id: Key ID (kid) for JWT header - REQUIRED for authentication
            base_url: Base URL for the API (default: https://api.kodelabs.com)
        """
        self.client_id = client_id
        self.private_key_path = private_key_path
        self.key_id = key_id
        self.base_url = base_url
        self.access_token = None
        self.token_expiry = None

    def load_private_key(self):
        """Load the private key from file"""
        with open(self.private_key_path, 'rb') as key_file:
            key_data = key_file.read()

        # Try to load as PKCS#8 format first (most common)
        try:
            private_key = serialization.load_pem_private_key(
                key_data,
                password=None,
                backend=default_backend()
            )
            return private_key
        except Exception as e:
            # If that fails, try legacy RSA format (PKCS#1)
            try:
                from cryptography.hazmat.primitives.asymmetric import rsa
                private_key = serialization.load_pem_private_key(
                    key_data,
                    password=None,
                    backend=default_backend()
                )
                return private_key
            except Exception:
                # Re-raise original error with more context
                raise Exception(f"Failed to load private key. Original error: {e}\nPlease ensure the key is in PEM format and is not password-protected.")

    def get_oauth_endpoints(self):
        """Get OAuth2 discovery endpoints"""
        discovery_url = f"{self.base_url}/oauth2/v1/.well-known/oauth-authorization-server"
        response = requests.get(discovery_url)
        response.raise_for_status()
        return response.json()

    def create_jwt_assertion(self, token_endpoint):
        """
        Create JWT assertion for authentication

        Args:
            token_endpoint: The OAuth2 token endpoint URL

        Returns:
            Signed JWT assertion string
        """
        private_key = self.load_private_key()

        now = int(time.time())
        expiry = now + 3600  # 1 hour from now

        # JWT Header
        headers = {
            "alg": "RS512",
            "typ": "JWT"
        }

        # Add kid (Key ID) if provided - REQUIRED by KODE API
        if self.key_id:
            headers["kid"] = self.key_id

        # JWT Payload
        payload = {
            "iss": self.client_id,  # Issuer: Client ID
            "sub": self.client_id,  # Subject: Client ID
            "aud": token_endpoint,  # Audience: Token endpoint
            "exp": expiry,          # Expiration time
            "iat": now,             # Issued at
            "jti": str(uuid.uuid4()) # Unique token identifier
        }

        # Sign the JWT with RS512
        token = jwt.encode(
            payload,
            private_key,
            algorithm="RS512",
            headers=headers
        )

        return token

    def authenticate(self):
        """Authenticate and get access token"""
        print("🔐 Authenticating with KODE OS API...")

        # Get OAuth endpoints
        print("   Fetching OAuth2 discovery endpoints...")
        endpoints = self.get_oauth_endpoints()
        token_endpoint = endpoints['token_endpoint']
        print(f"   Token endpoint: {token_endpoint}")

        # Create JWT assertion
        print("   Creating JWT assertion...")
        jwt_assertion = self.create_jwt_assertion(token_endpoint)

        # Request access token
        print("   Requesting access token...")
        data = {
            "grant_type": "client_credentials",
            "scope": "bms",
            "client_assertion_type": "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
            "client_assertion": jwt_assertion
        }

        response = requests.post(token_endpoint, data=data)
        response.raise_for_status()

        token_data = response.json()
        self.access_token = token_data['access_token']

        # Calculate token expiry (tokens are valid for 1 hour)
        self.token_expiry = datetime.now() + timedelta(seconds=token_data.get('expires_in', 3600))

        print(f"✅ Successfully authenticated! Token expires at {self.token_expiry.strftime('%Y-%m-%d %H:%M:%S')}")
        return self.access_token

    def get_headers(self):
        """Get headers with authentication token"""
        if not self.access_token or datetime.now() >= self.token_expiry:
            self.authenticate()

        return {
            "Authorization": f"Bearer {self.access_token}",
            "Accept": "application/json",
            "Content-Type": "application/json"
        }

    def list_buildings(self, page=1, limit=50):
        """
        List all buildings

        Args:
            page: Page number (default: 1)
            limit: Number of results per page (default: 50, max: 200)

        Returns:
            JSON response with buildings list
        """
        url = f"{self.base_url}/kodeos/api/v1/buildings"
        params = {
            "page": page,
            "limit": limit
        }

        response = requests.get(url, headers=self.get_headers(), params=params)
        response.raise_for_status()
        return response.json()

    def list_devices(self, building_id, page=1, limit=50):
        """
        List devices for a building

        Args:
            building_id: Building ID
            page: Page number (default: 1)
            limit: Number of results per page (default: 50, max: 200)

        Returns:
            JSON response with devices list
        """
        url = f"{self.base_url}/kodeos/api/v1/buildings/{building_id}/devices"
        params = {
            "page": page,
            "limit": limit
        }

        response = requests.get(url, headers=self.get_headers(), params=params)
        response.raise_for_status()
        return response.json()

    def list_points(self, building_id, page=1, limit=50, device_id=None):
        """
        List points for a building

        Args:
            building_id: Building ID
            page: Page number (default: 1)
            limit: Number of results per page (default: 50, max: 200)
            device_id: Optional device ID filter

        Returns:
            JSON response with points list
        """
        url = f"{self.base_url}/kodeos/api/v1/buildings/{building_id}/points"
        params = {
            "page": page,
            "limit": limit
        }
        if device_id:
            params["deviceId"] = device_id

        response = requests.get(url, headers=self.get_headers(), params=params)
        response.raise_for_status()
        return response.json()

    def list_datasources(self, building_id, page=1, limit=50):
        """
        List datasources for a building

        Args:
            building_id: Building ID
            page: Page number (default: 1)
            limit: Number of results per page (default: 50, max: 200)

        Returns:
            JSON response with datasources list
        """
        url = f"{self.base_url}/kodeos/api/v1/buildings/{building_id}/integrations/datasources"
        params = {
            "page": page,
            "limit": limit
        }

        response = requests.get(url, headers=self.get_headers(), params=params)
        response.raise_for_status()
        return response.json()

    def create_device(self, building_id, datasource_id, device_data):
        """
        Create a device with points in KODE OS

        Args:
            building_id: Building ID
            datasource_id: Datasource ID
            device_data: Device data in KODE OS format with points

        Returns:
            JSON response from API
        """
        url = f"{self.base_url}/kodeos/api/v1/buildings/{building_id}/integrations/datasources/{datasource_id}/devices"

        response = requests.post(url, headers=self.get_headers(), json=device_data)

        # Enhanced error handling
        if not response.ok:
            print(f"DEBUG: Request URL: {url}")
            print(f"DEBUG: Status Code: {response.status_code}")
            print(f"DEBUG: Response: {response.text}")

        response.raise_for_status()
        return response.json()


def main():
    """Main test function"""
    print("=" * 70)
    print("KODE OS Public API Test Script")
    print("=" * 70)
    print()

    # Configuration - YOU NEED TO UPDATE THESE VALUES
    CLIENT_ID = "your-client-id-here"  # Replace with your service account client ID
    PRIVATE_KEY_PATH = "path/to/your/private-key.pem"  # Replace with path to your private key

    # Check if configuration is set
    if CLIENT_ID == "your-client-id-here" or PRIVATE_KEY_PATH == "path/to/your/private-key.pem":
        print("❌ ERROR: Please update CLIENT_ID and PRIVATE_KEY_PATH in the script")
        print()
        print("You need to:")
        print("1. Set CLIENT_ID to your service account client ID")
        print("2. Set PRIVATE_KEY_PATH to the path of your private key PEM file")
        return

    try:
        # Initialize client
        client = KodeAPIClient(CLIENT_ID, PRIVATE_KEY_PATH)

        # Test 1: Authentication
        print("\n" + "=" * 70)
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
            for building in buildings['data'][:5]:  # Show first 5
                print(f"  • {building.get('name', 'N/A')} (ID: {building.get('_id', 'N/A')})")

            # Test 3: List Devices for first building
            first_building_id = buildings['data'][0]['_id']
            print("\n" + "=" * 70)
            print(f"TEST 3: List Devices for Building: {buildings['data'][0].get('name', 'N/A')}")
            print("=" * 70)
            devices = client.list_devices(first_building_id)
            print(f"✅ Found {len(devices.get('data', []))} device(s)")

            if devices.get('data'):
                print("\nDevices (first 5):")
                for device in devices['data'][:5]:
                    print(f"  • {device.get('name', 'N/A')} (ID: {device.get('_id', 'N/A')})")

                # Test 4: List Points for first device
                first_device_id = devices['data'][0]['_id']
                print("\n" + "=" * 70)
                print(f"TEST 4: List Points for Device: {devices['data'][0].get('name', 'N/A')}")
                print("=" * 70)
                points = client.list_points(first_building_id, device_id=first_device_id)
                print(f"✅ Found {len(points.get('data', []))} point(s)")

                if points.get('data'):
                    print("\nPoints (first 5):")
                    for point in points['data'][:5]:
                        print(f"  • {point.get('name', 'N/A')} (ID: {point.get('_id', 'N/A')})")

        # Summary
        print("\n" + "=" * 70)
        print("✅ ALL TESTS PASSED!")
        print("=" * 70)
        print("\nAPI connectivity is working correctly. You can now integrate with Niagara!")

    except FileNotFoundError as e:
        print(f"❌ ERROR: Private key file not found: {e}")
    except requests.exceptions.HTTPError as e:
        print(f"❌ HTTP ERROR: {e}")
        if hasattr(e.response, 'text'):
            print(f"Response: {e.response.text}")
    except Exception as e:
        print(f"❌ ERROR: {e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()
