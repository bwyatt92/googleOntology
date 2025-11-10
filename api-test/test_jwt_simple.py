#!/usr/bin/env python3
"""Simple JWT test using jwt library directly"""

import jwt
import time
import uuid

# Read the private key
with open('pk_fixed.pem', 'rb') as f:
    private_key = f.read()

# Create JWT payload
payload = {
    "iss": "690cf217a117992939ae8d10",
    "sub": "690cf217a117992939ae8d10",
    "aud": "https://api.kodelabs.com/oauth2/v1/token",
    "exp": int(time.time()) + 3600,
    "iat": int(time.time()),
    "jti": str(uuid.uuid4())
}

print("Attempting to create JWT...")
print(f"Payload: {payload}")

try:
    # Try to encode the JWT
    token = jwt.encode(payload, private_key, algorithm="RS512")
    print(f"\n✅ SUCCESS! JWT created:")
    print(f"{token[:100]}...")
    print(f"\nToken length: {len(token)}")

    # Try to decode it to verify
    decoded = jwt.decode(token, options={"verify_signature": False})
    print(f"\n✅ Decoded payload: {decoded}")

except Exception as e:
    print(f"\n❌ ERROR: {e}")
    import traceback
    traceback.print_exc()
