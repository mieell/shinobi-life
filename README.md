# Shinobi Life

An offline Android life simulator with original cartoon portraits, clan techniques, and a journal that records your choices.

Open **New life** to edit your avatar, choose a village and clan, and set your starting Looks through **Presence**. New lives begin at 18 after academy graduation. The app keeps one active life on the device and asks before replacing it.

- **Life** shows your resources and journal. Train, style your character, work, rest, or advance one season. Four seasons add a year.
- **Missions** offers five assignments and a turn-based public duel. A matching clan technique adds a bonus to an assignment or gives you a special move in the duel.
- **Clans** explains the origins, starting perk, technique, cost, and specialty of 20 original clans. A clanless character begins with extra Skill.
- **People** lets you build Trust with a family member, friend, and rival. A Looks score of 75 or higher improves Trust gains from visits. Completing missions and winning duels raises Fame. At 25 and 60 Fame, villagers react differently as seasons pass.

## Build and install

Use JDK 17 or newer and Android SDK Platform 36. The Gradle wrapper and Android Gradle plugin are included in the build configuration. Set `ANDROID_HOME` to your Android SDK location or open the project in Android Studio.

```powershell
.\gradlew.bat :app:assembleDebug :app:bundleRelease :app:lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

On macOS or Linux, use `./gradlew` instead of `.\gradlew.bat`. The APK requires Android 7.0 or later. The release bundle at `app/build/outputs/bundle/release/app-release.aab` requires your upload key for Play Console submission.

To check the rules for all 21 starting paths with just the JDK, run this from the project directory.

```powershell
$src = 'app/src/main/java/com/shinobilife/game'
javac --release 17 -encoding UTF-8 -d build/life-check "$src/Clans.java" "$src/Missions.java" "$src/Life.java" checks/LifeCheck.java
java -cp build/life-check com.shinobilife.game.LifeCheck
```
