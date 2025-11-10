# Build Troubleshooting Guide

## Error: "Cannot resolve symbol 'BComponent'"

This error means Gradle cannot find the Niagara framework JARs.

### Solution 1: Set NIAGARA_HOME Environment Variable

**Linux/macOS:**
```bash
export NIAGARA_HOME=/path/to/your/niagara
export NIAGARA_HOME=/opt/tridium/niagara-4.11  # Example
```

Make it permanent by adding to `~/.bashrc` or `~/.zshrc`:
```bash
echo 'export NIAGARA_HOME=/opt/tridium/niagara-4.11' >> ~/.bashrc
source ~/.bashrc
```

**Windows PowerShell:**
```powershell
$env:NIAGARA_HOME = "C:\Niagara\Niagara-4.11"
```

**Windows Command Prompt:**
```cmd
set NIAGARA_HOME=C:\Niagara\Niagara-4.11
```

Then rebuild:
```bash
./gradlew clean build
```

### Solution 2: Pass Path as Gradle Property

Instead of environment variable, pass directly:

```bash
./gradlew build -Pniagara.home=/path/to/niagara
```

**Windows example:**
```cmd
gradlew.bat build -Pniagara.home=C:\Niagara\Niagara-4.11
```

### Solution 3: Edit gradle.properties

Edit `googleOntology/googleOntology-rt/gradle.properties`:

```properties
# Uncomment and set to your Niagara installation path
niagara.home=/opt/tridium/niagara-4.11
niagara.user.home=/home/youruser/.niagara
```

**Windows example:**
```properties
niagara.home=C:/Niagara/Niagara-4.11
niagara.user.home=C:/Users/youruser/.niagara
```

Note: Use forward slashes (`/`) even on Windows!

### Verify Your Niagara Installation

Run this to check if Niagara is properly configured:

```bash
./gradlew validateNiagara
```

You should see:
```
✓ Niagara installation found at: /path/to/niagara
✓ Niagara version: 4.11.0.156
```

If you see an error, your path is wrong.

### Find Your Niagara Installation

**Linux/macOS:**
```bash
# Common locations
ls /opt/niagara
ls /opt/tridium
ls /usr/local/niagara

# Find niagara.sh
find / -name "niagara.sh" 2>/dev/null

# The NIAGARA_HOME is the directory containing "bin", "lib", "modules"
```

**Windows:**
```cmd
# Common locations
dir "C:\Niagara"
dir "C:\Program Files\Niagara"
dir "C:\Tridium"

# The NIAGARA_HOME should contain "bin", "lib", "modules" folders
```

### Verify Required JARs Exist

Check that these files exist:

```bash
ls $NIAGARA_HOME/modules/baja/baja-rt.jar
ls $NIAGARA_HOME/modules/control/control-rt.jar
ls $NIAGARA_HOME/modules/web/web-rt.jar
```

If any are missing, your Niagara installation may be incomplete.

## Error: "NIAGARA_HOME not found"

Gradle displays:
```
WARNING: NIAGARA_HOME not found at: /opt/niagara/current
```

**Solutions:** See "Cannot resolve symbol 'BComponent'" above - same fixes apply.

## Error: "No such file or directory" for Niagara modules

```
Could not resolve all files for configuration ':compileClasspath'.
> Could not find baja-rt.jar
```

**Cause:** Niagara is not installed or path is wrong.

**Solution:**

1. Verify Niagara is installed:
   ```bash
   ls $NIAGARA_HOME/modules/
   ```

2. You should see directories like:
   ```
   baja/
   control/
   web/
   driver/
   ...
   ```

3. If not, install Niagara or correct the path.

## Error: Java version incompatibility

```
Unsupported class file major version 55
```

**Cause:** Your JDK version doesn't match Niagara's requirements.

**Solution:**

1. Check your Java version:
   ```bash
   java -version
   ```

2. Niagara 4.11+ requires **Java 8 or Java 11**.

3. Install correct version:
   ```bash
   # macOS with Homebrew
   brew install openjdk@11

   # Ubuntu/Debian
   sudo apt install openjdk-11-jdk

   # Set JAVA_HOME
   export JAVA_HOME=/path/to/jdk11
   ```

## Error: Permission denied on gradlew

```
bash: ./gradlew: Permission denied
```

**Solution:**

Make the script executable:
```bash
chmod +x gradlew
```

Then run again:
```bash
./gradlew build
```

## Error: Gradle daemon issues

```
Daemon will be stopped at the end of the build after running out of JVM memory
```

**Solution:**

Edit `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
org.gradle.daemon=false
```

