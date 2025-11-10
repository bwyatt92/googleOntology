#!/bin/bash
# Quick diagnostic for Niagara path issues

echo "=== Niagara Build Diagnostic ==="
echo ""
echo "1. NIAGARA_HOME environment variable:"
if [ -z "$NIAGARA_HOME" ]; then
    echo "   ❌ NOT SET"
    echo "   Fix: export NIAGARA_HOME=/path/to/niagara"
else
    echo "   ✓ $NIAGARA_HOME"
fi
echo ""

if [ ! -z "$NIAGARA_HOME" ]; then
    echo "2. Niagara directory exists:"
    if [ -d "$NIAGARA_HOME" ]; then
        echo "   ✓ Directory found"
    else
        echo "   ❌ Directory NOT found at: $NIAGARA_HOME"
    fi
    echo ""

    echo "3. Required Niagara modules:"
    for module in baja control web; do
        jar_path="$NIAGARA_HOME/modules/$module/$module-rt.jar"
        if [ -f "$jar_path" ]; then
            echo "   ✓ $module-rt.jar"
        else
            echo "   ❌ $module-rt.jar MISSING"
            echo "      Expected: $jar_path"
        fi
    done
fi

echo ""
echo "4. Java version:"
java -version 2>&1 | head -1 || echo "   ❌ Java not found"
echo ""

echo "=== Recommendation ==="
if [ -z "$NIAGARA_HOME" ]; then
    echo "Set NIAGARA_HOME: export NIAGARA_HOME=/path/to/your/niagara"
elif [ ! -d "$NIAGARA_HOME" ]; then
    echo "Niagara directory not found. Check path: $NIAGARA_HOME"
elif [ ! -f "$NIAGARA_HOME/modules/baja/baja-rt.jar" ]; then
    echo "Niagara installation incomplete. Reinstall Niagara."
else
    echo "✓ Niagara configuration looks good!"
    echo "Try: ./gradlew clean build"
fi
