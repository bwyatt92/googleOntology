# KODE OS API Integration Plan for Niagara Module

## Overview

This document outlines the plan to integrate KODE OS Public API with the Google Ontology Niagara module to automate device and point mapping.

## Current Status

✅ **Phase 1: API Testing (Complete)**
- Python test client created
- Authentication with Private Key JWT implemented
- Basic API endpoints tested (buildings, devices, points)

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Niagara Station                           │
│  ┌────────────────────────────────────────────────────┐     │
│  │     Google Ontology Module (googleOntology-rt)     │     │
│  │                                                     │     │
│  │  ┌──────────────────────────────────────────┐     │     │
│  │  │  BGoogleOntologyService                  │     │     │
│  │  │  - Existing ontology logic               │     │     │
│  │  │  - Manual CSV import                     │     │     │
│  │  └──────────────────────────────────────────┘     │     │
│  │                      ▼                              │     │
│  │  ┌──────────────────────────────────────────┐     │     │
│  │  │  NEW: BKodeApiService                    │     │     │
│  │  │  - OAuth2 authentication                 │     │     │
│  │  │  - JWT token management                  │     │     │
│  │  │  - API client methods                    │     │     │
│  │  │  - Device/Point synchronization          │     │     │
│  │  └──────────────────────────────────────────┘     │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ HTTPS
                            ▼
                 ┌──────────────────────┐
                 │   KODE OS Public API  │
                 │  api.kodelabs.com     │
                 └──────────────────────┘
```

## Integration Phases

### Phase 2: Java API Client for Niagara (Next)

**Goal:** Create Java equivalent of Python test client that works within Niagara

**Tasks:**
1. Create `BKodeApiService` component
   - Extends `BComponent`
   - Properties: client_id, private_key_path, base_url
   - Actions: authenticate, sync, test connection

2. Implement JWT authentication in Java
   - Use Java JWT libraries (jjwt or similar)
   - RSA key loading and signing
   - Token management and refresh

3. Create API client methods
   - HTTP client (Java 11+ HttpClient or Apache HttpClient)
   - GET buildings, devices, points
   - POST for creating/updating devices

4. Add error handling and logging
   - Niagara logging integration
   - Rate limit handling
   - Connection retry logic

### Phase 3: Ontology Mapping

**Goal:** Map KODE OS data to Google ontology format

**Tasks:**
1. Create mapping service
   - KODE OS device types → Google ontology types
   - KODE OS point types → Google field names
   - Canonical type mapping

2. Implement transformation logic
   - Parse KODE OS ontology structure
   - Generate Google ontology compliant names
   - Handle special cases and conflicts

3. Create validation
   - Verify mappings are valid
   - Check for missing required fields
   - Report mapping issues

### Phase 4: Synchronization Engine

**Goal:** Automated sync between KODE OS and Niagara station

**Tasks:**
1. Create sync service
   - Pull devices/points from KODE OS API
   - Apply ontology mappings
   - Create/update Niagara points

2. Implement conflict resolution
   - Handle existing points
   - Merge vs. replace strategies
   - User preferences

3. Add scheduling
   - Periodic sync (hourly, daily)
   - Manual trigger action
   - Change detection

### Phase 5: UI Integration

**Goal:** User-friendly interface for configuration and monitoring

**Tasks:**
1. Create Property Sheet views
   - API configuration
   - Mapping rules editor
   - Sync status display

2. Add actions
   - "Test Connection"
   - "Sync Now"
   - "View Mapping Report"

3. Create views
   - Sync history
   - Error logs
   - Mapping preview

## Technical Considerations

### Java Libraries Needed

1. **JWT & Crypto:**
   - `io.jsonwebtoken:jjwt-api:0.12.3`
   - `io.jsonwebtoken:jjwt-impl:0.12.3`
   - `io.jsonwebtoken:jjwt-jackson:0.12.3`
   - Or use Bouncy Castle for RSA operations

2. **HTTP Client:**
   - Java 11+ `java.net.http.HttpClient` (built-in)
   - Or Apache HttpClient 5.x

3. **JSON Parsing:**
   - Jackson (already in Niagara)
   - Or javax.json (already in Niagara)

### Module Structure

```
googleOntology-rt/
├── src/
│   └── com/tridium/codeSamples/googleOntology/
│       ├── BGoogleOntologyService.java (existing)
│       ├── api/
│       │   ├── BKodeApiService.java (new)
│       │   ├── KodeApiClient.java (new)
│       │   ├── JwtAuthenticator.java (new)
│       │   └── models/
│       │       ├── Building.java
│       │       ├── Device.java
│       │       └── Point.java
│       ├── mapping/
│       │   ├── OntologyMapper.java (new)
│       │   ├── DeviceTypeMapper.java (new)
│       │   └── PointTypeMapper.java (new)
│       └── sync/
│           ├── SyncEngine.java (new)
│           └── SyncScheduler.java (new)
```

### Configuration Properties

The `BKodeApiService` component will need these properties:

```java
// API Configuration
@Property(defaultValue="https://api.kodelabs.com")
private BString baseUrl;

