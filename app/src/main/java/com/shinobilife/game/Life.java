package com.shinobilife.game;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

import static com.shinobilife.game.Clans.Stat;

final class Life {
    static final String[] VILLAGES = {"Hazekawa", "Lanternbridge", "Starwatch", "Hidden Leaf", "Hidden Mist", "Hidden Sand", "Hidden Stone", "Hidden Cloud"};
    static final String[] SEASONS = {"Spring", "Summer", "Autumn", "Winter"};
    static final String[] CONTACTS = {"Nao", "Jun", "Mika"};
    static final String[] GENDERS = {"Unspecified", "Woman", "Man", "Nonbinary"};
    static final String[] FAMILIES = {"Village household", "Merchant family", "Clinic household", "Shinobi family", "Raised by an elder"};
    private static final String[] GUARDIANS = {"Nao", "Mina", "Rei", "Kenta", "Sumi", "Akari", "Haru", "Rin"};
    // Original roster indices are stable across saved versions.
    private static final int[][] LOCAL_CLANS = {
        {0, 5, 7}, {3, 4, 12, 16}, {6, 19}, {0, 2, 7, 11},
        {1, 5, 13, 18}, {2, 10, 14}, {9, 15, 17}, {6, 10, 19}
    };
    static final String[] VILLAGE_WORK = {"Guide travelers", "Staff the lantern market", "Chart the night sky",
            "Help at the academy", "Repair ferry lines", "Deliver water", "Restore ridge paths", "Carry weather reports"};

    private static final int[] VILLAGE_PAY = {45, 60, 40, 45, 50, 55, 55, 50};
    private static final int[] VILLAGE_FAME = {2, 1, 5, 4, 3, 2, 2, 3};

    String activityDescription(VillageAction action) {
        return action == VillageAction.MARKET
                ? "10 stamina · +" + VILLAGE_PAY[village] + " coins · +" + VILLAGE_FAME[village] + " Fame"
                : action.description;
    }

    enum ActivityGroup {
        TRAINING("Academy & training"), WORK("Work & public life"), SOCIAL("Family & leisure"), WELLNESS("Health & recovery");
        final String label;
        ActivityGroup(String label) { this.label = label; }
    }

    enum VillageAction {
        STUDY(ActivityGroup.TRAINING, "Study tactics", "8 stamina · +2 Skill", 8, 0, 0),
        MARKET(ActivityGroup.WORK, "Work a local shift", "", 10, 0, 0),
        FESTIVAL(ActivityGroup.SOCIAL, "Visit the seasonal festival", "8 stamina · +8 Happiness · +5 family Trust", 8, 0, 0),
        CLINIC(ActivityGroup.WELLNESS, "Volunteer at the clinic", "10 stamina · +10 Health · +3 Fame", 10, 0, 0),
        SPAR(ActivityGroup.TRAINING, "Spar at the dojo", "18 stamina · +5 Skill · −5 Health", 18, 0, 18),
        CLASS(ActivityGroup.TRAINING, "Take a chakra lesson", "10 stamina and 30 coins · +3 Skill · +15 Chakra", 10, 30, 0),
        TUTOR(ActivityGroup.WORK, "Tutor academy students", "12 stamina · +70 coins · +4 Fame", 12, 0, 22),
        EXHIBITION(ActivityGroup.WORK, "Enter a public exhibition", "16 stamina · +35 coins · +8 Fame", 16, 0, 30),
        DINNER(ActivityGroup.SOCIAL, "Host a family dinner", "25 coins · +10 family Trust · +6 Happiness", 0, 25, 0),
        TEAHOUSE(ActivityGroup.SOCIAL, "Meet friends at the teahouse", "15 coins · +6 friend Trust · +7 Happiness", 0, 15, 0),
        WALK(ActivityGroup.SOCIAL, "Walk the river trail", "6 stamina · +9 Happiness", 6, 0, 0),
        BATH(ActivityGroup.WELLNESS, "Visit the bathhouse", "40 coins · +25 Health · +8 Stamina", 0, 40, 0),
        NAP(ActivityGroup.WELLNESS, "Take a quiet afternoon", "Recover 12 stamina and 6 Health", 0, 0, 0),
        CRAFT(ActivityGroup.WORK, "Craft seals for the market", "14 stamina and 20 coins · +85 coins · +2 Skill", 14, 20, 16),
        FIELD(ActivityGroup.TRAINING, "Practice field drills", "14 stamina · +4 Skill · +2 Fame", 14, 0, 22),
        PICNIC(ActivityGroup.SOCIAL, "Invite your rival on a picnic", "18 coins · +8 rival Trust · +5 Happiness", 0, 18, 0);

        final ActivityGroup group;
        final String title, description;
        final int stamina, cost, skill;
        VillageAction(ActivityGroup group, String title, String description, int stamina, int cost, int skill) {
            this.group = group; this.title = title; this.description = description;
            this.stamina = stamina; this.cost = cost; this.skill = skill;
        }
        int mask() { return 1 << ordinal(); }
    }

    enum Career {
        MARKET_HAND("Market hand", 0, 55),
        COURIER("Village courier", 16, 75),
        INSTRUCTOR("Academy instructor", 26, 100),
        WARDEN("Village warden", 35, 125);
        final String title;
        final int skill, pay;
        Career(String title, int skill, int pay) {
            this.title = title; this.skill = skill; this.pay = pay;
        }
    }

    Career career = Career.MARKET_HAND;
    int careerShifts;

    int workPay() { return career.pay + Math.min(30, careerShifts / 3 * 10); }

    boolean changeCareer(Career next) {
        if (age() < ADULT_AGE || next == career || get(Stat.SKILL) < next.skill || enemyHp > 0 || worked) return false;
        career = next;
        careerShifts = 0;
        note("Started work as " + next.title, "The village accepted your application. Your starting pay is " + next.pay + " coins per shift.");
        return true;
    }

    static final int RESIDENTS_PER_VILLAGE = 12;
    static final int RESIDENT_COUNT = 4 + VILLAGES.length * RESIDENTS_PER_VILLAGE;
    private static final String[] RESIDENT_GIVEN = {"Aki", "Rei", "Nao", "Sora", "Iori", "Sumi", "Kenta", "Yori",
            "Mina", "Haru", "Akari", "Rin", "Noa", "Ren", "Kei", "Towa"};
    private static final String[] RESIDENT_SURNAMES = {"Asa", "Ise", "Fujino", "Hibari", "Taki", "Mori", "Senda", "Ota"};

    static final class Interest {
        static final Interest EMI = new Interest(0, "Emi", "Woman", "A seal maker who sketches lanterns by the river.", RomanceAction.WALK, 0, 3, -1);
        static final Interest HARU = new Interest(1, "Haru", "Man", "A courier who likes sharing travel stories over dinner.", RomanceAction.DINNER, 1, 11, -1);
        static final Interest RENKA = new Interest(2, "Renka", "Nonbinary", "An academy archivist who collects local legends.", RomanceAction.TALK, 2, 6, -1);
        static final Interest YUNA = new Interest(3, "Yuna", "Woman", "A clinic volunteer who saves time for long walks after work.", RomanceAction.WALK, 2, 18, -1);

        final String name, gender, about;
        final RomanceAction favorite;
        final int id, ageOffset, clan, village;
        Interest(int id, String name, String gender, String about, RomanceAction favorite, int ageOffset, int clan, int village) {
            this.id = id; this.name = name; this.gender = gender; this.about = about;
            this.favorite = favorite; this.ageOffset = ageOffset; this.clan = clan; this.village = village;
        }
        int ordinal() { return id; }
        int eyes() { EyePower eye = Clans.ALL[clan].eye(); return eye == null ? 0 : eye.mask(); }
    }

    private Interest[] residents;
    long worldSeed = new Random().nextLong();

