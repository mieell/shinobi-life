package com.shinobilife.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Random;

import static com.shinobilife.game.Clans.Role;
import static com.shinobilife.game.Clans.Stat;

public final class MainActivity extends Activity {
    private static final int PAPER = 0xFFFBF7EF, WHITE = 0xFFFFFDF8, INK = 0xFF28232D;
    private static final int MUTED = 0xFF695D68, RED = 0xFF933B31, BORDER = 0xFFE9DFD3;
    private static final int BLUSH = 0xFFF6E5DF, LILAC = 0xFFEEE7F3, PURPLE = 0xFF60416F;
    private static final int GREEN = 0xFF245B49, MINT = 0xFFE4EFE5, AMBER = 0xFF87592A;
    private static final int MATCH = -1, WRAP = -2;
    private static final Typeface BODY = Typeface.create("sans-serif", Typeface.NORMAL);
    private static final Typeface MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    private static final Typeface DISPLAY = Typeface.create("serif", Typeface.BOLD);
    private static final String[] TABS = {"Life", "Missions", "Clans", "People"};
    private static final String[] TAB_MARKS = {"life", "missions", "clans", "people"};
    private static final int[][] CONTACT_STYLE = {
        {4, 1, 2, 1, 4, 0, 1}, {2, 3, 3, 4, 1, 0, 0}, {3, 2, 4, 3, 3, 1, 2}
    };
    private static final int[] RIVAL_STYLE = {5, 1, 2, 4, 1, 2, 0};

    private Life life;
    private String page = "life";
    private ScrollView scroll;
    private LinearLayout body;
    private Role filter;
    private int selectedClan, selectedContact;
    private boolean chooseClan;
    private Draft draft;

