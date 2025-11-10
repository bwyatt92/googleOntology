# Quick Setup Guide

Follow these steps to test your KODE OS API connectivity:

## Step 1: Install Python Dependencies

```bash
cd api-test
pip install -r requirements.txt
```

## Step 2: Create Configuration File

1. Copy the example config:
   ```bash
   cp config.example.json config.json
   ```

2. Edit `config.json` with your credentials:
   ```json
   {
     "client_id": "your-actual-client-id",
     "private_key_path": "./private-key.pem",
     "base_url": "https://api.kodelabs.com"
   }
   ```

## Step 3: Place Your Private Key

Copy your private key PEM file to the api-test directory:
```bash
cp /path/to/your/downloaded/private-key.pem ./private-key.pem
```

## Step 4: Run the Test

```bash
python kode_api_test_config.py
```

## What You Should See

If everything is set up correctly:

```
======================================================================
KODE OS Public API Test Script
======================================================================

📋 Configuration loaded:
   Client ID: your-client-id
   Private Key: ./private-key.pem
   Base URL: https://api.kodelabs.com

======================================================================
TEST 1: Authentication
======================================================================
🔐 Authenticating with KODE OS API...
   Fetching OAuth2 discovery endpoints...
   Token endpoint: https://api.kodelabs.com/oauth2/v1/token
   Creating JWT assertion...
   Requesting access token...
✅ Successfully authenticated! Token expires at 2025-11-06 12:34:56

======================================================================
TEST 2: List Buildings
======================================================================
✅ Found 5 building(s)
...

======================================================================
✅ ALL TESTS PASSED!
======================================================================

🎉 API connectivity is working correctly!
   You can now integrate this with your Niagara module.
```

## Troubleshooting

### Issue: "Private key file not found"
- Check that the `private_key_path` in `config.json` points to the correct file
- Use relative paths like `./private-key.pem` or absolute paths

### Issue: "401 Unauthorized" or "Invalid JWT"
- Verify your `client_id` is correct (copy it from KODE OS Service Account)
- Ensure the private key matches the public key you uploaded to KODE OS
- Check that you downloaded the private key (not the public key)

### Issue: "No buildings found"
- Your service account may not have permissions to any buildings
- Contact your KODE OS administrator to grant building access

### Issue: Module import errors
- Make sure you're in the `api-test` directory
- Run both test scripts from the same directory
- Verify all dependencies are installed: `pip list | grep -E "(requests|PyJWT|cryptography)"`

## Next Steps

Once the test passes:

1. Review the Python implementation in `kode_api_test.py`
2. Use the JWT creation logic as a reference for Java implementation
3. Implement similar authentication in your Niagara module
4. Start with basic API calls (list buildings) before moving to complex operations

## Security Notes

- **NEVER** commit `config.json` or `*.pem` files to git
- These files are already in `.gitignore`
- Keep your private key secure - treat it like a password
- Rotate your keys periodically for security
