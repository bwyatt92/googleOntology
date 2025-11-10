# How to Create a Datasource in KODE OS

## Problem

You need a valid datasource ID before you can create devices in KODE OS.

**What went wrong**: You entered the building ID (`690d054f3e4a4f66f2bf2dd6`) as the datasource ID. These are different things!

## Solution: Create a Datasource in KODE OS UI

### Step 1: Log in to KODE OS

Go to: https://dsrus.kodelabs.com

### Step 2: Navigate to Your Building

1. Select the **test_api** building (or whichever building you want to use)

### Step 3: Create a Datasource/Integration

1. Look for **Integrations** or **Data Sources** section
2. Click **Add Integration** or **New Datasource**
3. Fill in the details:
   - **Name**: `NiagaraSync` (or any name you prefer)
   - **Type**: Select **API** or **KODE API** (not BACnet, Modbus, etc.)
   - **API Type**: If asked, select **API_JACE** or **API**

### Step 4: Copy the Datasource ID

After creating the datasource:
1. The system will display the datasource ID (looks like `690914984c085f26b14c8e39`)
2. Copy this ID

### Step 5: Run the Script Again

```bash
python3 push_to_kode.py
```

When prompted for datasource ID, paste the actual datasource ID (not the building ID).

## Alternative: Use Existing Datasource

If you want to test with the **Office** building which already has a datasource:

1. Run the script: `python3 push_to_kode.py`
2. When asked to select a building, choose **Office** (option 2)
3. The script will find the existing datasource: `690914984c085f26b14c8e39`
4. Devices will be created there

## Verify Datasource Exists

You can verify datasources exist by running:

```bash
python3 check_datasources.py
```

This will show you all datasources for all buildings.
