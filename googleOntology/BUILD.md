# Building with Gradle

This guide explains how to build the Google Ontology N4 module using Gradle.

## Prerequisites

1. **Java Development Kit (JDK) 8 or 11**
   ```bash
   java -version
   ```

2. **Gradle 7.0+** (or use the Gradle Wrapper)
   ```bash
   gradle --version
   ```

3. **Niagara 4.11+** installed on your system

## Setup

### 1. Set Niagara Home Environment Variable

**Linux/macOS:**
```bash
export NIAGARA_HOME=/opt/niagara/current
export NIAGARA_USER_HOME=$HOME/.niagara
```

Add to your `~/.bashrc` or `~/.zshrc` to make it permanent:
```bash
echo 'export NIAGARA_HOME=/opt/niagara/current' >> ~/.bashrc
echo 'export NIAGARA_USER_HOME=$HOME/.niagara' >> ~/.bashrc
source ~/.bashrc
```

**Windows (PowerShell):**
```powershell
$env:NIAGARA_HOME = "C:\Niagara\Niagara-4.11"
$env:NIAGARA_USER_HOME = "$env:USERPROFILE\.niagara"
```

**Windows (Command Prompt):**
```cmd
set NIAGARA_HOME=C:\Niagara\Niagara-4.11
set NIAGARA_USER_HOME=%USERPROFILE%\.niagara
```

### 2. Alternative: Use Gradle Properties

Instead of environment variables, you can pass paths directly:

```bash
gradle build -Pniagara.home=/path/to/niagara -Pniagara.user.home=/path/to/niagara/user
```

Or edit `gradle.properties` and uncomment/set:
```properties
niagara.home=/opt/niagara/current
niagara.user.home=/home/user/.niagara
```

## Building

### Validate Niagara Installation

First, verify your Niagara paths are correct:

```bash
cd googleOntology/googleOntology-rt
gradle validateNiagara
```

You should see:
```
✓ Niagara installation found at: /opt/niagara/current
✓ Niagara version: 4.11.0.156
```

### Build the Module

```bash
gradle build
```

This will:
1. Validate Niagara installation
2. Compile Java sources
3. Package the module JAR
4. Output: `build/libs/googleOntology-rt.jar`

### Clean Build

```bash
gradle clean build
```

## Installation

### Option 1: Install to User Home (Recommended)

This installs to `~/.niagara/modules/googleOntology/`:

```bash
gradle installModule
```

Then restart your station to load the module.

### Option 2: Install to Niagara Installation

This installs to `$NIAGARA_HOME/modules/googleOntology/` (may require admin/sudo):

```bash
# Linux/macOS
sudo gradle installModuleToNiagara

# Windows (run as Administrator)
gradle installModuleToNiagara
```

Then restart your station.

### Option 3: Manual Installation

Copy the JAR manually:

```bash
# Build first
gradle build

# Copy to Niagara user home
mkdir -p ~/.niagara/modules/googleOntology
cp build/libs/googleOntology-rt.jar ~/.niagara/modules/googleOntology/

# OR copy to Niagara installation
sudo mkdir -p $NIAGARA_HOME/modules/googleOntology
sudo cp build/libs/googleOntology-rt.jar $NIAGARA_HOME/modules/googleOntology/
```

## Gradle Tasks

View all available tasks:

```bash
gradle tasks
```

### Common Tasks

| Task | Description |
|------|-------------|
| `gradle build` | Compile and package the module |
| `gradle clean` | Remove build artifacts |
| `gradle installModule` | Install to user home |
| `gradle installModuleToNiagara` | Install to Niagara installation |
| `gradle validateNiagara` | Verify Niagara paths |
| `gradle showPaths` | Display configured paths |
| `gradle jar` | Create JAR only (no validation) |

## Using Gradle Wrapper (Recommended)

If you want to ensure everyone uses the same Gradle version, use the Gradle Wrapper:

### 1. Generate Wrapper

```bash
gradle wrapper --gradle-version 8.5
```

This creates:
- `gradlew` (Linux/macOS)
- `gradlew.bat` (Windows)
- `gradle/wrapper/` directory

### 2. Use Wrapper

**Linux/macOS:**
```bash
./gradlew build
./gradlew installModule
```

