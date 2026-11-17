package com.shinobilife.game;

import static com.shinobilife.game.Clans.Role;

enum Missions {
    COURIER("Find the Lost Courier", "courier search", "Find the missing rider before fog closes the lantern road.", Role.SCOUT, 18, 110, 5),
    CARAVAN("Guard the Salt Caravan", "caravan escort", "Walk the ferry route with wagons carrying the winter salt allotment.", Role.WARDEN, 22, 140, 7),
    CLINIC("Help at the River Clinic", "clinic shift", "Take the evening shift so the river medics can rest.", Role.MEDIC, 16, 85, 6),
    TRUCE("Host the Market Truce", "market truce", "Bring feuding stallholders to the same table before opening day.", Role.ENVOY, 16, 95, 8),
    DEPOT("Repair the Academy Depot", "depot repair", "Refit worn equipment before the next class begins sparring.", Role.CRAFTER, 14, 105, 4),
    DUEL("Duel at Cedar Pass", "Cedar Pass duel", "Face Ibara Kase in a public, supervised challenge. Win to collect the purse.", Role.DUELIST, 24, 190, 18);

    final String title, shortName, description;
    final Role role;
    final int stamina, coins, fame;

    Missions(String title, String shortName, String description, Role role, int stamina, int coins, int fame) {
        this.title = title;
        this.shortName = shortName;
        this.description = description;
        this.role = role;
        this.stamina = stamina;
        this.coins = coins;
        this.fame = fame;
    }

    int mask() { return 1 << ordinal(); }

    static Missions forRole(Role role) {
        for (Missions mission : values()) {
            if (mission.role == role) return mission;
        }
        throw new IllegalArgumentException("No assignment uses " + role);
    }
}
