package com.shinobilife.game;

import java.util.HashSet;
import java.util.Set;

import static com.shinobilife.game.Clans.Role;
import static com.shinobilife.game.Clans.Stat;

final class LifeCheck {
    public static void main(String[] args) {
        Set<String> names = new HashSet<>();
        for (int i = 0; i < Clans.ALL.length; i++) {
            Life life = start(i, 2);
            Clans.Clan clan = life.clan();
            check(names.add(clan.name()), "duplicate clan");
            check(clan.perkLabel().contains(clan.perk().label), "perk label");
            combatArt(i);
            if (clan.role() == Role.OPEN) {
                check(life.get(Stat.SKILL) == 19, "independent skill");
                check(life.take(Missions.COURIER, false), "independent assignment");
                continue;
            }
            Missions mission = Missions.forRole(clan.role());
            int chakra = life.get(Stat.CHAKRA);
            if (mission != Missions.DUEL) {
                check(life.take(mission, true), "mission technique");
                check(!life.take(mission, false), "one assignment per season");
                check(life.journal.get(0).detail().contains(clan.action()), "technique journal entry");
                if (clan.bonus() != Stat.CHAKRA) check(life.get(Stat.CHAKRA) == chakra - clan.cost(), "mission cost");
            }
        }
        check(names.size() == 23, "22 clans plus clanless");
        Life life = start(0, 2);
        check(life.get(Stat.LOOKS) == 84 && life.styleUp() && !life.styleUp(), "looks styling");
        check(life.interact(0) && life.bonds[0] == 71 && !life.interact(0), "looks and trust");
        check(life.gift(0) && life.bonds[0] == 77 && !life.gift(0), "keepsake gift");
        check(life.allyFavor(0) && !life.allyFavor(0), "ally favor when trust >= 60");
        check(!life.allyFavor(2), "ally favor locked under 60 trust");
        check(life.meditate() && !life.meditate() && life.get(Stat.SKILL) == 11, "meditate once per season");
        check(life.take(Missions.COURIER, true) && life.get(Stat.COINS) == 295, "Aon bonus");
        life.add(Stat.FAME, 60);
        for (int i = 0; i < 4; i++) life.advance();
        check(life.age() == 19, "age after four seasons");
        check(life.journal.get(0).detail().contains("crowd cheered"), "fame changes the journal");
        check(life.styleUp() && life.get(Stat.LOOKS) == 100, "styling resets");
        check(life.meditate() && life.gift(1) && life.allyFavor(0), "seasonal actions reset");
        check(life.canStart(Missions.COURIER), "missions reopen after season");
        eyePowers();
        summonContracts();
        randomizedOrigins();
        romanceProgression();
        lineageProgression();
        academyAndPolitics();
        System.out.println("Life checks passed for 23 combat paths, three eye powers, five summons, meditation, gifts, favors, randomized origins, travel, escort branches, romance, mixed inheritance, succession, academy promotions, Hokage elections, war, and truces.");
    }

    private static void combatArt(int clan) {
        Life life = start(clan, 1);
        CombatArt art = CombatArt.forClan(life.clan());
        int chakra = life.get(Stat.CHAKRA);
        int hp = life.get(Stat.HEALTH);
        check(life.take(Missions.DUEL, false) && life.duel(Life.Move.TECHNIQUE), "usable art for " + life.clan().name());
        int burn = art.effect() == CombatArt.Effect.BURN ? 5 : 0;
        check(life.enemyHp == 72 - art.damage() - burn, "art damage for " + life.clan().name());
        int refill = art.effect() == CombatArt.Effect.REFILL ? 8 : 0;
        check(life.get(Stat.CHAKRA) == chakra - art.chakra() + refill, "art chakra for " + life.clan().name());
        int expectedHp = switch (art.effect()) {
            case BIND -> hp;
            case WARD -> hp - 5;
            case HEAL -> Math.min(100, hp + 14) - 10;
            default -> hp - 10;
        };
        check(life.get(Stat.HEALTH) == expectedHp, "art effect for " + life.clan().name());
        if (art.effect() == CombatArt.Effect.BIND) {
            check(!life.duel(Life.Move.TECHNIQUE) && life.round == 2, "bind is once per duel");
        }
        if (burn > 0) {
            int foe = life.enemyHp;
            check(life.duel(Life.Move.GUARD) && life.enemyHp == foe - 5, "burn lasts into the next turn");
        }
    }

