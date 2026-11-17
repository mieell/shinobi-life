# Shinobi Life

An offline Android life simulator with original cartoon portraits, clan techniques, and a journal that records your choices.

Open **New life** to edit your avatar, choose a village and clan, and set your starting Looks through **Presence**. New lives begin at 18 after academy graduation. The app keeps one active life on the device and asks before replacing it.

- **Life** shows your resources and journal. Train, style your character, work, rest, or advance one season. Four seasons add a year.
- **Missions** offers five assignments and a turn-based public duel. A matching clan technique adds a bonus to an assignment. Every starting path has a combat art for the duel, including fire, lightning, illusions, shadow binds, seals, regeneration, and gravity.
- **Clans** explains the origins, starting perk, combat art, and mission specialty of 22 original clans. Kagaribi can awaken the Sparkglass Eye at 19 Skill to read an attack and copy it. Shinkai can awaken the Horizon Eye at 25 Skill and two duel wins to pin an opponent with gravity. Awaken an eligible eye from the Life screen.
- **Summons** lets you sign and train five companions. Complete a companion’s matching assignment to unlock its contract. Toriba starts with a Lantern Shrike. A signed companion can help with its matching mission, and you can call up to two different companions during the same duel. Higher bond increases their duel damage.
- **People** lets you build Trust with a family member, friend, and rival. A Looks score of 75 or higher improves Trust gains from visits. Completing missions and winning duels raises Fame. At 25 and 60 Fame, villagers react differently as seasons pass.

## Clan tier list

This ranking combines duel strength and repeatable life progression. **Kuroishi** is the safest early duel pick. Three Basalt Turns win the opening match, and its ward halves the two counters you take. **Shinkai** is the strongest awakened duelist. Gravity Lock and two Tideweight Palms win in three turns with just one halved counter.

| Tier | Starting paths | Why |
| --- | --- | --- |
| S | Shinkai, Kuroishi, Benisuna, Tamayori, Nemunoki, Hanamichi | Shinkai and Kuroishi combine damage and defense, and Benisuna has the strongest repeatable hit. Tamayori earns 21 Fame on the Market Truce, Nemunoki clears the clinic for 2 net stamina, and Hanamichi starts at 100 Looks with Striking and raises every contact’s Trust. |
| A | Kagaribi, Hoshikiri, Tsugihara, Uramori, Mizuhira, Isen | Kagaribi reads and copies attacks. Hoshikiri saves stamina and hits hard with lightning. Tsugihara, Uramori, and Mizuhira scale Fame through assignments. Isen gains extra Skill and can halve counters with an earth ward. |
| B | Oriba, Kogane, Fuyose, Kazetobi, Aon, Toriba | Oriba, Kogane, and Aon earn extra coins on assignments. Fuyose can freeze a counter and earn extra Fame. Kazetobi has a low-cost wind attack, and Toriba starts with a summon. |
| C | Shiohama, Kasamori, Mamekura, Sazame | The water ward, regeneration, chakra recovery, and zero-cost strike help in longer duels. These paths offer smaller bonuses to repeatable progression. |
| D | Clanless | Starts with extra Skill and has the Academy Sweep. Its mission rewards follow the standard schedule, and it can still collect every summon. |

## Build and install

Use JDK 17 or newer and Android SDK Platform 36. The Gradle wrapper and Android Gradle plugin are included in the build configuration. Set `ANDROID_HOME` to your Android SDK location or open the project in Android Studio.

```powershell
.\gradlew.bat :app:assembleDebug :app:bundleRelease :app:lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

On macOS or Linux, use `./gradlew` instead of `.\gradlew.bat`. The APK requires Android 7.0 or later. The release bundle at `app/build/outputs/bundle/release/app-release.aab` requires your upload key for Play Console submission.

The app works offline and draws its portraits and icons directly. It refreshes the current screen content after actions and loads the journal 12 entries at a time to keep older phones responsive.

To check all 23 starting paths, the eye powers, and the summon rules with just the JDK, run this from the project directory.

```powershell
$src = 'app/src/main/java/com/shinobilife/game'
javac --release 17 -encoding UTF-8 -d build/life-check "$src/Clans.java" "$src/Missions.java" "$src/CombatArt.java" "$src/EyePower.java" "$src/Summon.java" "$src/Life.java" checks/LifeCheck.java
java -cp build/life-check com.shinobilife.game.LifeCheck
```
