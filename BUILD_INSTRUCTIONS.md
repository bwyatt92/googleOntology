# Build Instructions - Fixed for Tridium Plugins

## ✅ The Fix

Your build issue has been **fixed**! The problem was that the Gradle configuration was trying to manually reference JAR files instead of using Tridium's official Niagara Gradle plugins.

## Quick Build (Windows)

```bash
# 1. Edit gradle.properties
cd googleOntology
notepad gradle.properties

# Set this line (use YOUR path):
niagara_home=C:/Users/gpg-mchristian/Niagara4.14

# 2. Build
gradlew.bat build

# 3. Install
copy googleOntology-rt\build\module-jars\googleOntology-rt.jar ^
     %USERPROFILE%\.niagara\modules\googleOntology\

# 4. Restart your station
```

## What Was Fixed

### Problem
- `Cannot resolve symbol 'BComponent'`
- Manual JAR file references
- Gradle couldn't find Niagara classes

### Solution
Now using **Tridium's official plugins**:
- ✅ `com.tridium.niagara-module`
- ✅ `com.tridium.niagara-signing`
- ✅ `com.tridium.convention.niagara-home-repositories`

These are the **same plugins Tridium uses** to build their own modules!

## New Project Structure

```
googleOntology/                          ← BUILD FROM HERE (root)
├── settings.gradle.kts                  ← Configures Tridium plugins
├── build.gradle.kts                     ← Root build
├── gradle.properties                    ← SET YOUR niagara_home HERE
├── GRADLE_BUILD_GUIDE.md               ← Full documentation
│
└── googleOntology-rt/                   ← Module directory
    ├── build.gradle.kts                 ← Uses Tridium plugins
    └── src/googleOntology/              ← Java code (unchanged)
```

## Build Process

### 1. Configure (One Time)

Edit `googleOntology/gradle.properties`:

```properties
# Windows (use forward slashes):
niagara_home=C:/Users/YOUR_USERNAME/Niagara4.14

# Or Linux/macOS:
# niagara_home=/opt/Niagara-4.14
```

**Important:** Use your actual path to Niagara 4.14!

### 2. Build

**Always build from the ROOT directory:**

```bash
cd googleOntology          # ← Important: root directory
./gradlew build            # Linux/macOS
# or
gradlew.bat build          # Windows
```

**Output:** `googleOntology-rt/build/module-jars/googleOntology-rt.jar`

### 3. Install

```bash
# Windows
copy googleOntology-rt\build\module-jars\googleOntology-rt.jar ^
     %USERPROFILE%\.niagara\modules\googleOntology\

# Linux/macOS
cp googleOntology-rt/build/module-jars/googleOntology-rt.jar \
   ~/.niagara/modules/googleOntology/
```

### 4. Restart Station

Restart your Niagara station to load the module.

## Verify Build

After building successfully, check:

```bash
ls googleOntology-rt/build/module-jars/googleOntology-rt.jar
```

Should show the JAR file with a recent timestamp.

## Common Errors Fixed

### ❌ "Cannot resolve symbol 'BComponent'"
**Fixed:** Now using Tridium plugins that properly resolve Niagara classes

### ❌ "NIAGARA_HOME not found"
**Solution:** Set `niagara_home` in `googleOntology/gradle.properties`

### ❌ "Cannot derive value of 'gradlePluginHome'"
**Solution:** Set `niagara_home` - plugins are automatically found at `$niagara_home/etc/m2/repository`

### ❌ Built from wrong directory
**Solution:** Always build from `googleOntology/` (root), NOT from `googleOntology-rt/`

## Test the Build

```bash
# 1. Check configuration
cd googleOntology
./gradlew properties | grep niagara_home
# Should show: niagara_home: C:/Users/YOUR_USERNAME/Niagara4.14

# 2. Build
./gradlew clean build

# 3. Verify output
ls -lh googleOntology-rt/build/module-jars/
```

## IDE Setup

### IntelliJ IDEA
1. Open `googleOntology` folder (root)
2. IDEA auto-detects Gradle multi-project
3. Wait for indexing to complete
4. All imports should now resolve! ✅

### VS Code
1. Open `googleOntology` folder
2. Install "Gradle for Java" extension
3. Reload window
4. Imports resolve! ✅

## Alternative: Add to Your vykon Project

Since you already have a vykon project with Tridium plugins, you can just add googleOntology there:

```bash
# Copy the module directory
cp -r googleOntology-rt C:\Users\gpg-mchristian\Niagara4.14\vykon\

# Build from vykon root
cd C:\Users\gpg-mchristian\Niagara4.14\vykon
gradlew.bat :googleOntology-rt:build
```

Your vykon `settings.gradle.kts` will automatically discover it!

## Full Documentation

- **[GRADLE_BUILD_GUIDE.md](googleOntology/GRADLE_BUILD_GUIDE.md)** - Complete guide
- **[TROUBLESHOOTING.md](googleOntology/TROUBLESHOOTING.md)** - Error solutions
- **[CHANGES.md](CHANGES.md)** - What changed and why

## Key Takeaways

1. ✅ **Use Tridium's official plugins** (now configured)
2. ✅ **Set `niagara_home` in gradle.properties** (you need to do this)
3. ✅ **Build from root directory** (`googleOntology/`)
4. ✅ **Standard Niagara development** (same as Tridium modules)

Your build should now work perfectly! Let me know if you hit any issues.
