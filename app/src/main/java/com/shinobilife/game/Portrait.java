package com.shinobilife.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

@android.annotation.SuppressLint("ViewConstructor")
final class Portrait extends View {
    static final String[][] OPTIONS = {
        {"Silver Sweep", "Soft Crop", "Topknot", "Side Part", "Long Waves", "Round Curls",
                "Low Ponytail", "Cropped Fade", "Side Braid", "Wild Fringe"},
        {"Ink", "Chestnut", "Silver", "Auburn", "Plum", "Honey", "Crimson", "Frost Blue"},
        {"Amber", "Hazel", "Slate", "Jade", "Violet", "Dark Brown", "Ruby", "Ice Blue"},
        {"Porcelain", "Peach", "Warm Tan", "Copper", "Umber", "Deep Brown"},
        {"Trail Jacket", "Crimson Wrap", "Academy Vest", "Travel Cloak", "Ivory Kimono", "Shadow Haori", "Ward Armor"},
        {"None", "Cheek Mark", "Brow Cut", "Bridge Scar", "Sage Marks"},
        {"None", "Gold Stud", "Red Ribbon", "Round Glasses", "Headband", "Fox Mask", "Face Mask"},
        {"Classic", "Sharp", "Round", "Upturned"},
        {"Angular", "Soft", "Straight", "Broad"},
        {"Soft Smile", "Neutral", "Big Smile", "Frown"}
    };
    static final String[] LABELS = {"Hair style", "Hair color", "Eye color", "Skin tone", "Outfit", "Scar & Marks", "Gear & Acc.", "Eye shape", "Nose", "Mouth"};

    private static final int INK = 0xFF1F1A28;
    private static final int[] HAIR = {
        0xFF283148, 0xFF63423C, 0xFFB8B2B6, 0xFF9C463B,
        0xFF5A396B, 0xFFCA8E44, 0xFFA62C37, 0xFF5683A8
    };
    private static final int[] EYES = {
        0xFFAD5D28, 0xFF70704A, 0xFF4C6A92, 0xFF347C6A,
        0xFF784696, 0xFF4F312E, 0xFFB8303B, 0xFF58A2CE
    };
    private static final int[] SKIN = {
        0xFFF6D9C0, 0xFFEDBC99, 0xFFDC9C70, 0xFFBD7853,
        0xFF945B46, 0xFF6B463A
    };
    private static final int[] CLOTH = {
        0xFF336A65, 0xFF983842, 0xFF4E6280, 0xFF4B3E66,
        0xFFEAD9BF, 0xFF292A38, 0xFF833339
    };

    // Base symmetrical-heroic bust geometry in 120x120 coordinates (center x = 60)
    private static final Path TORSO = vector("M 6 123 C 11 103 27 94 46 91 L 60 97 L 74 91 C 93 94 109 103 114 123 Z");
    private static final Path UNDERSHIRT = vector("M 43 92 L 60 115 L 77 92 L 71 89 L 49 89 Z");
    private static final Path NECK = vector("M 49 78 L 71 78 L 72 95 Q 60 103 48 95 Z");
    private static final Path NECK_SHADE = vector("M 49 79 L 71 79 L 71 90 Q 60 96 49 90 Z");

    private static final Path LEFT_EAR = vector("M 36 52 Q 29 51 29 59 Q 30 68 37 67 Z");
    private static final Path RIGHT_EAR = vector("M 84 52 Q 91 51 91 59 Q 90 68 83 67 Z");
    private static final Path FACE = vector("M 35 45 C 35 25 46 19 60 19 C 74 19 85 25 85 45 L 84 62 Q 83 74 72 82 Q 64 88 60 88 Q 56 88 48 82 Q 37 74 36 62 Z");
    private static final Path FACE_SHADOW = vector("M 35 45 C 35 25 46 19 60 19 L 60 32 C 48 33 41 41 41 53 C 41 68 47 79 60 85 L 60 88 Q 55 88 48 82 Q 37 74 36 62 Z");
    private static final Path FOREHEAD_SHADE = vector("M 35 32 Q 60 44 85 32 L 85 44 Q 60 51 35 44 Z");

    private static final Path LEFT_EYE = vector("M 41 55 Q 47 49 54 53 Q 53 59 47 59 Q 42 59 41 55 Z");
    private static final Path RIGHT_EYE = vector("M 66 53 Q 73 49 79 55 Q 78 59 73 59 Q 67 59 66 53 Z");
    private static final Path LEFT_LASH = vector("M 40 54 Q 47 48 55 53");
    private static final Path RIGHT_LASH = vector("M 65 53 Q 73 48 80 54");
    private static final Path LEFT_BROW = vector("M 40 48 Q 47 44 55 47 L 54 49 Q 47 47 41 50 Z");
    private static final Path RIGHT_BROW = vector("M 65 47 Q 73 44 80 48 L 79 50 Q 73 47 66 49 Z");
    private static final Path NOSE = vector("M 59 57 L 61 63 L 58 64.5");
    private static final Path MOUTH = vector("M 53 74 Q 60 77.5 67 73.5");

