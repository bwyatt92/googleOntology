# Gradle Build Guide - Using Tridium Plugins

This module now uses Tridium's official Niagara Gradle plugins (the proper way to build Niagara modules).

## Quick Start

### 1. Configure Niagara Path

Edit `googleOntology/gradle.properties` and set your Niagara installation path:

```properties
# Windows (use forward slashes):
niagara_home=C:/Users/YOUR_USERNAME/Niagara4.14

# Linux/macOS:
# niagara_home=/opt/Niagara-4.14
```

### 2. Build from Root

```bash
cd googleOntology
./gradlew build
```

That's it! The Tridium plugins will handle everything.

## Project Structure

```
googleOntology/                    # Root project
├── settings.gradle.kts            # Root settings (configures Tridium plugins)
├── build.gradle.kts               # Root build script
├── gradle.properties              # Set niagara_home HERE
│
└── googleOntology-rt/             # Module subproject
    ├── build.gradle.kts           # Module build (uses Tridium plugins)
    ├── gradle.properties          # Module properties
    └── src/                       # Java sources
```

## Building

### Build Module

```bash
cd googleOntology
./gradlew build
```

Output: `googleOntology-rt/build/module-jars/googleOntology-rt.jar`

### Clean Build

```bash
./gradlew clean build
```

### Build Specific Module

```bash
./gradlew :googleOntology-rt:build
```

## Installation

### Install to Station

After building, copy the JAR:

```bash
# From googleOntology directory
cp googleOntology-rt/build/module-jars/googleOntology-rt.jar \
   ~/.niagara/modules/googleOntology/
```

Or on Windows:
```powershell
Copy-Item googleOntology-rt\build\module-jars\googleOntology-rt.jar `
  $env:USERPROFILE\.niagara\modules\googleOntology\
```

Then restart your station.

## Tridium Plugins

This build uses the following official Tridium plugins:

- **com.tridium.niagara-module** - Configures module building
- **com.tridium.niagara-signing** - Module signing
- **com.tridium.convention.niagara-home-repositories** - Resolves Niagara dependencies
- **com.tridium.niagara-annotation-processors** - Handles Niagara annotations

These plugins are automatically loaded from `$NIAGARA_HOME/etc/m2/repository`.

## Dependencies

Dependencies are declared using Niagara's dependency notation:

```kotlin
dependencies {
  nre(":nre")           // Niagara Runtime Environment
  api(":baja")          // Baja framework
  api(":control")       // Control points
  api(":web")           // Web servlets
  uberjar("javax.servlet:javax.servlet-api:3.0.1")  // Servlet API
}
```

The plugins automatically resolve these from your Niagara installation.

## Gradle Tasks

| Task | Description |
|------|-------------|
| `./gradlew build` | Build all modules |
| `./gradlew clean` | Clean build artifacts |
| `./gradlew :googleOntology-rt:build` | Build specific module |
| `./gradlew tasks` | List all available tasks |

## Troubleshooting

### Error: "Cannot derive value of 'gradlePluginHome'"

**Cause:** `niagara_home` not set

**Solution:** Edit `googleOntology/gradle.properties`:
```properties
niagara_home=C:/Users/YOUR_USERNAME/Niagara4.14
```

### Error: "Cannot resolve symbol 'BComponent'"

**Cause:** Niagara plugins not finding your installation

**Solution:**
1. Check `gradle.properties` has correct path
2. Verify `$NIAGARA_HOME/etc/m2/repository` exists
3. Verify `$NIAGARA_HOME/modules/baja/` exists

### Build from Wrong Directory

Always build from the **root** `googleOntology` directory, not from `googleOntology-rt`:

```bash
# Correct:
cd googleOntology
./gradlew build

# Wrong:
cd googleOntology/googleOntology-rt
./gradlew build  # This won't find the Tridium plugins!
```

### Check Configuration

```bash
cd googleOntology
./gradlew properties | grep niagara
```

Should show:
```
niagara_home: C:/Users/YOUR_USERNAME/Niagara4.14
```

## Alternative: Add to Existing vykon Project

If you already have a vykon project with Tridium plugins configured, you can simply copy the `googleOntology-rt` folder there:

```bash
# Copy module to your vykon project
cp -r googleOntology-rt /path/to/vykon/

# Build from vykon root
cd /path/to/vykon
./gradlew :googleOntology-rt:build
```

The vykon `settings.gradle.kts` will automatically discover it.

## Example: Windows Setup

```powershell
# 1. Edit gradle.properties
notepad googleOntology\gradle.properties
# Set: niagara_home=C:/Users/gpg-mchristian/Niagara4.14

# 2. Build
cd googleOntology
.\gradlew.bat build

# 3. Install
Copy-Item googleOntology-rt\build\module-jars\googleOntology-rt.jar `
  $env:USERPROFILE\.niagara\modules\googleOntology\

# 4. Restart station
```

## Example: Linux/macOS Setup

```bash
# 1. Edit gradle.properties
nano googleOntology/gradle.properties
# Set: niagara_home=/opt/Niagara-4.14

# 2. Build
cd googleOntology
./gradlew build

# 3. Install
cp googleOntology-rt/build/module-jars/googleOntology-rt.jar \
   ~/.niagara/modules/googleOntology/

# 4. Restart station
```

## IDE Setup

### IntelliJ IDEA

1. **Open Project:** Open the `googleOntology` root directory
2. **Auto-import:** IDEA will detect the Gradle multi-project
3. **Build:** Use Gradle tool window or `./gradlew build`

### Eclipse

1. **Import:** File > Import > Gradle > Existing Gradle Project
2. **Select:** Choose `googleOntology` root directory
3. **Build:** Right-click project > Gradle > Refresh Gradle Project

### VS Code

1. **Open Folder:** Open `googleOntology` directory
2. **Install Extension:** "Gradle for Java"
3. **Build:** Use Gradle sidebar or terminal

## Differences from Old Build

The old build.gradle.kts manually referenced JAR files. The new one uses Tridium's official plugins:

**Old (manual JAR references):**
```kotlin
compileOnly(files(
    "$niagaraHome/modules/baja/baja-rt.jar"
))
```

**New (Tridium plugins):**
```kotlin
plugins {
  id("com.tridium.niagara-module")
}

dependencies {
  api(":baja")
}
```

The plugins handle:
- Finding Niagara JARs
- Module manifest generation
- Proper JAR packaging
- Signing (if configured)
- Annotation processing

Much cleaner and matches how Tridium builds their modules!