    Interest[] residents() {
        if (residents != null) return residents;
        residents = new Interest[RESIDENT_COUNT];
        if (generation == 1 && !variedContacts) {
            residents[0] = Interest.EMI; residents[1] = Interest.HARU;
            residents[2] = Interest.RENKA; residents[3] = Interest.YUNA;
        }
        Random random = new Random(worldSeed);
        String[] jobs = {"Seal maker", "Academy aide", "Shopkeeper", "Courier", "Medic", "Gardener", "Scout", "Potter"};
        RomanceAction[] dates = {RomanceAction.TALK, RomanceAction.WALK, RomanceAction.DINNER};
        String[] likes = {"enjoys sharing stories", "likes evenings by the lanterns", "prefers a quiet dinner"};
        for (int i = 0; i < residents.length; i++) {
            if (residents[i] != null) continue;
            int village = i < 4 ? -1 : (i - 4) / RESIDENTS_PER_VILLAGE;
            int familyVillage = village < 0 ? homeVillage : village;
            int clan = rollClan(familyVillage, random);
            String given = RESIDENT_GIVEN[random.nextInt(RESIDENT_GIVEN.length)];
            String surname = RESIDENT_SURNAMES[random.nextInt(RESIDENT_SURNAMES.length)];
            String job = jobs[random.nextInt(jobs.length)];
            int taste = random.nextInt(dates.length);
            residents[i] = new Interest(i, given + " " + surname, GENDERS[1 + random.nextInt(3)],
                    job + " who " + likes[taste] + ".", dates[taste], random.nextInt(11) - 2, clan, village);
        }
        return residents;
    }

    int residentAge(Interest interest) {
        if (variedContacts && interest.id % 3 != 0) return Math.max(0, age() + interest.ageOffset);
        return Math.max(18, 18 + interest.ageOffset + (season - generationStart) / SEASONS.length);
    }

    enum RomanceAction {
        INTRODUCE("Introduce yourself", 0, 4, 0, 14, 2),
        TALK("Share stories", 1, 6, 0, 12, 3),
        WALK("Ask out on a lantern walk", 24, 8, 0, 16, 5),
        DINNER("Ask out on a dinner date", 24, 8, 28, 20, 7),
        COMMIT("Ask to become partners", 60, 0, 0, 0, 8),
        PROPOSE("Propose marriage", 90, 0, 100, 0, 12);

        final String label;
        final int affection, stamina, coins, gain, mood;
        RomanceAction(String label, int affection, int stamina, int coins, int gain, int mood) {
            this.label = label; this.affection = affection; this.stamina = stamina;
            this.coins = coins; this.gain = gain; this.mood = mood;
        }
    }

    final int[] affection = new int[RESIDENT_COUNT];
    final int[] romanceLastVisit = new int[RESIDENT_COUNT];
    int partner = -1;
    boolean married;

    int affection(Interest interest) { return affection[interest.ordinal()]; }
    boolean isPartner(Interest interest) { return partner == interest.ordinal(); }

    String romanceStatus(Interest interest, boolean residentsOnly) {
        if (isPartner(interest)) return married ? "Spouse" : "Partner";
        int value = affection(interest);
        if (residentsOnly || residentAge(interest) < ADULT_AGE) return value == 0 ? "New face" : "Acquaintance";
        return value == 0 ? "New face" : partner >= 0 ? "Acquaintance" : value < 24 ? "Getting acquainted" : "Ready to date";
    }

    boolean romanceVisited(Interest interest) { return romanceLastVisit[interest.ordinal()] == season + 1; }

    String romanceBlocker(Interest interest, RomanceAction action) {
        if (deceased) return "This life has ended.";
        if ((age() < ADULT_AGE || residentAge(interest) < ADULT_AGE)
                && action != RomanceAction.INTRODUCE && action != RomanceAction.TALK)
            return "Both people must be at least 18 to date.";
        if (age() < 3) return "You can meet village residents at age 3.";
        if (enemyHp > 0) return "Finish your active duel first.";
        if (partner >= 0 && !isPartner(interest) && action != RomanceAction.INTRODUCE && action != RomanceAction.TALK)
            return "You are already in an exclusive relationship.";
        if (romanceVisited(interest)) return "You already spent time together this season.";
        if (action == RomanceAction.INTRODUCE && affection(interest) > 0) return "You already know each other.";
        if (action == RomanceAction.COMMIT && partner >= 0) return "You are already partners.";
        if (action == RomanceAction.PROPOSE && (!isPartner(interest) || married)) return "Only an unmarried partner can propose.";
        if (affection(interest) < action.affection) return "Requires " + action.affection + " Affection.";
        if (get(Stat.STAMINA) < action.stamina || get(Stat.COINS) < action.coins) return "You need more stamina or coins.";
        return "";
    }

    int romanceGain(Interest interest, RomanceAction action) {
        if (action.gain == 0) return 0;
        return action.gain + (interest.favorite == action ? 5 : 0) + (get(Stat.LOOKS) >= 75 ? 2 : 0);
    }

    boolean romance(Interest interest, RomanceAction action) {
        if (!romanceBlocker(interest, action).isEmpty()) return false;
        romanceLastVisit[interest.ordinal()] = season + 1;
        add(Stat.STAMINA, -action.stamina);
        add(Stat.COINS, -action.coins);
        add(Stat.MOOD, action.mood);
        if (action == RomanceAction.COMMIT) {
            partner = interest.ordinal();
            note("Partners with " + interest.name, "You agreed to make the relationship official.");
        } else if (action == RomanceAction.PROPOSE) {
            married = true;
            note("Married " + interest.name, "Your partner said yes. You celebrated with a small ceremony in " + village() + ".");
        } else {
            int gained = Math.min(100 - affection(interest), romanceGain(interest, action));
            affection[interest.ordinal()] += gained;
            note(action == RomanceAction.INTRODUCE ? "Met " + interest.name : "Time with " + interest.name,
                    action.label + " · +" + gained + " Affection and +" + action.mood + " Happiness.");
        }
        return true;
    }

    boolean endRomance(Interest interest) {
        if (!isPartner(interest) || enemyHp > 0 || deceased) return false;
        note(married ? "Divorced " + interest.name : "Broke up with " + interest.name,
                "You ended the relationship. −12 Happiness.");
        married = false;
        partner = -1;
        affection[interest.ordinal()] = Math.min(20, affection(interest));
        romanceLastVisit[interest.ordinal()] = season + 1;
        add(Stat.MOOD, -12);
        return true;
    }

    static final String[] ROLES = {"Family", "Friend", "Rival"};
    static final String[] PRESENCE = {"Everyday", "Polished", "Striking"};
    static final int[] BASE_LOOKS = {52, 68, 84};
    static final int LOOK_COST = 35, GIFT_COST = 25, FAVOR_TRUST = 60;

    record Event(String date, String title, String detail) {}

