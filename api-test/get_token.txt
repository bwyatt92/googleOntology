import json
import jwt
import time
import requests
from pathlib import Path

# Load config
with open('config.json', 'r') as f:
    config = json.load(f)
CLIENT_ID = config['client_id']
KEY_ID = config['key_id']
PRIVATE_KEY_PATH = config['private_key_path']
# Read private key
with open(PRIVATE_KEY_PATH, 'r') as f:
    private_key = f.read()
# Token endpoint
TOKEN_ENDPOINT = "https://api.kodelabs.com/oauth2/v1/token"
# Create JWT
now = int(time.time())
jwt_payload = {
    "iss": CLIENT_ID,
    "sub": CLIENT_ID,
    "aud": TOKEN_ENDPOINT,
    "exp": now + 3600,
    "iat": now,
    "jti": str(time.time())  # unique identifier
}
jwt_token = jwt.encode(
    jwt_payload,
    private_key,
    algorithm='RS512',
    headers={'kid': KEY_ID}
)
# Exchange JWT for access token
response = requests.post(
    TOKEN_ENDPOINT,
    data={
        'grant_type': 'client_credentials',
        'scope': 'bms',
        'client_assertion_type': 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer',
        'client_assertion': jwt_token
    }
)
token_data = response.json()
print(json.dumps(token_data, indent=2))
# Save token for use with curl
access_token = token_data['access_token']
print(f"\n\nAccess Token:\n{access_token}")
# Save to file for easy use
with open('access_token.txt', 'w') as f:
    f.write(access_token)
print("\nToken saved to access_token.txt")