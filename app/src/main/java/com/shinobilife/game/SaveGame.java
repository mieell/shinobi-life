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
            life.presence = bounded(json.getInt("presence"), Life.PRESENCE.length - 1);
            life.season = Math.max(0, json.getInt("season"));
            life.completed = json.getInt("completed");
            life.interacted = json.getInt("interacted");
            life.victories = Math.max(0, json.getInt("victories"));
            life.styled = json.getBoolean("styled");
            life.worked = json.getBoolean("worked");
            life.rested = json.getBoolean("rested");
            life.meditated = json.optBoolean("meditated");
            life.gifted = json.optInt("gifted") & 7;
            life.favored = json.optInt("favored") & 7;
            life.enemyHp = bounded(json.optInt("enemyHp"), 72);
            life.round = Math.max(0, json.optInt("round"));
            life.duelLog = json.optString("duelLog");
            life.burnTurns = bounded(json.optInt("burnTurns"), 2);
            life.observedMove = json.optInt("observedMove");
            if (life.observedMove != 10 && life.observedMove != 18) life.observedMove = 0;
            life.bindUsed = json.optBoolean("bindUsed");
            life.eyeAwakened = life.clan().eye() != null && json.optBoolean("eyeAwakened");
            life.eyeUsed = json.optBoolean("eyeUsed");
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
            for (int i = 0; i < life.style.length; i++) life.style[i] = bounded(styles.getInt(i), Portrait.OPTIONS[i].length - 1);
            JSONArray bonds = json.getJSONArray("bonds");
            for (int i = 0; i < life.bonds.length; i++) life.bonds[i] = bounded(bonds.getInt(i), 100);
            JSONObject stats = json.getJSONObject("stats");
            for (Clans.Stat stat : Clans.Stat.values()) {
                life.stats.put(stat, bounded(stats.optInt(stat.name()), stat == Clans.Stat.COINS ? Integer.MAX_VALUE : 100));
            }
            JSONArray journal = json.getJSONArray("journal");
            for (int i = 0; i < Math.min(100, journal.length()); i++) {
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
            json.put("presence", life.presence);
            json.put("season", life.season);
            json.put("completed", life.completed);
            json.put("interacted", life.interacted);
            json.put("victories", life.victories);
            json.put("styled", life.styled);
            json.put("worked", life.worked);
            json.put("rested", life.rested);
            json.put("meditated", life.meditated);
            json.put("gifted", life.gifted);
            json.put("favored", life.favored);
            json.put("enemyHp", life.enemyHp);
            json.put("round", life.round);
            json.put("duelLog", life.duelLog);
            json.put("burnTurns", life.burnTurns);
            json.put("observedMove", life.observedMove);
            json.put("bindUsed", life.bindUsed);
            json.put("eyeAwakened", life.eyeAwakened);
            json.put("eyeUsed", life.eyeUsed);
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

    private static int bounded(int value, int max) { return Math.max(0, Math.min(max, value)); }
    private SaveGame() {}
}