    private static final Path[][] EYE_SHAPES = {
        {LEFT_EYE, RIGHT_EYE, LEFT_LASH, RIGHT_LASH},
        {vector("M 40 54 L 54 54 Q 53 58 47 58 Q 43 58 40 54 Z"),
         vector("M 66 54 L 80 54 Q 77 58 73 58 Q 67 58 66 54 Z"),
         vector("M 40 54 Q 47 51 55 54"), vector("M 65 54 Q 73 51 80 54")},
        {vector("M 41 55 Q 42 49 48 49 Q 53 49 54 55 Q 54 61 48 61 Q 42 61 41 55 Z"),
         vector("M 66 55 Q 67 49 73 49 Q 78 49 79 55 Q 78 61 73 61 Q 67 61 66 55 Z"),
         vector("M 40 55 Q 42 48 48 49 Q 54 49 55 55"), vector("M 65 55 Q 67 49 73 49 Q 79 48 80 55")},
        {vector("M 40 52 Q 49 50 54 56 Q 50 60 46 58 Z"), vector("M 66 56 Q 71 50 80 52 L 74 58 Q 69 60 66 56 Z"),
         vector("M 39 52 Q 48 48 55 56"), vector("M 65 56 Q 72 48 81 52")}
    };
    private static final Path[] NOSES = {NOSE,
            vector("M 59 58 Q 56 63 58 65 Q 61 67 64 64"),
            vector("M 60 55 L 60 64 L 63 65"),
            vector("M 59 58 Q 55 62 56 65 Q 60 67 65 64")};
    private static final Path[] MOUTHS = {MOUTH,
            vector("M 53 75 Q 60 73 67 75"),
            vector("M 52 73 Q 60 76 68 72 Q 63 81 57 79 Q 53 78 52 73 Z"),
            vector("M 53 77 Q 60 71 67 77")};
    private boolean evolvedEye, guardian;

    void setBloodlineVisuals(boolean evolved, boolean guard) {
        evolvedEye = evolved;
        guardian = guard;
        invalidate();
    }

    // Outfit detail paths
    private static final Path JACKET_COLLAR = vector("M 42 93 L 47 84 L 60 99 L 73 84 L 78 93 L 71 107 L 60 116 L 49 107 Z");
    private static final Path JACKET_VEST_L = vector("M 21 102 L 42 93 L 45 123 L 16 123 Z");
    private static final Path JACKET_VEST_R = vector("M 99 102 L 78 93 L 75 123 L 104 123 Z");

    private static final Path WRAP_LAPEL_L = vector("M 42 92 L 64 119 L 55 123 L 36 96 Z");
    private static final Path WRAP_LAPEL_R = vector("M 78 92 L 56 119 L 65 123 L 84 96 Z");

    private static final Path VEST_COLLAR = vector("M 38 94 Q 60 83 82 94 L 76 104 Q 60 98 44 104 Z");
    private static final Path VEST_BODY = vector("M 27 98 L 44 93 L 60 101 L 76 93 L 93 98 L 89 123 L 31 123 Z");

    private static final Path CLOAK_MANTLE = vector("M 10 118 Q 25 94 46 90 L 60 102 L 74 90 Q 95 94 110 118 L 104 123 L 16 123 Z");
    private static final Path CLOAK_COWL = vector("M 40 91 Q 60 85 80 91 L 73 105 Q 60 111 47 105 Z");

    private static final Path HAORI_L = vector("M 10 123 C 14 104 28 95 45 92 L 52 123 Z");
    private static final Path HAORI_R = vector("M 110 123 C 106 104 92 95 75 92 L 68 123 Z");

    private static final Path ARMOR_L = vector("M 14 115 Q 25 95 45 93 L 48 113 Q 32 117 16 122 Z");
    private static final Path ARMOR_R = vector("M 106 115 Q 95 95 75 93 L 72 113 Q 88 117 104 122 Z");
    private static final Path ARMOR_PLATE = vector("M 35 108 Q 60 100 85 108 L 82 123 L 38 123 Z");

