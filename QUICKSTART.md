# Quick Start Guide

Get the Google Ontology N4 module up and running in minutes.

## Installation

### 1. Copy Module to Niagara

```bash
# Copy the module to your Niagara installation
cp -r googleOntology <NIAGARA_HOME>/modules/
```

For Windows:
```
Copy-Item -Recurse googleOntology "C:\Niagara\Niagara-4.11\modules\"
```

### 2. Build the Module

1. Open **Niagara Workbench**
2. Go to **Tools > Module Manager**
3. Find `googleOntology` in the module list
4. Click **Build Module**
5. Wait for build to complete (check for success message)

### 3. Restart Your Station

Restart the station to load the new module.

## Configuration

### 1. Add the Service

1. Open your station in Workbench
2. Navigate to the **Services** folder
3. Right-click > **New**
4. Find **googleOntology > GoogleOntologyService**
5. Name it "GoogleOntologyService" and click OK

### 2. Configure Settings (Optional)

Select the service and view properties:
- **minConfidence** - Set to 70-85 for balanced results (default: 50)

### 3. Build the Index

1. Right-click on GoogleOntologyService
2. Select **Actions > rebuildIndex**
3. Check station logs for progress

You should see:
```
GoogleOntology reindex started...
GoogleOntology reindex complete [2s, 15 entities, 342 points scanned, 215 points matched]
GoogleOntologyService ready [version=1.0.0]
```

## Test the API

### 1. Get Module Info

```bash
curl http://localhost/googleOntology/v1/about -u admin:password
```

### 2. List Entities

```bash
curl http://localhost/googleOntology/v1/entities -u admin:password
```

### 3. Get Fields for an Entity

```bash
# Use an entity ID from the previous response
curl http://localhost/googleOntology/v1/entity/54d/fields -u admin:password
```

### 4. Get Current Values

```bash
curl http://localhost/googleOntology/v1/entity/54d/values -u admin:password
```

## What You'll See

### Entities Response
```json
{
  "entities": [
    {
      "id": "54d",
      "name": "AHU-1",
      "path": "/Drivers/BACnet/AHU-1",
      "entityType": "AHU",
      "numPoints": 25
    }
  ]
}
```

### Fields Response
```json
{
  "fields": [
    {
      "dboField": "supply_air_temperature_sensor",
      "originalName": "Supply Air Temp",
      "confidence": 95,
      "units": "°F"
    }
  ]
}
```

## Common Issues

### No entities appear
- **Check:** Do you have equipment/devices with points in your station?
- **Fix:** Make sure points are under folders/components (not floating)

### Low number of matched points
- **Check:** Your minConfidence threshold
- **Fix:** Lower minConfidence from 50 to 30-40 to see more matches

### Can't access HTTP endpoints
- **Check:** WebService is running
- **Fix:** Go to Services > WebService and make sure it's enabled
- **Check:** The servlet is registered
- **Fix:** Look in Services > WebService > Servlets for GoogleOntologyServlet

### Build fails
- **Check:** Module location is correct
- **Fix:** Ensure module is in `<NIAGARA_HOME>/modules/googleOntology/`
- **Check:** Java compiler version
- **Fix:** Make sure your JDK matches your Niagara version

## Next Steps

1. **Review Mappings** - Use the `/mappings` endpoint to see all confidence scores
2. **Adjust Confidence** - Fine-tune the `minConfidence` setting
3. **Integrate** - Use the API in your applications (see EXAMPLES.md)
4. **Customize** - Modify DboFieldLibrary.java to improve matching for your naming conventions

## Need Help?

- Check the full README.md for detailed documentation
- Review EXAMPLES.md for integration examples
- Check station logs for detailed error messages
- Look at the fuzzy matching confidence scores in `/mappings`

## Key Points to Remember

✅ **Rebuild the index** after adding/removing points
✅ **Check confidence scores** to validate mappings
✅ **Use display names** that include DBO keywords for better matching
✅ **Monitor station logs** during rebuild for issues

Enjoy mapping your building to the Google Digital Buildings Ontology!