    private static void eyePowers() {
        Life mirror = named("Kagaribi");
        check(!mirror.awakenEye(), "Sparkglass needs skill");
        check(mirror.train() && mirror.awakenEye() && !mirror.awakenEye(), "Sparkglass awakens once");
        check(mirror.take(Missions.DUEL, false) && !mirror.duel(Life.Move.EYE), "Sparkglass needs an observed attack");
        for (int i = 0; i < 3; i++) check(mirror.duel(Life.Move.GUARD), "observing");
        int hp = mirror.get(Stat.HEALTH);
        check(mirror.duel(Life.Move.EYE) && mirror.enemyHp == 44 && mirror.get(Stat.HEALTH) == hp,
                "copy Cedar Breaker and evade counter");
        check(!mirror.duel(Life.Move.EYE), "copy once");

        Life gravity = named("Shinkai");
        gravity.add(Stat.SKILL, 15);
        check(!gravity.awakenEye(), "Horizon also needs two wins");
        for (int victory = 1; victory <= 2; victory++) {
            check(gravity.take(Missions.DUEL, false), "Horizon qualification duel");
            while (gravity.enemyHp > 0) check(gravity.duel(Life.Move.TECHNIQUE), "Tideweight Palm");
            check(gravity.victories == victory, "qualification victory");
            gravity.advance();
        }
        check(gravity.awakenEye() && gravity.take(Missions.DUEL, false), "Horizon awakens after two wins");
        hp = gravity.get(Stat.HEALTH);
        check(gravity.duel(Life.Move.EYE) && gravity.enemyHp == 38 && gravity.get(Stat.HEALTH) == hp,
                "gravity lock damage and counter block");
    }

    private static void summonContracts() {
        for (Summon summon : Summon.ALL) {
            Life life = named("Clanless");
            check(!life.signSummon(summon), "mission required before signing");
            check(life.take(summon.mission, false) && life.signSummon(summon), "recruit " + summon.title);
            check(!life.signSummon(summon), "one pact per summon");
            check(life.bondSummon(summon) && !life.bondSummon(summon), "bond once per season");
            check(summon.damageAt(life.summonBond(summon)) == summon.duelDamage + 5, "bond boosts summon damage");
            life.advance();
            int chakra = life.get(Stat.CHAKRA);
            check(life.take(summon.mission, false, summon), "mission call");
            check(life.get(Stat.CHAKRA) == chakra - Summon.MISSION_CHAKRA, "mission call cost");
            check(life.journal.get(0).detail().contains(summon.title), "summon logged");
        }
        Life team = named("Toriba");
        check(team.hasSummon(Summon.SHRIKE), "Toriba starts with a shrike");
        check(team.take(Missions.CARAVAN, false) && team.signSummon(Summon.TORTOISE), "second summon");
        check(team.take(Missions.DEPOT, false) && team.signSummon(Summon.LYNX), "third summon");
        check(team.take(Missions.DUEL, false) && team.callInDuel(Summon.SHRIKE), "shrike called");
        check(!team.callInDuel(Summon.SHRIKE), "each summon once per duel");
        int hp = team.get(Stat.HEALTH);
        check(team.callInDuel(Summon.TORTOISE) && team.get(Stat.HEALTH) == hp, "tortoise blocks counter");
        check(team.duelSummonCount() == 2 && !team.callInDuel(Summon.LYNX), "two different calls per duel");
    }

