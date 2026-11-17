package com.shinobilife.game;

enum EyePower {
    SPARKGLASS("Sparkglass Eye", "Mirror Counter", 19, 0, 16,
            "Read Ibara’s next attack and catch its rhythm. Copy the last move you saw for its base damage +10 and evade the counter."),
    HORIZON("Horizon Eye", "Gravity Lock", 25, 2, 24,
            "Pin Ibara under shifting gravity, deal 34 damage, and stop the counter.");

    final String title, move, effect;
    final int skill, wins, chakra;

    EyePower(String title, String move, int skill, int wins, int chakra, String effect) {
        this.title = title;
        this.move = move;
        this.skill = skill;
        this.wins = wins;
        this.chakra = chakra;
        this.effect = effect;
    }

    String requirement() {
        return skill + " Skill" + (wins == 0 ? "" : " and " + wins + " Cedar Pass wins");
    }

    String description() {
        return effect + " Costs " + chakra + " chakra. Once per duel.";
    }

    int damage(int observedMove) { return this == HORIZON ? 34 : observedMove + 10; }

    String condition(int observedMove) {
        return this == SPARKGLASS && observedMove == 0 ? "Observe Ibara attack first" : "";
    }
}
