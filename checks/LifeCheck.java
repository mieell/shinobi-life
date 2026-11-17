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
        check(life.take(Missions.COURIER, true) && life.get(Stat.COINS) == 320, "Aon bonus");
        life.add(Stat.FAME, 60);
        for (int i = 0; i < 4; i++) life.advance();
        check(life.age() == 19, "age after four seasons");
        check(life.journal.get(0).detail().contains("crowd cheered"), "fame changes the journal");
        check(life.styleUp() && life.get(Stat.LOOKS) == 100, "styling resets");
        check(life.canStart(Missions.COURIER), "missions reopen after season");
        eyePowers();
        summonContracts();
        System.out.println("Life checks passed for 23 combat paths, both eye powers, and five summons.");
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
