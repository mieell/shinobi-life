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

    String name;
    int clan, village, presence, season, completed, interacted, victories;
    boolean styled, worked, rested;
    int enemyHp, round;
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
        completed = interacted = 0;
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
        if (!canStart(mission) || (technique && (mission == Missions.DUEL
                || !matches(mission) || get(Stat.CHAKRA) < clan().cost()))) return false;
        completed |= mission.mask();
        add(Stat.STAMINA, -mission.stamina);
        if (mission == Missions.DUEL) {
            enemyHp = 72;
            round = 1;
            duelLog = "Ibara bows. The referee calls the first round.";
        } else {
            String detail = "+" + mission.coins + " Coins · +" + mission.fame + " Fame";
            add(Stat.COINS, mission.coins);
            add(Stat.FAME, mission.fame);
            if (technique) {
                add(Stat.CHAKRA, -clan().cost());
                add(clan().bonus(), clan().amount());
                detail = clan().action() + " " + detail + " · +" + clan().amount() + " " + clan().bonus().label;
            }
            note(mission.title, detail);
        }
        return true;
    }

    enum Move { STRIKE, GUARD, TECHNIQUE, RETREAT }

    boolean duel(Move move) {
        if (enemyHp <= 0) return false;
        if (move == Move.RETREAT) {
            enemyHp = 0;
            duelLog = "You conceded the match. There is another challenge next season.";
            note("Retreated at Cedar Pass", duelLog);
            return true;
        }
        int damage = 0;
        String action;
        switch (move) {
            case TECHNIQUE:
                if (!matches(Missions.DUEL) || get(Stat.CHAKRA) < clan().cost()) return false;
                damage = clan().amount();
                add(Stat.CHAKRA, -clan().cost());
                action = "You used " + clan().technique();
                break;
            case GUARD:
                add(Stat.CHAKRA, 8);
                action = "You guarded and recovered 8 chakra";
                break;
            default:
                damage = 12 + get(Stat.SKILL) / 6;
                action = "You struck";
        }
        enemyHp = Math.max(0, enemyHp - damage);
        if (enemyHp == 0) {
            victories++;
            add(Stat.COINS, Missions.DUEL.coins);
            add(Stat.FAME, Missions.DUEL.fame);
            duelLog = action + " and won. +" + Missions.DUEL.coins + " Coins · +" + Missions.DUEL.fame + " Fame.";
            note("Won at Cedar Pass", duelLog);
            return true;
        }
        int counter = move == Move.GUARD ? 3 : (round % 3 == 0 ? 18 : 10);
        add(Stat.HEALTH, -counter);
        duelLog = action + (damage > 0 ? " for " + damage + " damage." : ".") + " Ibara dealt " + counter + " damage.";
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