Or increase memory:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
```

## Module doesn't appear in Workbench

You built successfully but don't see the module in Niagara Workbench.

### Check JAR Location

```bash
# Should be in one of these locations:
ls ~/.niagara/modules/googleOntology/googleOntology-rt.jar
# OR
ls $NIAGARA_HOME/modules/googleOntology/googleOntology-rt.jar
```

### Install Module

```bash
# Install to user home (recommended)
./gradlew installModule

# Install to Niagara installation (may need sudo)
sudo ./gradlew installModuleToNiagara
```

### Restart Station

After installing, you must restart your Niagara station:

1. Stop the station
2. Start the station
3. Check Workbench under Services

### Check Module Contents

Verify the JAR contains required files:

```bash
jar tf build/libs/googleOntology-rt.jar | grep -E "(module\.|\.class)"
```

Should see:
```
module-include.xml
module.palette
module.properties
googleOntology/service/BGoogleOntologyService.class
googleOntology/servlet/BGoogleOntologyServlet.class
...
```

If `module.properties` is missing, rebuild with:
```bash
./gradlew clean build
```

## Debugging Tips

### Enable Gradle Debug Output

```bash
./gradlew build --debug > build.log 2>&1
```

Then check `build.log` for details.

### Show All Gradle Properties

```bash
./gradlew properties | grep niagara
```

### Show Dependency Tree

```bash
./gradlew dependencies
```

### Validate Before Building

```bash
# Step 1: Validate Niagara
./gradlew validateNiagara

# Step 2: Show paths
./gradlew showPaths

# Step 3: Build
./gradlew clean build

# Step 4: Install
./gradlew installModule
```

### Check Station Logs

After installing, check Niagara logs for errors:

```bash
# Linux/macOS
tail -f ~/.niagara/log/station.log

# Windows
type %USERPROFILE%\.niagara\log\station.log
```

Look for:
```
Module loaded: googleOntology
GoogleOntologyService ready
```

## IDE-Specific Issues

### IntelliJ IDEA

1. **Import Project:**
   - File > Open > Select `googleOntology/googleOntology-rt`
   - Choose "Gradle" when prompted

2. **Set NIAGARA_HOME:**
   - Run > Edit Configurations
   - Add Environment Variable: `NIAGARA_HOME=/path/to/niagara`

3. **Refresh Gradle:**
   - Gradle tool window > Reload

### Eclipse

1. **Import Project:**
   - File > Import > Gradle > Existing Gradle Project

2. **Set Environment:**
   - Right-click project > Properties
   - Run/Debug Settings > Edit
   - Environment tab > Add `NIAGARA_HOME`

### VS Code

1. **Install Extensions:**
   - Extension Pack for Java
   - Gradle for Java

2. **Configure Settings:**

   Create `.vscode/settings.json`:
   ```json
   {
     "java.configuration.updateBuildConfiguration": "automatic",
     "terminal.integrated.env.linux": {
       "NIAGARA_HOME": "/path/to/niagara"
     },
     "terminal.integrated.env.windows": {
       "NIAGARA_HOME": "C:\\Niagara\\Niagara-4.11"
     }
   }
   ```

## Still Having Issues?

### Check Requirements

- ✅ Niagara 4.11 or newer installed
- ✅ JDK 8 or 11 installed
- ✅ Gradle 7.0+ installed (or use `./gradlew`)
- ✅ NIAGARA_HOME environment variable set
- ✅ Required Niagara modules present (baja, control, web)

### Quick Diagnostic

Run this diagnostic script:

```bash
#!/bin/bash
echo "=== Niagara Build Diagnostic ==="
echo ""
echo "1. NIAGARA_HOME:"
echo "   $NIAGARA_HOME"
echo ""
echo "2. Niagara installation exists:"
ls -ld "$NIAGARA_HOME" 2>/dev/null || echo "   NOT FOUND"
echo ""
echo "3. Required JARs:"
for jar in baja control web; do
  if [ -f "$NIAGARA_HOME/modules/$jar/$jar-rt.jar" ]; then
    echo "   ✓ $jar-rt.jar"
  else
    echo "   ✗ $jar-rt.jar MISSING"
  fi
done
echo ""
echo "4. Java version:"
java -version 2>&1 | head -1
echo ""
echo "5. Gradle version:"
./gradlew --version 2>&1 | head -1
echo ""
```

Save as `diagnostic.sh`, make executable, and run:
```bash
chmod +x diagnostic.sh
./diagnostic.sh
```

## Get Help

If you're still stuck after trying these solutions:

1. Run the diagnostic script above
2. Check station logs
3. Enable Gradle debug: `./gradlew build --debug`
4. Review the error messages carefully
5. Verify all prerequisites are met

Common causes:
- ❌ NIAGARA_HOME not set or incorrect
- ❌ Niagara not fully installed
- ❌ Wrong Java version
- ❌ Module not installed to correct location
- ❌ Station not restarted after install
