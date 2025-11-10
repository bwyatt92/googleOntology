# Changes - Gradle Build System

## What Changed

The Gradle build configuration has been updated to use **Tridium's official Niagara Gradle plugins**, matching how Tridium and other professional Niagara developers build modules.

## New Structure

```
googleOntology/                       # Root project (NEW)
├── settings.gradle.kts               # Configures Tridium plugins (NEW)
├── build.gradle.kts                  # Root build script (NEW)
├── gradle.properties                 # Set niagara_home here (UPDATED)
├── GRADLE_BUILD_GUIDE.md            # New comprehensive guide (NEW)
│
└── googleOntology-rt/                # Module subproject
    ├── build.gradle.kts              # Uses Tridium plugins (REWRITTEN)
    ├── gradle.properties             # Module metadata (SIMPLIFIED)
    └── src/                          # Java sources (unchanged)
```

## Key Improvements

### Before (Manual JAR References)
```kotlin
dependencies {
    compileOnly(fileTree("$niagaraHome/modules") {
        include("**/baja-rt.jar")
        include("**/web-rt.jar")
        // ...
    })
}
```

**Problems:**
- ❌ Manual JAR paths
- ❌ Symbol resolution issues
- ❌ Missing Niagara-specific features
- ❌ Non-standard approach

### After (Tridium Plugins)
```kotlin
plugins {
  id("com.tridium.niagara-module")
  id("com.tridium.niagara-signing")
  id("com.tridium.convention.niagara-home-repositories")
}

dependencies {
  nre(":nre")
  api(":baja")
  api(":control")
  api(":web")
}
```

**Benefits:**
- ✅ Proper dependency resolution
- ✅ Automatic Niagara integration
- ✅ Module signing support
- ✅ Standard Tridium approach
- ✅ Better IDE support

## Build Commands

### Old Way (Don't use)
```bash
cd googleOntology/googleOntology-rt
./gradlew build  # This won't work anymore!
```

### New Way (Correct)
```bash
cd googleOntology
./gradlew build  # Build from root
```

## Configuration

### Set Niagara Path

Edit `googleOntology/gradle.properties`:

```properties
# Windows:
niagara_home=C:/Users/YOUR_USERNAME/Niagara4.14

# Linux/macOS:
# niagara_home=/opt/Niagara-4.14
```

The plugins automatically find JARs in:
- `$niagara_home/modules/` - Niagara modules
- `$niagara_home/etc/m2/repository/` - Gradle plugins

## Migration Guide

If you cloned this repo before November 4, 2025:

1. **Pull latest changes:**
   ```bash
   git pull
   ```

2. **Set your Niagara path:**
   ```bash
   cd googleOntology
   nano gradle.properties  # Set niagara_home
   ```

3. **Build from root:**
   ```bash
   ./gradlew build
   ```

4. **Install:**
   ```bash
   cp googleOntology-rt/build/module-jars/googleOntology-rt.jar \
      ~/.niagara/modules/googleOntology/
   ```

## Troubleshooting

### "Cannot derive value of 'gradlePluginHome'"

**Solution:** Set `niagara_home` in `googleOntology/gradle.properties`

### "Cannot resolve symbol 'BComponent'"

**Solution:**
1. Check `niagara_home` path is correct
2. Build from root directory (`googleOntology/`)
3. Verify `$niagara_home/etc/m2/repository` exists

### Build fails with "Project not found"

**Solution:** Make sure you're building from `googleOntology/` (root), not `googleOntology-rt/`

## Alternative: Use in Existing vykon Project

If you have an existing Niagara project with Tridium plugins:

```bash
# Copy just the module directory
cp -r googleOntology-rt /path/to/your/vykon/project/

# Build from your vykon root
cd /path/to/your/vykon/project
./gradlew :googleOntology-rt:build
```

Your vykon project's `settings.gradle.kts` will auto-discover it.

## What Didn't Change

- Java source code (unchanged)
- Module functionality (unchanged)
- API endpoints (unchanged)
- Documentation (updated with new build instructions)

## Benefits of New Approach

1. **Professional Standard** - Matches Tridium's own build process
2. **Better IDE Support** - IntelliJ/Eclipse properly resolve symbols
3. **Easier Maintenance** - No manual JAR path management
4. **Module Signing** - Built-in support if you have certificates
5. **Annotation Processing** - Automatic Niagara code generation
6. **Multi-module Ready** - Easy to add more modules later

## Questions?

See the new **[GRADLE_BUILD_GUIDE.md](googleOntology/GRADLE_BUILD_GUIDE.md)** for comprehensive documentation.
