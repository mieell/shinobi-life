package com.shinobilife.game;

final class Clans {
    enum Role {
        SCOUT("Scout"), WARDEN("Guard"), MEDIC("Medic"),
        ENVOY("Envoy"), CRAFTER("Crafter"), DUELIST("Duelist"), OPEN("Independent");

        final String label;
        Role(String label) { this.label = label; }
    }

    enum Stat {
        HEALTH("Health"), CHAKRA("Chakra"), STAMINA("Stamina"), MOOD("Happiness"),
        LOOKS("Looks"), FAME("Fame"), COINS("Coins"), SKILL("Skill"), BOND("Trust with all contacts");

        final String label;
        Stat(String label) { this.label = label; }
    }

    record Clan(String name, Role role, int color, String origin, String knack,
                Stat perk, int start, String technique, String action, int cost,
                Stat bonus, int amount) {
        String perkLabel() { return "+" + start + " " + perk.label; }
        String costLabel() { return cost == 0 ? "0 chakra" : cost + " chakra"; }
        String effect() {
            if (role == Role.DUELIST) return "Deals " + amount + " damage in the Cedar Pass duel.";
            return "Grants +" + amount + " " + bonus.label + " on the " + Missions.forRole(role).shortName + " mission.";
        }
        String limit() {
            if (role == Role.DUELIST) return "Usable only in the Cedar Pass duel. Costs " + costLabel() + " per use.";
            return "Usable once per assignment on the " + Missions.forRole(role).shortName + " mission. Costs " + costLabel() + ".";
        }
    }