    // 10 distinct anime hairstyles
    private static final Path[] HAIRSTYLES = {
        // 0: Silver Sweep (spiky swept locks + dynamic parted bangs)
        vector("M 34 61 C 28 53 26 42 29 34 L 22 36 Q 23 24 35 19 L 31 14 Q 44 11 55 15 Q 68 10 81 16 L 86 12 Q 92 21 90 30 L 97 32 Q 94 44 86 58 Q 86 43 79 34 L 74 43 L 70 33 Q 61 45 53 45 L 55 34 Q 45 45 37 47 L 39 39 Q 34 49 34 61 Z"),
        // 1: Soft Crop (neat textured bangs)
        vector("M 35 58 Q 27 50 28 37 Q 29 21 43 16 Q 60 11 77 16 Q 91 21 92 37 Q 93 50 85 58 L 83 41 L 76 35 Q 70 43 64 41 L 60 35 Q 54 43 46 42 L 42 36 Q 36 44 35 58 Z"),
        // 2: Topknot (clean swept hairline with temple wisps)
        vector("M 35 58 Q 28 49 29 36 Q 32 20 46 16 Q 60 13 74 16 Q 88 20 91 36 Q 92 49 85 58 L 82 38 Q 74 28 60 28 Q 46 28 38 38 Z"),
        // 3: Side Part (smooth asymmetric sweep)
        vector("M 34 60 Q 27 50 28 36 Q 30 20 45 15 Q 62 11 79 17 Q 92 24 91 39 Q 91 51 85 59 L 82 37 Q 68 27 54 31 Q 49 39 38 45 L 41 37 Q 34 45 34 60 Z"),
        // 4: Long Waves (parted bangs + long cheek-framing side locks)
        vector("M 31 92 Q 25 65 27 38 Q 29 19 46 14 Q 60 11 74 14 Q 91 19 93 38 Q 95 65 89 92 L 82 88 Q 86 58 80 35 Q 70 30 60 26 Q 50 30 40 35 Q 34 58 38 88 Z"),
        // 5: Round Curls (fluffy scalloped curly crown)
        vector("M 34 57 Q 25 53 26 44 Q 20 37 26 29 Q 25 20 35 17 Q 41 10 51 14 Q 60 8 69 14 Q 79 10 85 17 Q 95 20 94 29 Q 100 37 94 44 Q 95 53 86 57 Q 84 42 76 38 Q 68 43 60 36 Q 52 43 44 38 Q 36 42 34 57 Z"),
        // 6: Low Ponytail (soft parted crown + gentle brow sweep)
        vector("M 34 58 Q 27 49 29 35 Q 32 18 48 14 Q 64 12 78 17 Q 91 23 91 38 Q 91 49 85 58 L 81 39 Q 72 30 62 29 Q 53 39 42 42 L 45 35 Q 35 43 34 58 Z"),
        // 7: Cropped Fade (short crisp buzzed hairline)
        vector("M 34 52 Q 29 44 30 34 Q 31 19 45 15 Q 60 12 75 15 Q 89 19 90 34 Q 91 44 86 52 L 83 36 Q 71 29 60 30 Q 49 29 37 36 Z"),
        // 8: Side Braid (sleek top + soft bangs)
        vector("M 34 60 Q 27 50 29 35 Q 32 18 47 15 Q 62 12 76 16 Q 90 21 91 37 Q 92 50 85 59 L 82 39 Q 73 29 61 28 Q 52 38 40 43 L 43 35 Q 35 44 34 60 Z"),
        // 9: Wild Fringe (dramatic spiky anime silhouette)
        vector("M 34 60 L 24 52 L 29 43 L 18 39 L 28 29 L 21 23 L 36 20 L 35 10 L 49 16 L 59 8 L 68 17 L 82 10 L 82 21 L 97 22 L 90 31 L 101 38 L 91 45 L 95 54 L 85 59 L 82 40 L 73 48 L 72 35 L 62 49 L 60 34 L 49 49 L 47 36 L 38 46 Z")
    };

    private static final Path HAIR_CROWN_SHADE = vector("M 15 38 Q 60 46 105 32 L 108 105 L 12 105 Z");
    private static final Path HAIR_HALO = vector("M 33 28 Q 60 15 87 28 Q 74 24 60 24 Q 46 24 33 28 Z");
    private static final Path HAIR_FLASH = vector("M 53 15 Q 60 13 65 17 C 60 27 52 38 42 46 Q 48 34 51 26 L 46 28 Z");
    private static final Path LONG_BACK = vector("M 26 30 Q 17 65 15 112 Q 30 120 42 108 L 78 108 Q 90 120 105 112 Q 103 65 94 30 Z");
    private static final Path PONYTAIL_BACK = vector("M 80 54 Q 99 54 103 74 Q 105 92 98 112 Q 86 106 83 92 Q 79 75 82 62 Z");
    private static final Path BRAID_DROP = vector("M 31 58 Q 23 64 25 74 L 22 83 L 26 91 L 23 100 L 28 112 L 35 101 L 33 91 L 37 82 L 35 72 L 37 61 Z");
    private static final Path WILD_BACK = vector("M 26 42 L 15 62 L 28 69 L 19 82 L 36 86 L 84 86 L 101 82 L 92 69 L 105 62 L 94 42 Z");
    private static final Path FOX_MASK = vector("M 71 19 L 76 11 L 81 18 L 89 18 L 94 11 L 98 20 Q 98 32 85 37 Q 71 32 71 19 Z");
    private static final Path FACE_MASK = vector("M 36 59 Q 60 55 84 59 L 83 72 Q 76 86 60 90 Q 44 86 37 72 Z");
    private static final Shader SPARKGLASS_AURA = new RadialGradient(60, 62, 54, 0x66D93B2C, 0x00D93B2C, Shader.TileMode.CLAMP);
    private static final Shader HORIZON_AURA = new RadialGradient(60, 60, 56, 0x667B4BC2, 0x007B4BC2, Shader.TileMode.CLAMP);

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path frame = new Path();
    private final int[] style;
    private final int age;
    private final int backdropColor;
    private final Shader backdrop;
    private EyePower eyePower;
    private final int previewSlot, previewValue;

    Portrait(Context context, int[] style, int age, int backdrop, String label) {
        this(context, style, age, backdrop, label, null);
    }

    Portrait(Context context, int[] style, int age, int backdrop, String label, EyePower eyePower) {
        this(context, style, age, backdrop, label, eyePower, -1, 0);
    }

    Portrait(Context context, int[] style, int age, int backdrop, String label, int previewSlot, int previewValue) {
        this(context, style, age, backdrop, label, null, previewSlot, previewValue);
    }

