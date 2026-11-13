# J5 EV Charge Calculator (Android Studio project)

- Theme: Dark Mode
- DC average charging power: 60 kW (fixed assumption in v1.0)
- Default battery capacity: 58.9 kWh (editable in the app)
- Emergency AC: 8A / 10A / 13A / 16A at 220V
- Wall charger: 3.5 / 7 / 11 kW
- Public DC: price per kWh and budget input
- Offline; no permissions or account required

## Build APK
1. Open this folder in Android Studio (Giraffe or newer; Android Gradle Plugin 8.7.3).
2. Allow Gradle sync and install Android SDK Platform 35 if prompted.
3. Select Build > Build Bundle(s) / APK(s) > Build APK(s).
4. APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Calculation notes
This is an estimate, not live vehicle/BMS data. AC wall input uses 90% assumed efficiency; DC uses 92%.
DC average is set to 60 kW as requested, but real charging speed varies with SOC, temperature, charger limits and battery conditioning. The battery capacity is editable because exact usable capacity can vary by trim/market.
