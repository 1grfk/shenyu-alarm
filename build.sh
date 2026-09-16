#!/bin/bash
set -x
SDK=/opt/android-sdk
BT=$SDK/build-tools/34.0.0
PLATFORM=$SDK/platforms/android-34/android.jar
SRC=/sdcard/Download/Operit/alarm_bridge
OUT=$SRC/out
rm -rf $OUT
mkdir -p $OUT/gen $OUT/classes $OUT/dex

# 1. aapt2 link -> base.apk
$BT/aapt2 link -o $OUT/base.apk -I $PLATFORM --manifest $SRC/AndroidManifest.xml --min-sdk-version 26 --target-sdk-version 34 --version-code 2 --version-name 2.0
# 2. javac
cd $OUT
JCS=$(find $SRC/src -name "*.java")
javac -source 11 -target 11 -classpath $PLATFORM -d $OUT/classes $JCS 2>&1 | head -40
# 3. d8 -> classes.dex
$BT/d8 --lib $PLATFORM --release --output $OUT/dex $(find $OUT/classes -name "*.class")
# 4. zip dex into apk
cp $OUT/dex/classes.dex $OUT/classes.dex
cd $OUT && zip -uj base.apk classes.dex
echo "=== build done, verifying ==="
$BT/aapt2 dump badging $OUT/base.apk 2>/dev/null | head -5
ls -la $OUT/base.apk