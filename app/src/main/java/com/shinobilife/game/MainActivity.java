package com.shinobilife.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Random;

import static com.shinobilife.game.Clans.Role;
import static com.shinobilife.game.Clans.Stat;

public final class MainActivity extends Activity {
    private static final int PAPER = 0xFFF3EEE8, WHITE = 0xFFFFFBF6, INK = 0xFF211D29;
    private static final int MUTED = 0xFF5D5460, RED = 0xFF8F263B, BORDER = 0xFFE0D4C9;
    private static final int NIGHT = 0xFF181C2A, NIGHT_PANEL = 0xFF292C3D, LIGHT_TEXT = 0xFFF4DFCE;
    private static final int BLUSH = 0xFFF3DFDF, LILAC = 0xFFECE4F0, PURPLE = 0xFF593E6D;
    private static final int GREEN = 0xFF245B49, MINT = 0xFFE4EFE5, AMBER = 0xFF87592A;
    private static final int GOLD = 0xFFD8A142, GOLD_BG = 0xFFFCF3DF, GOLD_INK = 0xFF704B10;
    private static final int MATCH = -1, WRAP = -2;
    private static final int JOURNAL_BATCH = 12;
    private static final Typeface BODY = Typeface.create("sans-serif", Typeface.NORMAL);
    private static final Typeface MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    private static final Typeface DISPLAY = Typeface.create("sans-serif", Typeface.BOLD);
    private static final String[] TABS = {"Life", "Missions", "Clans", "Summons", "People"};
    private static final String[] TAB_MARKS = {"life", "missions", "clans", "summons", "people"};
    private static final int[][] CONTACT_STYLE = {
        {4, 1, 2, 1, 4, 0, 1}, {2, 3, 3, 4, 2, 0, 4}, {3, 2, 4, 3, 5, 1, 5}
    };
    private static final int[][] RIVAL_STYLE = {{5, 1, 2, 4, 6, 3, 4}, {3, 2, 3, 3, 2, 0, 0}, {6, 4, 1, 4, 5, 0, 2}};
    private final android.util.SparseArray<android.graphics.drawable.Drawable.ConstantState> artworkCache = new android.util.SparseArray<>();
    private int oldPlayerHp = -1, oldRivalHp = -1;

    private Life life;
    private String page = "title";
    private String returnPage = "title";
    private ScrollView scroll;
    private LinearLayout body;
    private Role filter;
    private int selectedClan, selectedContact;
    private Life.Interest selectedInterest;
    private static final int[][] ROMANCE_STYLE = {
            {8, 3, 1, 1, 4, 0, 0}, {6, 0, 3, 3, 2, 1, 0},
            {3, 4, 4, 2, 3, 0, 0}, {4, 1, 0, 4, 1, 0, 0}
    };
    private int journalLimit = JOURNAL_BATCH, residentLimit = 16;
    private boolean editingOrigin, wardrobeMode, viewingDuelCircuit, residentDirectory;
    private EyePower previewEye;
    private Draft draft;
    private Portrait editorPortrait;
    private final java.util.ArrayList<Portrait> appearancePreviews = new java.util.ArrayList<>();

