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
            if (clan.role() == Role.OPEN) {
                check(life.get(Stat.SKILL) == 19, "independent skill");
                check(life.take(Missions.COURIER, false), "independent assignment");
                continue;
            }
            Missions mission = Missions.forRole(clan.role());
            int chakra = life.get(Stat.CHAKRA);
            if (mission == Missions.DUEL) {
                check(life.take(mission, false), "duel entry");
                check(life.duel(Life.Move.TECHNIQUE), "duel technique");
                check(life.enemyHp == 72 - clan.amount(), "duel damage");
                check(life.get(Stat.CHAKRA) == chakra - clan.cost(), "duel cost");
                while (life.enemyHp > 0) check(life.duel(Life.Move.STRIKE), "strike");
                check(life.victories == 1, "duel victory");
            } else {
                check(life.take(mission, true), "mission technique");
                check(!life.take(mission, false), "one assignment per season");
                check(life.journal.get(0).detail().contains(clan.action()), "technique journal entry");
                if (clan.bonus() != Stat.CHAKRA) check(life.get(Stat.CHAKRA) == chakra - clan.cost(), "mission cost");
            }
        }
        check(names.size() == 21, "20 clans plus clanless");
        Life life = start(0, 2);
        check(life.get(Stat.LOOKS) == 84 && life.styleUp() && !life.styleUp(), "looks styling");
        check(life.interact(0) && life.bonds[0] == 71 && !life.interact(0), "looks and trust");
        check(life.take(Missions.COURIER, true) && life.get(Stat.COINS) == 320, "Aon bonus");
        life.add(Stat.FAME, 60);
        for (int i = 0; i < 4; i++) life.advance();
        check(life.age() == 19, "age after four seasons");
        check(life.journal.get(0).detail().contains("crowd cheered"), "fame changes the journal");
        check(life.styleUp() && life.get(Stat.LOOKS) == 100, "styling resets");
        check(life.canStart(Missions.COURIER), "missions reopen after season");
        System.out.println("Life checks passed for all 21 paths.");
    }

    private static Life start(int clan, int presence) {
        return Life.begin("Ren", clan, 0, presence, new int[7]);
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
