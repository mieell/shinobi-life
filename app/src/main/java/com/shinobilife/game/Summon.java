package com.shinobilife.game;

import static com.shinobilife.game.Clans.Stat;

enum Summon {
    SHRIKE("Lantern Shrike", Missions.COURIER, 50, Stat.COINS, 45, 16, "Sweeps in from above.", 0xFF87613D),
    TORTOISE("Slate Tortoise", Missions.CARAVAN, 70, Stat.HEALTH, 14, 8, "Blocks the enemy counter completely.", 0xFF456B57),
    OTTER("Reed Otter", Missions.CLINIC, 60, Stat.STAMINA, 10, 10, "Restores 12 Health before the counter.", 0xFF37697A),
    MOTH("Moon Moth", Missions.TRUCE, 85, Stat.BOND, 4, 12, "Scatters dust and halves the enemy counter.", 0xFF69517B),
    LYNX("Ember Lynx", Missions.DEPOT, 100, Stat.FAME, 8, 24, "Pounces for heavy damage.", 0xFFAB543B);

    static final int MISSION_CHAKRA = 8, DUEL_CHAKRA = 12;
    static final int BOND_STAMINA = 8, STARTING_BOND = 30, MAX_DUEL_CALLS = 2;
    static final Summon[] ALL = values();

    final String title, duelEffect;
    final Missions mission;
    final int price, bonusAmount, duelDamage, color;
    final Stat bonus;

    Summon(String title, Missions mission, int price, Stat bonus, int bonusAmount,
           int duelDamage, String duelEffect, int color) {
        this.title = title;
        this.mission = mission;
        this.price = price;
        this.bonus = bonus;
        this.bonusAmount = bonusAmount;
        this.duelDamage = duelDamage;
        this.duelEffect = duelEffect;
        this.color = color;
    }

    int mask() { return 1 << ordinal(); }
    int damageAt(int bond) { return duelDamage + (bond >= 90 ? 10 : bond >= 50 ? 5 : 0); }
}
