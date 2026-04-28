# Number Quest

A simple, fast, family-friendly turn-based **random number** game for 2–8 players.
On each turn the active player taps a colored capsule, it "opens", and reveals
**two random numbers between 1 and 5**. Turn order rotates automatically.

> **Strictly not dice.** No cubes, no pips, no rolling, no spinning, no casino
> motifs anywhere in the UI or code. The reveal uses a "mystery box" /
> capsule-opening metaphor with rounded number cards.

## Features

- 2–8 players, each with a unique name and color
- Color-coded UI throughout (top bar, capsule, reveal cards, "next player" CTA)
- Two random integers in `[1, 5]` per turn (inclusive — not 1–6)
- Fast, minimal reveal animation (~300 ms total): cards bounce in side-by-side
- Idle capsule "breathes" gently to invite a tap
- Material 3 design with dynamic colors on Android 12+
- Light haptic feedback on reveal (toggleable in code)
- Subtle system click sound on tap (toggleable in code)
- Turn history with color-coded entries
- Light & dark themes
- Edge-to-edge layout
- Fully offline — no network, no backend, no analytics

## Tech stack

- **Language:** Kotlin 2.0.21
- **UI:** Jetpack Compose with Material 3
- **Build:** Gradle 8.9, Android Gradle Plugin 8.7.2
- **Min SDK:** 26 (Android 8.0 — covers 95%+ of active devices)
- **Target SDK:** 34 (Android 14)
- **External libraries:** none beyond AndroidX/Compose

## Project layout

```
NumberQuest/
├── build.gradle.kts                 # Root build script
├── settings.gradle.kts              # Module includes + repository config
├── gradle.properties                # Gradle JVM args & flags
├── gradlew, gradlew.bat             # Wrapper scripts
├── gradle/wrapper/
│   └── gradle-wrapper.properties    # Pins Gradle 8.9
├── README.md                        # You're reading it
└── app/
    ├── build.gradle.kts             # App module build (Kotlin/Compose deps)
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/numberquest/
        │   ├── MainActivity.kt
        │   ├── GameViewModel.kt
        │   ├── model/
        │   │   ├── Player.kt
        │   │   ├── PlayerPalette.kt
        │   │   └── TurnResult.kt
        │   └── ui/
        │       ├── SetupScreen.kt
        │       ├── GameScreen.kt
        │       ├── HistoryScreen.kt
        │       └── theme/
        │           ├── Color.kt
        │           ├── Type.kt
        │           └── Theme.kt
        └── res/
            ├── drawable/             # Adaptive icon vectors
            ├── mipmap-*/             # Launcher icons (PNG + adaptive XML)
            ├── values/               # strings, colors, themes
            └── values-night/         # dark theme overrides
```

## How to build the APK

You have **two** options. Either works.

### Option A — Android Studio (easiest)

1. Install **Android Studio Hedgehog (2023.1.1) or newer** from
   <https://developer.android.com/studio>.
2. In Android Studio: **File → Open** and select the unzipped
   `NumberQuest/` directory.
3. Android Studio will automatically:
   - Generate the missing `gradle/wrapper/gradle-wrapper.jar` (don't worry about
     it — it's a binary that the IDE re-generates on first open).
   - Download the Gradle distribution and Android dependencies.
   - Sync the project. (First sync takes a few minutes; subsequent ones are
     fast.)
4. Connect a device with USB debugging enabled, **or** start an emulator
   (Tools → Device Manager).
5. Click the green ▶ **Run 'app'** button. The APK will be installed and
   launched on the selected device.

To produce a standalone APK file:
- **Build → Build Bundle(s) / APK(s) → Build APK(s)**
- The unsigned debug APK appears at:
  `app/build/outputs/apk/debug/app-debug.apk`

For a signed release APK:
- **Build → Generate Signed Bundle / APK… → APK** and follow the wizard. You'll
  need a keystore (Android Studio can generate one for you).

### Option B — Command line (no IDE)

You need:
- **JDK 17** (e.g. `sudo apt install openjdk-17-jdk` or use SDKMAN)
- **Android command-line tools** + **platform-tools** + **build-tools 34.0.0**
  + **platforms;android-34**. The fastest way:
  ```bash
  # 1. Download command-line tools from
  #    https://developer.android.com/studio (scroll down to "Command line tools only")
  # 2. Unzip somewhere and set ANDROID_HOME, e.g.:
  export ANDROID_HOME=$HOME/Android/Sdk
  export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
  yes | sdkmanager --licenses
  sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
  ```
- A standalone Gradle 8.9+ install **on your first build only**, to bootstrap
  the wrapper jar:
  ```bash
  # Install via SDKMAN (recommended) or apt/brew
  curl -s "https://get.sdkman.io" | bash
  sdk install gradle 8.9
  ```

Then, from the project root:

```bash
# 1. One-time wrapper bootstrap (creates gradle/wrapper/gradle-wrapper.jar).
gradle wrapper

# 2. Build a debug APK.
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk

# 3. (Optional) Install on a connected device.
./gradlew installDebug
```

After the first `gradle wrapper` run, the wrapper jar is committed to your
copy of the project and you can delete the standalone Gradle install — the
wrapper handles everything from then on.

## Random generation

The two values per turn come from a single `kotlin.random.Random` instance:

```kotlin
val a = random.nextInt(1, 6)  // returns 1..5 — `until` is exclusive
val b = random.nextInt(1, 6)
```

There's no weighting, no streak detection, and no persistence — every tap is an
independent uniform draw over `{1, 2, 3, 4, 5}`.

## Customizing

- **Player count limits** — change `MIN_PLAYERS` and `MAX_PLAYERS` in
  `SetupScreen.kt`.
- **Number range** — change the bounds in `GameViewModel.drawNumbers()`.
  Keep the second arg of `nextInt(from, until)` exclusive.
- **Palette** — edit the `swatches` list in `model/PlayerPalette.kt`.
- **Reveal animation timing** — tweak the `tween` durations and `spring`
  damping ratios in `ui/GameScreen.kt`. Keep total under ~500 ms to stay snappy.
- **Sound / haptics defaults** — edit `_soundEnabled` / `_hapticsEnabled`
  initial values in `GameViewModel.kt`.

## Anti-dice design notes

These constraints were intentional:

- The reveal element is a **rounded pill / capsule**, never a square.
- Numbers display as **arabic digits**, never as pip patterns.
- Animation is a **scale-in spring**, never a rotation or tumble.
- Card aspect is **portrait 110×150 dp**, dissimilar to a die face.
- No "roll", "dice", "luck", "casino", or "wheel" wording anywhere in the app.
- The launcher icon shows the two-card motif, not a cube.

## License

This project is provided as a complete, self-contained example. Use it however
you like.
