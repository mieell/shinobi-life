package com.shinobilife.game;

enum EyePower {
    SPARKGLASS("Sparkglass Eye", "Mirror Counter", 19, 0, 16,
            "Read your opponent’s next attack and catch its rhythm. Copy the last move you saw for its base damage +10 and evade the counter.", "Emberglass Eye", "Glass Guardian", 28, 26,
            "Manifest chakra armor. Halves incoming damage this turn and next."),
    HORIZON("Horizon Eye", "Gravity Lock", 25, 2, 24,
            "Pin your opponent under shifting gravity, deal 34 damage, and stop the counter.", "Starfold Eye", "Falling Horizon", 32, 42,
            "Collapse gravity around the rival and stop their counter."),
    FARWATCH("Farwatch Eye", "Meridian Seal", 22, 1, 18,
            "See an opponent’s chakra channels, deal 24 damage, and seal their counter.", "Moon Meridian Eye", "Meridian Cage", 30, 29,
            "Seal the rival’s counter this turn and next.");

    final String title, move, effect, evolution, manifestation, formEffect;
    final int skill, wins, chakra, formChakra, formDamage;

    EyePower(String title, String move, int skill, int wins, int chakra, String effect,
             String evolution, String manifestation, int formChakra, int formDamage, String formEffect) {
        this.title = title;
        this.move = move;
        this.skill = skill;
        this.wins = wins;
        this.chakra = chakra;
        this.effect = effect;
        this.evolution = evolution;
        this.manifestation = manifestation;
        this.formChakra = formChakra;
        this.formDamage = formDamage;
        this.formEffect = formEffect;
    }

    String requirement() {
        return skill + " Skill" + (wins == 0 ? "" : " and " + wins + " Cedar Pass wins");
    }

    String description() {
        return effect + " Costs " + chakra + " chakra. Once per duel.";
    }

    int mask() { return 1 << ordinal(); }
    int damage(int observedMove) { return switch (this) {
        case HORIZON -> 34;
        case FARWATCH -> 24;
        case SPARKGLASS -> observedMove + 10;
    }; }

    String condition(int observedMove) {
        return this == SPARKGLASS && observedMove == 0 ? "Observe an enemy attack first" : "";
    }
}