    static final Clan[] ALL = {
        new Clan("Aon", Role.SCOUT, 0xFF30746F, "Lantern keepers once guided travelers across the mist bridges of Hazekawa.", "Aon eyes read the slight movement of warm air. The clan trains scouts to follow trails others miss.", Stat.CHAKRA, 16, "Lantern Trace", "Read a trail of residual warmth to find the lost courier.", 14, Stat.COINS, 65),
        new Clan("Shiohama", Role.WARDEN, 0xFF406993, "Ferry families banded together to protect workers crossing the Salt Reach.", "Generations of breathing drills help them channel force into water barriers. The drills can be taught.", Stat.HEALTH, 12, "Breakwater Ring", "Raise a ring of water around the wagons while the caravan crosses.", 18, Stat.HEALTH, 12),
        new Clan("Benisuna", Role.DUELIST, 0xFFA34739, "The families at Ember Kiln earned their name by rebuilding a town after its kilns collapsed.", "Their inherited ability stores body heat in ceramic beads, which they release in one burst.", Stat.CHAKRA, 12, "Kiln Pulse", "Release a burst of stored heat through a fired clay bead.", 22, Stat.HEALTH, 32),
        new Clan("Oriba", Role.CRAFTER, 0xFF996325, "Oriba bookbinders founded a public workshop after the old craft guild closed its doors to apprentices.", "They fold seals into equipment so anyone can activate it. Their art is learned.", Stat.COINS, 85, "Folded Promise", "Replace cracked talismans with stronger folded seals.", 12, Stat.COINS, 80),
        new Clan("Kogane", Role.ENVOY, 0xFF97652B, "Goldsmiths from the river market became trusted witnesses to contracts.", "Apprentices learn to spot hesitation and ask questions that let both sides save face.", Stat.LOOKS, 12, "Quiet Bargain", "Catch an unspoken objection and offer a face-saving trade.", 0, Stat.COINS, 55),
        new Clan("Mizuhira", Role.MEDIC, 0xFF337C78, "When the marsh road flooded, Mizuhira midwives organized the first floating clinic.", "Their inherited pulse sense makes it easier to find hidden injuries during an examination.", Stat.CHAKRA, 14, "Stillwater Pulse", "Find injuries early and direct the clinic to the people who need attention first.", 18, Stat.FAME, 11),
        new Clan("Hoshikiri", Role.SCOUT, 0xFF535C9A, "The observatory families of Starwatch mapped mountain routes from old survey towers.", "They memorize horizon marks and train to travel long distances at night.", Stat.STAMINA, 16, "North Thread", "Navigate a shortcut using landmarks and save energy on the return.", 8, Stat.STAMINA, 12),
        new Clan("Nemunoki", Role.MEDIC, 0xFF57723F, "This small family settled around a forest bathhouse and opened its doors to injured travelers.", "They practice restorative breathwork and volunteer as recovery aides.", Stat.MOOD, 14, "Cedar Lull", "Lead a calm breathing session in the clinic and recover some energy yourself.", 10, Stat.STAMINA, 14),
        new Clan("Uramori", Role.SCOUT, 0xFF614A88, "Surveyors from Fern Hollow built their reputation tracing missing hikers by sound.", "Their unusually sensitive hearing can separate a single footfall from running water.", Stat.SKILL, 5, "Hollow Echo", "Trace the courier's footsteps through the canyon's echo.", 20, Stat.FAME, 13),
        new Clan("Isen", Role.WARDEN, 0xFF815C43, "Isen masons once held a collapsing gate long enough for a whole street to evacuate.", "They train to spread an impact through their stance. The clan has no inherited power.", Stat.HEALTH, 16, "Gate Stance", "Hold the bridge approach steady until the caravan is through.", 0, Stat.SKILL, 2),
        new Clan("Sazame", Role.DUELIST, 0xFF723D61, "Sazame performers stopped taking patrons and built a school for traveling duelists.", "They learn to hide the rhythm of a movement until the final beat.", Stat.SKILL, 7, "Broken Beat", "Bait an early counter, then land a delayed strike.", 0, Stat.HEALTH, 17),
        new Clan("Toriba", Role.SCOUT, 0xFF33715C, "Toriba post riders shared sheltered routes with villages the king's messengers refused to visit.", "Apprentices learn bird calls and work alongside trained mountain shrikes.", Stat.FAME, 8, "Shrike Relay", "Send a trained shrike ahead with the rescue signal.", 0, Stat.COINS, 38),
        new Clan("Tamayori", Role.ENVOY, 0xFF94516C, "Tamayori actors once smuggled truce proposals between two besieged settlements.", "Their inherited voice control can carry one whispered sentence over a crowd.", Stat.MOOD, 12, "Single Voice", "Help the envoys hear the one witness everyone has been talking over.", 16, Stat.FAME, 13),
        new Clan("Fuyose", Role.WARDEN, 0xFF487785, "Fuyose parents built the ridge's first winter shelters out of wrecked supply sleds.", "Their inherited cold affinity can make a thin layer of frost unusually durable.", Stat.STAMINA, 12, "Rime Screen", "Cast a temporary frost screen to shield the rear wagon.", 16, Stat.FAME, 10),
        new Clan("Mamekura", Role.CRAFTER, 0xFF72713B, "This family pooled harvests in communal storehouses during a long famine.", "Their apprentices study knots that hold stored chakra until the wearer pulls a release cord.", Stat.COINS, 65, "Reserve Knot", "Refit the depot's worn gear and draw some chakra back from spent test seals.", 12, Stat.CHAKRA, 20),
        new Clan("Kuroishi", Role.DUELIST, 0xFF4D5671, "Former quarry guards founded Kuroishi's stone court, where disputes go before a referee.", "Their inherited dense bones absorb impact. Conditioning does the rest.", Stat.HEALTH, 18, "Basalt Turn", "Redirect the opponent's momentum through a low shoulder turn.", 16, Stat.HEALTH, 25),
        new Clan("Hanamichi", Role.ENVOY, 0xFFAB4E5A, "Gardeners from Lanternbridge began hosting talks on neutral ground.", "Their family etiquette makes tense meetings feel personal. Anyone can study it.", Stat.LOOKS, 16, "Guest Right", "Invite both sides to share tea and agree on what happens next.", 0, Stat.BOND, 5),
        new Clan("Tsugihara", Role.CRAFTER, 0xFF8C634B, "Scrap traders turned old battlefield gear into tools that families could afford.", "Some descendants sense tiny flaws in metal by feeling its vibration.", Stat.SKILL, 6, "Singing Rivet", "Locate stressed rivets by touch and reinforce the depot gear.", 15, Stat.FAME, 11),
        new Clan("Kasamori", Role.MEDIC, 0xFF49724A, "Kasamori keepers carried medicine between rain shelters during the Three Floods.", "They memorize treatment routines and train to keep a steady hand on the move.", Stat.HEALTH, 10, "Shelter Rounds", "Organize the clinic's treatment rounds and make time for your own care.", 0, Stat.HEALTH, 10),
        new Clan("Kazetobi", Role.DUELIST, 0xFF376D6D, "High ridge couriers built Kazetobi's school beside their old signal flags.", "Their inherited pressure sense lets them feel the air shift before a nearby movement.", Stat.STAMINA, 10, "Flagline Step", "Duck inside the opponent's reach and strike from an unexpected angle.", 12, Stat.HEALTH, 21),
        new Clan("Clanless", Role.OPEN, 0xFF73645D, "Independent shinobi come from all kinds of households and study at the public academy.", "Without a clan specialty, you start with stronger fundamentals and use the standard route in every mission.", Stat.SKILL, 9, "Academy Fundamentals", "Practice practical skills with the public academy.", 0, Stat.SKILL, 0)
    };

    private Clans() {}
}
