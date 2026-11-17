package com.shinobilife.game;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class SaveGame {
    private static final String SLOT = "active_life";

    static Life read(Context context) {
        String data = context.getSharedPreferences("shinobi", Context.MODE_PRIVATE).getString(SLOT, null);
        if (data == null) return null;
        try {
            JSONObject json = new JSONObject(data);
            Life life = new Life();
            life.name = json.getString("name");
            life.clan = bounded(json.getInt("clan"), Clans.ALL.length - 1);
            life.village = bounded(json.getInt("village"), Life.VILLAGES.length - 1);
            life.homeVillage = bounded(json.optInt("homeVillage", life.village), Life.VILLAGES.length - 1);
            life.career = Life.Career.values()[bounded(json.optInt("career"), Life.Career.values().length - 1)];
            life.careerShifts = Math.max(0, json.optInt("careerShifts"));
            life.gender = bounded(json.optInt("gender"), Life.GENDERS.length - 1);
            life.family = bounded(json.optInt("family"), Life.FAMILIES.length - 1);
            life.guardian = bounded(json.optInt("guardian"), 7);
            life.villageActions = json.optInt("villageActions") & ((1 << Life.VillageAction.values().length) - 1);
            life.presence = bounded(json.getInt("presence"), Life.PRESENCE.length - 1);
            life.season = Math.max(0, json.getInt("season"));
            life.completed = json.getInt("completed");
            life.interacted = json.getInt("interacted");
            life.victories = Math.max(0, json.getInt("victories"));
            int legacyRank = (life.victories >= 4 && json.getJSONObject("stats").optInt("SKILL") >= 35) ? 2
                    : life.victories >= 3 ? 1 : 0;
            life.academyRank = Life.AcademyRank.values()[bounded(json.optInt("academyRank", legacyRank), Life.AcademyRank.values().length - 1)];
            life.academyCredits = bounded(json.optInt("academyCredits"), 10);
            life.attendedClass = json.optBoolean("attendedClass");
            life.campaigned = json.optBoolean("campaigned");
            life.governed = json.optBoolean("governed");
            life.warService = json.optBoolean("warService");
            life.hokage = life.academyRank == Life.AcademyRank.JONIN && json.optBoolean("hokage");
            life.campaignSupport = bounded(json.optInt("campaignSupport"), 30);
            life.approval = bounded(json.optInt("approval"), 100);
            life.borderTension = bounded(json.optInt("borderTension", 24), 100);
            life.leafStability = bounded(json.optInt("leafStability", 75), 100);
            life.leafTreasury = bounded(json.optInt("leafTreasury", 180), 400);
            life.leafAtWar = json.optBoolean("leafAtWar");
            life.warEffort = bounded(json.optInt("warEffort"), 60);
            life.warSeasons = Math.max(0, json.optInt("warSeasons"));
            life.styled = json.getBoolean("styled");
            life.worked = json.getBoolean("worked");
            life.rested = json.getBoolean("rested");
            life.meditated = json.optBoolean("meditated");
            life.gifted = json.optInt("gifted") & 7;
            life.favored = json.optInt("favored") & 7;
            life.duelRival = Life.Rival.values()[bounded(json.optInt("duelRival"), Life.Rival.values().length - 1)];
            life.challengerWins = Math.max(0, json.optInt("challengerWins"));
            life.resolvedEvent = Math.max(-1, Math.min(life.season, json.optInt("resolvedEvent", -1)));
            life.enemyHp = bounded(json.optInt("enemyHp"), life.duelRival.hp);
            life.round = Math.max(0, json.optInt("round"));
            life.duelLog = json.optString("duelLog");
            int eyeMask = (1 << EyePower.values().length) - 1;
            life.generation = bounded(json.optInt("generation", 1), 100000);
            life.generationStart = bounded(json.optInt("generationStart"), life.season);
            life.startAge = Math.max(0, json.optInt("startAge", 18));
            life.worldSeed = json.optLong("worldSeed", 31L * life.name.hashCode() + life.homeVillage);
            life.deathAge = json.optInt("deathAge", Math.max(life.deathAge, life.age() + 1));
            life.deceased = json.optBoolean("deceased");
            life.seal = Life.SealState.values()[bounded(json.optInt("stillwaterSeal"), Life.SealState.values().length - 1)];
            life.graduated = json.optBoolean("graduated", life.age() >= Life.SHINOBI_AGE);
            life.populationStartAge = json.optInt("populationStartAge", life.startAge);
            life.populationStartSeason = bounded(json.optInt("populationStartSeason", life.generationStart), life.season);
            life.populationBirthVillage = bounded(json.optInt("populationBirthVillage", life.homeVillage), Life.VILLAGES.length - 1);
            life.peerResidents = json.optBoolean("peerResidents", json.optBoolean("variedContacts"));
            life.fixedResidents = json.optBoolean("fixedResidents", life.generation == 1 && !life.peerResidents);
            life.rescueVillage = indexOrMissing(json.optInt("rescueVillage", -1), Life.VILLAGES.length);
            life.rescueSeason = bounded(json.optInt("rescueSeason"), life.season + 1);
            JSONArray kin = json.optJSONArray("kin");
            if (kin != null) for (int i = 0; i < kin.length(); i++) {
                JSONObject relative = kin.getJSONObject(i);
                life.kin.add(new Life.Kin(relative.getString("name"), relative.getString("role"),
                        relative.getInt("born"), relative.optInt("died", -1),
                        indexOrMissing(relative.optInt("residentId", -1), Life.RESIDENT_COUNT)));
            }
            life.otherClan = bounded(json.optInt("otherClan", -1) + 1, Clans.ALL.length) - 1;
            life.inheritedEyes = json.has("inheritedEyes") ? json.optInt("inheritedEyes", -1) : -1;
            if (life.inheritedEyes >= 0) life.inheritedEyes &= eyeMask;
            life.nextChildSeason = Math.max(0, json.optInt("nextChildSeason"));
            JSONArray children = json.optJSONArray("children");
            if (children != null) for (int i = 0; i < Math.min(children.length(), 6); i++) {
                JSONObject child = children.optJSONObject(i);
                if (child == null) continue;
                life.children.add(new Life.Child(child.optString("name", "Ren"),
                        bounded(child.optInt("gender"), Life.GENDERS.length - 1),
                        bounded(child.optInt("clan"), Clans.ALL.length - 1),
                        bounded(child.optInt("otherClan", -1) + 1, Clans.ALL.length) - 1,
                        child.optInt("eyes") & eyeMask, bounded(child.optInt("born"), life.season),
                        bounded(child.optInt("village"), Life.VILLAGES.length - 1),
                        indexOrMissing(child.optInt("otherParent", -1), Life.RESIDENT_COUNT)));
            }
            int partner = json.optInt("partner", -1);
            life.partner = partner >= 0 && partner < Life.RESIDENT_COUNT ? partner : -1;
            life.married = life.partner >= 0 && json.optBoolean("married");
            JSONArray visits = json.optJSONArray("romanceLastVisit");
            int oldVisits = json.optInt("romanceVisits");
            JSONArray affection = json.optJSONArray("affection");
            for (int i = 0; i < life.affection.length; i++) {
                if (affection != null) life.affection[i] = bounded(affection.optInt(i), 100);
                life.romanceLastVisit[i] = visits == null ? (i < 4 && (oldVisits & (1 << i)) != 0 ? life.season + 1 : 0)
                        : bounded(visits.optInt(i), life.season + 1);
            }
            life.aptitude = Life.Aptitude.values()[bounded(json.optInt("aptitude"), Life.Aptitude.values().length - 1)];
            life.variedContacts = json.optBoolean("variedContacts");
            life.childhoodActions = json.optInt("childhoodActions") & 15;
            life.clanPractice = bounded(json.optInt("clanPractice"), 1000);
            life.guardianTurns = bounded(json.optInt("guardianTurns"), 2);
            life.sealTurns = bounded(json.optInt("sealTurns"), 2);
            JSONArray trials = json.optJSONArray("eyeTrials");
            if (trials != null) for (int i = 0; i < life.eyeTrials.length; i++) {
                life.eyeTrials[i] = bounded(trials.optInt(i), 100);
            }
            life.burnTurns = bounded(json.optInt("burnTurns"), 2);
            life.observedMove = json.optInt("observedMove");
            if (life.observedMove != life.duelRival.light && life.observedMove != life.duelRival.heavy) life.observedMove = 0;
            life.bindUsed = json.optBoolean("bindUsed");
            EyePower oldEye = life.clan().eye();
            int legacyEye = oldEye != null && oldEye != EyePower.FARWATCH ? oldEye.mask() : 0;
            life.awakenedEyes = json.optInt("awakenedEyes", json.optBoolean("eyeAwakened") ? legacyEye : 0) & life.heritageEyes();
            life.evolvedEyes = json.optInt("evolvedEyes") & life.awakenedEyes;
            life.usedForms = json.optInt("usedForms") & life.evolvedEyes;
            life.usedEyes = json.optInt("usedEyes", json.optBoolean("eyeUsed") ? legacyEye : 0) & life.awakenedEyes;
            life.scoutedMissions = json.optInt("scoutedMissions", life.completed);
            int summonMask = (1 << Summon.ALL.length) - 1;
            life.bondedSummons = json.optInt("bondedSummons") & summonMask;
            life.duelSummons = json.optInt("duelSummons") & summonMask;
            JSONArray summons = json.optJSONArray("summons");
            if (summons == null) life.grantStartingSummon();
            else for (int i = 0; i < life.summonBonds.length; i++) {
                life.summonBonds[i] = bounded(summons.optInt(i), 100);
            }
            JSONArray styles = json.getJSONArray("styles");
            for (int i = 0; i < life.style.length; i++) life.style[i] = bounded(styles.optInt(i, life.style[i]), Portrait.OPTIONS[i].length - 1);
            JSONArray bonds = json.getJSONArray("bonds");
            for (int i = 0; i < life.bonds.length; i++) life.bonds[i] = bounded(bonds.getInt(i), 100);
            JSONObject stats = json.getJSONObject("stats");
            for (Clans.Stat stat : Clans.Stat.values()) {
                life.stats.put(stat, bounded(stats.optInt(stat.name()), stat == Clans.Stat.COINS ? Integer.MAX_VALUE : 100));
            }
            JSONArray journal = json.getJSONArray("journal");
            for (int i = 0; i < journal.length(); i++) {
                JSONObject event = journal.getJSONObject(i);
                String title = event.getString("title");
                life.journal.add(new Life.Event(event.getString("date"), title, event.getString("detail")));
                if (!json.has("scoutedMissions")) {
                    for (Summon summon : Summon.ALL) {
                        if (summon.mission.title.equals(title)) life.scoutedMissions |= summon.mission.mask();
                    }
                }
            }
            return life;
        } catch (JSONException error) {
            Log.e("ShinobiLife", "Could not read saved life", error);
            return null;
        }
    }

    static void write(Context context, Life life) {
        try {
            JSONObject json = new JSONObject();
            json.put("name", life.name);
            json.put("clan", life.clan);
            json.put("village", life.village);
            json.put("homeVillage", life.homeVillage);
            json.put("career", life.career.ordinal());
            json.put("careerShifts", life.careerShifts);
            json.put("gender", life.gender);
            json.put("family", life.family);
            json.put("guardian", life.guardian);
            json.put("villageActions", life.villageActions);
            json.put("presence", life.presence);
            json.put("season", life.season);
            json.put("completed", life.completed);
            json.put("interacted", life.interacted);
            json.put("victories", life.victories);
            json.put("academyCredits", life.academyCredits);
            json.put("attendedClass", life.attendedClass);
            json.put("campaigned", life.campaigned);
            json.put("governed", life.governed);
            json.put("warService", life.warService);
            json.put("hokage", life.hokage);
            json.put("campaignSupport", life.campaignSupport);
            json.put("approval", life.approval);
            json.put("borderTension", life.borderTension);
            json.put("leafStability", life.leafStability);
            json.put("leafTreasury", life.leafTreasury);
            json.put("leafAtWar", life.leafAtWar);
            json.put("warEffort", life.warEffort);
            json.put("warSeasons", life.warSeasons);
            json.put("academyRank", life.academyRank.ordinal());
            json.put("styled", life.styled);
            json.put("worked", life.worked);
            json.put("rested", life.rested);
            json.put("meditated", life.meditated);
            json.put("gifted", life.gifted);
            json.put("favored", life.favored);
            json.put("duelRival", life.duelRival.ordinal());
            json.put("challengerWins", life.challengerWins);
            json.put("resolvedEvent", life.resolvedEvent);
            json.put("enemyHp", life.enemyHp);
            json.put("round", life.round);
            json.put("duelLog", life.duelLog);
            json.put("partner", life.partner);
            json.put("married", life.married);
            json.put("romanceLastVisit", new JSONArray(life.romanceLastVisit));
            json.put("generation", life.generation);
            json.put("generationStart", life.generationStart);
            json.put("startAge", life.startAge);
            json.put("worldSeed", life.worldSeed);
            json.put("deathAge", life.deathAge);
            json.put("deceased", life.deceased);
            json.put("stillwaterSeal", life.seal.ordinal());
            json.put("graduated", life.graduated);
            json.put("populationStartAge", life.populationStartAge);
            json.put("populationStartSeason", life.populationStartSeason);
            json.put("populationBirthVillage", life.populationBirthVillage);
            json.put("peerResidents", life.peerResidents);
            json.put("fixedResidents", life.fixedResidents);
            json.put("rescueVillage", life.rescueVillage);
            json.put("rescueSeason", life.rescueSeason);
            JSONArray kin = new JSONArray();
            for (Life.Kin relative : life.kin) {
                JSONObject entry = new JSONObject();
                entry.put("name", relative.name());
                entry.put("role", relative.role());
                entry.put("born", relative.born());
                entry.put("died", relative.died());
                entry.put("residentId", relative.residentId());
                kin.put(entry);
            }
            json.put("kin", kin);
            json.put("otherClan", life.otherClan);
            json.put("inheritedEyes", life.inheritedEyes);
            json.put("nextChildSeason", life.nextChildSeason);
            JSONArray children = new JSONArray();
            for (Life.Child child : life.children) {
                JSONObject data = new JSONObject();
                data.put("name", child.name());
                data.put("gender", child.gender());
                data.put("clan", child.clan());
                data.put("otherClan", child.otherClan());
                data.put("eyes", child.eyes());
                data.put("born", child.born());
                data.put("village", child.village());
                data.put("otherParent", child.otherParent());
                children.put(data);
            }
            json.put("children", children);
            json.put("affection", new JSONArray(life.affection));
            json.put("aptitude", life.aptitude.ordinal());
            json.put("variedContacts", life.variedContacts);
            json.put("childhoodActions", life.childhoodActions);
            json.put("clanPractice", life.clanPractice);
            json.put("evolvedEyes", life.evolvedEyes);
            json.put("usedForms", life.usedForms);
            json.put("guardianTurns", life.guardianTurns);
            json.put("sealTurns", life.sealTurns);
            json.put("eyeTrials", new JSONArray(life.eyeTrials));
            json.put("burnTurns", life.burnTurns);
            json.put("observedMove", life.observedMove);
            json.put("bindUsed", life.bindUsed);
            json.put("awakenedEyes", life.awakenedEyes);
            json.put("usedEyes", life.usedEyes);
            json.put("scoutedMissions", life.scoutedMissions);
            json.put("bondedSummons", life.bondedSummons);
            json.put("duelSummons", life.duelSummons);
            json.put("summons", new JSONArray(life.summonBonds));
            json.put("styles", new JSONArray(life.style));
            json.put("bonds", new JSONArray(life.bonds));
            JSONObject stats = new JSONObject();
            for (Clans.Stat stat : Clans.Stat.values()) stats.put(stat.name(), life.get(stat));
            json.put("stats", stats);
            JSONArray journal = new JSONArray();
            for (Life.Event entry : life.journal) {
                JSONObject event = new JSONObject();
                event.put("date", entry.date());
                event.put("title", entry.title());
                event.put("detail", entry.detail());
                journal.put(event);
            }
            json.put("journal", journal);
            context.getSharedPreferences("shinobi", Context.MODE_PRIVATE).edit().putString(SLOT, json.toString()).apply();
        } catch (JSONException error) {
            throw new IllegalStateException("Could not encode saved life", error);
        }
    }

    private static int indexOrMissing(int id, int count) {
        return id >= 0 && id < count ? id : -1;
    }

    private static int bounded(int value, int max) { return Math.max(0, Math.min(max, value)); }
    private SaveGame() {}
}
