#!/usr/bin/env python3
"""Debug JWT creation"""

import jwt
import time
import uuid
import json
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.backends import default_backend

# Load the private key
with open('pk_standard.pem', 'rb') as f:
    private_key = serialization.load_pem_private_key(
        f.read(),
        password=None,
        backend=default_backend()
    )

print("✅ Private key loaded successfully")
print(f"Key size: {private_key.key_size} bits")

# Create JWT payload
now = int(time.time())
payload = {
    "iss": "690cf217a117992939ae8d10",
    "sub": "690cf217a117992939ae8d10",
    "aud": "https://api.kodelabs.com/oauth2/v1/token",
    "exp": now + 3600,
    "iat": now,
    "jti": str(uuid.uuid4())
}

print("\nJWT Payload:")
print(json.dumps(payload, indent=2))

# Create JWT with RS512
headers = {
    "alg": "RS512",
    "typ": "JWT"
}

print("\nJWT Header:")
print(json.dumps(headers, indent=2))

try:
    token = jwt.encode(payload, private_key, algorithm="RS512", headers=headers)
    print(f"\n✅ JWT created successfully!")
    print(f"Token length: {len(token)} characters")
    print(f"\nToken: {token[:100]}...")
    print(f"       ...{token[-100:]}")

    # Decode without verification to see what we created
    decoded_header = jwt.get_unverified_header(token)
    decoded_payload = jwt.decode(token, options={"verify_signature": False})

    print("\n✅ Decoded JWT header:")
    print(json.dumps(decoded_header, indent=2))

    print("\n✅ Decoded JWT payload:")
    print(json.dumps(decoded_payload, indent=2))

except Exception as e:
    print(f"\n❌ ERROR creating JWT: {e}")
    import traceback
    traceback.print_exc()