**Windows:**
```cmd
gradlew.bat build
gradlew.bat installModule
```

Now anyone can build without installing Gradle separately!

## Troubleshooting

### Error: "NIAGARA_HOME not found"

**Solution:** Set the environment variable or pass as property:
```bash
gradle build -Pniagara.home=/path/to/niagara
```

### Error: "baja-rt.jar not found"

**Solution:** Verify Niagara is fully installed:
```bash
ls $NIAGARA_HOME/modules/baja/baja-rt.jar
```

### Error: "Cannot find symbol" during compilation

**Solution:** Check that all Niagara JARs are present:
```bash
ls $NIAGARA_HOME/modules/*/
```

Required modules:
- `baja/baja-rt.jar`
- `control/control-rt.jar`
- `web/web-rt.jar`

### Module doesn't appear in Workbench

**Solutions:**
1. Verify JAR location:
   ```bash
   ls -l ~/.niagara/modules/googleOntology/googleOntology-rt.jar
   ```

2. Check module.properties is included in JAR:
   ```bash
   jar tf build/libs/googleOntology-rt.jar | grep module.properties
   ```

3. Restart the station completely

4. Check Niagara logs:
   ```bash
   tail -f $NIAGARA_USER_HOME/log/station.log
   ```

## Development Workflow

### Typical Development Cycle

```bash
# 1. Make code changes
vim src/googleOntology/service/BGoogleOntologyService.java

# 2. Clean and rebuild
gradle clean build

# 3. Install to user home
gradle installModule

# 4. Restart station and test

# 5. Check logs for errors
tail -f ~/.niagara/log/station.log
```

### Quick Rebuild and Install

```bash
gradle clean build installModule
```

## IDE Integration

### IntelliJ IDEA

1. Open `googleOntology/googleOntology-rt` folder
2. IDEA will detect `build.gradle.kts`
3. Set NIAGARA_HOME in **Run > Edit Configurations > Environment Variables**
4. Use Gradle tool window for tasks

### Eclipse

1. Install Gradle plugin (Buildship)
2. **File > Import > Gradle > Existing Gradle Project**
3. Select `googleOntology/googleOntology-rt`
4. Configure environment variables in project settings

### VS Code

1. Install "Gradle for Java" extension
2. Open `googleOntology/googleOntology-rt` folder
3. Use Gradle tasks in sidebar
4. Set environment in `.vscode/settings.json`:
   ```json
   {
     "java.configuration.updateBuildConfiguration": "automatic",
     "terminal.integrated.env.linux": {
       "NIAGARA_HOME": "/opt/niagara/current"
     }
   }
   ```

## Continuous Integration

### GitHub Actions Example

```yaml
name: Build Module

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'

    - name: Download Niagara (if available)
      run: |
        # Download and extract Niagara SDK/runtime
        # This depends on your Niagara licensing

    - name: Build with Gradle
      run: |
        cd googleOntology/googleOntology-rt
        gradle build -Pniagara.home=/path/to/niagara

    - name: Upload JAR
      uses: actions/upload-artifact@v3
      with:
        name: googleOntology-rt.jar
        path: googleOntology/googleOntology-rt/build/libs/googleOntology-rt.jar
```

## Advanced Configuration

### Custom JAR Name

Edit `build.gradle.kts`:
```kotlin
tasks.jar {
    archiveBaseName.set("my-custom-name")
}
```

### Add Version to JAR Name

```kotlin
tasks.jar {
    archiveVersion.set(moduleVersion)
}
```

### Include Additional Resources

```kotlin
tasks.jar {
    from("resources") {
        include("**/*.yaml")
    }
}
```

## Building for Distribution

To create a distributable package:

```bash
# Build module
gradle clean build

# Create distribution
mkdir -p dist/googleOntology
cp build/libs/googleOntology-rt.jar dist/googleOntology/
cp module.properties dist/googleOntology/
cp module-include.xml dist/googleOntology/
cp module.palette dist/googleOntology/

# Create archive
cd dist
tar czf googleOntology-1.0.0.tar.gz googleOntology/
# or
zip -r googleOntology-1.0.0.zip googleOntology/
```

Now you have `googleOntology-1.0.0.tar.gz` ready for distribution!