    private static final class Draft {
        String name = "Ren";
        int clan, village, presence = 1;
        int[] style = {0, 0, 0, 2, 0, 0, 0};
    }

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        life = SaveGame.read(this);
        if (life == null) {
            Draft starter = new Draft();
            life = Life.begin(starter.name, starter.clan, starter.village, starter.presence, starter.style);
            SaveGame.write(this, life);
        }
        if (life.enemyHp > 0) page = "duel";
        if (Build.VERSION.SDK_INT >= 33) Back33.register(this, this::handleBack);
        render(0);
    }

    @android.annotation.TargetApi(33)
    private static final class Back33 {
        static void register(Activity activity, Runnable action) {
            activity.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, action::run);
        }
    }

    // Android 13 and later use Back33 for system back, including gestures.
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed() {
        handleBack();
    }

    private void handleBack() {
        switch (page) {
            case "duel" -> {
                if (life.enemyHp > 0) {
                    new AlertDialog.Builder(this).setTitle("Concede this match?")
                        .setMessage("The next challenge opens next season.")
                        .setNegativeButton("Keep playing", null)
                        .setPositiveButton("Concede", (dialog, which) -> {
                            life.duel(Life.Move.RETREAT);
                            update();
                        }).show();
                } else go("missions");
            }
            case "detail" -> go("clans");
            case "clans" -> {
                if (chooseClan) {
                    chooseClan = false;
                    go("create");
                } else go("life");
            }
            case "person" -> go("people");
            case "create" -> go("life");
            case "life" -> moveTaskToBack(true);
            default -> go("life");
        }
    }

    private void go(String next) {
        page = next;
        render(0);
    }

    private void update() {
        SaveGame.write(this, life);
        render(scroll.getScrollY());
    }

    private void render(int at) {
        FrameLayout window = new FrameLayout(this);
        window.setBackgroundColor(PAPER);
        LinearLayout root = column();
        window.addView(root, new FrameLayout.LayoutParams(MATCH, MATCH, Gravity.TOP | Gravity.CENTER_HORIZONTAL));
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().setNavigationBarContrastEnforced(false);
            window.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets safe = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                int sides = Math.max(0, (view.getWidth() - dp(580)) / 2);
                view.setPadding(Math.max(safe.left, sides), safe.top, Math.max(safe.right, sides), safe.bottom);
                WindowInsetsController bars = view.getWindowInsetsController();
                int light = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                if (bars != null) bars.setSystemBarsAppearance(light, light);
                return insets;
            });
            window.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (right - left != oldRight - oldLeft) view.requestApplyInsets();
            });
        } else {
            getWindow().setStatusBarColor(PAPER);
            getWindow().setNavigationBarColor(INK);
        }
        root.addView(header(), new LinearLayout.LayoutParams(MATCH, WRAP));
        if (page.equals("create")) root.addView(editorPreview());
        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        root.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1));
        body = column();
        inset(body, 16, 14, 16, 20);
        scroll.addView(body, new ScrollView.LayoutParams(MATCH, WRAP));
        switch (page) {
            case "life" -> lifePage();
            case "missions" -> missionsPage();
            case "clans" -> clansPage();
            case "detail" -> clanDetail();
            case "people" -> peoplePage();
            case "person" -> personPage();
            case "create" -> createPage();
            case "duel" -> duelPage();
        }
        if (page.equals("life")) root.addView(advanceBar());
        if (page.equals("detail") && chooseClan) {
            Clans.Clan clan = Clans.ALL[selectedClan];
            root.addView(footerButton("Choose " + clan.name(), () -> {
                draft.clan = selectedClan;
                chooseClan = false;
                go("create");
            }));
        }
        if (page.equals("create")) root.addView(footerButton("Begin life", this::confirmLife));
        if (isTab() && !chooseClan) root.addView(dock());
        setContentView(window);
        window.requestApplyInsets();
        ScrollView current = scroll;
        if (at > 0) current.post(() -> current.scrollTo(0, at));
    }

    private boolean isTab() {
        for (String tab : TAB_MARKS) if (page.equals(tab)) return true;
        return false;
    }

    private View header() {
        LinearLayout bar = row();
        inset(bar, 16, 4, 12, 4);
        bar.setBackgroundColor(PAPER);
        if (!isTab() || chooseClan) {
            Button back = button("‹", RED, BLUSH, null, this::handleBack);
            back.setTextSize(26);
            back.setContentDescription("Go back");
            bar.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        } else {
            android.widget.ImageView flag = new android.widget.ImageView(this);
            flag.setImageResource(R.drawable.crest);
            flag.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            bar.addView(flag, new LinearLayout.LayoutParams(dp(35), dp(35)));
        }
        TextView title = text("Shinobi Life", 23, INK, DISPLAY);
        LinearLayout.LayoutParams titleBox = new LinearLayout.LayoutParams(0, WRAP, 1);
        titleBox.setMarginStart(dp(9));
        bar.addView(title, titleBox);
        if (isTab() && !chooseClan) bar.addView(button("New life", RED, BLUSH, null, () -> {
            draft = new Draft();
            go("create");
        }));
        return bar;
    }

    private View dock() {
        LinearLayout layout = column();
        layout.setBackgroundColor(WHITE);
        line(layout);
        LinearLayout menu = row();
        inset(menu, 8, 4, 8, 4);
        layout.addView(menu);
        for (int i = 0; i < TABS.length; i++) {
            String next = TAB_MARKS[i];
            boolean active = next.equals(page);
            int color = active ? RED : MUTED;
            LinearLayout tab = column();
            tab.setGravity(Gravity.CENTER);
            tab.setMinimumHeight(dp(56));
            Marks icon = new Marks(this, next, color);
            tab.addView(icon, new LinearLayout.LayoutParams(dp(20), dp(20)));
            TextView label = text(TABS[i], 12, color, MEDIUM);
            label.setGravity(Gravity.CENTER);
            tab.addView(label);
            clickable(tab, active ? BLUSH : WHITE, null, () -> {
                if (next.equals("clans")) filter = null;
                go(next);
            });
            tab.setContentDescription(TABS[i] + (active ? ", selected" : ""));
            tab.setSelected(active);
            menu.addView(tab, new LinearLayout.LayoutParams(0, WRAP, 1));
        }
        return layout;
    }

    private void lifePage() {
        LinearLayout heading = row();
        heading.addView(text("Your life", 32, INK, DISPLAY), new LinearLayout.LayoutParams(0, WRAP, 1));
        heading.addView(pill(life.date(), RED, BLUSH));
        body.addView(heading);
        gap(body, 12);
        LinearLayout card = row();
        card.setBackground(box(LILAC, null, 22));
        inset(card, 12, 12, 12, 12);
        Portrait portrait = new Portrait(this, life.style, life.age(), 0xFFDDCEE8, "Portrait of " + life.name);
        card.addView(portrait, new LinearLayout.LayoutParams(dp(80), dp(80)));
        LinearLayout info = column();
        inset(info, 12, 0, 0, 0);
        info.addView(text(displayName(life.name, life.clan), 24, INK, DISPLAY));
        info.addView(text(life.village() + " · " + life.rank(), 13, PURPLE, MEDIUM));
        gap(info, 4);
        info.addView(text(money(life.get(Stat.COINS)) + " coins · Skill " + life.get(Stat.SKILL), 13, INK, MEDIUM));
        card.addView(info, new LinearLayout.LayoutParams(0, WRAP, 1));
        body.addView(card);
        gap(body, 10);
        LinearLayout meters = card(WHITE, 12);
        meters.addView(meterPair(Stat.HEALTH, GREEN, Stat.CHAKRA, PURPLE));
        gap(meters, 9);
        meters.addView(meterPair(Stat.STAMINA, AMBER, Stat.MOOD, RED));
        gap(meters, 9);
        meters.addView(meterPair(Stat.LOOKS, RED, Stat.FAME, PURPLE));
        body.addView(meters);
        gap(body, 13);
        LinearLayout actions = row();
        quick(actions, "Train", "training", () -> {
            if (!life.train()) message("Training needs 16 stamina and room to increase Skill.");
            else update();
        });
        quick(actions, "Style", "spark", () -> {
            new AlertDialog.Builder(this).setTitle("Visit the stylist")
                    .setMessage("Spend 35 coins for up to +8 Looks. Available once each season. At 75 Looks, social visits build more Trust.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Visit", (dialog, which) -> {
                        if (life.styled) message("You already visited this season.");
                        else if (life.get(Stat.LOOKS) == 100) message("Your Looks are already at 100.");
                        else if (!life.styleUp()) message("You need 35 coins.");
                        else update();
                    }).show();
        });
        quick(actions, "Work", "coin", () -> {
            if (!life.work()) message(life.worked ? "You already worked this season." : "You need 12 stamina.");
            else update();
        });
        quick(actions, "Rest", "life", () -> {
            if (!life.rest()) message("You already rested this season.");
            else update();
        });
        body.addView(actions);
        gap(body, 18);
        body.addView(text("Life journal", 24, INK, DISPLAY));
        gap(body, 7);
        for (Life.Event event : life.journal) journal(event);
    }

    private void journal(Life.Event event) {
        LinearLayout entry = row();
        entry.setGravity(Gravity.TOP);
        TextView knot = text("◆", 11, RED, MEDIUM);
        inset(knot, 0, 3, 0, 0);
        entry.addView(knot, new LinearLayout.LayoutParams(dp(18), WRAP));
        LinearLayout content = column();
        content.addView(text(event.date(), 12, RED, MEDIUM));
        content.addView(text(event.title(), 18, INK, DISPLAY));
        content.addView(text(event.detail(), 14, MUTED, BODY));
        inset(content, 0, 0, 0, 12);
        entry.addView(content, new LinearLayout.LayoutParams(0, WRAP, 1));
        body.addView(entry);
    }

    private View advanceBar() {
        return footerButton("Advance season  ›", () -> {
            life.advance();
            SaveGame.write(this, life);
            go("life");
        });
    }

    private void missionsPage() {
        heading("Mission board", life.village() + " · " + life.date());
        for (Missions mission : Missions.values()) {
            LinearLayout card = card(WHITE, 14);
            card.addView(text(mission.role.label + (life.matches(mission) ? " · Your specialty" : ""), 12, RED, MEDIUM));
            card.addView(text(mission.title, 21, INK, DISPLAY));
            card.addView(text(mission.description, 14, MUTED, BODY));
            gap(card, 7);
            card.addView(text(mission.stamina + " stamina · " + mission.coins + " coins · +" + mission.fame + " Fame", 13, GREEN, MEDIUM));
            gap(card, 7);
            if (life.isComplete(mission)) card.addView(pill("Closed for this season", MUTED, PAPER));
            else if (!life.canStart(mission)) {
                card.addView(pill(life.get(Stat.STAMINA) < mission.stamina ? "Low stamina" : "Needs 30 health", RED, BLUSH));
            } else {
                buttonIn(card, mission == Missions.DUEL ? "Enter duel" : "Take assignment", RED, BLUSH, () -> take(mission, false));
                if (mission != Missions.DUEL && life.matches(mission)) {
                    Clans.Clan clan = life.clan();
                    gap(card, 5);
                    card.addView(text("+" + clan.amount() + " " + clan.bonus().label + " with " + clan.technique(), 13, PURPLE, MEDIUM));
                    buttonIn(card, "Use " + clan.technique() + " · " + clan.costLabel(),
                            PURPLE, LILAC, () -> take(mission, true));
                }
            }
            body.addView(card);
            gap(body, 9);
        }
    }

    private void take(Missions mission, boolean technique) {
        if (!life.take(mission, technique)) {
            message("This assignment or technique is unavailable.");
            return;
        }
        SaveGame.write(this, life);
        if (mission == Missions.DUEL) go("duel");
        else {
            message("Assignment finished. Recorded in your journal.");
            render(scroll.getScrollY());
        }
    }

    private void duelPage() {
        heading("Cedar Pass", "Public duel · Ibara Kase");
        LinearLayout arena = row();
        arena.addView(fighter(life.name, life.style, life.age(), LILAC, life.get(Stat.HEALTH), 100), new LinearLayout.LayoutParams(0, WRAP, 1));
        TextView vs = text("VS", 16, RED, DISPLAY);
        inset(vs, 8, 0, 8, 0);
        arena.addView(vs);
        arena.addView(fighter("Ibara", RIVAL_STYLE, 27, BLUSH, life.enemyHp, 72), new LinearLayout.LayoutParams(0, WRAP, 1));
        body.addView(arena);
        gap(body, 12);
        LinearLayout log = card(MINT, 15);
        log.addView(text(life.enemyHp > 0 ? "Round " + life.round + " · " + life.get(Stat.CHAKRA) + " chakra" : "Match ended", 14, GREEN, MEDIUM));
        log.addView(text(life.duelLog, 16, INK, BODY));
        log.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        body.addView(log);
        gap(body, 12);
        if (life.enemyHp <= 0) {
            buttonIn(body, "Return to mission board", Color.WHITE, RED, () -> go("missions"));
            return;
        }
        duelButton("Strike · " + (12 + life.get(Stat.SKILL) / 6) + " damage", Life.Move.STRIKE, Color.WHITE, RED);
        duelButton("Guard · Recover 8 chakra", Life.Move.GUARD, GREEN, MINT);
        if (life.matches(Missions.DUEL)) {
            Clans.Clan clan = life.clan();
            duelButton(clan.technique() + " · " + clan.amount() + " damage · " + clan.costLabel(), Life.Move.TECHNIQUE, PURPLE, LILAC);
        }
        buttonIn(body, "Concede match", MUTED, WHITE, this::handleBack);
    }

    private LinearLayout fighter(String name, int[] style, int age, int color, int hp, int max) {
        LinearLayout card = card(color, 10);
        card.setGravity(Gravity.CENTER);
        card.addView(new Portrait(this, style, age, color == LILAC ? 0xFFD9C7E9 : 0xFFE7C3B0, name),
                new LinearLayout.LayoutParams(dp(75), dp(75)));
        TextView label = text(name, 19, INK, DISPLAY);
        label.setGravity(Gravity.CENTER);
        card.addView(label);
        card.addView(text(hp + "/" + max + " HP", 13, GREEN, MEDIUM));
        gap(card, 6);
        card.addView(progress(hp * 100 / max, GREEN));
        return card;
    }

    private void duelButton(String label, Life.Move move, int color, int fill) {
        buttonIn(body, label, color, fill, () -> {
            if (!life.duel(move)) message("You need more chakra.");
            else update();
        });
        gap(body, 7);
    }

    private void clansPage() {
        heading(chooseClan ? "Choose a clan" : "Clan codex",
                "A clan is your family and fighting tradition. It gives you a starting perk and a technique for certain missions.");
        if (!chooseClan) {
            LinearLayout current = card(LILAC, 13);
            current.addView(text("YOUR LINEAGE", 11, PURPLE, MEDIUM));
            current.addView(text(life.clan().name(), 24, INK, DISPLAY));
            current.addView(text(life.clan().role().label + " · " + life.clan().technique(), 14, PURPLE, MEDIUM));
            clickable(current, LILAC, null, () -> detail(life.clan));
            body.addView(current);
            gap(body, 10);
        }
        LinearLayout filterRow = row();
        filterRow.addView(text((filter == null ? "20 clans + Clanless" : filter.label), 16, INK, MEDIUM),
                new LinearLayout.LayoutParams(0, WRAP, 1));
        filterRow.addView(button("Filter", RED, BLUSH, null, this::filterClans));
        body.addView(filterRow);
        gap(body, 7);
        for (int i = 0; i < Clans.ALL.length; i++) {
            Clans.Clan clan = Clans.ALL[i];
            if (filter == null || clan.role() == filter) clanRow(i);
        }
    }

    private void filterClans() {
        Role[] roles = Role.values();
        String[] names = new String[roles.length + 1];
        names[0] = "All clans";
        for (int i = 0; i < roles.length; i++) names[i + 1] = roles[i].label;
        new AlertDialog.Builder(this).setTitle("Filter by playstyle").setSingleChoiceItems(names,
                filter == null ? 0 : filter.ordinal() + 1, (dialog, which) -> {
                    dialog.dismiss();
                    filter = which == 0 ? null : roles[which - 1];
                    render(0);
                }).setNegativeButton("Close", null).show();
    }

    private void clanRow(int index) {
        Clans.Clan clan = Clans.ALL[index];
        LinearLayout item = row();
        inset(item, 12, 9, 12, 9);
        addSeal(item, index, 44);
        LinearLayout info = column();
        inset(info, 10, 0, 8, 0);
        info.addView(text(clan.name(), 20, INK, DISPLAY));
        info.addView(text(clan.role().label + " · " + clan.perkLabel(), 13, MUTED, MEDIUM));
        item.addView(info, new LinearLayout.LayoutParams(0, WRAP, 1));
        item.addView(text("›", 26, RED, BODY));
        clickable(item, WHITE, BORDER, () -> detail(index));
        body.addView(item);
        gap(body, 7);
    }

    private void detail(int clan) {
        selectedClan = clan;
        go("detail");
    }

    private void clanDetail() {
        Clans.Clan clan = Clans.ALL[selectedClan];
        LinearLayout banner = row();
        banner.setBackground(box(LILAC, null, 20));
        inset(banner, 13, 13, 13, 13);
        addSeal(banner, selectedClan, 58);
        LinearLayout title = column();
        inset(title, 12, 0, 0, 0);
        title.addView(text(clan.role().label, 13, PURPLE, MEDIUM));
        title.addView(text(clan.name(), 32, INK, DISPLAY));
        banner.addView(title, new LinearLayout.LayoutParams(0, WRAP, 1));
        body.addView(banner);
        gap(body, 10);
        fact("Starting perk", clan.perkLabel() + " at the start of a new life.");
        if (clan.role() == Role.OPEN) {
            fact("How you play", clan.knack());
        } else {
            fact("Signature technique · " + clan.technique(), clan.action() + " " + clan.effect());
            fact("When you can use it", clan.limit());
            fact("Family tradition", clan.knack());
        }
        fact("Origins", clan.origin());
        if (!chooseClan && selectedClan != life.clan) {
            buttonIn(body, "Start a life as " + clan.name(), Color.WHITE, RED, () -> {
                draft = new Draft();
                draft.clan = selectedClan;
                go("create");
            });
        }
    }

    private void addSeal(LinearLayout host, int index, int size) {
        Clans.Clan clan = Clans.ALL[index];
        FrameLayout seal = new FrameLayout(this);
        seal.setBackground(box(clan.color(), null, 15));
        seal.addView(new Marks(this, Integer.toString(index), 0xFFFFE8BE),
                new FrameLayout.LayoutParams(dp(size * 5 / 8), dp(size * 5 / 8), Gravity.CENTER));
        host.addView(seal, new LinearLayout.LayoutParams(dp(size), dp(size)));
    }

    private void peoplePage() {
        heading("Relationships", "Spend time with people in your life and build Trust.");
        for (int i = 0; i < Life.CONTACTS.length; i++) {
            int contact = i;
            LinearLayout item = row();
            inset(item, 12, 9, 12, 9);
            item.addView(contactPortrait(i), new LinearLayout.LayoutParams(dp(59), dp(59)));
            LinearLayout info = column();
            inset(info, 10, 0, 8, 0);
            info.addView(text(Life.CONTACTS[i], 21, INK, DISPLAY));
            info.addView(text(Life.ROLES[i] + " · " + life.bonds[i] + " Trust", 13, MUTED, MEDIUM));
            item.addView(info, new LinearLayout.LayoutParams(0, WRAP, 1));
            item.addView(text("›", 26, RED, BODY));
            clickable(item, WHITE, BORDER, () -> {
                selectedContact = contact;
                go("person");
            });
            body.addView(item);
            gap(body, 8);
        }
    }

    private Portrait contactPortrait(int contact) {
        int[] hues = {0xFFE9CDBA, 0xFFCBDED2, 0xFFDFCEE9};
        return new Portrait(this, CONTACT_STYLE[contact], contact == 0 ? life.age() + 20 : life.age(),
                hues[contact], "Portrait of " + Life.CONTACTS[contact]);
    }

    private void personPage() {
        int contact = selectedContact;
        LinearLayout intro = card(LILAC, 14);
        intro.setGravity(Gravity.CENTER);
        intro.addView(contactPortrait(contact), new LinearLayout.LayoutParams(dp(112), dp(112)));
        intro.addView(text(Life.CONTACTS[contact], 29, INK, DISPLAY));
        intro.addView(text(Life.ROLES[contact] + " · " + life.bonds[contact] + " Trust", 14, PURPLE, MEDIUM));
        body.addView(intro);
        gap(body, 12);
        String[] history = {
            "Nao took you to lantern festivals when you were too small to carry your own.",
            "You and Jun used to race home from academy drills. Jun still insists you cut corners.",
            "Mika was your closest match in academy trials and still asks how your training is going."
        };
        fact("How you met", history[contact]);
        if ((life.interacted & (1 << contact)) != 0) {
            body.addView(pill("You spent time together this season", GREEN, MINT));
        } else {
            buttonIn(body, "Spend time together", Color.WHITE, RED, () -> {
                if (life.interact(contact)) update();
            });
        }
    }

    private View editorPreview() {
        LinearLayout surround = column();
        inset(surround, 16, 4, 16, 0);
        LinearLayout preview = row();
        preview.setBackground(box(LILAC, null, 18));
        inset(preview, 10, 8, 10, 8);
        preview.addView(new Portrait(this, draft.style, 18, 0xFFDCCCE7, "Avatar preview"),
                new LinearLayout.LayoutParams(dp(83), dp(83)));
        LinearLayout control = column();
        inset(control, 10, 0, 0, 0);
        control.addView(text("Create your shinobi", 22, INK, DISPLAY));
        control.addView(button("Randomize", PURPLE, LILAC, null, this::randomize));
        preview.addView(control, new LinearLayout.LayoutParams(0, WRAP, 1));
        surround.addView(preview);
        return surround;
    }

    private void createPage() {
        body.addView(text("Appearance", 24, INK, DISPLAY));
        gap(body, 5);
        LinearLayout options = card(WHITE, 12);
        for (int i = 0; i < Portrait.LABELS.length; i++) {
            int slot = i;
            choice(options, Portrait.LABELS[i], Portrait.OPTIONS[i], draft.style[i], selected -> draft.style[slot] = selected);
        }
        body.addView(options);
        gap(body, 12);
        body.addView(text("Origin & presence", 24, INK, DISPLAY));
        gap(body, 5);
        LinearLayout identity = card(WHITE, 13);
        TextView nameLabel = text("Given name", 14, MUTED, MEDIUM);
        identity.addView(nameLabel);
        EditText name = new EditText(this);
        name.setId(View.generateViewId());
        nameLabel.setLabelFor(name.getId());
        name.setSingleLine(true);
        name.setTextColor(INK);
        name.setTextSize(18);
        name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
        name.setText(draft.name);
        name.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { draft.name = s.toString(); }
            public void afterTextChanged(android.text.Editable text) {}
        });
        identity.addView(name, new LinearLayout.LayoutParams(MATCH, WRAP));
        gap(identity, 5);
        choice(identity, "Village", Life.VILLAGES, draft.village, i -> draft.village = i);
        choice(identity, "Presence", Life.PRESENCE, draft.presence, i -> draft.presence = i);
        int looks = Life.BASE_LOOKS[draft.presence] + (Clans.ALL[draft.clan].perk() == Stat.LOOKS ? Clans.ALL[draft.clan].start() : 0);
        identity.addView(text("Starting Looks " + Math.min(100, looks) + "/100. Your physical features are yours to choose.", 13, MUTED, BODY));
        body.addView(identity);
        gap(body, 10);
        LinearLayout ancestry = card(WHITE, 13);
        ancestry.addView(text("Clan", 15, MUTED, MEDIUM));
        ancestry.addView(text(Clans.ALL[draft.clan].name() + " · " + Clans.ALL[draft.clan].perkLabel(), 20, INK, DISPLAY));
        gap(ancestry, 4);
        buttonIn(ancestry, "Browse clans", RED, BLUSH, () -> {
            chooseClan = true;
            filter = null;
            go("clans");
        });
        body.addView(ancestry);
        body.setFocusableInTouchMode(true);
        body.requestFocus();
    }

    private void choice(LinearLayout target, String label, String[] options, int current, java.util.function.IntConsumer apply) {
        Button value = button(label + "   ·   " + options[current] + "   ›", INK, PAPER, null, () -> {
            new AlertDialog.Builder(this).setTitle(label).setSingleChoiceItems(options, current, (dialog, which) -> {
                dialog.dismiss();
                apply.accept(which);
                render(scroll.getScrollY());
            }).setNegativeButton("Close", null).show();
        });
        value.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        target.addView(value, new LinearLayout.LayoutParams(MATCH, WRAP));
        gap(target, 4);
    }

    private void randomize() {
        Random random = new Random();
        draft.name = Life.randomName(random);
        draft.village = random.nextInt(Life.VILLAGES.length);
        draft.clan = random.nextInt(Clans.ALL.length);
        draft.presence = random.nextInt(Life.PRESENCE.length);
        for (int i = 0; i < draft.style.length; i++) draft.style[i] = random.nextInt(Portrait.OPTIONS[i].length);
        render(0);
    }

    private void confirmLife() {
        String name = draft.name.trim();
        if (name.isEmpty()) {
            message("Enter a given name.");
            return;
        }
        new AlertDialog.Builder(this).setTitle("Begin life as " + displayName(name, draft.clan) + "?")
                .setMessage("Beginning a new life replaces the current save on this device.")
                .setNegativeButton("Keep current life", null)
                .setPositiveButton("Begin life", (dialog, which) -> {
                    getSystemService(InputMethodManager.class).hideSoftInputFromWindow(body.getWindowToken(), 0);
                    life = Life.begin(name, draft.clan, draft.village, draft.presence, draft.style);
                    SaveGame.write(this, life);
                    draft = null;
                    go("life");
                }).show();
    }

    private String displayName(String name, int clan) {
        return Clans.ALL[clan].role() == Role.OPEN ? name : name + " " + Clans.ALL[clan].name();
    }

    private void heading(String title, String subtitle) {
        TextView view = text(title, 32, INK, DISPLAY);
        if (Build.VERSION.SDK_INT >= 28) view.setAccessibilityHeading(true);
        body.addView(view);
        body.addView(text(subtitle, 15, MUTED, BODY));
        gap(body, 13);
    }

    private void fact(String title, String detail) {
        LinearLayout card = card(WHITE, 13);
        card.addView(text(title, 18, INK, DISPLAY));
        gap(card, 3);
        card.addView(text(detail, 15, MUTED, BODY));
        body.addView(card);
        gap(body, 8);
    }

    private LinearLayout meterPair(Stat left, int leftColor, Stat right, int rightColor) {
        LinearLayout pair = row();
        pair.addView(meter(left, leftColor), new LinearLayout.LayoutParams(0, WRAP, 1));
        pair.addView(new View(this), new LinearLayout.LayoutParams(dp(14), 1));
        pair.addView(meter(right, rightColor), new LinearLayout.LayoutParams(0, WRAP, 1));
        return pair;
    }

    private LinearLayout meter(Stat stat, int color) {
        LinearLayout meter = column();
        LinearLayout label = row();
        label.addView(text(stat.label, 13, INK, MEDIUM), new LinearLayout.LayoutParams(0, WRAP, 1));
        label.addView(text(Integer.toString(life.get(stat)), 13, color, MEDIUM));
        meter.addView(label);
        gap(meter, 4);
        meter.addView(progress(life.get(stat), color));
        return meter;
    }

    private View progress(int value, int color) {
        View bar = new View(this) {
            private final android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            @Override protected void onDraw(android.graphics.Canvas canvas) {
                float r = getHeight() / 2f;
                paint.setColor(BORDER);
                canvas.drawRoundRect(0, 0, getWidth(), getHeight(), r, r, paint);
                paint.setColor(color);
                if (value > 0) canvas.drawRoundRect(0, 0, getWidth() * value / 100f, getHeight(), r, r, paint);
            }
        };
        bar.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        bar.setLayoutParams(new LinearLayout.LayoutParams(MATCH, dp(5)));
        return bar;
    }

    private void quick(LinearLayout host, String label, String mark, Runnable run) {
        LinearLayout tile = column();
        tile.setGravity(Gravity.CENTER);
        tile.setMinimumHeight(dp(58));
        tile.addView(new Marks(this, mark, RED), new LinearLayout.LayoutParams(dp(19), dp(19)));
        TextView name = text(label, 12, INK, MEDIUM);
        name.setGravity(Gravity.CENTER);
        tile.addView(name);
        clickable(tile, WHITE, BORDER, run);
        tile.setContentDescription(label);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, WRAP, 1);
        if (host.getChildCount() > 0) params.setMarginStart(dp(7));
        host.addView(tile, params);
    }

    private View footerButton(String label, Runnable action) {
        LinearLayout shell = column();
        inset(shell, 16, 4, 16, 6);
        shell.setBackgroundColor(PAPER);
        Button button = button(label, Color.WHITE, RED, null, action);
        button.setTextSize(17);
        shell.addView(button, new LinearLayout.LayoutParams(MATCH, WRAP));
        return shell;
    }

    private Button button(String label, int ink, int fill, Integer stroke, Runnable action) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setStateListAnimator(null);
        button.setTypeface(MEDIUM);
        button.setTextSize(14);
        button.setTextColor(ink);
        button.setText(label);
        button.setMinimumHeight(dp(48));
        button.setMinimumWidth(dp(48));
        inset(button, 11, 6, 11, 6);
        clickable(button, fill, stroke, action);
        return button;
    }

    private void buttonIn(LinearLayout target, String label, int ink, int fill, Runnable action) {
        target.addView(button(label, ink, fill, null, action), new LinearLayout.LayoutParams(MATCH, WRAP));
    }

    private TextView pill(String label, int ink, int fill) {
        TextView pill = text(label, 12, ink, MEDIUM);
        inset(pill, 9, 5, 9, 5);
        pill.setBackground(box(fill, null, 12));
        return pill;
    }

    private TextView text(String label, int size, int color, Typeface face) {
        TextView view = new TextView(this);
        view.setText(label);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(face);
        return view;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout row() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        return layout;
    }

    private LinearLayout card(int color, int padding) {
        LinearLayout layout = column();
        layout.setBackground(box(color, color == WHITE ? BORDER : null, 17));
        inset(layout, padding, padding, padding, padding);
        return layout;
    }

    private GradientDrawable box(int fill, Integer stroke, int radius) {
        GradientDrawable box = new GradientDrawable();
        box.setColor(fill);
        box.setCornerRadius(dp(radius));
        if (stroke != null) box.setStroke(dp(1), stroke);
        return box;
    }

    private void clickable(View view, int fill, Integer stroke, Runnable action) {
        ColorStateList ripple = new ColorStateList(new int[][]{{android.R.attr.state_focused}, {}},
                new int[]{0x44484463, 0x32774249});
        view.setBackground(new RippleDrawable(ripple, box(fill, stroke, 14), box(Color.WHITE, null, 14)));
        view.setFocusable(true);
        view.setOnClickListener(v -> action.run());
    }

    private void gap(LinearLayout target, int height) {
        target.addView(new View(this), new LinearLayout.LayoutParams(MATCH, dp(height)));
    }

    private void line(LinearLayout target) {
        View rule = new View(this);
        rule.setBackgroundColor(BORDER);
        target.addView(rule, new LinearLayout.LayoutParams(MATCH, dp(1)));
    }

    private void inset(View view, int left, int top, int right, int bottom) {
        view.setPadding(dp(left), dp(top), dp(right), dp(bottom));
    }

    private String money(int coins) { return String.format(Locale.US, "%,d", coins); }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private void message(String label) { Toast.makeText(this, label, Toast.LENGTH_SHORT).show(); }
}