    private static void randomizedOrigins() {
        int[] style = new int[7];
        Set<Integer> rolled = new HashSet<>();
        Set<Integer> households = new HashSet<>();
        java.util.Random random = new java.util.Random(91);
        for (int village = 0; village < Life.VILLAGES.length; village++) {
            for (int i = 0; i < 300; i++) {
                Life life = Life.rollNewLife("Ren", village, 3, 1, style, random);
                check(life.gender == 3 && life.homeVillage == village, "selected identity");
                check(life.awakenedEyes == 0 && !life.duel(Life.Move.EYE), "eyes are dormant at birth");
                if (life.clan().eye() == EyePower.SPARKGLASS) check(village == 3, "Sparkglass origins");
                if (life.clan().eye() == EyePower.HORIZON) check(village == 2 || village == 4, "Horizon origins");
                rolled.add(life.clan);
                households.add(life.family);
            }
        }
        check(rolled.size() == Clans.ALL.length && households.size() == Life.FAMILIES.length, "all origins obtainable");
        Life life = start(0, 1);
        check(life.visitVillage(1) && life.homeVillage == 0 && life.clan == 0, "travel preserves heritage");
        int coins = life.get(Stat.COINS);
        check(life.villageActivity(Life.VillageAction.MARKET) && life.get(Stat.COINS) == coins + 60,
                "Lanternbridge wages");
        check(!life.villageActivity(Life.VillageAction.MARKET), "seasonal activity limit");
        life.advance();
        check(life.villageActivity(Life.VillageAction.MARKET), "activities reset");
        check(!life.escortWithScout() && life.escortShortcut(), "unaided shortcut");
        check(life.journal.get(0).title().equals("Caravan ambushed"), "unprepared escort outcome");
        Life worker = start(0, 1);
        check(!worker.changeCareer(Life.Career.INSTRUCTOR), "career skill gate");
        worker.add(Stat.SKILL, 16);
        check(worker.changeCareer(Life.Career.INSTRUCTOR), "career application");
        for (int i = 0; i < 3; i++) {
            check(worker.work(), "career shift");
            worker.advance();
        }
        check(worker.workPay() == 110, "job raises");
        int trust = worker.bonds[2];
        check(worker.villageActivity(Life.VillageAction.PICNIC) && worker.bonds[2] == trust + 8,
                "social activity has a relationship outcome");
        Life prepared = start(0, 1);
        prepared.add(Stat.SKILL, 12);
        check(prepared.escortShortcut() && prepared.get(Stat.FAME) == 12, "prepared escort extra fame");
        Life scout = named("Toriba");
        check(scout.escortWithScout() && !scout.escortShortcut(), "scouted escort uses contract and closes mission");
        check(scout.journal.get(0).title().equals("A lantern in the pass"), "scouted outcome logged");
    }

    private static void romanceProgression() {
        Life player = start(0, 2);
        Life.Interest emi = Life.Interest.EMI;
        Life.Interest haru = Life.Interest.HARU;
        check(!player.romance(emi, Life.RomanceAction.WALK), "introductions come first");
        check(player.romance(emi, Life.RomanceAction.INTRODUCE), "meet");
        check(!player.romance(emi, Life.RomanceAction.TALK), "one visit per season");
        player.advance();
        check(player.romance(emi, Life.RomanceAction.TALK), "share stories");
        player.advance();
        int affection = player.affection(emi);
        check(player.romance(emi, Life.RomanceAction.WALK) && player.affection(emi) == affection + 23,
                "favorite date and Looks bonuses");
        player.advance();
        check(!player.romance(emi, Life.RomanceAction.COMMIT), "commitment threshold");
        check(player.romance(emi, Life.RomanceAction.WALK), "second date");
        player.advance();
        check(player.romance(emi, Life.RomanceAction.COMMIT) && player.isPartner(emi), "mutual commitment");
        player.affection[haru.ordinal()] = 45;
        check(!player.romance(haru, Life.RomanceAction.DINNER) && player.romance(haru, Life.RomanceAction.TALK),
                "partner blocks other dates but allows friendship");
        player.advance();
        check(!player.romance(emi, Life.RomanceAction.PROPOSE), "proposal threshold");
        int coins = player.get(Stat.COINS);
        check(player.romance(emi, Life.RomanceAction.DINNER) && player.get(Stat.COINS) == coins - 28, "paid dinner");
        player.advance();
        check(player.romance(emi, Life.RomanceAction.PROPOSE) && player.married, "marriage");
        check(player.endRomance(emi) && player.partner == -1 && !player.married, "divorce and clears partnership");
        check(player.journal.get(0).title().equals("Divorced Emi"), "romance in journal");
    }