    private Portrait(Context context, int[] style, int age, int backdropColor, String label,
                     EyePower eyePower, int previewSlot, int previewValue) {
        super(context);
        this.style = style;
        this.age = age;
        this.backdropColor = backdropColor;
        this.eyePower = eyePower;
        this.previewSlot = previewSlot;
        this.previewValue = previewValue;
        this.backdrop = new LinearGradient(104, 0, 18, 120,
                tone(backdropColor, 0xFFFFF0D4, .22f),
                tone(backdropColor, 0xFF333952, .56f),
                Shader.TileMode.CLAMP);
        frame.addRoundRect(1, 1, 119, 119, 16, 16, Path.Direction.CW);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        setContentDescription(eyePower == null ? label : label + ", " + eyePower.title + " awakened");
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    void setEyePower(EyePower eyePower) {
        this.eyePower = eyePower;
        invalidate();
    }

    static boolean isColor(int slot) { return slot >= 1 && slot <= 3; }

    static int optionColor(int slot, int variant) {
        return switch (slot) {
            case 1 -> HAIR[clamp(variant, HAIR.length)];
            case 2 -> EYES[clamp(variant, EYES.length)];
            case 3 -> SKIN[clamp(variant, SKIN.length)];
            default -> throw new IllegalArgumentException("No color palette for slot " + slot);
        };
    }

    private static int clamp(int index, int len) { return Math.max(0, Math.min(len - 1, index)); }

    private int part(int slot) {
        int raw = slot == previewSlot ? previewValue : slot < style.length ? style[slot] : 0;
        return clamp(raw, OPTIONS[slot].length);
    }

    @Override protected void onDraw(Canvas c) {
        float scale = Math.min(getWidth(), getHeight()) / 120f;
        c.save();
        c.translate((getWidth() - 120 * scale) / 2f, (getHeight() - 120 * scale) / 2f);
        c.scale(scale, scale);
        c.clipPath(frame);

        drawBackdrop(c);
        if (guardian) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(0x80F38B6D);
            c.drawRoundRect(6, 6, 114, 114, 21, 21, paint);
            paint.setColor(0xA8FFC078);
            c.drawArc(12, 12, 108, 111, 198, 144, false, paint);
            paint.setStyle(Paint.Style.FILL);
        }
        c.save();
        if (previewSlot >= 7) c.scale(1.65f, 1.65f, 60, 61);
        else if (age < 15) {
            float childScale = age < 6 ? .78f : .90f;
            c.scale(childScale, childScale, 60, 109);
        }

        int hair = HAIR[age >= 65 ? 2 : part(1)];
        int skin = SKIN[part(3)];
        int outfit = part(4);
        int hairStyle = part(0);

        drawBackHair(c, hair, hairStyle);
        drawBodyAndOutfit(c, skin, outfit);
        drawHeadAndFace(c, hair, skin);
        drawScarsAndMarks(c, skin);
        drawFrontHair(c, hair, hairStyle);
        drawAccessories(c, skin);
        c.restore();

        // Subtle inner frame edge highlight
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(0x38FFFFFF);
        c.drawRoundRect(2, 2, 118, 118, 15, 15, paint);
        paint.setStyle(Paint.Style.FILL);
        c.restore();
    }

