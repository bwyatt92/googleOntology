# Finding Your Key ID (kid) in KODE OS

The Key ID (`kid`) is **REQUIRED** for JWT authentication with the KODE OS Public API.

## Where to Find It

1. **Log in to KODE OS** (https://dsrus.kodelabs.com or your environment)

2. **Navigate to Service Accounts:**
   - Go to Settings → Service Accounts
   - Or go directly to your service account page

3. **Open Your Service Account:**
   - Click on the service account you created
   - Client ID should be: `690cf217a117992939ae8d10`

4. **Find the Credentials Section:**
   - Look for a "Credentials" table or section
   - You should see your public key listed there

5. **Locate the Key ID:**
   - There should be a "Key" field or "Key ID" field
   - This is usually a string of characters (could be similar to the Client ID format)
   - Copy this value

## What It Looks Like

The Key ID format may vary, but it typically looks like one of these:
- `690cf217a117992939ae8d11` (similar to Client ID format)
- `key-1234567890abcdef`
- `rsa-key-2024-01`

## Adding It to Your Config

Once you have the Key ID, edit `config.json`:

```json
{
  "client_id": "690cf217a117992939ae8d10",
  "key_id": "YOUR_ACTUAL_KEY_ID_HERE",     ← Put it here!
  "private_key_path": "./pk_standard.pem",
  "base_url": "https://dsrus.kodelabs.com"
}
```

## Testing

After adding the Key ID, run the test again:

```bash
python3 kode_api_test_config.py
```

You should see:
```
📋 Configuration loaded:
   Client ID: 690cf217a117992939ae8d10
   Key ID: your-actual-key-id        ← Should show your key ID
   Private Key: ./pk_standard.pem
   Base URL: https://dsrus.kodelabs.com
```

## Still Having Issues?

If you can't find the Key ID:

1. **Check if it's the same as Client ID** - Sometimes they're the same value
2. **Look for "Public Key ID"** - It might be labeled differently
3. **Check the API documentation** - Your KODE OS admin may have docs
4. **Contact KODE Support** - They can tell you where to find it for your environment

## Why Is This Needed?

From the KODE OS API documentation:

> **JWT Header**
> - `kid`: Id of public key (Key field to Credentials table inside Service Account Details)

The `kid` tells the API which public key to use to verify your JWT signature. Without it, the API doesn't know which key corresponds to your private key signature.