    final List<Event> journal = new ArrayList<>();
    final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);
    final int[] bonds = {64, 52, 32};
    final int[] style = {0, 0, 1, 2, 0, 0, 0, 0, 0, 0};
    final int[] summonBonds = new int[Summon.ALL.length];

    String name;
    int clan, village, presence, season, completed, interacted, victories;
    boolean styled, worked, rested, meditated;
    int gender, family, guardian, homeVillage, villageActions;
    int gifted, favored;
    int scoutedMissions, bondedSummons, duelSummons;
    int enemyHp, round;
    int burnTurns, observedMove;
    boolean bindUsed;
    int inheritedEyes = -1, awakenedEyes, usedEyes, evolvedEyes, usedForms;
    final int[] eyeTrials = new int[EyePower.values().length];
    int clanPractice, guardianTurns, sealTurns;
    int childhoodActions;
    boolean variedContacts;
    enum Aptitude {
        NONE("No early aptitude", Stat.SKILL, 0),
        QUICK_STUDY("Quick study", Stat.SKILL, 6),
        DEEP_RESERVE("Deep chakra reserve", Stat.CHAKRA, 17),
        TOUGH_CONSTITUTION("Tough constitution", Stat.HEALTH, 12),
        HIGH_ENERGY("High energy", Stat.STAMINA, 14),
        SUNNY("Sunny disposition", Stat.MOOD, 14),
        CHARMING("Naturally charming", Stat.LOOKS, 8);

        final String title;
        final Stat stat;
        final int bonus;
        Aptitude(String title, Stat stat, int bonus) {
            this.title = title; this.stat = stat; this.bonus = bonus;
        }
    }
    Aptitude aptitude = Aptitude.NONE;
    static final int SHINOBI_AGE = 15, ADULT_AGE = 18;
    static final int[] START_AGES = {0, 8, 15, 18};
    static final String[] START_LABELS = {"Newborn", "Academy student · 8", "Teen shinobi · 15", "Adult · 18"};

    enum ChildhoodAction {
        PLAY("Play with family", "+6 Happiness · +4 family Trust", 0),
        REST("Take a nap", "+12 Stamina · +8 Health", 0),
        STORIES("Listen to village stories", "+2 Skill · +3 Happiness", 3),
        DRILLS("Practice academy drills", "8 Stamina · +3 Skill", 6);
        final String title, result;
        final int age;
        ChildhoodAction(String title, String result, int age) {
            this.title = title; this.result = result; this.age = age;
        }
        int mask() { return 1 << ordinal(); }
    }

    boolean childhoodAction(ChildhoodAction action) {
        if (deceased || age() >= SHINOBI_AGE || age() < action.age || (childhoodActions & action.mask()) != 0
                || (action == ChildhoodAction.DRILLS && get(Stat.STAMINA) < 8)) return false;
        childhoodActions |= action.mask();
        switch (action) {
            case PLAY -> { add(Stat.MOOD, 6); bonds[0] = Math.min(100, bonds[0] + 4); }
            case REST -> { add(Stat.STAMINA, 12); add(Stat.HEALTH, 8); }
            case STORIES -> { add(Stat.SKILL, 2); add(Stat.MOOD, 3); }
            case DRILLS -> { add(Stat.STAMINA, -8); add(Stat.SKILL, 3); }
        }
        note(action.title, action.result + ".");
        return true;
    }

    int mastery() {
        int skill = get(Stat.SKILL);
        return clanPractice >= 15 && skill >= 35 ? 2 : clanPractice >= 6 && skill >= 22 ? 1 : 0;
    }

    CombatArt clanArt() { return CombatArt.forClan(clan()).withMastery(mastery()); }

    boolean hasEvolved(EyePower eye) { return eye != null && (evolvedEyes & eye.mask()) != 0; }
    int eyeDamage(EyePower eye) { return eye.damage(observedMove) + (hasEvolved(eye) ? 6 : 0); }

    boolean canEvolve(EyePower eye) {
        return !deceased && enemyHp == 0 && (awakenedEyes & eye.mask()) != 0 && !hasEvolved(eye)
                && eyeTrials[eye.ordinal()] >= 3 && get(Stat.SKILL) >= 35;
    }

    boolean evolve(EyePower eye) {
        if (!canEvolve(eye)) return false;
        evolvedEyes |= eye.mask();
        note("Awakened the " + eye.evolution, "Your bloodline evolved. Its regular attack now deals 6 more damage.");
        return true;
    }

    String manifestationBlocker(EyePower eye) {
        if (!hasEvolved(eye) || get(Stat.SKILL) < 45 || challengerWins < 2) return "Needs an evolved eye, 45 Skill, and 2 circuit wins beyond Cedar Pass";
        if ((usedForms & eye.mask()) != 0) return "Already used this duel";
        return get(Stat.CHAKRA) < eye.formChakra ? "Low chakra" : "";
    }

    boolean castManifestation(EyePower eye) {
        if (enemyHp <= 0 || !manifestationBlocker(eye).isEmpty()) return false;
        usedForms |= eye.mask();
        add(Stat.CHAKRA, -eye.formChakra);
        if (eye == EyePower.SPARKGLASS) guardianTurns = 2;
        if (eye == EyePower.FARWATCH) sealTurns = 2;
        return resolveRound("You manifested " + eye.manifestation, eye.formDamage,
                eye == EyePower.HORIZON ? Counter.STOP : Counter.FULL);
    }
    String duelLog = "";
    int startAge = 18, generation = 1, generationStart;
    int deathAge = 72 + new Random(worldSeed).nextInt(25);
    int nextChildSeason;
    boolean deceased;
    static final int CHILD_COST = 80;
    record Child(String name, int gender, int clan, int otherClan, int eyes, int born, int village) {}
    final List<Child> children = new ArrayList<>();
    int otherClan = -1;

    String familyBlocker() {
        if (deceased || enemyHp > 0) return "Finish this life event first.";
        if (age() < ADULT_AGE) return "Family planning opens at age 18.";
        if (partner < 0 || affection[partner] < 70) return "Requires a partner with 70 Affection.";
        if (children.size() >= 6) return "Your household already has six children.";
        if (season < nextChildSeason) return "You can plan another child in a later season.";
        if (get(Stat.COINS) < CHILD_COST) return "You need " + CHILD_COST + " coins for family supplies.";
        return "";
    }

    static int inheritEyes(int firstParent, int secondParent, Random random) {
        int inherited = 0;
        for (EyePower eye : EyePower.values()) {
            int bit = eye.mask();
            boolean first = (firstParent & bit) != 0 && random.nextBoolean();
            boolean second = (secondParent & bit) != 0 && random.nextBoolean();
            if (first || second) inherited |= bit;
        }
        return inherited;
    }

    boolean welcomeChild(String childName, Random random) {
        if (childName.trim().isEmpty() || childName.length() > 24 || !familyBlocker().isEmpty()) return false;
        Interest parent = residents()[partner];
        boolean primaryFromPlayer = random.nextBoolean();
        int primary = primaryFromPlayer ? clan : parent.clan;
        int secondary = primaryFromPlayer ? parent.clan : clan;
        int eyes = inheritEyes(heritageEyes(), parent.eyes(), random);
        Child child = new Child(childName.trim(), 1 + random.nextInt(3), primary, secondary == primary ? -1 : secondary,
                eyes, season, village);
        children.add(child);
        add(Stat.COINS, -CHILD_COST);
        add(Stat.MOOD, 12);
        nextChildSeason = season + 4;
        note("Welcomed " + child.name(), "You and " + parent.name + " welcomed " + child.name() + ". "
                + bloodlineNames(eyes) + ".");
        return true;
    }

    int childAge(Child child) { return Math.max(0, (season - child.born()) / 4); }

    Life succeed(Child child) {
        if (!deceased || !children.contains(child)) return null;
        Life next = begin(child.name(), child.clan(), child.village(), 1, style);
        next.gender = child.gender();
        next.otherClan = child.otherClan();
        next.inheritedEyes = child.eyes();
        next.generation = generation + 1;
        next.season = Math.max(season, child.born() + 72);
        next.generationStart = next.season;
        next.startAge = Math.max(18, childAge(child));
        next.deathAge = Math.max(next.deathAge, next.startAge + 4);
        next.borderTension = borderTension;
        next.leafAtWar = leafAtWar;
        next.warSeasons = warSeasons;
        next.warEffort = warEffort;
        next.leafTreasury = leafTreasury;
        next.leafStability = leafStability;
        next.add(Stat.COINS, get(Stat.COINS) / 2);
        next.journal.clear();
        next.journal.addAll(journal);
        next.note("Generation " + next.generation + " · " + next.name,
                "You continue as " + next.name + ", child of " + name + ". You inherited half the estate. "
                + (next.inheritedEyes == 0 ? "You inherited no eye powers." : bloodlineNames(next.inheritedEyes) + " start dormant."));
        return next;
    }

    static Life begin(String name, int clan, int village, int presence, int[] style) {
        Life life = new Life();
        life.name = name;
        life.clan = clan;
        life.village = life.homeVillage = village;
        life.presence = presence;
        System.arraycopy(style, 0, life.style, 0, Math.min(style.length, life.style.length));
        life.stats.put(Stat.HEALTH, 82);
        life.stats.put(Stat.CHAKRA, 75);
        life.stats.put(Stat.STAMINA, 84);
        life.stats.put(Stat.MOOD, 70);
        life.stats.put(Stat.LOOKS, BASE_LOOKS[presence]);
        life.stats.put(Stat.FAME, 0);
        life.stats.put(Stat.COINS, 180);
        life.stats.put(Stat.SKILL, 10);
        life.add(life.clan().perk(), life.clan().start());
        life.grantStartingSummon();
        life.note("A life of your own", "You graduated from the " + life.village() + " academy. The mission board and the village are open to you.");
        return life;
    }

    private static int rollClan(int village, Random random) {
        int roll = random.nextInt(100);
        if (roll < 5 && village == 3) return 21;
        if (roll < 5 && (village == 2 || village == 4)) return 22;
        if ((roll < 5 && village == 7) || (roll >= 5 && roll < 10 && village == 2)) return 8;
        if (roll >= 75) return 20;
        int[] locals = LOCAL_CLANS[village];
        return locals[random.nextInt(locals.length)];
    }

    static Life rollNewLife(String name, int village, int gender, int presence, int[] style, Random random) {
        return rollNewLife(name, village, gender, presence, style, ADULT_AGE, random);
    }

    static Life rollNewLife(String name, int village, int gender, int presence, int[] style, int age, Random random) {
        int clan = rollClan(village, random);
        Life life = begin(name, clan, village, presence, style);
        life.gender = gender;
        life.startAge = age;
        life.variedContacts = true;
        life.worldSeed = random.nextLong();
        life.deathAge = 72 + new Random(life.worldSeed).nextInt(25);
        if (age < 6) life.stats.put(Stat.SKILL, life.clan().perk() == Stat.SKILL ? life.clan().start() : 0);
        for (int i = 0; i < life.bonds.length; i++) life.bonds[i] += random.nextInt(13) - 6;
        life.journal.clear();
        life.note(age == 0 ? "Born in " + life.village() : "A fresh start",
                age == 0 ? "Your family welcomed you in " + life.village() + "." :
                        "Your story began in " + life.village() + " at age " + age + ".");
        life.family = random.nextInt(FAMILIES.length);
        life.aptitude = Aptitude.values()[1 + random.nextInt(Aptitude.values().length - 1)];
        life.add(life.aptitude.stat, life.aptitude.bonus);
        life.borderTension = 16 + random.nextInt(22);
        life.guardian = (village + random.nextInt(GUARDIANS.length)) % GUARDIANS.length;
        String perk = switch (life.family) {
            case 1 -> { life.add(Stat.COINS, 75); yield "+75 Coins"; }
            case 2 -> { life.add(Stat.HEALTH, 12); yield "+12 Health"; }
            case 3 -> { life.add(Stat.SKILL, 3); yield "+3 Skill"; }
            case 4 -> { life.bonds[0] += 12; yield "+12 family Trust"; }
            default -> { life.add(Stat.MOOD, 10); yield "+10 Happiness"; }
        };
        life.note("Your roots in " + life.village(), GENDERS[gender] + " · " + life.clan().name()
                + ". " + FAMILIES[life.family] + " with " + life.contactName(0) + ". " + perk + ". " + life.aptitude.title + ".");
        return life;
    }

    String contactName(int contact) {
        if (contact == 0) return GUARDIANS[guardian];
        if (!variedContacts) return CONTACTS[contact];
        int index = Math.floorMod((int) (worldSeed >>> (contact * 13)), RESIDENT_GIVEN.length / 2);
        int surname = Math.floorMod((int) (worldSeed >>> (contact * 7)), RESIDENT_SURNAMES.length);
        return RESIDENT_GIVEN[index * 2 + contact - 1] + " " + RESIDENT_SURNAMES[surname];
    }

    boolean knowsContact(int contact) { return contact == 0 || age() >= (contact == 1 ? 3 : 6); }

    int contactAge(int contact) {
        if (!variedContacts) return contact == 0 ? age() + 20 : age();
        int offset = new Random(worldSeed ^ (0x9e3779b97f4a7c15L * (contact + 1))).nextInt(contact == 0 ? 19 : 5);
        return age() + (contact == 0 ? 20 + offset : offset - 2);
    }

    String contactHistory(int contact) {
        String name = contactName(contact);
        if (contact == 0) return name + " helped raise you in " + VILLAGES[homeVillage] + ".";
        if (contact == 1) return name + (age() < 6 ? " grew up playing with you near home." :
                " was one of your first friends in " + VILLAGES[homeVillage] + ".");
        return name + " first challenged you during an academy drill.";
    }

    boolean visitVillage(int destination) {
        if (enemyHp > 0 || destination < 0 || destination >= VILLAGES.length || destination == village
                || get(Stat.STAMINA) < 12 || get(Stat.COINS) < 20) return false;
        add(Stat.STAMINA, -12);
        add(Stat.COINS, -20);
        village = destination;
        note("Arrived in " + village(), "You paid 20 coins and spent 12 stamina traveling.");
        return true;
    }

    boolean villageActivity(VillageAction action) {
        if ((villageActions & action.mask()) != 0 || enemyHp > 0 || get(Stat.STAMINA) < action.stamina
                || get(Stat.COINS) < action.cost || get(Stat.SKILL) < action.skill) return false;
        villageActions |= action.mask();
        add(Stat.STAMINA, -action.stamina);
        add(Stat.COINS, -action.cost);
        switch (action) {
            case STUDY -> add(Stat.SKILL, 2);
            case MARKET -> { add(Stat.COINS, VILLAGE_PAY[village]); add(Stat.FAME, VILLAGE_FAME[village]); }
            case FESTIVAL -> { add(Stat.MOOD, 8); bonds[0] = Math.min(100, bonds[0] + 5); }
            case CLINIC -> { add(Stat.HEALTH, 10); add(Stat.FAME, 3); }
            case SPAR -> { add(Stat.SKILL, 5); add(Stat.HEALTH, -5); }
            case CLASS -> { add(Stat.SKILL, 3); add(Stat.CHAKRA, 15); }
            case TUTOR -> { add(Stat.COINS, 70); add(Stat.FAME, 4); }
            case EXHIBITION -> { add(Stat.COINS, 35); add(Stat.FAME, 8); }
            case DINNER -> { bonds[0] = Math.min(100, bonds[0] + 10); add(Stat.MOOD, 6); }
            case TEAHOUSE -> { bonds[1] = Math.min(100, bonds[1] + 6); add(Stat.MOOD, 7); }
            case WALK -> add(Stat.MOOD, 9);
            case BATH -> { add(Stat.HEALTH, 25); add(Stat.STAMINA, 8); }
            case NAP -> { add(Stat.STAMINA, 12); add(Stat.HEALTH, 6); }
            case CRAFT -> { add(Stat.COINS, 85); add(Stat.SKILL, 2); }
            case FIELD -> { add(Stat.SKILL, 4); add(Stat.FAME, 2); }
            case PICNIC -> { bonds[2] = Math.min(100, bonds[2] + 8); add(Stat.MOOD, 5); }
        }
        note(action == VillageAction.MARKET ? VILLAGE_WORK[village] : action.title,
                village() + " · " + activityDescription(action));
        return true;
    }

    boolean escortWithScout() {
        if (!hasSummon(Summon.SHRIKE) || get(Stat.CHAKRA) < Summon.MISSION_CHAKRA
                || !take(Missions.CARAVAN, false)) return false;
        add(Stat.CHAKRA, -Summon.MISSION_CHAKRA);
        add(Stat.COINS, 30);
        add(Stat.FAME, 3);
        note("A lantern in the pass", "Your shrike spotted the ambush. The caravan avoided it. +30 Coins and +3 Fame.");
        return true;
    }

    boolean escortShortcut() {
        boolean prepared = get(Stat.SKILL) >= 22;
        if (!take(Missions.CARAVAN, false)) return false;
        if (prepared) {
            add(Stat.COINS, 65);
            add(Stat.FAME, 5);
            note("Shortcut secured", "You spotted the ambush early and got the caravan through. +65 Coins and +5 Fame.");
        } else {
            add(Stat.HEALTH, -20);
            add(Stat.COINS, -45);
            note("Caravan ambushed", "You got the caravan through, but were injured and lost part of the payment. −20 Health and −45 Coins.");
        }
        return true;
    }

    Clans.Clan clan() { return Clans.ALL[clan]; }
    String village() { return VILLAGES[village]; }
    int age() { return startAge + (season - generationStart) / SEASONS.length; }
    String date() { return SEASONS[season % SEASONS.length] + " · Age " + age(); }
    enum AcademyRank {
        GENIN("Genin"), CHUNIN("Chūnin"), JONIN("Jōnin");
        final String title;
        AcademyRank(String title) { this.title = title; }
    }

    enum Lesson {
        TACTICS("Tactics class", "+4 Skill", 4, 0, 0),
        CONTROL("Chakra control class", "+2 Skill · +18 Chakra", 2, 18, 0),
        TEAMWORK("Field teamwork class", "+2 Skill · +3 Fame", 2, 0, 3);
        final String title, effect;
        final int skill, chakra, fame;
        Lesson(String title, String effect, int skill, int chakra, int fame) {
            this.title = title; this.effect = effect; this.skill = skill; this.chakra = chakra; this.fame = fame;
        }
    }

    static final int LESSON_STAMINA = 10, LEAF = 3;
    AcademyRank academyRank = AcademyRank.GENIN;
    int academyCredits;
    boolean attendedClass, campaigned, governed, warService;

    String rank() {
        if (age() < 6) return "Child";
        if (age() < SHINOBI_AGE) return "Academy student";
        return hokage ? "Hokage" : academyRank.title;
    }

    AcademyRank nextRank() {
        return switch (academyRank) {
            case GENIN -> AcademyRank.CHUNIN;
            case CHUNIN -> AcademyRank.JONIN;
            case JONIN -> null;
        };
    }

    boolean attendClass(Lesson lesson) {
        if (age() < 6 || deceased || enemyHp > 0 || attendedClass || get(Stat.STAMINA) < LESSON_STAMINA) return false;
        attendedClass = true;
        add(Stat.STAMINA, -LESSON_STAMINA);
        add(Stat.SKILL, lesson.skill);
        add(Stat.CHAKRA, lesson.chakra);
        add(Stat.FAME, lesson.fame);
        academyCredits = Math.min(10, academyCredits + 1);
        note(lesson.title, "You attended the " + village() + " academy. " + lesson.effect + " · +1 class credit.");
        return true;
    }

    String examRequirements() {
        return academyRank == AcademyRank.GENIN ? "2 class credits · 22 Skill · 1 duel win · 16 stamina"
                : "3 new class credits · 35 Skill · 3 total duel wins · 22 stamina";
    }

    boolean canTakeExam() {
        if (age() < SHINOBI_AGE || deceased || enemyHp > 0 || nextRank() == null) return false;
        boolean first = academyRank == AcademyRank.GENIN;
        return academyCredits >= (first ? 2 : 3) && get(Stat.SKILL) >= (first ? 22 : 35)
                && victories >= (first ? 1 : 3) && get(Stat.STAMINA) >= (first ? 16 : 22);
    }

    boolean takeExam() {
        if (!canTakeExam()) return false;
        add(Stat.STAMINA, academyRank == AcademyRank.GENIN ? -16 : -22);
        add(Stat.FAME, academyRank == AcademyRank.GENIN ? 8 : 12);
        academyRank = nextRank();
        academyCredits = 0;
        note("Promoted to " + academyRank.title, "You passed the " + academyRank.title + " examination in " + village() + ".");
        return true;
    }

    int campaignSupport, approval, borderTension = 24, leafStability = 75, leafTreasury = 180, warSeasons, warEffort;
    boolean hokage, leafAtWar;

    int electionSupport() {
        int trust = (bonds[0] + bonds[1] + bonds[2]) / 3;
        return Math.min(100, (get(Stat.FAME) * 6 + trust * 4) / 10 + campaignSupport);
    }

    String villageLeader() {
        if (village == LEAF && hokage) return name + " · Hokage";
        int index = 4 + village * RESIDENTS_PER_VILLAGE + (season / 16) % RESIDENTS_PER_VILLAGE;
        return residents()[index].name + (village == LEAF ? " · Hokage" : " · Village head");
    }

    boolean campaign() {
        if (deceased || enemyHp > 0 || village != LEAF || hokage || campaigned
                || get(Stat.COINS) < 25 || campaignSupport >= 30) return false;
        campaigned = true;
        add(Stat.COINS, -25);
        campaignSupport = Math.min(30, campaignSupport + 6);
        note("Met with Hidden Leaf residents", "You organized an open meeting and earned support for your candidacy.");
        return true;
    }

    boolean canRunForHokage() {
        return age() >= ADULT_AGE && !deceased && enemyHp == 0 && village == LEAF && !hokage && academyRank == AcademyRank.JONIN
                && get(Stat.FAME) >= 55 && electionSupport() >= 60;
    }

    boolean runForHokage() {
        if (!canRunForHokage()) return false;
        approval = electionSupport();
        hokage = true;
        note("Elected Hokage", "The Hidden Leaf council confirmed your election. You can now make one council decision each season.");
        return true;
    }

    enum Policy {
        RELIEF("Fund civilian relief", "70 village coins · +16 stability · +5 approval", 70),
        DIPLOMACY("Negotiate with Hidden Stone", "35 village coins · −25 tension. A truce is signed at 35 tension or lower.", 35),
        DEFENSE("Protect village shelters", "50 village coins · +10 stability · +6 tension", 50),
        WAR("Declare war on Hidden Stone", "Requires 40 tension · starts war · −8 stability", 0);
        final String title, description;
        final int cost;
        Policy(String title, String description, int cost) {
            this.title = title; this.description = description; this.cost = cost;
        }
    }

    String policyBlocker(Policy policy) {
        if (deceased || enemyHp > 0 || village != LEAF || !hokage) return "Requires the Hokage in Hidden Leaf.";
        if (governed) return "You already made a council decision this season.";
        if (leafTreasury < policy.cost) return "The village treasury cannot cover this.";
        if (policy == Policy.WAR && (leafAtWar || borderTension < 40)) return "Requires peace and at least 40 tension.";
        return "";
    }

    boolean govern(Policy policy) {
        if (!policyBlocker(policy).isEmpty()) return false;
        governed = true;
        leafTreasury -= policy.cost;
        switch (policy) {
            case RELIEF -> {
                leafStability = Math.min(100, leafStability + 16);
                approval = Math.min(100, approval + 5);
                if (leafAtWar) warEffort = Math.min(60, warEffort + 12);
            }
            case DIPLOMACY -> {
                borderTension = Math.max(0, borderTension - 25);
                if (leafAtWar && borderTension <= 35) {
                    leafAtWar = false;
                    warSeasons = warEffort = 0;
                    note("Truce with Hidden Stone", "Both villages agreed to stop the fighting.");
                    return true;
                }
            }
            case DEFENSE -> {
                leafStability = Math.min(100, leafStability + 10);
                borderTension = Math.min(100, borderTension + 6);
                if (leafAtWar) warEffort = Math.min(60, warEffort + 18);
            }
            case WAR -> {
                leafAtWar = true;
                warSeasons = warEffort = 0;
                borderTension = Math.max(75, borderTension);
                leafStability = Math.max(0, leafStability - 8);
            }
        }
        note(policy.title, "Hidden Leaf council · " + policy.description);
        return true;
    }

    boolean helpDuringWar(boolean supplies) {
        int stamina = supplies ? 12 : 16;
        if (deceased || enemyHp > 0 || village != LEAF || !leafAtWar || warService
                || get(Stat.STAMINA) < stamina || (!supplies && get(Stat.HEALTH) < 20)) return false;
        warService = true;
        add(Stat.STAMINA, -stamina);
        if (!supplies) add(Stat.HEALTH, -10);
        add(Stat.FAME, supplies ? 5 : 8);
        leafStability = Math.min(100, leafStability + (supplies ? 7 : 11));
        warEffort = Math.min(60, warEffort + (supplies ? 10 : 14));
        note(supplies ? "Delivered emergency supplies" : "Escorted civilians to safety",
                "You helped Hidden Leaf during the war. The village gained stability.");
        return true;
    }

    private void updateLeafPolitics() {
        leafTreasury = Math.min(400, leafTreasury + (leafAtWar ? 10 : 25));
        if (!leafAtWar && season % 4 == 2) {
            borderTension = Math.min(100, borderTension + 8);
            if (borderTension >= 75) {
                leafAtWar = true;
                note("War on the border", "Diplomacy broke down and Hidden Leaf went to war with Hidden Stone.");
            } else if (borderTension >= 55) {
                note("Border tensions rise", "Hidden Leaf and Hidden Stone are struggling to reach an agreement.");
            }
        }
        if (leafAtWar && warEffort >= 60) {
            leafAtWar = false;
            borderTension = 30;
            warSeasons = warEffort = 0;
            if (hokage) approval = Math.min(100, approval + 12);
            note("Hidden Leaf held the border", "With the village protected and supplies secured, Hidden Stone withdrew.");
        }
        if (leafAtWar && ++warSeasons >= 6 && !hokage) {
            leafAtWar = false;
            borderTension = 35;
            warSeasons = warEffort = 0;
            note("Council-brokered ceasefire", "Hidden Leaf and Hidden Stone agreed to suspend hostilities.");
        }
        if (leafAtWar) {
            leafStability = Math.max(0, leafStability - 4);
            if (hokage && leafStability < 45) approval = Math.max(0, approval - 4);
            if (village == LEAF || village == 6) add(Stat.MOOD, -4);
            if (leafStability == 0) {
                leafAtWar = false;
                borderTension = 35;
                warSeasons = warEffort = 0;
                leafTreasury = Math.max(0, leafTreasury - 50);
                hokage = false;
                note("Hidden Leaf accepted defeat", "The council accepted emergency peace terms and called for new leadership.");
            }
        } else leafStability = Math.min(100, leafStability + 2);
    }

    record SeasonChoice(String title, Stat priceStat, int price, Stat rewardStat, int reward, String outcome) {
        String terms() {
            return (price == 0 ? "" : price + " " + priceStat.label + " · ") + "+" + reward + " " + rewardStat.label;
        }
    }

    enum SeasonEvent {
        FLOOD("Flood at the ferry", "The river washed away the ferry landing. Neighbors are hauling timber uphill.",
                new SeasonChoice("Help rebuild", Stat.STAMINA, 12, Stat.FAME, 8, "You worked alongside the ferry crew until the landing reopened."),
                new SeasonChoice("Pay for a temporary ferry", Stat.COINS, 35, Stat.BOND, 6, "Your payment got families across before nightfall.")),
        ARCHIVE("A disputed scroll", "The academy archivist wants a second opinion on a damaged scroll.",
                new SeasonChoice("Study the old script", Stat.STAMINA, 10, Stat.SKILL, 4, "You reconstructed a training exercise from the surviving lines."),
                new SeasonChoice("Buy a private reading", Stat.COINS, 45, Stat.CHAKRA, 26, "The archivist traded a guided reading for money to restore the scroll.")),
        MARKET("The lantern market", "The evening stalls are short on staff, and your neighbors have set aside a table.",
                new SeasonChoice("Cover a stall", Stat.STAMINA, 12, Stat.COINS, 80, "The stall owner split the evening profits with you."),
                new SeasonChoice("Host the table", Stat.COINS, 22, Stat.MOOD, 15, "Your neighbors stayed long after the lanterns burned down.")),
        CARAVAN("Medicine on the ridge", "A merchant is carrying medicine to a settlement before the weather turns.",
                new SeasonChoice("Carry the crates", Stat.STAMINA, 14, Stat.COINS, 70, "You delivered the medicine before the road closed."),
                new SeasonChoice("Donate to the clinic", Stat.COINS, 30, Stat.FAME, 9, "The clinic treated several patients using supplies you funded.")),
        GARDEN("The abandoned garden", "Your academy friends want to turn a ruined courtyard into a public garden.",
                new SeasonChoice("Clear the courtyard", Stat.STAMINA, 10, Stat.BOND, 7, "You spent the afternoon clearing stones together."),
                new SeasonChoice("Buy seedlings", Stat.COINS, 25, Stat.MOOD, 13, "The first plants went in before sunset.")),
        EXHIBITION("A public demonstration", "Traveling students want to see how local shinobi train.",
                new SeasonChoice("Demonstrate footwork", Stat.STAMINA, 14, Stat.FAME, 10, "The students repeated your drill and thanked you at the gate."),
                new SeasonChoice("Join the practice", Stat.CHAKRA, 14, Stat.SKILL, 5, "Trading drills with the travelers sharpened your technique."));

        final String title, situation;
        final SeasonChoice[] choices;
        SeasonEvent(String title, String situation, SeasonChoice first, SeasonChoice second) {
            this.title = title; this.situation = situation; choices = new SeasonChoice[]{first, second};
        }
    }

    int resolvedEvent = -1;

    SeasonEvent seasonalEvent() {
        SeasonEvent[] options = SeasonEvent.values();
        int cycle = season / options.length;
        shuffleSeasonEvents(options, cycle);
        int index = season % options.length;
        if (cycle > 0 && index < 2) {
            SeasonEvent[] previous = SeasonEvent.values();
            shuffleSeasonEvents(previous, cycle - 1);
            if (options[0] == previous[previous.length - 1]) index = 1 - index;
        }
        return options[index];
    }

    private void shuffleSeasonEvents(SeasonEvent[] options, int cycle) {
        Random random = new Random(worldSeed ^ (0x9e3779b97f4a7c15L * (cycle + 1L)));
        for (int i = options.length - 1; i > 0; i--) {
            int other = random.nextInt(i + 1);
            SeasonEvent event = options[i];
            options[i] = options[other];
            options[other] = event;
        }
    }

    boolean hasSeasonEvent() { return age() >= SHINOBI_AGE && !deceased && enemyHp == 0 && resolvedEvent != season; }

    boolean resolveSeasonEvent(int choice) {
        if (!hasSeasonEvent() || choice < 0 || choice >= seasonalEvent().choices.length) return false;
        SeasonEvent event = seasonalEvent();
        SeasonChoice option = event.choices[choice];
        if (get(option.priceStat()) < option.price()) return false;
        resolvedEvent = season;
        add(option.priceStat(), -option.price());
        add(option.rewardStat(), option.reward());
        note(event.title, option.outcome() + " " + option.terms() + ".");
        return true;
    }

    enum Rival {
        IBARA("Ibara", "Cedar Pass", "Alternates quick jabs with a heavier third strike.",
                AcademyRank.GENIN, 72, 10, 18, 3, "Stone Jab", "Cedar Breaker", Missions.DUEL.coins, Missions.DUEL.fame),
        KANA("Kana", "Lantern Bridge", "Starts with a heavy opening and repeats it every other round.",
                AcademyRank.CHUNIN, 88, 7, 21, 2, "River Cut", "Lantern Rush", 170, 19),
        DAICHI("Daichi", "Storm Court", "Keeps steady pressure and delivers a heavy strike every third round.",
                AcademyRank.JONIN, 104, 14, 25, 3, "Thunder Palm", "Storm Hammer", 240, 25);

        final String name, arena, description, lightName, heavyName;
        final AcademyRank rank;
        final int hp, light, heavy, cadence, coins, fame;

        Rival(String name, String arena, String description, AcademyRank rank, int hp, int light, int heavy,
              int cadence, String lightName, String heavyName, int coins, int fame) {
            this.name = name; this.arena = arena; this.description = description; this.rank = rank;
            this.hp = hp; this.light = light; this.heavy = heavy; this.cadence = cadence;
            this.lightName = lightName; this.heavyName = heavyName; this.coins = coins; this.fame = fame;
        }
        int damage(int turn) { return (this == KANA ? turn % cadence == 1 : turn % cadence == 0) ? heavy : light; }
        String attackName(int damage) { return damage == heavy ? heavyName : lightName; }
    }

    Rival duelRival = Rival.IBARA;
    int challengerWins;

    boolean enterDuel(Rival rival) {
        if (deceased || academyRank.ordinal() < rival.rank.ordinal() || !canStart(Missions.DUEL)) return false;
        completed |= Missions.DUEL.mask();
        add(Stat.STAMINA, -Missions.DUEL.stamina);
        duelRival = rival;
        enemyHp = rival.hp;
        round = 1;
        burnTurns = observedMove = duelSummons = usedEyes = usedForms = guardianTurns = sealTurns = 0;
        bindUsed = false;
        duelLog = rival.name + " bows. The referee calls the first round.";
        return true;
    }

    int signedSummonCount() {
        int count = 0;
        for (Summon summon : Summon.ALL) if (hasSummon(summon)) count++;
        return count;
    }
    int trustedAllyCount() {
        int count = 0;
        for (int bond : bonds) if (bond >= 75) count++;
        return count;
    }
    static String trustTier(int bond) {
        if (bond >= 90) return "Sworn Bond";
        if (bond >= 60) return "Trusted Ally";
        if (bond >= 40) return "Friend";
        return "Acquaintance";
    }
    int get(Stat stat) { return stats.getOrDefault(stat, 0); }

    void add(Stat stat, int change) {
        if (stat == Stat.BOND) {
            for (int i = 0; i < bonds.length; i++) bonds[i] = Math.min(100, bonds[i] + change);
            return;
        }
        int cap = stat == Stat.COINS ? Integer.MAX_VALUE : 100;
        stats.put(stat, (int) Math.max(0, Math.min(cap, (long) get(stat) + change)));
    }

    void note(String title, String detail) {
        journal.add(0, new Event(date(), title, detail));
        if (journal.size() > 100) journal.remove(journal.size() - 1);
    }

    void advance() {
        if (deceased || enemyHp > 0) return;
        season++;
        if (age() >= deathAge) {
            deceased = true;
            note(name + " passed away", "Your life ended at age " + age() + ". "
                    + (children.isEmpty() ? "You had no heirs." : "Your children can carry on the family line."));
            return;
        }
        completed = interacted = bondedSummons = gifted = favored = villageActions = 0;
        styled = worked = rested = meditated = false;
        attendedClass = campaigned = governed = warService = false;
        childhoodActions = 0;
        add(Stat.STAMINA, 45);
        add(Stat.CHAKRA, 48);
        add(Stat.HEALTH, 14);
        String[] moments = {
            "The cooks set up breakfast stalls outside the academy.",
            "The bridge lanterns stayed lit late for the first summer market.",
            "Red maple leaves drifted over the gate as the evenings grew cooler.",
            "The village shared a pot of ginger broth after the first frost."
        };
        String detail = moments[season % moments.length] + " You recovered health, chakra, and stamina.";
        if (get(Stat.FAME) >= 60) {
            detail = moments[season % moments.length] + " The market crowd cheered when you arrived. +5 Happiness.";
            add(Stat.MOOD, 5);
        } else if (get(Stat.FAME) >= 25) {
            detail = moments[season % moments.length] + " A shopkeeper recognized you from the mission board. +3 Happiness.";
            add(Stat.MOOD, 3);
        }
        note((season - generationStart) % SEASONS.length == 0 ? "You turned " + age() : SEASONS[season % SEASONS.length] + " arrived", detail);
        if ((season - generationStart) % 4 == 0) {
            switch (age()) {
                case 6 -> note("First day at the academy", "You can attend classes and start earning credits.");
                case SHINOBI_AGE -> note("Became a Genin", "The mission board, duels, and inherited eye training are open.");
                case ADULT_AGE -> note("Reached adulthood", "Careers, dating, and starting a family are open.");
                default -> {
                    if (age() < SHINOBI_AGE && age() > 0) childhoodMemory();
                }
            }
        }
        updateLeafPolitics();
    }

    private void childhoodMemory() {
        Random random = new Random(worldSeed ^ (age() * 0x9e3779b97f4a7c15L));
        switch (random.nextInt(4)) {
            case 0 -> {
                add(Stat.MOOD, 5);
                note("A surprise at home", contactName(0) + " made your favorite meal to mark the year. +5 Happiness.");
            }
            case 1 -> {
                add(Stat.SKILL, 2);
                note("An afternoon of questions", "You listened to a traveler explain how birds find their way home. +2 Skill.");
            }
            case 2 -> {
                add(Stat.HEALTH, -4);
                note("A rainy week", "A small cold kept you indoors for a few days. −4 Health.");
            }
            default -> {
                bonds[0] = Math.min(100, bonds[0] + 5);
                note("Family keepsake", contactName(0) + " told you the story behind a family keepsake. +5 family Trust.");
            }
        }
    }

    boolean train() {
        if (get(Stat.STAMINA) < 16 || get(Stat.SKILL) == 100) return false;
        add(Stat.STAMINA, -16);
        add(Stat.SKILL, 3);
        note("Training at the academy", "You practiced footwork and defensive drills. +3 Skill.");
        return true;
    }

    boolean styleUp() {
        if (styled || get(Stat.COINS) < LOOK_COST || get(Stat.LOOKS) == 100) return false;
        styled = true;
        add(Stat.COINS, -LOOK_COST);
        add(Stat.LOOKS, 8);
        note("A fresh look", "A stylist helped you find a look you feel good in. +8 Looks.");
        return true;
    }

    boolean work() {
        if (age() < ADULT_AGE || worked || get(Stat.STAMINA) < 12) return false;
        worked = true;
        add(Stat.STAMINA, -12);
        int pay = workPay();
        careerShifts++;
        add(Stat.COINS, pay);
        note("A shift as " + career.title, "You completed your work for the season. +" + pay + " Coins.");
        return true;
    }

    boolean rest() {
        if (rested) return false;
        rested = true;
        add(Stat.HEALTH, 20);
        add(Stat.MOOD, 6);
        note("An afternoon off", "You rested in the garden near the academy. +20 Health and +6 Happiness.");
        return true;
    }

    boolean meditate() {
        if (meditated || enemyHp > 0) return false;
        meditated = true;
        add(Stat.CHAKRA, 22);
        add(Stat.MOOD, 4);
        add(Stat.SKILL, 1);
        note("Shrine meditation", "You focused your breathing beneath the cedar trees. +22 Chakra, +4 Happiness, and +1 Skill.");
        return true;
    }

    boolean interact(int contact) {
        if (!knowsContact(contact) || (interacted & (1 << contact)) != 0) return false;
        interacted |= 1 << contact;
        int gain = get(Stat.LOOKS) >= 75 ? 7 : 5;
        bonds[contact] = Math.min(100, bonds[contact] + gain);
        add(Stat.MOOD, 3);
        note("Time with " + contactName(contact), "You traded stories after the market closed. +" + gain + " Trust.");
        return true;
    }

    boolean gift(int contact) {
        if (!knowsContact(contact) || enemyHp > 0 || (gifted & (1 << contact)) != 0 || get(Stat.COINS) < GIFT_COST || bonds[contact] == 100) return false;
        gifted |= 1 << contact;
        add(Stat.COINS, -GIFT_COST);
        int gain = Math.min(6, 100 - bonds[contact]);
        bonds[contact] += gain;
        add(Stat.MOOD, 4);
        note("A gift for " + contactName(contact), "You brought a lantern keepsake from the market. +" + gain + " Trust and +4 Happiness.");
        return true;
    }

    boolean canAllyFavor(int contact) {
        return knowsContact(contact) && enemyHp == 0 && bonds[contact] >= FAVOR_TRUST && (favored & (1 << contact)) == 0;
    }

    boolean allyFavor(int contact) {
        if (!canAllyFavor(contact)) return false;
        favored |= 1 << contact;
        switch (contact) {
            case 0 -> {
                add(Stat.STAMINA, 16);
                add(Stat.HEALTH, 12);
                note("Home cooking with Nao", "Nao packed spiced rice balls and ginger tea for the road. +16 Stamina and +12 Health.");
            }
            case 1 -> {
                add(Stat.COINS, 40);
                add(Stat.CHAKRA, 12);
                note("Jun's market tip", "Jun pointed out an unguarded courier shortcut and split the bonus. +40 Coins and +12 Chakra.");
            }
            case 2 -> {
                add(Stat.SKILL, 2);
                add(Stat.FAME, 5);
                note("Rooftop spar with Mika", "You drew a crowd trading footwork on the academy roof. +2 Skill and +5 Fame.");
            }
        }
        return true;
    }

    boolean isComplete(Missions mission) { return (completed & mission.mask()) != 0; }

    boolean canStart(Missions mission) {
        return age() >= SHINOBI_AGE && enemyHp == 0 && !isComplete(mission) && get(Stat.STAMINA) >= mission.stamina
                && (mission != Missions.DUEL || get(Stat.HEALTH) >= 30);
    }

    boolean matches(Missions mission) { return clan().role() == mission.role; }

    boolean take(Missions mission, boolean technique) {
        return take(mission, technique, null);
    }

    boolean take(Missions mission, boolean technique, Summon summon) {
        if (!canStart(mission) || (technique && (mission == Missions.DUEL
                || !matches(mission) || get(Stat.CHAKRA) < clan().cost()))) return false;
        if (summon != null && (technique || !canSummonOnMission(summon, mission))) return false;
        if (mission == Missions.DUEL) return enterDuel(Rival.IBARA);
        completed |= mission.mask();
        add(Stat.STAMINA, -mission.stamina);
        scoutedMissions |= mission.mask();
        String detail = "+" + mission.coins + " Coins · +" + mission.fame + " Fame";
        add(Stat.COINS, mission.coins);
        add(Stat.FAME, mission.fame);
        if (technique) {
            add(Stat.CHAKRA, -clan().cost());
            add(clan().bonus(), clan().amount());
            detail = clan().action() + " " + detail + " · +" + clan().amount() + " " + clan().bonus().label;
        }
        if (summon != null) {
            add(Stat.CHAKRA, -Summon.MISSION_CHAKRA);
            add(summon.bonus, summon.bonusAmount);
            detail = summon.title + " answered your call. " + detail
                    + " · +" + summon.bonusAmount + " " + summon.bonus.label;
        }
        note(mission.title, detail);
        return true;
    }

    void grantStartingSummon() {
        if (clan().name().equals("Toriba")) summonBonds[Summon.SHRIKE.ordinal()] = Summon.STARTING_BOND;
    }

    boolean hasSummon(Summon summon) { return summonBond(summon) > 0; }
    int summonBond(Summon summon) { return summonBonds[summon.ordinal()]; }
    boolean knowsSummon(Summon summon) { return (scoutedMissions & summon.mission.mask()) != 0; }
    boolean bondedThisSeason(Summon summon) { return (bondedSummons & summon.mask()) != 0; }

    boolean signSummon(Summon summon) {
        if (enemyHp > 0 || hasSummon(summon) || !knowsSummon(summon) || get(Stat.COINS) < summon.price) return false;
        add(Stat.COINS, -summon.price);
        summonBonds[summon.ordinal()] = Summon.STARTING_BOND;
        note("A pact with " + summon.title, "You signed a summon contract after helping with the "
                + summon.mission.shortName + ". " + summon.title + " can now join you in missions and duels.");
        return true;
    }

    boolean bondSummon(Summon summon) {
        if (enemyHp > 0 || !hasSummon(summon) || bondedThisSeason(summon) || summonBond(summon) == 100
                || get(Stat.STAMINA) < Summon.BOND_STAMINA) return false;
        bondedSummons |= summon.mask();
        int gain = Math.min(20, 100 - summonBond(summon));
        summonBonds[summon.ordinal()] += gain;
        add(Stat.STAMINA, -Summon.BOND_STAMINA);
        note("Time with " + summon.title, "You trained together at the shrine. +" + gain + " Bond.");
        return true;
    }

    boolean canSummonOnMission(Summon summon, Missions mission) {
        return summon.mission == mission && hasSummon(summon) && get(Stat.CHAKRA) >= Summon.MISSION_CHAKRA;
    }

    int heritageEyes() {
        if (inheritedEyes >= 0) return inheritedEyes;
        EyePower eye = clan().eye();
        return eye == null ? 0 : eye.mask();
    }

    static String bloodlineNames(int mask) {
        List<String> names = new ArrayList<>();
        for (EyePower eye : EyePower.values()) if ((mask & eye.mask()) != 0) names.add(eye.title);
        return names.isEmpty() ? "No inherited eye power" : String.join(" + ", names);
    }

    EyePower portraitEye() {
        for (EyePower eye : EyePower.values()) if ((awakenedEyes & eye.mask()) != 0) return eye;
        return null;
    }

    boolean canAwakenEye(EyePower eye) {
        return age() >= SHINOBI_AGE && eye != null && !deceased && (heritageEyes() & eye.mask()) != 0 && (awakenedEyes & eye.mask()) == 0
                && enemyHp == 0 && get(Stat.SKILL) >= eye.skill && victories >= eye.wins;
    }

    boolean awakenEye() { return awakenEye(clan().eye()); }

    boolean awakenEye(EyePower eye) {
        if (!canAwakenEye(eye)) return false;
        awakenedEyes |= eye.mask();
        note(eye.title + " awakened", "You can now use " + eye.move + " in duels. " + eye.description());
        return true;
    }

    String eyeDuelBlocker(EyePower eye) {
        if (eye == null || (awakenedEyes & eye.mask()) == 0) return "Awaken the bloodline first";
        if ((usedEyes & eye.mask()) != 0) return "Already used this duel";
        if (get(Stat.CHAKRA) < eye.chakra) return "Low chakra";
        return eye.condition(observedMove);
    }

    boolean castEye(EyePower eye) {
        if (enemyHp <= 0 || !eyeDuelBlocker(eye).isEmpty()) return false;
        usedEyes |= eye.mask();
        eyeTrials[eye.ordinal()] = Math.min(100, eyeTrials[eye.ordinal()] + 1);
        add(Stat.CHAKRA, -eye.chakra);
        String action = "You used " + eye.move;
        if (eye == EyePower.SPARKGLASS) action += " and copied " + duelRival.attackName(observedMove);
        return resolveRound(action, eyeDamage(eye), Counter.STOP);
    }

    enum Move { STRIKE, GUARD, TECHNIQUE, EYE, RETREAT }
    private enum Counter { FULL, HALF, GUARD, STOP }

    int strikeDamage() { return 12 + get(Stat.SKILL) / 6; }
    int incomingDamage() { return duelRival.damage(round); }

    String artDuelBlocker(CombatArt art) {
        if (art.effect() == CombatArt.Effect.BIND && bindUsed) return "Already used this duel";
        return get(Stat.CHAKRA) < art.chakra() ? "Low chakra" : "";
    }

    boolean duel(Move move) {
        if (enemyHp <= 0) return false;
        if (move == Move.RETREAT) {
            enemyHp = 0;
            duelLog = "You conceded the match. There is another challenge next season.";
            note("Retreated at " + duelRival.arena, duelLog);
            return true;
        }
        return switch (move) {
            case TECHNIQUE -> castArt();
            case EYE -> castEye(clan().eye());
            case GUARD -> {
                add(Stat.CHAKRA, 8);
                yield resolveRound("You guarded and recovered 8 chakra", 0, Counter.GUARD);
            }
            case STRIKE -> resolveRound("You struck", strikeDamage(), Counter.FULL);
            case RETREAT -> throw new AssertionError("Retreat was already handled");
        };
    }

    private boolean castArt() {
        CombatArt art = clanArt();
        if (!artDuelBlocker(art).isEmpty()) return false;
        int before = mastery();
        clanPractice = Math.min(1000, clanPractice + 1);
        if (mastery() > before) note("Developed " + clanArt().name(), "Your clan technique improved through practice.");
        add(Stat.CHAKRA, -art.chakra());
        String action = "You used " + art.name();
        switch (art.effect()) {
            case BURN -> burnTurns = 2;
            case BIND -> bindUsed = true;
            case HEAL -> {
                add(Stat.HEALTH, 14);
                action += " and restored health";
            }
            case REFILL -> {
                add(Stat.CHAKRA, 8);
                action += " and drew chakra from the seal";
            }
            default -> { }
        }
        Counter counter = switch (art.effect()) {
            case BIND -> Counter.STOP;
            case WARD -> Counter.HALF;
            default -> Counter.FULL;
        };
        return resolveRound(action, art.damage(), counter);
    }

    int duelSummonCount() { return Integer.bitCount(duelSummons); }
    boolean usedInDuel(Summon summon) { return (duelSummons & summon.mask()) != 0; }

    boolean callInDuel(Summon summon) {
        if (enemyHp <= 0 || !hasSummon(summon) || usedInDuel(summon) || duelSummonCount() >= Summon.MAX_DUEL_CALLS
                || get(Stat.CHAKRA) < Summon.DUEL_CHAKRA) return false;
        duelSummons |= summon.mask();
        add(Stat.CHAKRA, -Summon.DUEL_CHAKRA);
        if (summon == Summon.OTTER) add(Stat.HEALTH, 12);
        Counter counter = switch (summon) {
            case TORTOISE -> Counter.STOP;
            case MOTH -> Counter.HALF;
            default -> Counter.FULL;
        };
        return resolveRound("You called " + summon.title + ". " + summon.duelEffect,
                summon.damageAt(summonBond(summon)), counter);
    }

    private boolean resolveRound(String action, int damage, Counter defense) {
        String recap = action + (action.endsWith(".") ? "" : ".");
        if (damage > 0) recap += " " + damage + " damage.";
        if (burnTurns > 0) {
            burnTurns--;
            damage += 5;
            recap += " Burn dealt 5 damage.";
        }
        enemyHp = Math.max(0, enemyHp - damage);
        if (enemyHp == 0) {
            if (duelRival == Rival.IBARA) victories++;
            else challengerWins++;
            add(Stat.COINS, duelRival.coins);
            add(Stat.FAME, duelRival.fame);
            duelLog = recap + " You won. +" + duelRival.coins + " Coins · +" + duelRival.fame + " Fame.";
            note("Won at " + duelRival.arena, duelLog);
            return true;
        }
        int attack = incomingDamage();
        int counter = switch (defense) {
            case FULL -> attack;
            case HALF -> attack / 2;
            case GUARD -> 3;
            case STOP -> 0;
        };
        if (sealTurns > 0) {
            sealTurns--;
            counter = 0;
        }
        if (guardianTurns > 0) {
            guardianTurns--;
            if (counter > 0) {
                counter = (counter + 1) / 2;
                recap += " Glass Guardian absorbed part of the counter.";
            }
        }
        if (counter > 0) observedMove = attack;
        add(Stat.HEALTH, -counter);
        duelLog = recap + (counter == 0 ? " " + duelRival.name + "’s counter was stopped."
                : " " + duelRival.name + " used " + duelRival.attackName(attack) + " for " + counter + " damage.");
        round++;
        if (get(Stat.HEALTH) == 0) {
            enemyHp = 0;
            duelLog += " The referee called the match.";
            note("Lost at " + duelRival.arena, duelLog);
        }
        return true;
    }

    static String randomName(Random random) {
        String[] names = {"Ren", "Sora", "Aki", "Haru", "Yori", "Iori", "Noa", "Rei"};
        return names[random.nextInt(names.length)];
    }
}