    private static final class Draft {
        String name = Life.randomName(new Random());
        int village = 3, gender, presence = 1, start;
        int[] style = {0, 0, 0, 2, 0, 0, 4, 0, 0, 0};
    }

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        life = SaveGame.read(this);
        if (life == null) {
            Draft starter = new Draft();
            life = Life.rollNewLife(starter.name, starter.village, starter.gender, starter.presence, starter.style, Life.START_AGES[starter.start], new Random());
            SaveGame.write(this, life);
        }
        page = life.deceased ? "legacy" : life.enemyHp > 0 ? "duel" : "title";
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
            case "clans" -> go(returnPage.equals("title") ? "title" : "life");
            case "career" -> go("village");
            case "academy", "council", "season-event", "stillwater" -> go("life");
            case "person", "romance", "residents" -> go("people");
            case "date" -> go(residentDirectory ? "residents" : "romance");
            case "family" -> go("people");
            case "legacy" -> go("title");
            case "create" -> {
                wardrobeMode = false;
                go(returnPage.equals("title") ? "title" : "life");
            }
            case "life" -> go("title");
            case "title" -> moveTaskToBack(true);
            default -> go("life");
        }
    }

    private void go(String next) {
        if (next.equals("romance") && life.age() < Life.ADULT_AGE) {
            message("Romance opens at age 18.");
            return;
        }
        if (life.age() < Life.SHINOBI_AGE && (next.equals("missions") || next.equals("summons"))) {
            message("Available from age 15.");
            return;
        }
        if (!life.graduated && (next.equals("missions") || next.equals("summons"))) {
            message("Pass your Genin assessment to unlock missions and summons.");
            go("academy");
            return;
        }
        if (next.equals("residents") && life.age() < 3) {
            message("You can meet village residents at age 3.");
            return;
        }
        if (next.equals("residents")) residentDirectory = true;
        else if (next.equals("romance")) residentDirectory = false;
        page = next;
        if (next.equals("life")) journalLimit = JOURNAL_BATCH;
        if (next.equals("romance") && selectedInterest == null) residentLimit = 16;
        render(0);
    }

    private void openCreator(boolean wardrobe, String fromPage) {
        draft = new Draft();
        wardrobeMode = wardrobe;
        editingOrigin = false;
        returnPage = fromPage;
        if (wardrobe) {
            draft.name = life.name;
            draft.village = life.village;
            draft.presence = life.presence;
            System.arraycopy(life.style, 0, draft.style, 0, Math.min(life.style.length, draft.style.length));
            previewEye = life.portraitEye();
        } else {
            previewEye = null;
        }
        go("create");
    }

    private void update() {
        SaveGame.write(this, life);
        if (!page.equals("duel")) {
            refreshBody();
            return;
        }
        int at = scroll.getScrollY();
        body.removeViews(2, body.getChildCount() - 2);
        duelRound();
        restoreScroll(at);
    }

    private void refreshBody() {
        int at = scroll.getScrollY();
        body.removeAllViews();
        populateBody();
        restoreScroll(at);
    }

    private void restoreScroll(int at) {
        ScrollView current = scroll;
        if (at > 0) current.post(() -> current.scrollTo(0, at));
    }

    private void render(int at) {
        appearancePreviews.clear();
        editorPortrait = null;
        FrameLayout window = new FrameLayout(this);
        window.setBackgroundColor(NIGHT);
        LinearLayout root = column();
        root.setBackgroundColor(PAPER);
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
                if (bars != null) bars.setSystemBarsAppearance(0, light);
                return insets;
            });
            window.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (right - left != oldRight - oldLeft) view.requestApplyInsets();
            });
        } else {
            getWindow().setStatusBarColor(NIGHT);
            getWindow().setNavigationBarColor(NIGHT);
        }
        if (!page.equals("title")) root.addView(header(), new LinearLayout.LayoutParams(MATCH, WRAP));
        if (page.equals("create")) root.addView(editorPreview());
        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        root.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1));
        body = column();
        inset(body, 16, page.equals("title") ? 16 : 14, 16, 20);
        scroll.addView(body, new ScrollView.LayoutParams(MATCH, WRAP));
        populateBody();
        if (page.equals("life")) root.addView(advanceBar());
        if (page.equals("create")) {
            root.addView(footerButton(wardrobeMode ? "Save appearance" : "Begin life",
                    wardrobeMode ? this::saveWardrobe : this::confirmLife));
        }
        if (isTab()) root.addView(dock());
        setContentView(window);
        window.requestApplyInsets();
        restoreScroll(at);
    }

    private void populateBody() {
        switch (page) {
            case "title" -> titlePage();
            case "life" -> lifePage();
            case "milestones" -> milestonePage();
            case "village" -> villagePage();
            case "career" -> careerPage();
            case "academy" -> academyPage();
            case "season-event" -> seasonEventPage();
            case "stillwater" -> stillwaterPage();
            case "council" -> councilPage();
            case "missions" -> missionsPage();
            case "clans" -> clansPage();
            case "detail" -> clanDetail();
            case "summons" -> summonsPage();
            case "people" -> peoplePage();
            case "person" -> personPage();
            case "romance", "residents" -> romancePage();
            case "date" -> datePage();
            case "family", "legacy" -> familyPage();
            case "create" -> createPage();
            case "duel" -> duelPage();
        }
    }

    private boolean isTab() {
        for (String tab : TAB_MARKS) if (page.equals(tab)) return true;
        return false;
    }

    private View header() {
        LinearLayout bar = row();
        inset(bar, 12, 4, 12, 4);
        bar.setBackgroundColor(NIGHT);
        if (!isTab()) {
            Button back = button("←", Color.WHITE, NIGHT, null, this::handleBack);
            back.setTextSize(21);
            back.setContentDescription("Go back");
            bar.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        } else {
            LinearLayout homeBrand = row();
            inset(homeBrand, 4, 6, 4, 6);
            homeBrand.setMinimumHeight(dp(48));
            ImageView flag = new ImageView(this);
            flag.setImageResource(R.drawable.crest);
            flag.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            homeBrand.addView(flag, new LinearLayout.LayoutParams(dp(30), dp(30)));
            TextView title = text("Shinobi Life", 18, Color.WHITE, Typeface.create("sans-serif", Typeface.BOLD_ITALIC));
            title.setSingleLine(true);
            LinearLayout.LayoutParams titleBox = new LinearLayout.LayoutParams(WRAP, WRAP);
            titleBox.setMarginStart(dp(7));
            homeBrand.addView(title, titleBox);
            clickable(homeBrand, NIGHT, null, () -> go("title"));
            homeBrand.setContentDescription("Return to title screen");
            bar.addView(homeBrand, new LinearLayout.LayoutParams(0, WRAP, 1));
            Button titleBtn = button("Title", LIGHT_TEXT, NIGHT_PANEL, null, () -> go("title"));
            titleBtn.setTextSize(12);
            titleBtn.setSingleLine(true);
            inset(titleBtn, 8, 4, 8, 4);
            LinearLayout.LayoutParams titleBtnParams = new LinearLayout.LayoutParams(WRAP, WRAP);
            titleBtnParams.setMarginEnd(dp(5));
            bar.addView(titleBtn, titleBtnParams);
            Button newBtn = button("New life", Color.WHITE, RED, null, () -> openCreator(false, page));
            newBtn.setTextSize(12);
            newBtn.setSingleLine(true);
            inset(newBtn, 9, 4, 9, 4);
            bar.addView(newBtn);
            return bar;
        }
        String headerLabel = page.equals("create")
                ? (wardrobeMode ? "Stylist Wardrobe" : "Create Shinobi")
                : "Shinobi Life";
        TextView title = text(headerLabel, 21, Color.WHITE, Typeface.create("sans-serif", Typeface.BOLD_ITALIC));
        LinearLayout.LayoutParams titleBox = new LinearLayout.LayoutParams(0, WRAP, 1);
        titleBox.setMarginStart(dp(8));
        bar.addView(title, titleBox);
        return bar;
    }

    private View dock() {
        LinearLayout layout = column();
        layout.setBackgroundColor(NIGHT);
        LinearLayout menu = row();
        inset(menu, 8, 4, 8, 4);
        layout.addView(menu);
        for (int i = 0; i < TABS.length; i++) {
            String next = TAB_MARKS[i];
            boolean active = next.equals(page);
            int color = active ? Color.WHITE : LIGHT_TEXT;
            LinearLayout tab = column();
            tab.setGravity(Gravity.CENTER);
            tab.setMinimumHeight(dp(56));
            Marks icon = new Marks(this, next, color);
            tab.addView(icon, new LinearLayout.LayoutParams(dp(20), dp(20)));
            TextView label = text(TABS[i], 12, color, MEDIUM);
            label.setGravity(Gravity.CENTER);
            tab.addView(label);
            clickable(tab, active ? RED : NIGHT, null, () -> {
                if (next.equals("clans")) {
                    filter = null;
                    returnPage = "life";
                }
                go(next);
            });
            tab.setContentDescription(TABS[i] + (active ? ", selected" : ""));
            tab.setSelected(active);
            menu.addView(tab, new LinearLayout.LayoutParams(0, WRAP, 1));
        }
        return layout;
    }

    private void titlePage() {
        if (life.deceased) {
            heading("Shinobi Life", "Generation " + life.generation + " · " + life.name + " passed away");
            buttonIn(body, "View family legacy", Color.WHITE, RED, () -> go("legacy"));
            gap(body, 8);
            buttonIn(body, "New Shinobi Life", RED, BLUSH, () -> openCreator(false, "title"));
            return;
        }
        body.addView(titleHeroBanner());
        gap(body, 14);

        // Active shinobi continue card
        LinearLayout saveCard = card(WHITE, 14);
        LinearLayout saveHead = row();
        saveHead.addView(text("ACTIVE CHRONICLE", 11, RED, DISPLAY), new LinearLayout.LayoutParams(0, WRAP, 1));
        saveHead.addView(pill(life.date(), GOLD_INK, GOLD_BG));
        saveCard.addView(saveHead);
        gap(saveCard, 10);

        LinearLayout heroRow = row();
        Portrait portrait = new Portrait(this, life.style, life.age(), 0xFFEBCEB5,
                "Portrait of " + life.name, life.portraitEye());
        heroRow.addView(portrait, new LinearLayout.LayoutParams(dp(88), dp(88)));
        LinearLayout details = column();
        inset(details, 12, 0, 0, 0);
        LinearLayout nameLine = row();
        addSeal(nameLine, life.clan, 26);
        TextView charName = text(displayName(life.name, life.clan), 22, INK, DISPLAY);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, WRAP, 1);
        nameParams.setMarginStart(dp(8));
        nameLine.addView(charName, nameParams);
        details.addView(nameLine);
        gap(details, 3);
        details.addView(text(life.village() + " · " + life.rank(), 13, PURPLE, MEDIUM));
        gap(details, 6);
        LinearLayout statStrip = row();
        statStrip.addView(miniStat("Coins", money(life.get(Stat.COINS)), AMBER), new LinearLayout.LayoutParams(0, WRAP, 1));
        statStrip.addView(miniStat("Skill", Integer.toString(life.get(Stat.SKILL)), GREEN), new LinearLayout.LayoutParams(0, WRAP, 1));
        statStrip.addView(miniStat("Fame", Integer.toString(life.get(Stat.FAME)), RED), new LinearLayout.LayoutParams(0, WRAP, 1));
        statStrip.addView(miniStat("Wins", Integer.toString(life.victories), PURPLE), new LinearLayout.LayoutParams(0, WRAP, 1));
        details.addView(statStrip);
        heroRow.addView(details, new LinearLayout.LayoutParams(0, WRAP, 1));
        saveCard.addView(heroRow);
        gap(saveCard, 12);

        String ctaLabel = life.enemyHp > 0
                ? "Resume " + life.duelRival.arena + " Duel  ›"
                : "Continue as " + displayName(life.name, life.clan) + "  ›";
        buttonIn(saveCard, ctaLabel, Color.WHITE, RED, () -> go(life.enemyHp > 0 ? "duel" : "life"));
        body.addView(saveCard);
        gap(body, 11);

        // Title menu actions
        titleActionCard("New Shinobi Life",
                "Customize appearance, village origin & 23 clan paths",
                "spark", RED, BLUSH, () -> openCreator(false, "title"));
        gap(body, 8);
        titleActionCard("Wardrobe & Appearance",
                "Change hairstyle, outfit, scars, headband or mask anytime",
                "wardrobe", PURPLE, LILAC, () -> openCreator(true, "title"));
        gap(body, 8);
        titleActionCard("Clan & Bloodline Codex",
                "Inspect all 22 clans, combat jutsu, summons & eye powers",
                "clans", GREEN, MINT, () -> {
                    filter = null;
                    returnPage = "title";
                    go("clans");
                });
        gap(body, 12);

        // Career milestones overview
        LinearLayout milestones = card(WHITE, 13);
        milestones.addView(text("CHRONICLE MILESTONES", 11, MUTED, DISPLAY));
        gap(milestones, 8);
        LinearLayout row1 = row();
        row1.addView(milestoneChip("Summon Pacts", life.signedSummonCount() + " / " + Summon.ALL.length, GREEN),
                new LinearLayout.LayoutParams(0, WRAP, 1));
        row1.addView(new View(this), new LinearLayout.LayoutParams(dp(8), 1));
        row1.addView(milestoneChip("Trusted Allies", life.trustedAllyCount() + " / " + Life.CONTACTS.length, AMBER),
                new LinearLayout.LayoutParams(0, WRAP, 1));
        milestones.addView(row1);
        gap(milestones, 7);
        LinearLayout row2 = row();
        String bloodlineStatus = Life.bloodlineNames(life.heritageEyes());
        row2.addView(milestoneChip("Bloodline Eye", bloodlineStatus, PURPLE),
                new LinearLayout.LayoutParams(0, WRAP, 1));
        row2.addView(new View(this), new LinearLayout.LayoutParams(dp(8), 1));
        row2.addView(milestoneChip("Journal Entries", Integer.toString(life.journal.size()), RED),
                new LinearLayout.LayoutParams(0, WRAP, 1));
        milestones.addView(row2);
        body.addView(milestones);
    }

    private FrameLayout titleHeroBanner() {
        ImageView scenery = artwork(R.drawable.village_dusk);
        TitleSceneArt overlay = new TitleSceneArt(this);
        LinearLayout content = column();
        inset(content, 18, 22, 18, 20);
        content.setMinimumHeight(dp(186));
        content.setGravity(Gravity.BOTTOM);

        LinearLayout topBadgeRow = row();
        ImageView crest = new ImageView(this);
        crest.setImageResource(R.drawable.crest);
        crest.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        topBadgeRow.addView(crest, new LinearLayout.LayoutParams(dp(34), dp(34)));
        TextView badge = pill("OFFLINE SHINOBI RPG", GOLD, 0xCC222436);
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(WRAP, WRAP);
        badgeParams.setMarginStart(dp(8));
        topBadgeRow.addView(badge, badgeParams);
        content.addView(topBadgeRow);
        gap(content, 24);

        TextView title = text("SHINOBI LIFE", 31, Color.WHITE, Typeface.create("sans-serif", Typeface.BOLD_ITALIC));
        if (Build.VERSION.SDK_INT >= 28) title.setAccessibilityHeading(true);
        content.addView(title);
        gap(content, 3);
        content.addView(text("Chronicles of the Three Villages · 23 Paths & Summons", 13, LIGHT_TEXT, MEDIUM));

        FrameLayout frame = new FrameLayout(this) {
            @Override protected void onMeasure(int widthSpec, int heightSpec) {
                measureChild(content, widthSpec, heightSpec);
                int width = resolveSize(content.getMeasuredWidth(), widthSpec);
                int height = resolveSize(content.getMeasuredHeight(), heightSpec);
                setMeasuredDimension(width, height);
                int exactW = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                int exactH = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                scenery.measure(exactW, exactH);
                overlay.measure(exactW, exactH);
            }
        };
        frame.setBackground(box(NIGHT, null, 20));
        frame.setClipToOutline(true);
        frame.addView(scenery, new FrameLayout.LayoutParams(MATCH, MATCH));
        frame.addView(overlay, new FrameLayout.LayoutParams(MATCH, MATCH));
        frame.addView(content, new FrameLayout.LayoutParams(MATCH, WRAP));
        return frame;
    }

    private void titleActionCard(String title, String subtitle, String mark, int accent, int badgeFill, Runnable action) {
        LinearLayout card = row();
        inset(card, 12, 11, 12, 11);
        FrameLayout iconWrap = new FrameLayout(this);
        iconWrap.setBackground(box(badgeFill, null, 12));
        iconWrap.addView(new Marks(this, mark, accent), new FrameLayout.LayoutParams(dp(22), dp(22), Gravity.CENTER));
        card.addView(iconWrap, new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout info = column();
        inset(info, 12, 0, 8, 0);
        info.addView(text(title, 17, INK, DISPLAY));
        info.addView(text(subtitle, 12, MUTED, BODY));
        card.addView(info, new LinearLayout.LayoutParams(0, WRAP, 1));
        card.addView(text("›", 24, accent, BODY));
        clickable(card, WHITE, BORDER, action);
        card.setContentDescription(title + ". " + subtitle);
        body.addView(card);
    }

    private LinearLayout miniStat(String label, String value, int color) {
        LinearLayout box = column();
        box.addView(text(value, 14, color, DISPLAY));
        box.addView(text(label, 11, MUTED, MEDIUM));
        return box;
    }

    private LinearLayout milestoneChip(String label, String value, int accent) {
        LinearLayout chip = column();
        chip.setBackground(box(PAPER, BORDER, 12));
        inset(chip, 10, 8, 10, 8);
        chip.addView(text(label, 11, MUTED, MEDIUM));
        chip.addView(text(value, 14, accent, DISPLAY));
        return chip;
    }

    private void lifePage() {
        LinearLayout hero = column();
        inset(hero, 14, 13, 14, 14);
        LinearLayout heading = row();
        heading.addView(text("Your life", 15, LIGHT_TEXT, DISPLAY), new LinearLayout.LayoutParams(0, WRAP, 1));
        heading.addView(pill(life.date(), Color.WHITE, NIGHT_PANEL));
        hero.addView(heading);
        gap(hero, 9);
        LinearLayout card = row();
        Portrait portrait = new Portrait(this, life.style, life.age(), 0xFFEBCEB5, "Portrait of " + life.name,
                life.portraitEye());
        portrait.setBloodlineVisuals(life.hasEvolved(life.portraitEye()), false);
        card.addView(portrait, new LinearLayout.LayoutParams(dp(86), dp(86)));
        LinearLayout info = column();
        inset(info, 12, 0, 0, 0);
        LinearLayout titleRow = row();
        addSeal(titleRow, life.clan, 24);
        TextView nameView = text(displayName(life.name, life.clan), 24, Color.WHITE, DISPLAY);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, WRAP, 1);
        nameParams.setMarginStart(dp(7));
        titleRow.addView(nameView, nameParams);
        info.addView(titleRow);
        info.addView(text(life.village() + " · " + life.rank(), 13, LIGHT_TEXT, MEDIUM));
        info.addView(text(Life.GENDERS[life.gender] + " · " + Life.FAMILIES[life.family], 12, LIGHT_TEXT, MEDIUM));
        if (life.aptitude != Life.Aptitude.NONE) info.addView(text(life.aptitude.title, 12, LIGHT_TEXT, MEDIUM));
        gap(info, 6);
        LinearLayout quickStats = row();
        addHeroBadge(quickStats, "Coins", money(life.get(Stat.COINS)), true);
        addHeroBadge(quickStats, "Skill", Integer.toString(life.get(Stat.SKILL)), true);
        addHeroBadge(quickStats, "Wins", Integer.toString(life.victories + life.challengerWins), true);
        addHeroBadge(quickStats, "Pacts", life.signedSummonCount() + "/" + Summon.ALL.length, false);
        info.addView(quickStats);
        card.addView(info, new LinearLayout.LayoutParams(0, WRAP, 1));
        hero.addView(card);
        body.addView(scene(hero));
        gap(body, 8);

        LinearLayout meters = card(WHITE, 11);
        meters.addView(meterPair(Stat.HEALTH, GREEN, Stat.CHAKRA, PURPLE));
        gap(meters, 7);
        meters.addView(meterPair(Stat.STAMINA, AMBER, Stat.MOOD, RED));
        gap(meters, 7);
        meters.addView(meterPair(Stat.LOOKS, RED, Stat.FAME, PURPLE));
        gap(meters, 6);
        String fameHint = life.get(Stat.FAME) >= 60 ? "Renowned (+5 Mood/season)"
                : life.get(Stat.FAME) >= 25 ? "Recognized (+3 Mood/season)" : "25 Fame unlocks recognition";
        String looksHint = life.get(Stat.LOOKS) >= 75 ? "75+ Looks active (+2 Trust)" : "75 Looks boosts visits";
        meters.addView(text(looksHint + " · " + fameHint, 11, MUTED, MEDIUM));
        body.addView(meters);
        gap(body, 9);

        if (life.ageless()) {
            buttonIn(body, "Ageless · Stillwater Seal", PURPLE, LILAC, () -> go("stillwater"));
            gap(body, 9);
        }
        if (life.hasSealEncounter()) {
            buttonIn(body, life.seal == Life.SealState.UNDISCOVERED ? "The sealed archive" : "Decide the seal’s fate",
                    Color.WHITE, RED, () -> go("stillwater"));
            gap(body, 9);
        } else if (life.hasSeasonEvent()) {
            seasonEventBanner();
            gap(body, 10);
        } else if (!life.journal.isEmpty()) {
            latestEvent(life.journal.get(0));
            gap(body, 9);
        }

        if (life.age() < Life.SHINOBI_AGE) {
            childhoodPanel();
            lifeJournal();
            return;
        }

        LinearLayout actions = row();
        quick(actions, "Train", life.get(Stat.SKILL) >= 100 ? "Max" : "+3 Skill", "training", () -> {
            if (!life.train()) message("Training needs 16 stamina and room to increase Skill.");
            else update();
        });
        quick(actions, "Meditate", life.meditated ? "Done" : "+22 Cha", "meditate", () -> {
            if (!life.meditate()) message("You already meditated this season.");
            else update();
        });
        quick(actions, "Style", life.styled ? "Wardrobe" : "35 / Free", "wardrobe", this::openStyleMenu);
        if (life.age() >= Life.ADULT_AGE) quick(actions, "Work", life.worked ? "Done" : "+" + life.workPay() + " Coin", "coin", () -> {
            if (!life.work()) message(life.worked ? "You already worked this season." : "You need 12 stamina.");
            else update();
        });
        quick(actions, "Rest", life.rested ? "Done" : "+20 HP", "life", () -> {
            if (!life.rest()) message("You already rested this season.");
            else update();
        });
        body.addView(actions);
        gap(body, 8);
        if (life.age() >= Life.SHINOBI_AGE) buttonIn(body, "Activities & travel", RED, BLUSH, () -> go("village"));
        gap(body, 7);
        LinearLayout offices = row();
        offices.addView(button("Academy", RED, WHITE, BORDER, () -> go("academy")),
                new LinearLayout.LayoutParams(0, WRAP, 1));
        LinearLayout.LayoutParams council = new LinearLayout.LayoutParams(0, WRAP, 1);
        council.setMarginStart(dp(7));
        offices.addView(button(life.leafAtWar && life.village == Life.LEAF ? "Council · At war" : "Village council",
                RED, WHITE, BORDER, () -> go("council")), council);
        body.addView(offices);

        gap(body, 9);
        CombatArt currentArt = life.clanArt();
        fact("Clan technique · " + currentArt.name(), currentArt.description() + " "
                + life.clanPractice + " duel uses. Refined at 6 uses and 22 Skill. Mastered at 15 uses and 35 Skill.");
        for (EyePower eye : EyePower.values()) {
            if ((life.heritageEyes() & eye.mask()) == 0) continue;
            gap(body, 10);
            LinearLayout bloodline = card(LILAC, 12);
            bloodline.addView(text(life.hasEvolved(eye) ? eye.evolution : eye.title, 19, INK, DISPLAY));
            if ((life.awakenedEyes & eye.mask()) != 0) {
                bloodline.addView(text("Awakened · " + eye.move + (life.hasEvolved(eye) ? " · +6 damage" : ""), 13, PURPLE, MEDIUM));
                if (life.hasEvolved(eye)) {
                    bloodline.addView(text(eye.manifestation + " · " + eye.formEffect + " Requires 45 Skill and 2 circuit wins beyond Cedar Pass.", 13, INK, BODY));
                } else {
                    bloodline.addView(text(eye.evolution + " requires 35 Skill and 3 uses of this eye (" +
                            life.eyeTrials[eye.ordinal()] + "/3).", 13, MUTED, BODY));
                    if (life.canEvolve(eye)) buttonIn(bloodline, "Evolve to " + eye.evolution, PURPLE, WHITE, () -> {
                        if (life.evolve(eye)) update();
                    });
                }
            } else {
                String progress = "Skill " + life.get(Stat.SKILL) + "/" + eye.skill
                        + (eye.wins > 0 ? " · Cedar Pass wins " + life.victories + "/" + eye.wins : "");
                bloodline.addView(text("Dormant · Awakens at " + eye.requirement() + " (" + progress + ")", 13, PURPLE, MEDIUM));
            }
            if (life.canAwakenEye(eye)) {
                gap(bloodline, 6);
                buttonIn(bloodline, "Awaken " + eye.title, PURPLE, WHITE, () -> {
                    if (life.awakenEye(eye)) update();
                });
            }
            body.addView(bloodline);
        }

        lifeJournal();
    }

    private void childhoodPanel() {
        fact(life.age() < 6 ? "Growing up" : "Academy years", life.age() < 6
                ? "Spend time with family. Academy classes open at age 6."
                : "Gain Skill and academy credits before the mission board opens at 15.");
        for (Life.ChildhoodAction action : Life.ChildhoodAction.values()) {
            if (life.age() < action.age || (life.childhoodActions & action.mask()) != 0) continue;
            buttonIn(body, action.title + " · " + action.result, RED, BLUSH, () -> {
                if (life.childhoodAction(action)) update();
                else message("This activity requires 8 stamina.");
            });
            gap(body, 6);
        }
        if (life.age() >= 6) buttonIn(body, "Academy", RED, WHITE, () -> go("academy"));
    }

    private void lifeJournal() {
        int firstEntry = life.hasSeasonEvent() ? 0 : 1;
        if (life.journal.size() > firstEntry) {
            gap(body, 16);
            body.addView(text("Life journal", 23, INK, DISPLAY));
            buttonIn(body, "View milestones", RED, BLUSH, () -> go("milestones"));
            gap(body, 8);
            appendJournal(firstEntry);
        }
    }

    private void milestonePage() {
        heading("Life milestones", "Birth, graduation, family, and major achievements.");
        int shown = 0;
        for (Life.Event event : life.journal) {
            if (!Life.isMilestone(event.title())) continue;
            journal(event);
            if (++shown >= journalLimit) break;
        }
        if (shown == journalLimit && life.journal.stream().filter(event -> Life.isMilestone(event.title())).count() > shown) {
            buttonIn(body, "Show older milestones", RED, BLUSH, () -> {
                journalLimit += JOURNAL_BATCH;
                refreshBody();
            });
        }
    }

    private void addHeroBadge(LinearLayout host, String label, String value, boolean spacer) {
        LinearLayout badge = column();
        badge.setBackground(box(0x88292C3D, null, 8));
        inset(badge, 5, 4, 5, 4);
        badge.addView(text(value, 13, Color.WHITE, DISPLAY));
        badge.addView(text(label, 10, LIGHT_TEXT, MEDIUM));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, WRAP, 1);
        if (spacer) params.setMarginEnd(dp(4));
        host.addView(badge, params);
    }

    private void openStyleMenu() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle("Stylist & Wardrobe")
                .setMessage("Change your hair, outfit, scars, and gear anytime for free, or pay 35 coins once per season for +8 Looks (75+ Looks improves Trust gains).")
                .setNeutralButton("Open Wardrobe (Free)", (d, w) -> openCreator(true, "life"))
                .setNegativeButton("Close", null);
        if (!life.styled && life.get(Stat.LOOKS) < 100) {
            dialog.setPositiveButton("Style up (35 coins)", (d, w) -> {
                if (!life.styleUp()) message("You need 35 coins.");
                else update();
            });
        }
        dialog.show();
    }

    private void seasonEventBanner() {
        Life.SeasonEvent event = life.seasonalEvent();
        LinearLayout panel = row();
        inset(panel, 12, 12, 12, 12);
        ImageView letter = artwork(R.drawable.academy_letter);
        letter.setBackground(box(WHITE, null, 9));
        letter.setClipToOutline(true);
        panel.addView(letter, new LinearLayout.LayoutParams(dp(64), dp(74)));
        LinearLayout copy = column();
        inset(copy, 11, 0, 6, 0);
        copy.addView(text(event.title, 18, INK, DISPLAY));
        gap(copy, 4);
        copy.addView(text("Make a seasonal choice", 13, RED, MEDIUM));
        panel.addView(copy, new LinearLayout.LayoutParams(0, WRAP, 1));
        panel.addView(text("›", 25, RED, DISPLAY));
        clickable(panel, WHITE, BORDER, () -> go("season-event"));
        body.addView(panel);
    }

    private void stillwaterPage() {
        heading("The Stillwater Seal", life.village() + " · " + life.date());
        if (life.ageless()) {
            fact("Ageless", "Years still pass, but old age cannot end your life. You recover 28 Chakra per season instead of 48. The seal breaks if your Health reaches 0.");
            buttonIn(body, "Release the seal", RED, BLUSH, () -> new AlertDialog.Builder(this)
                    .setTitle("Return to a mortal lifespan?")
                    .setMessage("If you have already outlived your natural lifespan, old age can end your life on your next birthday.")
                    .setNegativeButton("Keep the seal", null)
                    .setPositiveButton("Release", (dialog, choice) -> resolveStillwater(() -> life.releaseSeal(false))).show());
        } else if (life.hasSealEncounter()) {
            if (life.seal == Life.SealState.UNDISCOVERED) {
                fact("The sealed archive", "A seasonal tide has exposed a chamber beneath Starwatch. The archivist asks you to decipher a seal rumored to halt aging.");
                buttonIn(body, "Decipher the seal · " + Life.SEAL_STAMINA + " Stamina", Color.WHITE, RED,
                        () -> resolveStillwater(life::decipherSeal));
            } else {
                fact("One charge remains", "The seal can stop old-age death, or Starwatch can use its remaining charge "
                        + (life.leafAtWar ? "to treat families displaced by the war." : "to study long-term care."));
                fact("Binding cost", Life.SEAL_CHAKRA + " Chakra now and " + Life.SEAL_UPKEEP
                        + " Chakra per season. The seal breaks at 0 Health.");
                buttonIn(body, "Bind the seal · Become ageless", Color.WHITE, RED, () -> resolveStillwater(life::bindSeal));
                buttonIn(body, "Donate the seal to the clinic", PURPLE, LILAC, () -> resolveStillwater(life::donateSeal));
            }
        }
        buttonIn(body, "Return to Life", RED, WHITE, () -> go("life"));
    }

    private void resolveStillwater(java.util.function.BooleanSupplier choice) {
        if (!choice.getAsBoolean()) {
            message("Check your Stamina or Chakra before trying again.");
            return;
        }
        SaveGame.write(this, life);
        go("life");
    }

    private void seasonEventPage() {
        Life.SeasonEvent event = life.seasonalEvent();
        heading(event.title, life.village() + " · " + life.date());
        TextView narrative = text(event.situation, 17, INK, BODY);
        inset(narrative, 4, 4, 4, 6);
        body.addView(narrative);
        gap(body, 10);
        for (int i = 0; i < event.choices.length; i++) {
            int choice = i;
            Life.SeasonChoice option = event.choices[i];
            body.addView(text(option.terms(), 14, MUTED, MEDIUM));
            gap(body, 4);
            buttonIn(body, option.title(), Color.WHITE, RED, () -> {
                if (life.resolveSeasonEvent(choice)) {
                    SaveGame.write(this, life);
                    go("life");
                } else message("Requires " + option.price() + " " + option.priceStat().label + ".");
            });
            gap(body, 13);
        }
    }

    private void latestEvent(Life.Event event) {
        LinearLayout panel = card(WHITE, 12);
        panel.addView(text("Latest life event", 12, RED, DISPLAY));
        gap(panel, 6);
        LinearLayout layout = row();
        ImageView image = artwork(R.drawable.academy_letter);
        image.setBackground(box(PAPER, null, 10));
        image.setClipToOutline(true);
        layout.addView(image, new LinearLayout.LayoutParams(dp(72), dp(76)));
        LinearLayout detail = column();
        inset(detail, 10, 0, 0, 0);
        detail.addView(text(event.title(), 18, INK, DISPLAY));
        detail.addView(text(event.detail(), 13, MUTED, BODY));
        layout.addView(detail, new LinearLayout.LayoutParams(0, WRAP, 1));
        panel.addView(layout);
        body.addView(panel);
    }

    private void appendJournal(int from) {
        int end = Math.min(journalLimit, life.journal.size());
        for (int i = from; i < end; i++) journal(life.journal.get(i));
        if (end < life.journal.size()) {
            buttonIn(body, "Show older entries", RED, BLUSH, () -> {
                body.removeViewAt(body.getChildCount() - 1);
                journalLimit += JOURNAL_BATCH;
                appendJournal(end);
            });
        }
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
        LinearLayout actions = row();
        inset(actions, 16, 7, 16, 7);
        Button season = button("Advance season", Color.WHITE, RED, null, () -> passTime(false));
        actions.addView(season, new LinearLayout.LayoutParams(0, WRAP, 1));
        Button year = button("Age +1", RED, BLUSH, BORDER, () -> passTime(true));
        LinearLayout.LayoutParams yearLayout = new LinearLayout.LayoutParams(WRAP, WRAP);
        yearLayout.setMarginStart(dp(7));
        actions.addView(year, yearLayout);
        return actions;
    }

    private void passTime(boolean year) {
        int age = life.age();
        do {
            life.advance();
        } while (year && !life.deceased && life.enemyHp == 0 && life.age() == age);
        if (life.deceased) {
            SaveGame.write(this, life);
            go("legacy");
            return;
        }
        journalLimit = JOURNAL_BATCH;
        scroll.scrollTo(0, 0);
        update();
    }

    private void villagePage() {
        heading("Activities", life.village() + " · " + life.date() + " · " + life.get(Stat.STAMINA) + " stamina");
        if (life.age() >= Life.ADULT_AGE) buttonIn(body, "Career · " + life.career.title, RED, BLUSH, () -> go("career"));
        gap(body, 7);
        buttonIn(body, "Meet village residents", PURPLE, LILAC, () -> go("residents"));
        gap(body, 7);
        buttonIn(body, "Travel · 20 coins and 12 stamina", Color.WHITE, RED, () -> {
            new AlertDialog.Builder(this).setTitle("Choose a destination").setItems(Life.VILLAGES, (dialog, which) -> {
                if (which == life.village) return;
                if (life.visitVillage(which)) update();
                else message("Travel requires 20 coins and 12 stamina.");
            }).setNegativeButton("Stay here", null).show();
        });
        gap(body, 12);
        for (Life.ActivityGroup group : Life.ActivityGroup.values()) {
            body.addView(text(group.label, 21, INK, DISPLAY));
            gap(body, 7);
            for (Life.VillageAction action : Life.VillageAction.values()) {
                if (action.group == group) activityCard(action);
            }
        }
    }

    private void activityCard(Life.VillageAction action) {
            LinearLayout activity = card(WHITE, 13);
            activity.addView(text(action == Life.VillageAction.MARKET ? Life.VILLAGE_WORK[life.village] : action.title, 19, INK, DISPLAY));
            activity.addView(text(life.activityDescription(action), 14, MUTED, BODY));
            gap(activity, 5);
            if ((life.villageActions & action.mask()) != 0) activity.addView(pill("Done this season", GREEN, MINT));
            else if (life.get(Stat.SKILL) < action.skill) activity.addView(pill("Requires " + action.skill + " Skill", MUTED, PAPER));
            else buttonIn(activity, "Do activity", RED, BLUSH, () -> {
                if (life.villageActivity(action)) update();
                else message("Requires " + action.stamina + " stamina and " + action.cost + " coins.");
            });
            body.addView(activity);
            gap(body, 8);
    }

    private void academyPage() {
        heading(life.village() + " Academy", life.rank() + " · " + life.academyCredits + " class credits");
        boolean student = !life.graduated;
        Life.AcademyRank next = life.nextRank();
        if (student) {
            fact("Genin assessment", "Age 15 · 2 class credits · 12 Skill. You have "
                    + life.academyCredits + " credits and " + life.get(Stat.SKILL) + " Skill.");
            if (life.canGraduate()) {
                for (Life.AcademyTrack track : Life.AcademyTrack.values()) {
                    buttonIn(body, "Graduate in " + track.title + " · +" + track.bonus + " " + track.stat.label, Color.WHITE, RED, () -> {
                        if (life.graduate(track)) update();
                    });
                    gap(body, 8);
                }
            }
        } else if (next != null) {
            fact(next.title + " exam", life.examRequirements() + ". You have " + life.academyCredits + " credits, "
                    + life.get(Stat.SKILL) + " Skill, and " + life.victories + " duel wins.");
            if (life.canTakeExam()) {
                buttonIn(body, "Take the " + next.title + " exam", Color.WHITE, RED, () -> {
                    if (life.takeExam()) update();
                });
                gap(body, 10);
            }
        } else fact("Advanced classes", "You reached Jōnin. Keep studying or build support to run for Hokage in Hidden Leaf.");
        if (life.attendedClass) {
            body.addView(pill("Class attended this season", GREEN, MINT));
            return;
        }
        String classes = student ? "Academy classes" : next == null ? "Jōnin classes" : next.title + " preparation";
        body.addView(text(classes, 20, INK, DISPLAY));
        gap(body, 7);
        for (Life.Lesson lesson : Life.Lesson.values()) {
            LinearLayout card = card(WHITE, 13);
            card.addView(text(lesson.effect + " · +1 credit · " + Life.LESSON_STAMINA + " stamina", 13, MUTED, BODY));
            buttonIn(card, lesson.title, PURPLE, LILAC, () -> {
                if (life.attendClass(lesson)) update();
                else message("Class requires " + Life.LESSON_STAMINA + " stamina.");
            });
            body.addView(card);
            gap(body, 8);
        }
    }

    private void councilPage() {
        heading(life.village() + " Council", life.villageLeader());
        if (life.village != Life.LEAF) {
            fact("Border news", life.leafAtWar ? "Hidden Leaf and Hidden Stone are at war."
                    : "Hidden Leaf and Hidden Stone are at peace.");
            fact("Running for Hokage", "Travel to Hidden Leaf to meet the council and register your candidacy.");
            return;
        }
        fact(life.leafAtWar ? "At war with Hidden Stone" : "At peace with Hidden Stone",
                "Border tension " + life.borderTension + "/100 · Village stability " + life.leafStability
                        + "/100 · Treasury " + life.leafTreasury + " coins");
        if (life.leafAtWar) {
            fact("War effort", life.warEffort + "/60. Fund relief, protect shelters, or help civilians. At 60, Hidden Leaf holds the border at the end of the season. At zero stability, the council accepts defeat.");
        }
        if (life.hokage) {
            fact("Hokage approval", life.approval + "/100. Choose one council decision per season.");
            for (Life.Policy policy : Life.Policy.values()) {
                LinearLayout action = card(WHITE, 12);
                action.addView(text(policy.description, 14, MUTED, BODY));
                String blocker = life.policyBlocker(policy);
                if (blocker.isEmpty()) {
                    buttonIn(action, policy.title, Color.WHITE, RED, () -> {
                        if (policy == Life.Policy.WAR) {
                            new AlertDialog.Builder(this).setTitle("Declare war?")
                                    .setMessage("War lowers village stability each season. A negotiated truce can end it.")
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton("Declare war", (dialog, which) -> {
                                        if (life.govern(policy)) update();
                                    }).show();
                        } else if (life.govern(policy)) update();
                    });
                } else {
                    action.addView(text(policy.title, 17, INK, DISPLAY));
                    action.addView(pill(blocker, MUTED, PAPER));
                }
                body.addView(action);
                gap(body, 7);
            }
        } else {
            fact("Hokage election", "Requires Jōnin rank, 55 Fame, and 60 public support. Current support: "
                    + life.electionSupport() + "/100.");
            if (!life.campaigned && life.campaignSupport < 30) {
                buttonIn(body, "Meet residents · 25 coins", PURPLE, LILAC, () -> {
                    if (life.campaign()) update();
                    else message("The meeting costs 25 coins.");
                });
                gap(body, 8);
            }
            if (life.canRunForHokage()) {
                buttonIn(body, "Run for Hokage", Color.WHITE, RED, () -> {
                    if (life.runForHokage()) update();
                });
                gap(body, 9);
            }
        }
        if (life.leafAtWar) {
            fact("Wartime service", life.warService ? "You already helped this season."
                    : "One assignment per season. Helping civilians boosts Fame and village stability.");
            if (!life.warService) {
                buttonIn(body, "Deliver supplies · 12 stamina · +5 Fame", GREEN, MINT, () -> {
                    if (life.helpDuringWar(true)) update();
                    else message("Delivering supplies requires 12 stamina.");
                });
                gap(body, 6);
                buttonIn(body, "Escort evacuees · 16 stamina · −10 Health · +8 Fame", GREEN, MINT, () -> {
                    if (life.helpDuringWar(false)) update();
                    else message("Escorting evacuees requires 16 stamina and at least 20 Health.");
                });
            }
        }
    }

    private void careerPage() {
        heading("Career", life.career.title + " · " + life.careerShifts + " completed shifts");
        fact("Current pay", life.workPay() + " coins per shift. Every three shifts in the same job raises your pay by 10 coins, up to 30.");
        if (!life.worked) {
            buttonIn(body, "Work a shift · 12 stamina", Color.WHITE, RED, () -> {
                if (life.work()) update();
                else message("Work requires 12 stamina.");
            });
            gap(body, 10);
        }
        for (Life.Career job : Life.Career.values()) {
            LinearLayout card = card(WHITE, 13);
            card.addView(text(job.title, 18, INK, DISPLAY));
            card.addView(text(job.pay + " coins starting pay · " + job.skill + " Skill required", 14, MUTED, BODY));
            gap(card, 5);
            if (job == life.career) card.addView(pill("Current job", GREEN, MINT));
            else if (life.worked) card.addView(pill("Applications reopen next season", MUTED, PAPER));
            else if (life.get(Stat.SKILL) < job.skill) card.addView(pill("Build more Skill to apply", MUTED, PAPER));
            else buttonIn(card, "Apply", RED, BLUSH, () -> {
                if (life.changeCareer(job)) update();
            });
            body.addView(card);
            gap(body, 8);
        }
    }

    private void missionsPage() {
        heading("Mission board", life.village() + " · " + life.date());
        LinearLayout tabs = row();
        boardTab(tabs, "Assignments", false);
        boardTab(tabs, "Duel circuit", true);
        body.addView(tabs);
        gap(body, 11);
        if (viewingDuelCircuit) {
            arenaBoard();
            return;
        }
        if (life.rescueVillage >= 0) {
            LinearLayout search = card(WHITE, 14);
            search.addView(text("Find the missing caravan crew", 20, INK, DISPLAY));
            search.addView(text("Rescue in " + Life.VILLAGES[life.rescueVillage] + " · +65 Coins · +8 Fame", 14, MUTED, BODY));
            String blocker = life.rescueBlocker();
            if (blocker.isEmpty()) buttonIn(search, "Lead the rescue", Color.WHITE, RED, () -> {
                if (life.rescueCaravan()) update();
            });
            else search.addView(pill(blocker, RED, BLUSH));
            body.addView(search);
            gap(body, 9);
        }
        for (Missions mission : Missions.values()) {
            if (mission == Missions.DUEL) continue;
            LinearLayout card = card(WHITE, 14);
            LinearLayout topRow = row();
            topRow.addView(text(mission.role.label + (life.matches(mission) ? " · Your specialty" : ""), 12, RED, MEDIUM),
                    new LinearLayout.LayoutParams(0, WRAP, 1));
            for (Summon summon : Summon.ALL) {
                if (summon.mission != mission) continue;
                String tag = life.hasSummon(summon) ? "Companion: " + summon.title
                        : life.knowsSummon(summon) ? "Pact ready: " + summon.title
                        : "Unlocks " + summon.title;
                topRow.addView(pill(tag, life.hasSummon(summon) ? GREEN : AMBER, life.hasSummon(summon) ? MINT : GOLD_BG));
            }
            card.addView(topRow);
            gap(card, 3);
            card.addView(text(mission.title, 21, INK, DISPLAY));
            card.addView(text(mission == Missions.COURIER ? life.courierLead().detail : mission.description, 14, MUTED, BODY));
            gap(card, 7);
            card.addView(text(mission.stamina + " stamina · " + mission.coins + " coins · +" + mission.fame + " Fame", 13, GREEN, MEDIUM));
            gap(card, 7);
            if (life.isComplete(mission)) card.addView(pill("Closed for this season", MUTED, PAPER));
            else if (!life.canStart(mission)) {
                String blocker = mission == Missions.CARAVAN && life.rescueVillage >= 0 ? "Route closed until the crew is rescued"
                        : life.get(Stat.STAMINA) < mission.stamina ? "Low stamina" : "Needs " + Life.FIELD_HEALTH + " Health";
                card.addView(pill(blocker, RED, BLUSH));
            } else {
                String accept = mission == Missions.CARAVAN ? "Choose escort route"
                        : mission == Missions.COURIER ? "Search with a village guide" : "Take assignment";
                buttonIn(card, accept, RED, BLUSH, () -> {
                    if (mission == Missions.CARAVAN) escortRoutes();
                    else take(mission, false);
                });
                if (mission == Missions.COURIER) {
                    Life.CourierLead lead = life.courierLead();
                    card.addView(text("Risky lead · " + lead.requiredSkill + " Skill for the bonus. A failed search pays nothing.",
                            13, MUTED, BODY));
                    buttonIn(card, lead.choice, RED, WHITE, () -> {
                        if (life.followCourierLead()) update();
                    });
                }
                if (mission != Missions.DUEL && life.matches(mission)) {
                    Clans.Clan clan = life.clan();
                    gap(card, 5);
                    card.addView(text("+" + clan.amount() + " " + clan.bonus().label + " with " + clan.technique(), 13, PURPLE, MEDIUM));
                    buttonIn(card, "Use " + clan.technique() + " · " + clan.costLabel(),
                            PURPLE, LILAC, () -> take(mission, true));
                }
                for (Summon summon : Summon.ALL) {
                    if (summon.mission != mission || !life.hasSummon(summon)) continue;
                    gap(card, 5);
                    card.addView(text("+" + summon.bonusAmount + " " + summon.bonus.label, 13, GREEN, MEDIUM));
                    if (life.canSummonOnMission(summon, mission)) {
                        buttonIn(card, "Call " + summon.title + " · " + Summon.MISSION_CHAKRA + " chakra",
                                GREEN, MINT, () -> take(mission, false, summon));
                    } else card.addView(pill(summon.title + " needs " + Summon.MISSION_CHAKRA + " chakra", MUTED, PAPER));
                }
            }
            body.addView(card);
            gap(body, 9);
        }
    }

    private void boardTab(LinearLayout tabs, String name, boolean circuit) {
        boolean selected = viewingDuelCircuit == circuit;
        Button tab = button(name, selected ? Color.WHITE : INK, selected ? RED : WHITE, selected ? null : BORDER, () -> {
            if (viewingDuelCircuit == circuit) return;
            viewingDuelCircuit = circuit;
            scroll.scrollTo(0, 0);
            refreshBody();
        });
        tab.setSelected(selected);
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(0, WRAP, 1);
        if (circuit) size.setMarginStart(dp(7));
        tabs.addView(tab, size);
    }

    private void arenaBoard() {
        LinearLayout card = card(WHITE, 14);
        card.addView(text("24 stamina · One match per season", 13, MUTED, MEDIUM));
        gap(card, 8);
        for (Life.Rival rival : Life.Rival.values()) {
            card.addView(text(rival.arena + " · " + rival.name, 16, INK, DISPLAY));
            card.addView(text(rival.description + " " + rival.hp + " HP · +" + rival.fame + " Fame · "
                    + rival.coins + " coins.", 13, MUTED, BODY));
            gap(card, 4);
            if (life.isComplete(Missions.DUEL)) card.addView(pill("Match used this season", MUTED, PAPER));
            else if (life.academyRank.ordinal() < rival.rank.ordinal()) card.addView(pill("Requires " + rival.rank.title, MUTED, PAPER));
            else if (!life.canStart(Missions.DUEL)) card.addView(pill("Needs 24 stamina and 30 Health", RED, BLUSH));
            else buttonIn(card, "Challenge " + rival.name, RED, BLUSH, () -> {
                if (!life.enterDuel(rival)) return;
                SaveGame.write(this, life);
                go("duel");
            });
            gap(card, 8);
        }
        body.addView(card);
        gap(body, 14);
    }

    private void escortRoutes() {
        boolean canScout = life.hasSummon(Summon.SHRIKE);
        String[] routes = canScout ? new String[]{"Main road", "Shortcut", "Send Lantern Shrike to scout · 8 chakra"}
                : new String[]{"Main road", "Shortcut"};
        LinearLayout panel = column();
        inset(panel, 20, 6, 20, 5);
        panel.addView(text("The merchant suggests a shortcut through Cedar Pass. At 22 Skill, the shortcut earns +65 coins and +5 Fame. Below 22 Skill, an ambush costs 20 Health and 45 coins from the reward, and the missing crew will need a rescue.",
                15, INK, BODY));
        if (canScout) panel.addView(text("Scouting avoids the ambush and earns +30 coins and +3 Fame.", 14, GREEN, MEDIUM));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("A dangerous shortcut")
                .setView(panel).setNegativeButton("Decide later", null).create();
        for (int i = 0; i < routes.length; i++) {
            int route = i;
            gap(panel, 6);
            buttonIn(panel, routes[i], RED, BLUSH, () -> {
                if (route == 0) {
                    dialog.dismiss();
                    take(Missions.CARAVAN, false);
                } else if (route == 1 ? life.escortShortcut() : life.escortWithScout()) {
                    dialog.dismiss();
                    update();
                } else message("This route is unavailable. Scouting requires 8 chakra.");
            });
        }
        dialog.show();
    }

    private void take(Missions mission, boolean technique) {
        take(mission, technique, null);
    }

    private void take(Missions mission, boolean technique, Summon summon) {
        if (!life.take(mission, technique, summon)) {
            message("This assignment or technique is unavailable.");
            return;
        }
        SaveGame.write(this, life);
        if (mission == Missions.DUEL) go("duel");
        else {
            message("Assignment finished. Recorded in your journal.");
            refreshBody();
        }
    }

    private void duelPage() {
        heading(life.duelRival.arena, "Public duel · " + life.duelRival.name);
        duelRound();
    }

    private void duelRound() {
        Life.Rival rival = life.duelRival;
        LinearLayout arena = row();
        arena.addView(fighter(life.name, life.style, life.age(), LILAC, life.get(Stat.HEALTH), 100,
                life.get(Stat.CHAKRA), life.portraitEye(), oldPlayerHp), new LinearLayout.LayoutParams(0, WRAP, 1));
        TextView vs = text("VS", 16, RED, DISPLAY);
        inset(vs, 8, 0, 8, 0);
        arena.addView(vs);
        arena.addView(fighter(rival.name, RIVAL_STYLE[rival.ordinal()], 27, BLUSH, life.enemyHp, rival.hp, -1, null, oldRivalHp),
                new LinearLayout.LayoutParams(0, WRAP, 1));
        body.addView(arena);
        oldPlayerHp = oldRivalHp = -1;
        gap(body, 10);

        LinearLayout log = card(MINT, 14);
        LinearLayout logHead = row();
        logHead.addView(text(life.enemyHp > 0 ? "Round " + life.round + " · " + life.get(Stat.CHAKRA) + " chakra" : "Match ended",
                14, GREEN, DISPLAY), new LinearLayout.LayoutParams(0, WRAP, 1));
        if (life.enemyHp > 0) {
            boolean heavy = life.incomingDamage() == rival.heavy;
            logHead.addView(pill(rival.attackName(life.incomingDamage()) + " · " + life.incomingDamage() + " dmg",
                    heavy ? RED : MUTED, heavy ? BLUSH : WHITE));
        }
        log.addView(logHead);
        gap(log, 4);
        log.addView(text(life.duelLog, 15, INK, BODY));
        if (life.enemyHp > 0 && life.burnTurns > 0) {
            gap(log, 4);
            log.addView(text("Burn active: 5 fire damage next turn", 13, RED, MEDIUM));
        }
        log.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        body.addView(log);
        gap(body, 12);

        if (life.enemyHp <= 0) {
            buttonIn(body, "Return to mission board", Color.WHITE, RED, () -> go("missions"));
            return;
        }
        duelButton("Strike · " + life.strikeDamage() + " damage", Life.Move.STRIKE, Color.WHITE, RED);
        duelButton("Guard · Recover 8 chakra (reduces counter to 3)", Life.Move.GUARD, GREEN, MINT);
        CombatArt art = life.clanArt();
        duelArt(art.name(), art.description(), life.artDuelBlocker(art), Life.Move.TECHNIQUE);
        for (EyePower eye : EyePower.values()) {
            if ((life.awakenedEyes & eye.mask()) == 0) continue;
            String blocker = life.eyeDuelBlocker(eye);
            if (blocker.isEmpty()) {
                body.addView(text(life.eyeDamage(eye) + " damage · " + eye.chakra + " chakra · Stops the counter", 13, PURPLE, MEDIUM));
                buttonIn(body, eye.move, PURPLE, LILAC, () -> {
                    performDuelMove(() -> life.castEye(eye));
                });
            } else body.addView(pill(eye.move + " · " + blocker, MUTED, PAPER));
            gap(body, 7);
            if (life.hasEvolved(eye)) {
                String formBlocker = life.manifestationBlocker(eye);
                if (formBlocker.isEmpty()) {
                    body.addView(text(eye.formDamage + " damage · " + eye.formChakra + " chakra · " + eye.formEffect, 13, PURPLE, MEDIUM));
                    buttonIn(body, eye.manifestation, Color.WHITE, RED, () ->
                            performDuelMove(() -> life.castManifestation(eye)));
                } else body.addView(pill(eye.manifestation + " · " + formBlocker, MUTED, PAPER));
                gap(body, 7);
            }
        }
        java.util.ArrayList<Summon> available = new java.util.ArrayList<>();
        boolean hasPact = false;
        for (Summon summon : Summon.ALL) {
            if (!life.hasSummon(summon)) continue;
            hasPact = true;
            if (!life.usedInDuel(summon)) available.add(summon);
        }
        if (hasPact) {
            String remaining = life.duelSummonCount() + "/" + Summon.MAX_DUEL_CALLS + " calls used";
            if (life.duelSummonCount() >= Summon.MAX_DUEL_CALLS || available.isEmpty()) {
                body.addView(pill("Summons · " + remaining + " · No further calls", MUTED, PAPER));
            } else if (life.get(Stat.CHAKRA) < Summon.DUEL_CHAKRA) {
                body.addView(pill("Summons need " + Summon.DUEL_CHAKRA + " chakra", MUTED, PAPER));
            } else {
                buttonIn(body, "Call a summon · " + remaining, GREEN, MINT, () -> chooseDuelSummon(available));
            }
            gap(body, 7);
        }
        buttonIn(body, "Concede match", MUTED, WHITE, this::handleBack);
    }

    private LinearLayout fighter(String name, int[] style, int age, int color, int hp, int max, int chakra, EyePower eye, int previousHp) {
        LinearLayout card = card(color, 10);
        card.setGravity(Gravity.CENTER);
        Portrait figure = new Portrait(this, style, age, color == LILAC ? 0xFFD9C7E9 : 0xFFE7C3B0, name, eye);
        if (chakra >= 0) figure.setBloodlineVisuals(life.hasEvolved(eye), life.guardianTurns > 0 && life.enemyHp > 0);
        card.addView(figure, new LinearLayout.LayoutParams(dp(78), dp(78)));
        TextView label = text(name, 19, INK, DISPLAY);
        label.setGravity(Gravity.CENTER);
        card.addView(label);
        String change = previousHp >= 0 && previousHp != hp ? " · " + (hp > previousHp ? "+" : "−") + Math.abs(hp - previousHp) : "";
        card.addView(text(hp + "/" + max + " HP" + change, 13, hp * 3 <= max ? RED : GREEN, MEDIUM));
        gap(card, 4);
        MeterView hpBar = new MeterView(hp * 100 / max, hp * 3 <= max ? RED : GREEN);
        card.addView(hpBar);
        if (previousHp >= 0 && previousHp != hp && (Build.VERSION.SDK_INT < 26 || android.animation.ValueAnimator.areAnimatorsEnabled())) {
            hpBar.transition(previousHp * 100 / max, hp * 100 / max);
        }
        if (chakra >= 0) {
            gap(card, 5);
            card.addView(text(chakra + "/100 Chakra", 12, PURPLE, MEDIUM));
            gap(card, 3);
            card.addView(progress(chakra, PURPLE));
        }
        return card;
    }

    private void performDuelMove(java.util.function.BooleanSupplier move) {
        int health = life.get(Stat.HEALTH);
        int rivalHealth = life.enemyHp;
        if (!move.getAsBoolean()) {
            message("This move is unavailable.");
            return;
        }
        oldPlayerHp = health;
        oldRivalHp = rivalHealth;
        if (health != life.get(Stat.HEALTH) || rivalHealth != life.enemyHp) {
            body.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
        }
        update();
    }

    private void duelButton(String label, Life.Move move, int color, int fill) {
        buttonIn(body, label, color, fill, () -> {
            performDuelMove(() -> life.duel(move));
        });
        gap(body, 7);
    }

    private void duelArt(String title, String detail, String blocker, Life.Move move) {
        body.addView(text(detail, 13, PURPLE, MEDIUM));
        if (blocker.isEmpty()) duelButton(title, move, PURPLE, LILAC);
        else {
            body.addView(pill(title + " · " + blocker, MUTED, PAPER));
            gap(body, 7);
        }
    }

    private void chooseDuelSummon(java.util.List<Summon> summons) {
        String[] labels = new String[summons.size()];
        for (int i = 0; i < labels.length; i++) {
            Summon summon = summons.get(i);
            labels[i] = summon.title + " · " + summon.damageAt(life.summonBond(summon)) + " damage\n" + summon.duelEffect;
        }
        new AlertDialog.Builder(this).setTitle("Call a summon · " + Summon.DUEL_CHAKRA + " chakra")
                .setItems(labels, (dialog, which) -> {
                    performDuelMove(() -> life.callInDuel(summons.get(which)));
                }).setNegativeButton("Cancel", null).show();
    }

    private void clansPage() {
        heading("Clan codex",
                "Your clan shapes your starting perk and jutsu. Three lineages carry rare eye powers.");
        {
            LinearLayout current = card(LILAC, 13);
            LinearLayout row = row();
            addSeal(row, life.clan, 46);
            LinearLayout info = column();
            inset(info, 10, 0, 0, 0);
            info.addView(text("YOUR LINEAGE", 11, PURPLE, MEDIUM));
            info.addView(text(life.clan().name(), 22, INK, DISPLAY));
            info.addView(text(life.clan().role().label + " · " + CombatArt.forClan(life.clan()).name(), 13, PURPLE, MEDIUM));
            row.addView(info, new LinearLayout.LayoutParams(0, WRAP, 1));
            current.addView(row);
            clickable(current, LILAC, null, () -> detail(life.clan));
            body.addView(current);
            gap(body, 10);
        }
        LinearLayout filterRow = row();
        filterRow.addView(text((filter == null ? (Clans.ALL.length - 1) + " clans + Clanless" : filter.label), 16, INK, MEDIUM),
                new LinearLayout.LayoutParams(0, WRAP, 1));
        filterRow.addView(button("Filter", RED, BLUSH, null, this::filterClans));
        body.addView(filterRow);
        gap(body, 7);
        for (boolean showBloodlines : new boolean[]{true, false}) {
            for (int i = 0; i < Clans.ALL.length; i++) {
                Clans.Clan clan = Clans.ALL[i];
                if ((clan.eye() != null) == showBloodlines && (filter == null || clan.role() == filter)) clanRow(i);
            }
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
        addSeal(item, index, 46);
        LinearLayout info = column();
        inset(info, 10, 0, 8, 0);
        info.addView(text(clan.name(), 20, INK, DISPLAY));
        String specialty = clan.eye() == null ? CombatArt.forClan(clan).school() : clan.eye().title;
        info.addView(text(specialty + " · " + clan.perkLabel(), 13, MUTED, MEDIUM));
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
        EyePower eye = clan.eye();
        if (eye != null) {
            fact(eye.title, "Awaken with " + eye.requirement() + ". " + eye.description());
        }
        CombatArt art = CombatArt.forClan(clan);
        fact("Combat jutsu · " + art.name(), art.description() + " Use it in the Cedar Pass duel.");
        fact("Starting perk", clan.perkLabel() + " at the start of a new life.");
        if (clan.role() != Role.OPEN && clan.role() != Role.DUELIST) {
            fact("Mission technique · " + clan.technique(), clan.action() + " " + clan.effect() + " " + clan.limit());
        }
        fact("Family tradition", clan.knack());
        fact("Origins", clan.origin());
    }

    private void addSeal(LinearLayout host, int index, int size) {
        Clans.Clan clan = Clans.ALL[index];
        FrameLayout seal = new FrameLayout(this);
        seal.setBackground(box(clan.color(), null, Math.max(8, size / 3)));
        seal.addView(new Marks(this, Integer.toString(index), 0xFFFFE8BE),
                new FrameLayout.LayoutParams(dp(size * 3 / 4), dp(size * 3 / 4), Gravity.CENTER));
        host.addView(seal, new LinearLayout.LayoutParams(dp(size), dp(size)));
    }

    private void summonsPage() {
        int owned = life.signedSummonCount();
        heading("Summon contracts", owned + " of " + Summon.ALL.length
                + " companions. Call one on a matching assignment or up to two different companions in a duel.");
        for (Summon summon : Summon.ALL) {
            LinearLayout card = card(WHITE, 13);
            LinearLayout identity = row();
            FrameLayout badge = new FrameLayout(this);
            badge.setBackground(box(summon.color, null, 15));
            badge.addView(new Marks(this, summon.name(), 0xFFFFE8BE),
                    new FrameLayout.LayoutParams(dp(42), dp(42), Gravity.CENTER));
            identity.addView(badge, new LinearLayout.LayoutParams(dp(56), dp(56)));
            LinearLayout name = column();
            inset(name, 10, 0, 0, 0);
            name.addView(text(summon.title, 20, INK, DISPLAY));
            boolean signed = life.hasSummon(summon);
            name.addView(text(signed ? "Contract signed · " + life.summonBond(summon) + " Bond"
                    : "Pact fee · " + summon.price + " coins", 13, signed ? GREEN : MUTED, MEDIUM));
            identity.addView(name, new LinearLayout.LayoutParams(0, WRAP, 1));
            card.addView(identity);
            gap(card, 8);
            card.addView(text("Mission · +" + summon.bonusAmount + " " + summon.bonus.label + " on the "
                    + summon.mission.shortName + " for " + Summon.MISSION_CHAKRA + " chakra.", 14, MUTED, BODY));
            int damage = summon.damageAt(life.summonBond(summon));
            card.addView(text("Duel · " + damage + " damage for " + Summon.DUEL_CHAKRA + " chakra. "
                    + summon.duelEffect, 14, MUTED, BODY));
            gap(card, 8);
            if (signed) {
                int bond = life.summonBond(summon);
                card.addView(progress(bond, GREEN));
                gap(card, 5);
                card.addView(text(bond >= 90 ? "Maximum duel damage bonus" : "Duel damage increases at 50 and 90 Bond",
                        13, GREEN, MEDIUM));
                if (bond < 100 && !life.bondedThisSeason(summon)) {
                    gap(card, 5);
                    buttonIn(card, "Train together · " + Summon.BOND_STAMINA + " stamina", GREEN, MINT, () -> {
                        if (life.bondSummon(summon)) update();
                        else message("You need " + Summon.BOND_STAMINA + " stamina.");
                    });
                } else card.addView(pill(bond == 100 ? "Maximum bond" : "Trained this season", GREEN, MINT));
            } else if (life.knowsSummon(summon)) {
                buttonIn(card, "Sign contract · " + summon.price + " coins", RED, BLUSH, () -> {
                    if (life.signSummon(summon)) update();
                    else message("You need " + summon.price + " coins.");
                });
            } else {
                card.addView(pill("Unlock by completing " + summon.mission.title, MUTED, PAPER));
            }
            body.addView(card);
            gap(body, 9);
        }
    }

    private void peoplePage() {
        heading("Relationships", "Spend time, offer gifts, and unlock ally favors at 60 Trust.");
        String relationship = life.partner < 0 ? "Meet someone" : life.residents()[life.partner].name
                + (life.married ? " · Spouse" : " · Partner");
        if (life.age() >= Life.ADULT_AGE) buttonIn(body, "Romance · " + relationship, Color.WHITE, RED, () -> go("romance"));
        if (life.age() >= 3) buttonIn(body, "Village residents", RED, WHITE, () -> go("residents"));
        gap(body, 7);
        buttonIn(body, "Family & lineage", PURPLE, LILAC, () -> go("family"));
        gap(body, 9);
        for (int i = 0; i < Life.CONTACTS.length; i++) {
            if (!life.knowsContact(i)) continue;
            int contact = i;
            LinearLayout item = row();
            inset(item, 12, 9, 12, 9);
            item.addView(contactPortrait(i), new LinearLayout.LayoutParams(dp(64), dp(64)));
            LinearLayout info = column();
            inset(info, 10, 0, 8, 0);
            LinearLayout top = row();
            top.addView(text(life.contactName(i), 20, INK, DISPLAY), new LinearLayout.LayoutParams(0, WRAP, 1));
            top.addView(pill(Life.trustTier(life.bonds[i]), GREEN, MINT));
            info.addView(top);
            info.addView(text(Life.ROLES[i] + " · Age " + life.contactAge(i), 13, MUTED, MEDIUM));
            gap(info, 4);
            info.addView(text(life.bonds[i] + " / 100 Trust"
                    + (life.canAllyFavor(i) ? " · Favor ready" : ""), 12, GREEN, MEDIUM));
            gap(info, 3);
            info.addView(progress(life.bonds[i], GREEN));
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

    private Portrait romancePortrait(Life.Interest interest) {
        int[] appearance;
        if (interest.id < ROMANCE_STYLE.length && life.fixedResidents) {
            appearance = ROMANCE_STYLE[interest.id];
        } else {
            Random random = new Random(life.worldSeed ^ (interest.id * 0x9e3779b97f4a7c15L));
            appearance = new int[Portrait.OPTIONS.length];
            for (int i = 0; i < appearance.length; i++) appearance[i] = random.nextInt(Portrait.OPTIONS[i].length);
        }
        return new Portrait(this, appearance, life.residentAge(interest),
                0xFFEDCFC9, "Portrait of " + interest.name);
    }

    private void romancePage() {
        heading("People in " + life.village(), residentDirectory ? "Meet your neighbors and get to know their stories." : "Get to know local residents, meet old friends, or ask someone out.");
        int shown = 0;
        for (Life.Interest interest : life.residents()) {
            if (interest.village >= 0 && interest.village != life.village && !life.isPartner(interest)
                    && life.affection(interest) == 0) continue;
            if (shown++ == residentLimit) {
                buttonIn(body, "Show more residents", RED, BLUSH, () -> {
                    residentLimit += 16;
                    refreshBody();
                });
                break;
            }
            LinearLayout item = row();
            inset(item, 12, 10, 12, 10);
            item.addView(romancePortrait(interest), new LinearLayout.LayoutParams(dp(61), dp(61)));
            LinearLayout copy = column();
            inset(copy, 10, 0, 7, 0);
            copy.addView(text(interest.name, 20, INK, DISPLAY));
            copy.addView(text(interest.gender + " · Age " + life.residentAge(interest) + " · " + Clans.ALL[interest.clan].name(), 13, MUTED, BODY));
            copy.addView(text(life.romanceStatus(interest, residentDirectory) + " · " + life.affection(interest) + "/100 Affection", 13, RED, MEDIUM));
            item.addView(copy, new LinearLayout.LayoutParams(0, WRAP, 1));
            item.addView(text("›", 23, RED, BODY));
            clickable(item, WHITE, BORDER, () -> {
                selectedInterest = interest;
                go("date");
            });
            body.addView(item);
            gap(body, 8);
        }
    }

    private void datePage() {
        Life.Interest interest = selectedInterest;
        LinearLayout intro = row();
        inset(intro, 14, 13, 14, 13);
        intro.addView(romancePortrait(interest), new LinearLayout.LayoutParams(dp(84), dp(84)));
        LinearLayout identity = column();
        inset(identity, 12, 0, 0, 0);
        identity.addView(text(interest.name, 27, Color.WHITE, DISPLAY));
        identity.addView(text(life.romanceStatus(interest, residentDirectory) + " · " + life.residentAge(interest) + " years old",
                14, LIGHT_TEXT, MEDIUM));
        intro.addView(identity, new LinearLayout.LayoutParams(0, WRAP, 1));
        body.addView(scene(intro));
        gap(body, 9);
        LinearLayout bond = card(WHITE, 13);
        bond.addView(text("Affection · " + life.affection(interest) + "/100", 17, RED, DISPLAY));
        gap(bond, 6);
        bond.addView(progress(life.affection(interest), RED));
        body.addView(bond);
        gap(body, 9);
        String background = life.residentAge(interest) < Life.ADULT_AGE ? "Grew up in " + life.village() + "." : interest.about;
        fact("About " + interest.name, background + " " + Clans.ALL[interest.clan].name() + " lineage.");
        if (life.isPartner(interest)) {
            buttonIn(body, "Family & children", PURPLE, LILAC, () -> go("family"));
            gap(body, 8);
        }
        boolean friendshipOnly = residentDirectory || life.isRelative(interest) || life.age() < Life.ADULT_AGE
                || life.residentAge(interest) < Life.ADULT_AGE || (life.partner >= 0 && !life.isPartner(interest));
        if (friendshipOnly) {
            body.addView(pill("Getting acquainted", MUTED, PAPER));
            gap(body, 8);
        }
        if (life.romanceVisited(interest)) {
            body.addView(pill("You already spent time together this season.", GREEN, MINT));
            gap(body, 8);
        } else if (life.affection(interest) == 0) {
            romanceActionCard(interest, Life.RomanceAction.INTRODUCE);
        } else {
            romanceActionCard(interest, Life.RomanceAction.TALK);
            if (friendshipOnly) return;
            romanceActionCard(interest, Life.RomanceAction.WALK);
            romanceActionCard(interest, Life.RomanceAction.DINNER);
            if (!life.isPartner(interest)) romanceActionCard(interest, Life.RomanceAction.COMMIT);
            else if (!life.married) romanceActionCard(interest, Life.RomanceAction.PROPOSE);
        }
        if (life.isPartner(interest)) {
            String leave = life.married ? "Get divorced" : "Break up";
            buttonIn(body, leave, RED, BLUSH, () -> new AlertDialog.Builder(this)
                    .setTitle(leave + "?")
                    .setMessage("This ends your relationship with " + interest.name + " and costs 12 Happiness.")
                    .setNegativeButton("Stay together", null)
                    .setPositiveButton(leave, (dialog, which) -> {
                        if (life.endRomance(interest)) update();
                    }).show());
        }
    }

    private void familyPage() {
        heading(life.deceased ? "Family legacy" : "Family & lineage",
                "Generation " + life.generation + " · " + life.name + " · Age " + life.age());
        if (life.deceased) {
            fact("Life ended", life.name + " passed away at age " + life.age() + ". Choose which child will carry on this save.");
        } else {
            String heritage = life.clan().name()
                    + (life.otherClan < 0 ? "" : " + " + Clans.ALL[life.otherClan].name());
            fact("Your ancestry", heritage + " · " + Life.bloodlineNames(life.heritageEyes()));
            if (life.partner >= 0) {
                Life.Interest otherParent = life.residents()[life.partner];
                fact("Your partner", otherParent.name + " · " + Clans.ALL[otherParent.clan].name()
                        + " · " + Life.bloodlineNames(otherParent.eyes()));
                fact("Inheritance", "Each parent independently passes down each of their eye powers 50% of the time. A child can inherit both different powers, one, or neither. Inherited powers start dormant.");
            }
            String blocker = life.familyBlocker();
            if (blocker.isEmpty()) {
                buttonIn(body, "Welcome a child · " + Life.CHILD_COST + " coins", Color.WHITE, RED, this::nameChild);
                gap(body, 10);
            } else fact("Growing your family", blocker);
        }
        if (!life.kin.isEmpty()) {
            body.addView(text("Parents and relatives", 21, INK, DISPLAY));
            for (Life.Kin relative : life.kin) {
                fact(relative.name(), relative.role() + " · " + (relative.died() < 0 ? "Age " : "Died at age ")
                        + life.kinAge(relative));
            }
        }
        for (Life.Child child : life.children) {
            LinearLayout panel = card(WHITE, 13);
            panel.addView(text(child.name(), 21, INK, DISPLAY));
            panel.addView(text(Life.GENDERS[child.gender()] + " · Age " + life.childAge(child), 14, MUTED, BODY));
            String ancestry = Clans.ALL[child.clan()].name();
            if (child.otherClan() >= 0) ancestry += " + " + Clans.ALL[child.otherClan()].name();
            panel.addView(text(ancestry + " · " + Life.bloodlineNames(child.eyes()), 14, PURPLE, MEDIUM));
            if (life.deceased) {
                gap(panel, 6);
                buttonIn(panel, "Continue as " + child.name(), Color.WHITE, RED, () -> chooseHeir(child));
            }
            body.addView(panel);
            gap(body, 8);
        }
        if (life.deceased && life.children.isEmpty()) {
            buttonIn(body, "Begin a new life", Color.WHITE, RED, () -> openCreator(false, "title"));
        }
    }

    private void nameChild() {
        Random random = new Random();
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
        String suggested = Life.randomName(random);
        input.setText(suggested);
        new AlertDialog.Builder(this).setTitle("Name your child").setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Welcome child", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (life.welcomeChild(name.isEmpty() ? suggested : name, random)) update();
                }).show();
    }

    private void chooseHeir(Life.Child child) {
        String age = child.name() + " will take over at age " + life.childAge(child) + ". ";
        new AlertDialog.Builder(this).setTitle("Continue as " + child.name() + "?")
                .setMessage(age + "Your heir inherits half the estate and keeps the family journal, relatives, and village history.")
                .setNegativeButton("Back", null)
                .setPositiveButton("Continue", (dialog, which) -> {
                    Life successor = life.succeed(child);
                    if (successor != null) {
                        life = successor;
                        SaveGame.write(this, life);
                        go("life");
                    }
                }).show();
    }

    private void romanceActionCard(Life.Interest interest, Life.RomanceAction action) {
        LinearLayout row = card(WHITE, 11);
        String cost = action.stamina > 0 ? action.stamina + " stamina" : "";
        if (action.coins > 0) cost += (cost.isEmpty() ? "" : " · ") + action.coins + " coins";
        int gain = Math.min(100 - life.affection(interest), life.romanceGain(interest, action));
        if (gain > 0) cost += (cost.isEmpty() ? "" : " · ") + "+" + gain + " Affection";
        if (!cost.isEmpty()) row.addView(text(cost, 13, MUTED, MEDIUM));
        String blocker = life.romanceBlocker(interest, action);
        String actionLabel = action == Life.RomanceAction.TALK && interest.village >= 0 && interest.village != life.village
                ? "Exchange letters" : action.label;
        if (blocker.isEmpty()) {
            buttonIn(row, actionLabel, Color.WHITE, RED, () -> {
                if (life.romance(interest, action)) update();
            });
        } else {
            row.addView(text(actionLabel, 16, INK, DISPLAY));
            row.addView(pill(blocker, MUTED, PAPER));
        }
        body.addView(row);
        gap(body, 8);
    }

    private Portrait contactPortrait(int contact) {
        Life.Kin parent = contact == 0 ? life.guardianParent() : null;
        if (parent != null && parent.residentId() >= 0) return romancePortrait(life.residents()[parent.residentId()]);
        int[] hues = {0xFFE9CDBA, 0xFFCBDED2, 0xFFDFCEE9};
        int[] style = CONTACT_STYLE[contact];
        if (life.variedContacts) {
            Random random = new Random(life.worldSeed + 71L * contact);
            style = new int[Portrait.OPTIONS.length];
            for (int i = 0; i < style.length; i++) style[i] = random.nextInt(Portrait.OPTIONS[i].length);
        }
        return new Portrait(this, style, life.contactAge(contact),
                hues[contact], "Portrait of " + life.contactName(contact));
    }

    private void personPage() {
        int contact = selectedContact;
        LinearLayout intro = row();
        inset(intro, 14, 13, 14, 13);
        intro.addView(contactPortrait(contact), new LinearLayout.LayoutParams(dp(86), dp(86)));
        LinearLayout identity = column();
        inset(identity, 12, 0, 0, 0);
        identity.addView(text(life.contactName(contact), 28, Color.WHITE, DISPLAY));
        identity.addView(text(Life.ROLES[contact] + " · Age " + life.contactAge(contact) + " · " + Life.trustTier(life.bonds[contact]),
                13, LIGHT_TEXT, MEDIUM));
        intro.addView(identity, new LinearLayout.LayoutParams(0, WRAP, 1));
        body.addView(scene(intro));
        gap(body, 9);

        LinearLayout trust = card(WHITE, 13);
        trust.addView(text("Trust · " + life.bonds[contact] + "/100", 16, GREEN, DISPLAY));
        gap(trust, 6);
        trust.addView(progress(life.bonds[contact], GREEN));
        body.addView(trust);
        gap(body, 10);

        fact(contact == 0 ? Life.FAMILIES[life.family] : "How you met", life.contactHistory(contact));

        if ((life.interacted & (1 << contact)) != 0) {
            body.addView(pill("You spent time together this season", GREEN, MINT));
        } else {
            int gain = life.get(Stat.LOOKS) >= 75 ? 7 : 5;
            buttonIn(body, "Spend time together · +" + gain + " Trust", Color.WHITE, RED, () -> {
                if (life.interact(contact)) update();
            });
        }
        gap(body, 8);

        if ((life.gifted & (1 << contact)) != 0) {
            body.addView(pill("Keepsake given this season", MUTED, WHITE));
        } else if (life.bonds[contact] >= 100) {
            body.addView(pill("Trust is already at 100", GREEN, MINT));
        } else {
            buttonIn(body, "Give market keepsake · " + Life.GIFT_COST + " coins (+6 Trust)", PURPLE, LILAC, () -> {
                if (!life.gift(contact)) message("You need " + Life.GIFT_COST + " coins.");
                else update();
            });
        }
        gap(body, 10);

        String[] favorTitles = {"Lantern Bento", "Market Tip-Off", "Rooftop Sparring"};
        String[] favorEffects = {
            "Recover +16 Stamina and +12 Health from Nao's home cooking.",
            "Collect +40 Coins and +12 Chakra from Jun's courier lead.",
            "Gain +2 Skill and +5 Fame sparring with Mika."
        };
        LinearLayout favorCard = card(GOLD_BG, 13);
        favorCard.addView(text("Sworn Ally Favor · " + favorTitles[contact], 17, GOLD_INK, DISPLAY));
        gap(favorCard, 3);
        favorCard.addView(text(favorEffects[contact], 13, MUTED, BODY));
        gap(favorCard, 8);
        if (life.bonds[contact] < Life.FAVOR_TRUST) {
            favorCard.addView(pill("Unlocks at " + Life.FAVOR_TRUST + " Trust", AMBER, WHITE));
        } else if ((life.favored & (1 << contact)) != 0) {
            favorCard.addView(pill("Favor claimed this season", GREEN, MINT));
        } else {
            buttonIn(favorCard, "Ask for " + favorTitles[contact], Color.WHITE, AMBER, () -> {
                if (life.allyFavor(contact)) update();
            });
        }
        body.addView(favorCard);
    }

    private View editorPreview() {
        LinearLayout surround = column();
        inset(surround, 16, 8, 16, 0);
        LinearLayout preview = row();
        inset(preview, 12, 10, 12, 10);
        editorPortrait = new Portrait(this, draft.style, wardrobeMode ? life.age() : Life.START_AGES[draft.start], 0xFFEBCEB5,
                "Avatar preview", previewEye);
        editorPortrait.setBloodlineVisuals(wardrobeMode && life.hasEvolved(previewEye), false);
        preview.addView(editorPortrait, new LinearLayout.LayoutParams(dp(106), dp(106)));
        LinearLayout control = column();
        inset(control, 12, 0, 0, 0);
        control.addView(text(wardrobeMode ? "Stylist Wardrobe" : "Create your shinobi", 20, Color.WHITE, DISPLAY));
        String previewName = wardrobeMode ? displayName(life.name, life.clan)
                : draft.name.trim().isEmpty() ? "Ren" : draft.name.trim();
        control.addView(text(previewName + " · " + (wardrobeMode ? life.clan().role().label : Life.GENDERS[draft.gender]),
                12, LIGHT_TEXT, MEDIUM));
        gap(control, 6);
        LinearLayout btnRow = row();
        Button shuffle = button("Randomize", Color.WHITE, NIGHT_PANEL, null, this::randomize);
        shuffle.setTextSize(12);
        shuffle.setSingleLine(true);
        inset(shuffle, 9, 4, 9, 4);
        btnRow.addView(shuffle, new LinearLayout.LayoutParams(WRAP, WRAP));
        control.addView(btnRow);
        preview.addView(control, new LinearLayout.LayoutParams(0, WRAP, 1));
        surround.addView(scene(preview));
        gap(surround, 8);
        if (wardrobeMode) {
            surround.addView(pill("Customize any feature freely · Tap Save appearance below when done", PURPLE, LILAC));
        } else {
            LinearLayout tabs = row();
            editorTab(tabs, "Appearance", false);
            editorTab(tabs, "Origin", true);
            surround.addView(tabs);
        }
        return surround;
    }



    private void editorTab(LinearLayout tabs, String title, boolean origin) {
        boolean selected = editingOrigin == origin;
        Button button = button(title, selected ? Color.WHITE : INK, selected ? RED : WHITE,
                selected ? null : BORDER, () -> {
                    if (editingOrigin == origin) return;
                    getSystemService(InputMethodManager.class).hideSoftInputFromWindow(body.getWindowToken(), 0);
                    editingOrigin = origin;
                    render(0);
                });
        button.setSelected(selected);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, WRAP, 1);
        if (origin) params.setMarginStart(dp(7));
        tabs.addView(button, params);
    }

    private void createPage() {
        if (editingOrigin && !wardrobeMode) {
            originPage();
            return;
        }
        appearancePreviews.clear();
        for (int slot : new int[]{0, 1, 2, 7, 8, 9, 3, 4, 5, 6}) {
            appearanceField(slot);
            gap(body, 8);
        }
    }

    private void appearanceField(int slot) {
        LinearLayout card = card(WHITE, 10);
        LinearLayout title = row();
        String[] options = Portrait.OPTIONS[slot];
        int current = Math.min(draft.style[slot], options.length - 1);
        TextView chosen = text(options[current], 13, RED, MEDIUM);
        title.addView(text(Portrait.LABELS[slot], 16, INK, DISPLAY), new LinearLayout.LayoutParams(0, WRAP, 1));
        title.addView(chosen);
        card.addView(title);
        gap(card, 7);

        boolean swatches = Portrait.isColor(slot);
        HorizontalScrollView scroller = new HorizontalScrollView(this);
        scroller.setHorizontalScrollBarEnabled(false);
        LinearLayout rail = row();
        View[] tiles = new View[options.length];
        for (int i = 0; i < options.length; i++) {
            int variant = i;
            LinearLayout tile = column();
            tile.setGravity(Gravity.CENTER);
            tile.setMinimumHeight(dp(48));
            inset(tile, 3, 4, 3, 4);
            if (swatches) {
                View color = new View(this);
                color.setBackground(box(Portrait.optionColor(slot, variant), BORDER, 8));
                tile.addView(color, new LinearLayout.LayoutParams(dp(29), dp(29)));
            } else {
                Portrait image = new Portrait(this, draft.style, 18, 0xFFE9D8CE, options[i], slot, i);
                image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                appearancePreviews.add(image);
                tile.addView(image, new LinearLayout.LayoutParams(dp(50), dp(50)));
                TextView label = text(options[i], 11, INK, MEDIUM);
                label.setGravity(Gravity.CENTER);
                label.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                tile.addView(label);
            }
            tile.setBackground(selectionTile());
            tile.setContentDescription(options[i]);
            tile.setFocusable(true);
            tile.setSelected(i == current);
            tile.setOnClickListener(view -> {
                int previous = draft.style[slot];
                if (previous == variant) return;
                draft.style[slot] = variant;
                tiles[previous].setSelected(false);
                tile.setSelected(true);
                chosen.setText(options[variant]);
                editorPortrait.invalidate();
                for (Portrait image : appearancePreviews) image.invalidate();
            });
            tiles[i] = tile;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(swatches ? 48 : 78), WRAP);
            if (i > 0) params.setMarginStart(dp(swatches ? 4 : 7));
            rail.addView(tile, params);
        }
        scroller.addView(rail, new FrameLayout.LayoutParams(WRAP, WRAP));
        card.addView(scroller, new LinearLayout.LayoutParams(MATCH, WRAP));
        body.addView(card);
        View initial = tiles[current];
        scroller.post(() -> scroller.scrollTo(Math.max(0, initial.getRight() - scroller.getWidth()), 0));
    }

    private RippleDrawable selectionTile() {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_selected}, box(BLUSH, RED, 9));
        states.addState(new int[]{android.R.attr.state_focused}, box(WHITE, RED, 9));
        states.addState(new int[]{}, box(PAPER, BORDER, 9));
        return new RippleDrawable(ColorStateList.valueOf(0x2C8F263B), states, box(Color.WHITE, null, 9));
    }

    private void originPage() {
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
        choice(identity, "Starting age", Life.START_LABELS, draft.start, i -> draft.start = i);
        choice(identity, "Gender", Life.GENDERS, draft.gender, i -> draft.gender = i);
        choice(identity, "Birth village", Life.VILLAGES, draft.village, i -> draft.village = i);
        choice(identity, "Presence", Life.PRESENCE, draft.presence, i -> draft.presence = i);
        int looks = Life.BASE_LOOKS[draft.presence];
        identity.addView(text("Starting Looks " + Math.min(100, looks) + "/100. Your physical features are yours to choose.", 13, MUTED, BODY));
        body.addView(identity);
        gap(body, 10);
        LinearLayout ancestry = card(WHITE, 13);
        ancestry.addView(text("Random clan & family", 18, INK, DISPLAY));
        ancestry.addView(text("Your village determines which clans are possible. Your clan and household are revealed once you begin.", 14, MUTED, BODY));
        gap(ancestry, 6);
        ancestry.addView(text(draft.village == 3 ? "Rare bloodline · Sparkglass Eye" :
                draft.village == 2 ? "Rare bloodlines · Horizon Eye or Farwatch Eye" :
                draft.village == 4 ? "Rare bloodline · Horizon Eye" :
                draft.village == 7 ? "Rare bloodline · Farwatch Eye" :
                "Local lineages and clanless households", 13, PURPLE, MEDIUM));
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
        if (!wardrobeMode) {
            draft.name = Life.randomName(random);
            draft.village = random.nextInt(Life.VILLAGES.length);
            draft.gender = random.nextInt(Life.GENDERS.length);
            draft.presence = random.nextInt(Life.PRESENCE.length);
        }
        for (int i = 0; i < draft.style.length; i++) draft.style[i] = random.nextInt(Portrait.OPTIONS[i].length);
        render(0);
    }

    private void saveWardrobe() {
        boolean changed = !java.util.Arrays.equals(life.style, draft.style);
        System.arraycopy(draft.style, 0, life.style, 0, Math.min(draft.style.length, life.style.length));
        if (changed) {
            life.note("Wardrobe updated", "You tried on a fresh look in the mirror before heading back out.");
        }
        SaveGame.write(this, life);
        draft = null;
        wardrobeMode = false;
        message("Appearance updated.");
        go("life");
    }

    private void confirmLife() {
        String name = draft.name.trim();
        if (name.isEmpty()) {
            message("Enter a given name.");
            return;
        }
        new AlertDialog.Builder(this).setTitle("Begin life as " + name + "?")
                .setMessage("Village: " + Life.VILLAGES[draft.village]
                        + "\nStarting age: " + Life.START_LABELS[draft.start] + "\nYour clan and family will be rolled when you begin."
                        + "\n\nBeginning a new life replaces the current save on this device.")
                .setNegativeButton("Keep current life", null)
                .setPositiveButton("Begin life", (dialog, which) -> {
                    getSystemService(InputMethodManager.class).hideSoftInputFromWindow(body.getWindowToken(), 0);
                    life = Life.rollNewLife(name, draft.village, draft.gender, draft.presence, draft.style, Life.START_AGES[draft.start], new Random());
                    SaveGame.write(this, life);
                    draft = null;
                    wardrobeMode = false;
                    go("life");
                }).show();
    }

    private String displayName(String name, int clan) {
        return Clans.ALL[clan].role() == Role.OPEN ? name : name + " " + Clans.ALL[clan].name();
    }

    private void heading(String title, String subtitle) {
        LinearLayout content = column();
        inset(content, 15, 16, 15, 16);
        content.setMinimumHeight(dp(94));
        content.setGravity(Gravity.CENTER_VERTICAL);
        TextView view = text(title, 27, Color.WHITE, DISPLAY);
        if (Build.VERSION.SDK_INT >= 28) view.setAccessibilityHeading(true);
        content.addView(view);
        gap(content, 3);
        content.addView(text(subtitle, 14, LIGHT_TEXT, BODY));
        body.addView(scene(content));
        gap(body, 13);
    }

    private FrameLayout scene(LinearLayout content) {
        ImageView scenery = artwork(R.drawable.village_dusk);
        View shade = new View(this);
        FrameLayout frame = new FrameLayout(this) {
            @Override protected void onMeasure(int widthSpec, int heightSpec) {
                // The backdrop fills the banner without determining its height.
                measureChild(content, widthSpec, heightSpec);
                int width = resolveSize(content.getMeasuredWidth(), widthSpec);
                int height = resolveSize(content.getMeasuredHeight(), heightSpec);
                setMeasuredDimension(width, height);
                int exactWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                int exactHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                scenery.measure(exactWidth, exactHeight);
                shade.measure(exactWidth, exactHeight);
            }
        };
        frame.setBackground(box(NIGHT, null, 17));
        frame.setClipToOutline(true);
        frame.addView(scenery, new FrameLayout.LayoutParams(MATCH, MATCH));
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xF31B1D29, 0xCE1A1D2A, 0x6E181C2A}));
        frame.addView(shade, new FrameLayout.LayoutParams(MATCH, MATCH));
        frame.addView(content, new FrameLayout.LayoutParams(MATCH, WRAP));
        return frame;
    }

    private ImageView artwork(int asset) {
        ImageView art = new ImageView(this);
        art.setScaleType(ImageView.ScaleType.CENTER_CROP);
        android.graphics.drawable.Drawable.ConstantState shared = artworkCache.get(asset);
        if (shared == null) {
            art.setImageResource(asset);
            if (art.getDrawable() != null) artworkCache.put(asset, art.getDrawable().getConstantState());
        } else art.setImageDrawable(shared.newDrawable(getResources()));
        art.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return art;
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

    private View progress(int value, int color) { return new MeterView(value, color); }

    private final class MeterView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int color;
        private int value;
        private android.animation.ValueAnimator animator;

        MeterView(int value, int color) {
            super(MainActivity.this);
            this.value = value;
            this.color = color;
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            setLayoutParams(new LinearLayout.LayoutParams(MATCH, dp(5)));
        }

        void transition(int from, int to) {
            value = from;
            animator = android.animation.ValueAnimator.ofInt(from, to);
            animator.setDuration(190);
            animator.setInterpolator(new android.view.animation.DecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                value = (int) animation.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        @Override protected void onDetachedFromWindow() {
            if (animator != null) animator.cancel();
            super.onDetachedFromWindow();
        }

        @Override protected void onDraw(Canvas canvas) {
            float radius = getHeight() / 2f;
            paint.setColor(BORDER);
            canvas.drawRoundRect(0, 0, getWidth(), getHeight(), radius, radius, paint);
            paint.setColor(color);
            if (value > 0) canvas.drawRoundRect(0, 0, getWidth() * Math.min(100, value) / 100f,
                    getHeight(), radius, radius, paint);
        }
    }

    private void quick(LinearLayout host, String label, String sub, String mark, Runnable run) {
        LinearLayout tile = column();
        tile.setGravity(Gravity.CENTER);
        tile.setMinimumHeight(dp(64));
        inset(tile, 2, 6, 2, 6);
        tile.addView(new Marks(this, mark, RED), new LinearLayout.LayoutParams(dp(19), dp(19)));
        TextView name = text(label, 11, INK, DISPLAY);
        name.setGravity(Gravity.CENTER);
        name.setMaxLines(1);
        tile.addView(name);
        TextView hint = text(sub, 10, sub.equals("Done") ? MUTED : GREEN, MEDIUM);
        hint.setGravity(Gravity.CENTER);
        hint.setMaxLines(1);
        tile.addView(hint);
        clickable(tile, WHITE, BORDER, run);
        tile.setContentDescription(label + ", " + sub);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, WRAP, 1);
        if (host.getChildCount() > 0) params.setMarginStart(dp(5));
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

    private void inset(View view, int left, int top, int right, int bottom) {
        view.setPadding(dp(left), dp(top), dp(right), dp(bottom));
    }

    private String money(int coins) { return String.format(Locale.US, "%,d", coins); }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private void message(String label) { Toast.makeText(this, label, Toast.LENGTH_SHORT).show(); }

    @android.annotation.SuppressLint("ViewConstructor")
    private static final class TitleSceneArt extends View {
        private static final int[] SCRIM_COLORS = {0xB8141724, 0x7A181C2A, 0xEC181C2A};
        private static final float[] SCRIM_STOPS = {0f, 0.45f, 1f};
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path shinobi = new Path();
        private Shader scrimShader, haloShader;

        TitleSceneArt(Context context) {
            super(context);
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        }

        @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (w <= 0 || h <= 0) return;
            scrimShader = new LinearGradient(0, 0, 0, h, SCRIM_COLORS, SCRIM_STOPS, Shader.TileMode.CLAMP);
            float mx = w * 0.85f;
            float my = h * 0.23f;
            float mr = Math.min(w, h) * 0.145f;
            haloShader = new RadialGradient(mx, my, mr * 2.4f, 0x55FFF1CC, 0x00FFF1CC, Shader.TileMode.CLAMP);
        }

        @Override protected void onDraw(Canvas c) {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            // Cinematic dusk scrim so title typography pops cleanly
            paint.setShader(scrimShader);
            c.drawRect(0, 0, w, h, paint);
            paint.setShader(null);

            // Luminous moon + halo on upper right sky
            float mx = w * 0.85f;
            float my = h * 0.23f;
            float mr = Math.min(w, h) * 0.145f;
            paint.setShader(haloShader);
            c.drawCircle(mx, my, mr * 2.4f, paint);
            paint.setShader(null);
            paint.setColor(0xEEFFF2D6);
            c.drawCircle(mx, my, mr, paint);

            // Moonlit shinobi silhouette standing on rooftop beam overlooking village
            float sx = w * 0.78f;
            float sy = h * 0.65f;
            float u = Math.min(w, h) * 0.01f;
            paint.setColor(0xFF151722);
            shinobi.rewind();
            // Pagoda eaves / spire tip
            shinobi.moveTo(sx - 22 * u, sy + 19 * u);
            shinobi.quadTo(sx - 5 * u, sy + 12 * u, sx + 22 * u, sy + 18 * u);
            shinobi.lineTo(sx + 22 * u, sy + 25 * u);
            shinobi.lineTo(sx - 22 * u, sy + 25 * u);
            shinobi.close();
            c.drawPath(shinobi, paint);

            // Katana sheath diagonally behind shoulder
            paint.setStrokeWidth(1.5f * u);
            paint.setColor(0xFF282B3D);
            c.drawLine(sx - 7 * u, sy - 6 * u, sx + 7 * u, sy + 9 * u, paint);

            // Cloaked shinobi figure + billowing scarf
            paint.setColor(0xFF151722);
            shinobi.rewind();
            shinobi.moveTo(sx - 4 * u, sy + 15 * u);
            shinobi.lineTo(sx - 5.2f * u, sy + 4 * u);
            shinobi.lineTo(sx - 2.6f * u, sy - 4 * u);
            shinobi.lineTo(sx + 3.0f * u, sy - 4 * u);
            shinobi.lineTo(sx + 5.5f * u, sy + 4 * u);
            shinobi.lineTo(sx + 4.3f * u, sy + 15 * u);
            shinobi.close();
            c.drawPath(shinobi, paint);
            // Head + spiky hair silhouette
            c.drawCircle(sx, sy - 7.3f * u, 3.6f * u, paint);
            shinobi.rewind();
            shinobi.moveTo(sx - 3.5f * u, sy - 9 * u);
            shinobi.lineTo(sx - 6.0f * u, sy - 11.5f * u);
            shinobi.lineTo(sx - 1.5f * u, sy - 10.5f * u);
            shinobi.lineTo(sx + 0.5f * u, sy - 12.8f * u);
            shinobi.lineTo(sx + 2.5f * u, sy - 10.2f * u);
            shinobi.lineTo(sx + 5.0f * u, sy - 10.5f * u);
            shinobi.lineTo(sx + 3.2f * u, sy - 7.5f * u);
            shinobi.close();
            c.drawPath(shinobi, paint);
            shinobi.rewind();
            shinobi.moveTo(sx - 2 * u, sy - 4.3f * u);
            shinobi.quadTo(sx - 14 * u, sy - 7 * u, sx - 24 * u, sy - 1.5f * u);
            shinobi.quadTo(sx - 14 * u, sy - 3 * u, sx - 2 * u, sy - 2 * u);
            shinobi.close();
            paint.setColor(0xFF8C2537);
            c.drawPath(shinobi, paint);

            // Floating lantern embers
            paint.setColor(0xBBFFD170);
            c.drawCircle(w * 0.63f, h * 0.48f, 1.8f * u, paint);
            c.drawCircle(w * 0.91f, h * 0.45f, 1.3f * u, paint);
            c.drawCircle(w * 0.58f, h * 0.28f, 1.1f * u, paint);
            c.drawCircle(w * 0.93f, h * 0.67f, 1.5f * u, paint);
        }
    }
}
