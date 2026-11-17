package com.shinobilife.game;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

import static com.shinobilife.game.Clans.Stat;

final class Life {
    static final String[] VILLAGES = {"Hazekawa", "Lanternbridge", "Starwatch"};
    static final String[] SEASONS = {"Spring", "Summer", "Autumn", "Winter"};
    static final String[] CONTACTS = {"Nao", "Jun", "Mika"};
    static final String[] ROLES = {"Family", "Friend", "Rival"};
    static final String[] PRESENCE = {"Everyday", "Polished", "Striking"};
    static final int[] BASE_LOOKS = {52, 68, 84};
    static final int LOOK_COST = 35;

    record Event(String date, String title, String detail) {}

    final List<Event> journal = new ArrayList<>();
    final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);
    final int[] bonds = {64, 52, 32};
    final int[] style = {0, 0, 1, 2, 0, 0, 0};
    final int[] summonBonds = new int[Summon.ALL.length];

    String name;
    int clan, village, presence, season, completed, interacted, victories;
    boolean styled, worked, rested;
    int scoutedMissions, bondedSummons, duelSummons;
    int enemyHp, round;
    int burnTurns, observedMove;
    boolean bindUsed, eyeAwakened, eyeUsed;
    String duelLog = "";
    int startAge = 18;

    static Life begin(String name, int clan, int village, int presence, int[] style) {
        Life life = new Life();
        life.name = name;
        life.clan = clan;
        life.village = village;
        life.presence = presence;
        System.arraycopy(style, 0, life.style, 0, style.length);
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

    Clans.Clan clan() { return Clans.ALL[clan]; }
    String village() { return VILLAGES[village]; }
    int age() { return startAge + season / SEASONS.length; }
    String date() { return SEASONS[season % SEASONS.length] + " · Age " + age(); }
    String rank() { return victories >= 3 ? "Pass Champion" : get(Stat.SKILL) >= 22 ? "Field Shinobi" : "Academy Graduate"; }
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
        season++;
        completed = interacted = bondedSummons = 0;
        styled = worked = rested = false;
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
        note(season % SEASONS.length == 0 ? "You turned " + age() : SEASONS[season % SEASONS.length] + " arrived", detail);
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
        if (worked || get(Stat.STAMINA) < 12) return false;
        worked = true;
        add(Stat.STAMINA, -12);
        add(Stat.COINS, 55);
        note("A shift at the market", "You stocked the produce stalls before dawn. +55 Coins.");
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

    boolean interact(int contact) {
        if ((interacted & (1 << contact)) != 0) return false;
        interacted |= 1 << contact;
        int gain = get(Stat.LOOKS) >= 75 ? 7 : 5;
        bonds[contact] = Math.min(100, bonds[contact] + gain);
        add(Stat.MOOD, 3);
        note("Time with " + CONTACTS[contact], "You traded stories after the market closed. +" + gain + " Trust.");
        return true;
    }

    boolean isComplete(Missions mission) { return (completed & mission.mask()) != 0; }

    boolean canStart(Missions mission) {
        return enemyHp == 0 && !isComplete(mission) && get(Stat.STAMINA) >= mission.stamina
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
        completed |= mission.mask();
        add(Stat.STAMINA, -mission.stamina);
        if (mission == Missions.DUEL) {
            enemyHp = 72;
            round = 1;
            burnTurns = observedMove = duelSummons = 0;
            bindUsed = eyeUsed = false;
            duelLog = "Ibara bows. The referee calls the first round.";
        } else {
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
        }
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

    boolean canAwakenEye() {
        EyePower eye = clan().eye();
        return eye != null && !eyeAwakened && enemyHp == 0 && get(Stat.SKILL) >= eye.skill && victories >= eye.wins;
    }

    boolean awakenEye() {
        if (!canAwakenEye()) return false;
        eyeAwakened = true;
        EyePower eye = clan().eye();
        note(eye.title + " awakened", "You can now use " + eye.move + " at Cedar Pass. " + eye.description());
        return true;
    }

    String eyeDuelBlocker() {
        EyePower eye = clan().eye();
        if (!eyeAwakened || eye == null) return "Awaken the bloodline first";
        if (eyeUsed) return "Already used this duel";
        if (get(Stat.CHAKRA) < eye.chakra) return "Low chakra";
        return eye.condition(observedMove);
    }

    enum Move { STRIKE, GUARD, TECHNIQUE, EYE, RETREAT }
    private enum Counter { FULL, HALF, GUARD, STOP }

    int strikeDamage() { return 12 + get(Stat.SKILL) / 6; }
    int incomingDamage() { return round % 3 == 0 ? 18 : 10; }

    String artDuelBlocker(CombatArt art) {
        if (art.effect() == CombatArt.Effect.BIND && bindUsed) return "Already used this duel";
        return get(Stat.CHAKRA) < art.chakra() ? "Low chakra" : "";
    }

    boolean duel(Move move) {
        if (enemyHp <= 0) return false;
        if (move == Move.RETREAT) {
            enemyHp = 0;
            duelLog = "You conceded the match. There is another challenge next season.";
            note("Retreated at Cedar Pass", duelLog);
            return true;
        }
        return switch (move) {
            case TECHNIQUE -> castArt();
            case EYE -> {
                if (!eyeDuelBlocker().isEmpty()) yield false;
                EyePower eye = clan().eye();
                eyeUsed = true;
                add(Stat.CHAKRA, -eye.chakra);
                String action = "You used " + eye.move;
                if (eye == EyePower.SPARKGLASS) action += " and copied " + (observedMove == 18 ? "Cedar Breaker" : "Stone Jab");
                yield resolveRound(action, eye.damage(observedMove), Counter.STOP);
            }
            case GUARD -> {
                add(Stat.CHAKRA, 8);
                yield resolveRound("You guarded and recovered 8 chakra", 0, Counter.GUARD);
            }
            case STRIKE -> resolveRound("You struck", strikeDamage(), Counter.FULL);
            case RETREAT -> throw new AssertionError("Retreat was already handled");
        };
    }

    private boolean castArt() {
        CombatArt art = CombatArt.forClan(clan());
        if (!artDuelBlocker(art).isEmpty()) return false;
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
            victories++;
            add(Stat.COINS, Missions.DUEL.coins);
            add(Stat.FAME, Missions.DUEL.fame);
            duelLog = recap + " You won. +" + Missions.DUEL.coins + " Coins · +" + Missions.DUEL.fame + " Fame.";
            note("Won at Cedar Pass", duelLog);
            return true;
        }
        int attack = incomingDamage();
        int counter = switch (defense) {
            case FULL -> attack;
            case HALF -> attack / 2;
            case GUARD -> 3;
            case STOP -> 0;
        };
        if (counter > 0) observedMove = attack;
        add(Stat.HEALTH, -counter);
        duelLog = recap + (counter == 0 ? " Ibara’s counter was stopped."
                : " Ibara used " + (attack == 18 ? "Cedar Breaker" : "Stone Jab") + " for " + counter + " damage.");
        round++;
        if (get(Stat.HEALTH) == 0) {
            enemyHp = 0;
            duelLog += " The referee called the match.";
            note("Lost at Cedar Pass", duelLog);
        }
        return true;
    }

    static String randomName(Random random) {
        String[] names = {"Ren", "Sora", "Aki", "Haru", "Yori", "Iori", "Noa", "Rei"};
        return names[random.nextInt(names.length)];
    }
}