@Property
private BString clientId;

@Property(flags=Flags.HIDDEN) // Sensitive
private BString privateKeyPath;

// Sync Configuration
@Property(defaultValue="3600000") // 1 hour
private BLong syncIntervalMs;

@Property(defaultValue="true")
private BBoolean autoSync;

// Status (read-only)
@Property(flags=Flags.READONLY)
private BString lastSyncTime;

@Property(flags=Flags.READONLY)
private BString connectionStatus;
```

### Security Considerations

1. **Private Key Storage:**
   - Store in Niagara's secure credential store
   - Never log private key contents
   - Use file permissions to protect key file

2. **Token Management:**
   - Store tokens in memory only
   - Automatic refresh before expiry
   - Clear tokens on service stop

3. **API Rate Limits:**
   - Implement request throttling
   - Queue requests when approaching limits
   - Exponential backoff on 429 errors

## Testing Strategy

### Unit Tests
- JWT creation and signing
- API response parsing
- Ontology mapping logic

### Integration Tests
- API authentication flow
- Building/device/point retrieval
- Sync process end-to-end

### Manual Tests
- Test with real KODE OS environment
- Verify point creation in Niagara
- Check error handling

## Success Criteria

✅ Phase 2 Complete:
- [ ] Java API client authenticates successfully
- [ ] Can retrieve buildings, devices, points
- [ ] Component loads in Niagara without errors

✅ Phase 3 Complete:
- [ ] KODE OS devices map to Google ontology
- [ ] Point types correctly identified
- [ ] Mapping rules are configurable

✅ Phase 4 Complete:
- [ ] Automatic sync creates correct Niagara points
- [ ] Sync can be scheduled or triggered manually
- [ ] Handles errors gracefully

✅ Phase 5 Complete:
- [ ] Property sheets are user-friendly
- [ ] Status information is clear
- [ ] Actions work as expected

## Timeline Estimate

- **Phase 2:** 2-3 days (Java API client)
- **Phase 3:** 1-2 days (Ontology mapping)
- **Phase 4:** 2-3 days (Sync engine)
- **Phase 5:** 1-2 days (UI integration)

**Total:** ~1-2 weeks of development time

## Next Immediate Steps

1. ✅ Test Python API client with your credentials
2. Review test output and verify data structure
3. Start Phase 2: Create basic `BKodeApiService` component
4. Implement JWT authentication in Java
5. Test authentication from Niagara

## Resources

- **Python Test Client:** `api-test/kode_api_test.py`
- **API Documentation:** `KODE OS Public API.pdf`
- **Current Module:** `googleOntology-rt/`
- **Setup Guide:** `api-test/SETUP.md`
