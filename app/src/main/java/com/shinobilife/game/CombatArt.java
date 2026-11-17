package com.shinobilife.game;

record CombatArt(String name, String school, int damage, int chakra, Effect effect) {
    enum Effect {
        HIT(""), BURN("5 burn damage this turn and next"),
        BIND("Stops the counter once per duel"), WARD("Halves the counter"),
        HEAL("Restores 14 Health"), REFILL("Restores 8 Chakra");

        final String detail;
        Effect(String detail) { this.detail = detail; }
    }

    String description() {
        return school + " · " + damage + " damage · " + chakra + " chakra"
                + (effect == Effect.HIT ? "" : ". " + effect.detail + ".");
    }

    static CombatArt forClan(Clans.Clan clan) {
        return switch (clan.name()) {
            case "Aon" -> new CombatArt("Lantern Gale", "Wind", 20, 14, Effect.WARD);
            case "Shiohama" -> new CombatArt("Undertow Break", "Water", 22, 17, Effect.WARD);
            case "Benisuna" -> new CombatArt(clan.technique(), "Fire", clan.amount(), clan.cost(), Effect.HIT);
            case "Oriba" -> new CombatArt("Folded Prison", "Sealing", 19, 20, Effect.BIND);
            case "Kogane" -> new CombatArt("Gilded Mirage", "Illusion", 20, 13, Effect.BIND);
            case "Mizuhira" -> new CombatArt("Pulse Stitch", "Medical", 19, 17, Effect.HEAL);
            case "Hoshikiri" -> new CombatArt("Flash Meridian", "Lightning", 26, 18, Effect.HIT);
            case "Nemunoki" -> new CombatArt("Dreamroot Veil", "Wood", 18, 12, Effect.WARD);
            case "Uramori" -> new CombatArt("Bellshadow Snare", "Shadow", 23, 20, Effect.BIND);
            case "Isen" -> new CombatArt("Quarry Mantle", "Earth", 23, 15, Effect.WARD);
            case "Sazame" -> new CombatArt(clan.technique(), "Body arts", clan.amount(), clan.cost(), Effect.HIT);
            case "Toriba" -> new CombatArt("Whistle Blade", "Wind", 19, 9, Effect.HIT);
            case "Tamayori" -> new CombatArt("Hush Bell", "Sound", 22, 16, Effect.WARD);
            case "Fuyose" -> new CombatArt("White Rime", "Ice", 21, 18, Effect.BIND);
            case "Mamekura" -> new CombatArt("Seal Siphon", "Sealing", 12, 0, Effect.REFILL);
            case "Kuroishi" -> new CombatArt(clan.technique(), "Earth", clan.amount(), clan.cost(), Effect.WARD);
            case "Hanamichi" -> new CombatArt("Petal Double", "Illusion", 17, 16, Effect.BIND);
            case "Tsugihara" -> new CombatArt("Iron Rain", "Metal", 27, 19, Effect.HIT);
            case "Kasamori" -> new CombatArt("Rain Stitch", "Regeneration", 14, 12, Effect.HEAL);
            case "Kazetobi" -> new CombatArt(clan.technique(), "Wind", clan.amount(), clan.cost(), Effect.HIT);
            case "Kagaribi" -> new CombatArt("Coalglass Flare", "Fire", 18, 14, Effect.BURN);
            case "Shinkai" -> new CombatArt("Tideweight Palm", "Gravity", 22, 17, Effect.WARD);
            case "Clanless" -> new CombatArt("Academy Sweep", "Body arts", 17, 10, Effect.HIT);
            default -> throw new IllegalArgumentException("Missing combat art for " + clan.name());
        };
    }
}