    private void drawBackdrop(Canvas c) {
        paint.setShader(backdrop);
        c.drawRect(0, 0, 120, 120, paint);
        paint.setShader(null);

        // Moon glow
        paint.setColor(0x4CFFF3D8);
        c.drawCircle(88, 32, 26, paint);
        paint.setColor(0x28FFF7E6);
        c.drawCircle(88, 32, 35, paint);

        // Diagonal light beam
        paint.setColor(0x1EFFF2DB);
        c.drawLine(8, 108, 42, 74, paint);
        segment(c, 104, 15, 112, 15, 0x99FFE9C8, 1.1f);
        segment(c, 108, 11, 108, 19, 0x99FFE9C8, 1.1f);
        segment(c, 15, 22, 21, 22, 0x66FFE9C8, 0.9f);
        segment(c, 18, 19, 18, 25, 0x66FFE9C8, 0.9f);

        // Bloodline aura behind character
        if (eyePower == EyePower.SPARKGLASS) {
            paint.setShader(SPARKGLASS_AURA);
            c.drawCircle(60, 62, 54, paint);
            paint.setShader(null);
            paint.setColor(0x88FFCC70);
            c.drawCircle(24, 72, 1.4f, paint);
            c.drawCircle(95, 58, 1.6f, paint);
            c.drawCircle(30, 38, 1.2f, paint);
        } else if (eyePower == EyePower.HORIZON) {
            paint.setShader(HORIZON_AURA);
            c.drawCircle(60, 60, 56, paint);
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.2f);
            paint.setColor(0x66DFB8FF);
            c.drawCircle(60, 56, 36, paint);
            paint.setColor(0x44C49BFF);
            c.drawCircle(60, 56, 46, paint);
            paint.setStyle(Paint.Style.FILL);
        } else {
            paint.setColor(0x16FFFFFF);
            c.drawCircle(60, 62, 42, paint);
        }
    }

    private void drawBackHair(Canvas c, int hair, int hairStyle) {
        int backTone = tone(hair, INK, .30f);
        if (hairStyle == 4) layer(c, LONG_BACK, backTone, 1.6f);
        if (hairStyle == 6) {
            layer(c, PONYTAIL_BACK, backTone, 1.6f);
            paint.setColor(0xFFB43B46);
            c.drawCircle(83, 60, 3.2f, paint);
            strokeCircle(c, 83, 60, 3.2f, 1.2f);
        }
        if (hairStyle == 9) layer(c, WILD_BACK, backTone, 1.5f);
        if (hairStyle == 2) {
            paint.setColor(hair);
            c.drawOval(49, 4, 71, 23, paint);
            strokeOval(c, 49, 4, 71, 23, 1.6f);
            paint.setColor(0xFFB43B46);
            c.drawRoundRect(52, 17, 68, 21.5f, 1.5f, 1.5f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.1f);
            paint.setColor(INK);
            c.drawRoundRect(52, 17, 68, 21.5f, 1.5f, 1.5f, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawBodyAndOutfit(Canvas c, int skin, int outfit) {
        int baseCloth = CLOTH[outfit];
        int darkCloth = tone(baseCloth, INK, .48f);
        int lightCloth = tone(baseCloth, 0xFFFFF4E3, .26f);

        // Torso & neck
        layer(c, TORSO, baseCloth, 1.7f);
        layer(c, UNDERSHIRT, 0xFF282B3A, 1.2f);
        // Mesh collar detail
        segment(c, 52, 95, 58, 108, 0x44FFFFFF, 0.7f);
        segment(c, 68, 95, 62, 108, 0x44FFFFFF, 0.7f);

        layer(c, NECK, skin, 1.45f);
        layer(c, NECK_SHADE, tone(skin, 0xFF8C4C54, .38f), 0);

        switch (outfit) {
            case 0 -> { // Trail Jacket
                layer(c, JACKET_VEST_L, 0xFFD9C5A3, 1.3f);
                layer(c, JACKET_VEST_R, 0xFFD9C5A3, 1.3f);
                layer(c, JACKET_COLLAR, darkCloth, 1.4f);
                segment(c, 60, 114, 60, 122, INK, 1.3f);
                segment(c, 26, 106, 40, 100, lightCloth, 1.1f);
                segment(c, 94, 106, 80, 100, lightCloth, 1.1f);
            }
            case 1 -> { // Crimson Wrap
                layer(c, WRAP_LAPEL_L, 0xFF332938, 1.3f);
                layer(c, WRAP_LAPEL_R, 0xFF332938, 1.3f);
                segment(c, 41, 96, 61, 121, 0xFFDAB05E, 1.15f);
                segment(c, 79, 96, 59, 121, 0xFFDAB05E, 1.15f);
                segment(c, 20, 110, 40, 103, darkCloth, 1.2f);
                segment(c, 100, 110, 80, 103, darkCloth, 1.2f);
            }
            case 2 -> { // Academy Vest
                layer(c, VEST_BODY, baseCloth, 1.5f);
                layer(c, VEST_COLLAR, lightCloth, 1.4f);
                segment(c, 60, 101, 60, 122, INK, 1.3f);
                // Dual chest pockets
                paint.setColor(darkCloth);
                c.drawRoundRect(30, 107, 45, 118, 2, 2, paint);
                c.drawRoundRect(75, 107, 90, 118, 2, 2, paint);
                paint.setColor(lightCloth);
                c.drawRoundRect(30, 107, 45, 111, 1.5f, 1.5f, paint);
                c.drawRoundRect(75, 107, 90, 111, 1.5f, 1.5f, paint);
            }
            case 3 -> { // Travel Cloak
                layer(c, CLOAK_MANTLE, darkCloth, 1.5f);
                layer(c, CLOAK_COWL, baseCloth, 1.4f);
                paint.setColor(0xFFD6A858);
                c.drawCircle(60, 106, 3.2f, paint);
                strokeCircle(c, 60, 106, 3.2f, 1.1f);
            }
            case 4 -> { // Ivory Kimono
                layer(c, TORSO, 0xFF883948, 1.5f);
                layer(c, HAORI_L, baseCloth, 1.45f);
                layer(c, HAORI_R, baseCloth, 1.45f);
                segment(c, 45, 93, 52, 122, 0xFFD4A75A, 1.4f);
                segment(c, 75, 93, 68, 122, 0xFFD4A75A, 1.4f);
                segment(c, 49, 109, 71, 109, 0xFF9E3240, 2.2f);
                paint.setColor(0xFFE6C275);
                c.drawCircle(60, 109, 2.2f, paint);
            }
            case 5 -> { // Shadow Haori
                layer(c, JACKET_COLLAR, 0xFF9C2F3E, 1.4f);
                segment(c, 22, 106, 44, 96, 0xFF43465E, 1.3f);
                segment(c, 98, 106, 76, 96, 0xFF43465E, 1.3f);
                paint.setColor(0xFFC4C8D6);
                c.drawCircle(35, 103, 1.8f, paint);
                c.drawCircle(85, 103, 1.8f, paint);
            }
            case 6 -> { // Ward Armor
                layer(c, ARMOR_PLATE, 0xFF3A3F52, 1.5f);
                layer(c, ARMOR_L, baseCloth, 1.5f);
                layer(c, ARMOR_R, baseCloth, 1.5f);
                segment(c, 21, 105, 42, 102, 0xFFD4A75A, 1.2f);
                segment(c, 99, 105, 78, 102, 0xFFD4A75A, 1.2f);
                paint.setColor(0xFFE0B86A);
                c.drawCircle(38, 108, 1.6f, paint);
                c.drawCircle(82, 108, 1.6f, paint);
            }
        }
    }

    private void drawHeadAndFace(Canvas c, int hair, int skin) {
        int earFill = tone(skin, 0xFF9E5A5E, .20f);
        layer(c, LEFT_EAR, earFill, 1.4f);
        layer(c, RIGHT_EAR, earFill, 1.4f);
        segment(c, 32.5f, 57, 34.5f, 62, tone(skin, INK, .40f), 0.95f);
        segment(c, 87.5f, 57, 85.5f, 62, tone(skin, INK, .40f), 0.95f);

        layer(c, FACE, skin, 0);
        c.save();
        c.clipPath(FACE);
        layer(c, FACE_SHADOW, tone(skin, 0xFF96505A, .26f), 0);
        layer(c, FOREHEAD_SHADE, tone(skin, 0xFF8C4C54, .22f), 0);
        // Warm soft cheek tint
        paint.setColor(tone(skin, 0xFFE57A73, .22f));
        c.drawOval(38, 61, 47, 66, paint);
        c.drawOval(73, 61, 82, 66, paint);
        // Highlight bridge on cheek
        paint.setColor(tone(skin, 0xFFFFEFE0, .35f));
        c.drawOval(67, 62, 74, 65.5f, paint);
        c.restore();
        stroke(c, FACE, INK, 1.5f);

        // Eyes & brows
        int browColor = tone(hair, INK, .55f);
        layer(c, LEFT_BROW, browColor, 0);
        layer(c, RIGHT_BROW, browColor, 0);

        int eyeColor = EYES[part(2)];
        Path[] eyeShape = EYE_SHAPES[part(7)];
        drawEye(c, eyeShape[0], 47.6f, 54.6f, eyeColor);
        drawEye(c, eyeShape[1], 72.4f, 54.6f, eyeColor);
        stroke(c, eyeShape[2], INK, 1.55f);
        stroke(c, eyeShape[3], INK, 1.55f);

        // Nose & mouth
        stroke(c, NOSES[part(8)], tone(skin, INK, .42f), 1.05f);
        if (part(9) == 2) layer(c, MOUTHS[2], 0xFF9D4F62, 0);
        stroke(c, MOUTHS[part(9)], tone(skin, INK, .66f), 1.15f);
        segment(c, 57, 79, 63, 79, tone(skin, INK, .28f), 0.85f);

        if (age >= 55) {
            int line = tone(skin, INK, .38f);
            segment(c, 42, 61, 48, 62, line, 0.8f);
            segment(c, 72, 62, 78, 61, line, 0.8f);
            segment(c, 49, 73, 47, 78, line, 0.8f);
            segment(c, 71, 73, 73, 78, line, 0.8f);
        }
    }

    private void drawEye(Canvas c, Path eyeShape, float x, float y, int normalColor) {
        layer(c, eyeShape, 0xFFF3ECE6, 0.7f);
        c.save();
        c.clipPath(eyeShape);
        // Subtle upper sclera shadow
        paint.setColor(0x28453B52);
        c.drawRect(x - 8, y - 6, x + 8, y - 2.6f, paint);

        int iris = eyePower == EyePower.SPARKGLASS ? 0xFFD0362F
                : eyePower == EyePower.HORIZON ? 0xFF7B4AC2 : eyePower == EyePower.FARWATCH ? 0xFFB4DFE0 : normalColor;
        paint.setColor(iris);
        c.drawOval(x - 4.0f, y - 4.1f, x + 4.0f, y + 4.2f, paint);

        if (eyePower == EyePower.SPARKGLASS) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(0.75f);
            paint.setColor(0xFFFFD885);
            c.drawCircle(x, y, 2.7f, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(INK);
            c.drawCircle(x, y, 1.45f, paint);
            paint.setColor(0xFFFFE59D);
            c.drawCircle(x + 2.1f, y - 1.5f, 0.7f, paint);
            c.drawCircle(x - 2.1f, y + 1.5f, 0.7f, paint);
            c.drawCircle(x - 1.5f, y - 1.9f, 0.65f, paint);
        } else if (eyePower == EyePower.HORIZON) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(0.7f);
            paint.setColor(0xFFEBC8FF);
            c.drawCircle(x, y, 3.1f, paint);
            c.drawCircle(x, y, 1.85f, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(INK);
            c.drawCircle(x, y, 0.95f, paint);
            segment(c, x - 3.2f, y, x + 3.2f, y, 0x88F8DFFF, 0.65f);
        } else {
            // Lower iris brightness + crisp dark pupil + dual highlights
            paint.setColor(tone(iris, Color.WHITE, .28f));
            c.drawOval(x - 2.6f, y + 0.8f, x + 2.6f, y + 3.6f, paint);
            paint.setColor(INK);
            c.drawOval(x - 1.5f, y - 2.1f, x + 1.5f, y + 2.1f, paint);
            paint.setColor(0xFFFAF6F0);
            c.drawCircle(x + 1.45f, y - 1.45f, 0.95f, paint);
            c.drawCircle(x - 1.2f, y + 1.4f, 0.5f, paint);
        }
        if (evolvedEye && eyePower != null) {
            segment(c, x - 3.2f, y - 2.8f, x + 3.2f, y + 2.8f, 0xFFEFE2A0, .85f);
            segment(c, x + 3.2f, y - 2.8f, x - 3.2f, y + 2.8f, 0xFFEFE2A0, .85f);
        }
        c.restore();
    }

    private void drawScarsAndMarks(Canvas c, int skin) {
        int scar = tone(skin, 0xFF9C3D4A, .58f);
        int scarLight = tone(skin, 0xFFFFEFE0, .42f);
        switch (part(5)) {
            case 1 -> { // Cheek Mark
                segment(c, 40, 63.5f, 48, 70.5f, scar, 1.3f);
                segment(c, 40, 64.9f, 48, 71.9f, scarLight, 0.7f);
            }
            case 2 -> { // Brow Cut
                segment(c, 42, 42, 47, 52, scar, 1.35f);
                segment(c, 43.2f, 42, 48.2f, 52, scarLight, 0.65f);
            }
            case 3 -> { // Bridge Scar
                segment(c, 53, 58.2f, 67, 59.2f, scar, 1.35f);
                segment(c, 53, 59.5f, 67, 60.5f, scarLight, 0.65f);
            }
            case 4 -> { // Sage Marks
                paint.setColor(0xFFC84336);
                paint.setStrokeWidth(1.5f);
                paint.setStyle(Paint.Style.STROKE);
                c.drawLine(37, 54, 41, 51, paint);
                c.drawLine(37, 57, 42, 57, paint);
                c.drawLine(83, 54, 79, 51, paint);
                c.drawLine(83, 57, 78, 57, paint);
                paint.setStyle(Paint.Style.FILL);
                c.drawOval(41, 45, 51, 48, paint);
                c.drawOval(69, 45, 79, 48, paint);
            }
        }
    }

    private void drawFrontHair(Canvas c, int color, int hairStyle) {
        Path silhouette = HAIRSTYLES[hairStyle];
        layer(c, silhouette, color, 0);
        c.save();
        c.clipPath(silhouette);
        layer(c, HAIR_CROWN_SHADE, tone(color, INK, .36f), 0);
        layer(c, HAIR_HALO, tone(color, 0xFFF3E2E6, .28f), 0);
        if (hairStyle == 0) layer(c, HAIR_FLASH, tone(color, 0xFFFAF2EA, .84f), 0);
        c.restore();
        stroke(c, silhouette, INK, 1.55f);

        if (hairStyle == 8) {
            layer(c, BRAID_DROP, color, 1.45f);
            for (int i = 0; i < 4; i++) {
                float y = 68 + i * 8;
                segment(c, 26, y, 34, y + 4, tone(color, INK, .55f), 0.9f);
                segment(c, 34, y, 26, y + 4, tone(color, INK, .55f), 0.9f);
            }
            paint.setColor(0xFFB43B46);
            c.drawCircle(29, 105, 2.2f, paint);
        }
    }

    private void drawAccessories(Canvas c, int skin) {
        switch (part(6)) {
            case 1 -> { // Gold Stud
                paint.setColor(0xFFE7BA5C);
                c.drawCircle(33, 65.5f, 2.1f, paint);
                c.drawCircle(87, 65.5f, 2.1f, paint);
                strokeCircle(c, 33, 65.5f, 2.1f, 0.9f);
                strokeCircle(c, 87, 65.5f, 2.1f, 0.9f);
                paint.setColor(0xFFFFF3C4);
                c.drawCircle(32.4f, 64.9f, 0.65f, paint);
                c.drawCircle(86.4f, 64.9f, 0.65f, paint);
            }
            case 2 -> { // Red Ribbon
                paint.setColor(0xFFB83A47);
                c.drawOval(25, 25, 36, 33, paint);
                c.drawOval(34, 29, 43, 38, paint);
                strokeOval(c, 25, 25, 36, 33, 1.1f);
                strokeOval(c, 34, 29, 43, 38, 1.1f);
                segment(c, 34, 33, 27, 43, 0xFFB83A47, 2.1f);
                segment(c, 35, 33, 39, 44, 0xFF942A36, 2.0f);
                paint.setColor(0xFFE05A66);
                c.drawCircle(34.5f, 32.5f, 2.2f, paint);
                strokeCircle(c, 34.5f, 32.5f, 2.2f, 1.0f);
            }
            case 3 -> { // Round Glasses
                paint.setColor(0x22FFFFFF);
                c.drawCircle(47.6f, 54.6f, 8.2f, paint);
                c.drawCircle(72.4f, 54.6f, 8.2f, paint);
                strokeCircle(c, 47.6f, 54.6f, 8.2f, 1.15f);
                strokeCircle(c, 72.4f, 54.6f, 8.2f, 1.15f);
                segment(c, 55.8f, 53.5f, 64.2f, 53.5f, INK, 1.15f);
                segment(c, 35.5f, 53.0f, 39.4f, 53.5f, INK, 1.1f);
                segment(c, 80.6f, 53.5f, 84.5f, 53.0f, INK, 1.1f);
                segment(c, 43, 51, 46, 49, 0x66FFFFFF, 0.9f);
                segment(c, 68, 51, 71, 49, 0x66FFFFFF, 0.9f);
            }
            case 4 -> { // Shinobi Headband (Forehead Protector)
                paint.setColor(0xFF282D3F);
                c.drawRoundRect(33, 27.0f, 87, 35.5f, 3, 3, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.25f);
                paint.setColor(INK);
                c.drawRoundRect(33, 27.0f, 87, 35.5f, 3, 3, paint);
                paint.setStyle(Paint.Style.FILL);
                // Brushed steel plate
                paint.setColor(0xFFC8CFD9);
                c.drawRoundRect(46, 27.5f, 74, 35.0f, 2.2f, 2.2f, paint);
                paint.setColor(0x88FFFFFF);
                c.drawLine(50, 28.5f, 56, 34.0f, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.1f);
                paint.setColor(INK);
                c.drawRoundRect(46, 27.5f, 74, 35.0f, 2.2f, 2.2f, paint);
                // Rivets & spiral crest
                paint.setStyle(Paint.Style.FILL);
                c.drawCircle(48.5f, 29.6f, 0.7f, paint);
                c.drawCircle(48.5f, 33.0f, 0.7f, paint);
                c.drawCircle(71.5f, 29.6f, 0.7f, paint);
                c.drawCircle(71.5f, 33.0f, 0.7f, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.0f);
                c.drawCircle(60, 31.3f, 2.1f, paint);
                c.drawLine(62.1f, 31.3f, 64.1f, 29.7f, paint);
                paint.setStyle(Paint.Style.FILL);
            }
            case 5 -> { // Kitsune Fox Mask on side of head
                c.save();
                c.rotate(18, 82, 24);
                layer(c, FOX_MASK, 0xFFF7EFE6, 1.25f);
                segment(c, 77, 24, 82, 25.5f, 0xFFB8333D, 1.35f);
                segment(c, 87, 25.5f, 92, 24, 0xFFB8333D, 1.35f);
                segment(c, 74, 28, 79, 31, 0xFFB8333D, 1.1f);
                segment(c, 95, 28, 90, 31, 0xFFB8333D, 1.1f);
                paint.setColor(0xFFB8333D);
                c.drawCircle(84.5f, 20.5f, 1.2f, paint);
                c.restore();
            }
            case 6 -> { // Lower Face Mask
                layer(c, FACE_MASK, 0xFF282D3C, 1.45f);
                segment(c, 39, 62, 60, 60, 0xFF474F66, 1.1f);
                segment(c, 81, 62, 60, 60, 0xFF474F66, 1.1f);
                segment(c, 44, 71, 60, 75, 0xFF1A1E28, 1.0f);
                segment(c, 76, 71, 60, 75, 0xFF1A1E28, 1.0f);
                paint.setColor(tone(skin, INK, .35f));
                c.drawCircle(60, 58, 0.7f, paint);
            }
        }
    }

    private void layer(Canvas c, Path path, int color, float outline) {
        paint.setColor(color);
        c.drawPath(path, paint);
        if (outline > 0) stroke(c, path, INK, outline);
    }

    private void stroke(Canvas c, Path path, int color, float width) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        paint.setColor(color);
        c.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void strokeCircle(Canvas c, float x, float y, float r, float width) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        paint.setColor(INK);
        c.drawCircle(x, y, r, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void strokeOval(Canvas c, float l, float t, float r, float b, float width) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        paint.setColor(INK);
        c.drawOval(l, t, r, b, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void segment(Canvas c, float x, float y, float x2, float y2, int color, float width) {
        paint.setColor(color);
        paint.setStrokeWidth(width);
        c.drawLine(x, y, x2, y2, paint);
    }

    private static int tone(int from, int to, float amount) {
        return Color.rgb(
                (int) (Color.red(from) + (Color.red(to) - Color.red(from)) * amount),
                (int) (Color.green(from) + (Color.green(to) - Color.green(from)) * amount),
                (int) (Color.blue(from) + (Color.blue(to) - Color.blue(from)) * amount));
    }

    private static Path vector(String data) {
        String[] tokens = data.split(" ");
        Path path = new Path();
        for (int i = 0; i < tokens.length;) {
            switch (tokens[i++]) {
                case "M" -> path.moveTo(read(tokens, i++), read(tokens, i++));
                case "L" -> path.lineTo(read(tokens, i++), read(tokens, i++));
                case "Q" -> path.quadTo(read(tokens, i++), read(tokens, i++), read(tokens, i++), read(tokens, i++));
                case "C" -> path.cubicTo(read(tokens, i++), read(tokens, i++), read(tokens, i++), read(tokens, i++), read(tokens, i++), read(tokens, i++));
                case "Z" -> path.close();
                default -> throw new IllegalArgumentException("Unexpected path command in portrait");
            }
        }
        return path;
    }

    private static float read(String[] tokens, int point) { return Float.parseFloat(tokens[point]); }
}
