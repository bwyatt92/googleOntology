# Datasource IDs for KODE OS Buildings

**Generated**: 2025-11-06

## test_api Building

- **Building Name**: test_api
- **Building ID**: `690d054f3e4a4f66f2bf2dd6`
- **Datasource Name**: Beau's Virtual Building
- **Datasource ID**: `690d074c5e543310004e2c5a` ← **USE THIS**
- **Datasource Type**: API_KODE

## Office Building

- **Building Name**: Office
- **Building ID**: `690495f73e4a4f66f2bf195d`
- **Datasource Name**: JACE
- **Datasource ID**: `690914984c085f26b14c8e39` ← **USE THIS**
- **Datasource Type**: API_JACE

## Warehouse Building

- **Building Name**: Warehouse
- **Building ID**: `6903c1a86c822602dddb80d1`
- **Datasource Name**: DataSilo1
- **Datasource ID**: `6904b1dcd41e3b77d5581233` ← **USE THIS**
- **Datasource Type**: API_JACE

---

## What Was Wrong

When you ran the script, you entered the **building ID** instead of the **datasource ID**.

- ❌ You entered: `690d054f3e4a4f66f2bf2dd6` (building ID)
- ✅ You should enter: `690d074c5e543310004e2c5a` (datasource ID)

## To Run the Demo

```bash
python3 push_to_kode.py
```

1. Select building **test_api** (option 1)
2. When prompted for datasource ID, enter: `690d074c5e543310004e2c5a`
3. Watch the devices being created!

---

**Note**: You can verify datasources anytime by running `python3 check_datasources.py`