    private static void lineageProgression() {
        int copy = EyePower.SPARKGLASS.mask();
        int far = EyePower.FARWATCH.mask();
        Set<Integer> outcomes = new HashSet<>();
        java.util.Random random = new java.util.Random(74);
        for (int i = 0; i < 200; i++) outcomes.add(Life.inheritEyes(copy, far, random));
        check(outcomes.contains(0) && outcomes.contains(copy) && outcomes.contains(far) && outcomes.contains(copy | far),
                "both, either, or neither bloodline");
        check(Life.inheritEyes(0, 0, random) == 0, "no spontaneous eye powers");

        Life parent = named("Kagaribi");
        Life.Interest other = null;
        for (int seed = 0; seed < 30 && other == null; seed++) {
            parent = named("Kagaribi");
            parent.worldSeed = seed;
            for (Life.Interest resident : parent.residents()) {
                if ((resident.eyes() & far) != 0) { other = resident; break; }
            }
        }
        check(parent.residents().length == 100 && other != null, "populated world with inherited bloodlines");
        parent.partner = other.ordinal();
        parent.affection[parent.partner] = 90;
        java.util.Random passesBoth = new java.util.Random() {
            @Override public boolean nextBoolean() { return true; }
        };
        check(parent.welcomeChild("Aki", passesBoth), "family growth");
        check(!parent.welcomeChild("Ren", passesBoth), "spacing between children");
        Life.Child child = parent.children.get(0);
        check(child.eyes() == (copy | far) && child.otherClan() == other.clan, "mixed child data");
        check(parent.succeed(child) == null, "succession after death only");
        parent.deathAge = 19;
        for (int i = 0; i < 4; i++) parent.advance();
        Life heir = parent.succeed(child);
        check(parent.deceased && heir != null && heir.age() == 18 && heir.generation == 2, "continue as child");
        check(heir.heritageEyes() == (copy | far) && heir.awakenedEyes == 0, "inherited eyes are dormant");
        heir.add(Stat.SKILL, 20);
        heir.victories = 1;
        check(!heir.awakenEye(EyePower.HORIZON), "unrelated eye cannot awaken");
        check(heir.awakenEye(EyePower.SPARKGLASS) && heir.awakenEye(EyePower.FARWATCH), "independent awakenings");
        check(heir.take(Missions.DUEL, false) && heir.duel(Life.Move.GUARD)
                && heir.castEye(EyePower.SPARKGLASS) && heir.castEye(EyePower.FARWATCH)
                && !heir.castEye(EyePower.FARWATCH), "both inherited combat arts usable");
    }

    private static void academyAndPolitics() {
        Life player = Life.begin("Ren", 0, Life.LEAF, 1, new int[7]);
        check(player.rank().equals("Genin") && !player.runForHokage(), "academy and office start locked");
        check(player.attendClass(Life.Lesson.TACTICS) && !player.attendClass(Life.Lesson.CONTROL), "one class per season");
        player.advance();
        check(player.attendClass(Life.Lesson.TACTICS), "credit persists and new term opens");
        player.add(Stat.SKILL, 4);
        check(!player.takeExam(), "duel requirement");
        player.victories = 1;
        check(player.takeExam() && player.academyRank == Life.AcademyRank.CHUNIN, "first promotion");
        check(player.academyCredits == 0, "new preparation cycle");
        for (int i = 0; i < 3; i++) {
            player.advance();
            check(player.attendClass(Life.Lesson.TACTICS), "advanced class");
        }
        player.add(Stat.SKILL, 1);
        player.victories = 3;
        check(player.takeExam() && player.academyRank == Life.AcademyRank.JONIN, "second promotion");
        player.add(Stat.FAME, 40);
        check(!player.runForHokage(), "public support is required");
        for (int i = 0; i < 2; i++) {
            player.advance();
            check(player.campaign() && !player.campaign(), "public meetings once per season");
        }
        check(player.runForHokage() && player.rank().equals("Hokage"), "Hidden Leaf election");
        player.borderTension = 41;
        check(player.govern(Life.Policy.WAR) && player.leafAtWar, "declaration");
        check(!player.govern(Life.Policy.DIPLOMACY), "one policy per season");
        check(player.helpDuringWar(true) && !player.helpDuringWar(false), "wartime service");
        int personal = player.get(Stat.COINS);
        player.advance();
        check(player.govern(Life.Policy.DIPLOMACY) && player.leafAtWar, "first diplomatic talks");
        player.advance();
        check(player.govern(Life.Policy.DIPLOMACY) && !player.leafAtWar, "negotiated ceasefire");
        check(player.get(Stat.COINS) == personal, "policy fees use the village treasury");

        player.leafAtWar = true;
        player.warEffort = 59;
        check(player.helpDuringWar(true), "can rally village effort");
        player.advance();
        check(!player.leafAtWar && player.hokage, "successful defense ends war");
        player.leafAtWar = true;
        player.leafStability = 4;
        player.advance();
        check(!player.leafAtWar && !player.hokage, "defeat removes leader");

        Life civilian = start(0, 1);
        civilian.borderTension = 70;
        civilian.advance();
        civilian.advance();
        check(civilian.leafAtWar, "war can begin independently of the player");
        for (int i = 0; i < 5; i++) civilian.advance();
        check(!civilian.leafAtWar, "council can broker an AI ceasefire");
    }

    private static Life named(String name) {
        for (int i = 0; i < Clans.ALL.length; i++) {
            if (Clans.ALL[i].name().equals(name)) return start(i, 1);
        }
        throw new AssertionError("Unknown clan " + name);
    }

    private static Life start(int clan, int presence) {
        return Life.begin("Ren", clan, 0, presence, new int[7]);
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
