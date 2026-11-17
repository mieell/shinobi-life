package com.shinobilife.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.view.View;

@android.annotation.SuppressLint("ViewConstructor")
final class Portrait extends View {
    static final String[][] OPTIONS = {
        {"Silver Sweep", "Soft Crop", "Topknot", "Side Part", "Long Waves", "Round Curls",
                "Low Ponytail", "Cropped Fade", "Side Braid", "Wild Fringe"},
        {"Ink", "Chestnut", "Silver", "Auburn", "Plum", "Honey"},
        {"Amber", "Hazel", "Slate", "Jade", "Violet", "Dark Brown"},
        {"Porcelain", "Peach", "Warm Tan", "Copper", "Umber", "Deep Brown"},
        {"Trail Jacket", "Crimson Wrap", "Academy Vest", "Travel Cloak", "Ivory Kimono"},
        {"None", "Cheek Mark", "Brow Cut"},
        {"None", "Gold Stud", "Red Ribbon", "Round Glasses"}
    };
    static final String[] LABELS = {"Hair style", "Hair color", "Eye color", "Skin tone", "Outfit", "Scar", "Accessory"};
    private static final int INK = 0xFF25202D;
    private static final int[] HAIR = {0xFF323C53, 0xFF634742, 0xFFB6ADAE, 0xFF985044, 0xFF5D416C, 0xFFC68A4A};
    private static final int[] EYES = {0xFF995427, 0xFF727050, 0xFF50688A, 0xFF3A7365, 0xFF74458B, 0xFF543532};
    private static final int[] SKIN = {0xFFF4D5B9, 0xFFEAB997, 0xFFD99B71, 0xFFBC7955, 0xFF955D49, 0xFF6E493D};
    private static final int[] CLOTH = {0xFF3F6D6A, 0xFF994048, 0xFF586A88, 0xFF584D6C, 0xFFE6D1B1};

    private static final Path COAT = vector("M -6 127 C 2 111 17 103 43 96 L 73 92 C 90 95 111 104 125 121 L 125 130 Z");
    private static final Path NECK = vector("M 47 78 L 78 78 L 77 90 Q 79 96 86 100 L 65 118 L 42 99 Q 49 93 47 78 Z");
    private static final Path NECK_SHADE = vector("M 48 80 L 78 79 L 77 89 Q 67 97 46 94 Z");
    private static final Path FACE = vector("M 36 45 C 35 27 53 22 64 22 C 82 21 92 35 91 48 L 89 56 Q 90 63 95 67 Q 96 70 89 72 L 87 81 Q 85 87 74 93 Q 66 97 57 92 Q 42 85 36 75 L 33 61 Z");
    private static final Path FACE_SHADE = vector("M 28 38 Q 51 25 70 33 L 64 43 C 50 49 48 65 53 78 Q 61 88 74 91 L 74 99 L 29 92 Z");
    private static final Path CHEEK_LIGHT = vector("M 73 63 Q 78 61 85 61 L 90 67 L 84 68 Q 76 67 73 63 Z");
    private static final Path EAR = vector("M 38 59 Q 30 53 28 60 Q 27 70 39 77 L 42 71 Z");

    private static final Path LEFT_EYE = vector("M 46 57 Q 55 51 65 56 Q 60 61 53 61 Q 49 61 46 57 Z");
    private static final Path RIGHT_EYE = vector("M 73 55 Q 81 50 88 53 Q 85 59 79 59 Q 76 58 73 55 Z");
    private static final Path LEFT_LID = vector("M 44 56 Q 52 52 57 53 Q 62 53 66 56");
    private static final Path RIGHT_LID = vector("M 72 54 Q 78 50 84 50 L 89 52");
    private static final Path LEFT_BROW = vector("M 44 50 Q 53 44 62 48 L 65 50 Q 56 48 46 52 Z");
    private static final Path RIGHT_BROW = vector("M 72 48 Q 79 43 86 45 L 89 47 Q 82 45 73 50 Z");
    private static final Path MOUTH = vector("M 64 79 Q 70 76 74 77 Q 79 77 82 76");
    private static final Path NOSE = vector("M 79 56 Q 79 63 83 67 L 80 69");
    private static final Path EAR_LINE = vector("M 32 61 Q 37 61 37 67");

    private static final Path[] HAIRSTYLES = {
        vector("M 35 67 C 30 62 25 57 26 48 L 22 51 Q 19 41 30 31 L 23 32 Q 30 20 46 17 L 42 13 Q 54 12 64 15 C 78 12 91 19 97 26 L 91 26 Q 101 30 103 43 L 97 38 Q 97 45 89 52 Q 90 40 82 34 Q 72 41 63 44 L 66 37 Q 52 48 37 51 L 39 45 Q 33 52 35 67 Z"),
        vector("M 35 65 Q 24 58 26 44 Q 26 30 39 21 Q 54 11 69 16 Q 89 18 96 34 Q 99 42 91 55 L 88 43 L 81 37 Q 76 43 68 41 L 64 36 Q 58 42 50 42 L 48 38 Q 45 46 36 48 Z"),
        vector("M 35 68 Q 25 61 26 44 Q 28 25 47 19 Q 64 11 82 20 Q 94 26 96 40 Q 94 49 90 53 L 86 42 Q 83 36 78 31 Q 64 38 55 38 Q 44 40 37 49 Z"),
        vector("M 34 65 Q 24 60 25 46 Q 25 32 32 25 Q 43 11 62 16 Q 79 13 91 26 Q 97 34 92 50 Q 88 36 72 30 Q 67 37 57 43 L 60 35 Q 52 45 37 50 Z"),
        vector("M 30 79 C 24 70 23 60 24 45 Q 25 28 35 21 Q 47 12 64 16 Q 82 12 94 28 Q 102 42 96 65 L 90 72 Q 91 48 81 36 Q 76 33 72 28 Q 62 43 46 46 Q 35 48 35 66 L 37 85 Z"),
        vector("M 36 64 Q 26 63 26 54 Q 19 47 25 39 Q 22 30 31 26 Q 30 19 43 17 Q 48 10 59 15 Q 69 10 77 17 Q 88 14 92 23 Q 101 28 98 36 Q 104 43 95 51 L 89 53 Q 88 43 81 41 Q 73 46 66 39 Q 59 44 53 38 Q 46 45 40 43 Q 35 49 36 64 Z"),
        vector("M 35 66 Q 24 61 26 46 Q 27 29 38 23 Q 55 13 71 17 Q 92 20 95 37 Q 96 46 89 52 Q 87 40 81 34 Q 65 44 51 43 L 54 39 Q 44 45 36 50 Z"),
        vector("M 31 58 Q 26 52 27 43 Q 26 30 38 23 Q 48 16 63 16 Q 80 16 91 27 Q 97 35 92 49 L 87 41 Q 66 34 42 39 Q 37 44 34 54 Z"),
        vector("M 36 65 Q 26 59 26 45 Q 26 29 41 21 Q 53 14 66 17 Q 86 16 95 33 Q 99 44 91 51 L 86 43 Q 81 34 73 29 Q 65 41 52 43 L 54 36 Q 47 43 38 46 Z"),
        vector("M 35 66 Q 24 62 25 50 L 21 54 Q 18 45 28 37 L 24 37 Q 29 29 42 22 L 34 20 Q 48 16 57 17 L 56 12 Q 66 13 75 21 L 81 16 Q 91 21 96 32 L 102 31 Q 104 45 92 54 Q 92 47 88 41 L 81 47 L 81 39 Q 74 47 67 50 L 70 41 Q 61 47 52 50 L 54 43 Q 44 49 37 52 Z")
    };
    private static final Path HAIR_SHADE = vector("M 12 45 Q 44 35 64 35 Q 84 34 108 23 L 112 73 L 9 80 Z");
    private static final Path HAIR_LIGHT = vector("M 25 39 Q 48 15 79 24 Q 70 25 64 29 Q 41 27 25 39 Z");
    private static final Path HAIR_FLASH = vector("M 52 17 Q 62 15 67 17 C 61 25 52 37 37 47 Q 46 36 49 30 Q 46 31 43 31 Z");
    private static final Path LONG_HAIR = vector("M 31 31 Q 16 45 23 73 Q 25 83 19 92 Q 35 96 43 85 L 80 82 Q 88 99 105 94 Q 98 81 101 68 Q 105 42 91 32 Z");
    private static final Path PONYTAIL = vector("M 83 51 Q 98 46 104 57 Q 105 64 101 73 Q 96 85 104 93 Q 90 92 85 79 Q 81 66 87 59 Z");
    private static final Path BRAID = vector("M 32 63 Q 22 64 23 73 L 24 79 L 21 86 L 24 93 L 21 98 Q 24 105 32 106 L 37 99 L 35 93 L 37 86 L 34 79 L 36 71 Z");

    private static final Path[] COLLARS = {
        vector("M 11 109 Q 24 99 44 96 L 58 106 L 76 95 Q 89 96 104 105 L 73 121 L 61 123 Z"),
        vector("M 12 109 L 45 93 Q 63 104 77 93 L 96 101 Q 77 111 57 115 L 39 131 L 19 127 L 44 111 Z"),
        vector("M 18 107 L 43 95 L 56 104 L 63 108 L 74 95 L 99 104 L 93 124 L 32 130 Z"),
        vector("M 10 111 Q 21 95 43 93 Q 61 108 77 92 Q 95 93 110 109 L 100 125 L 50 128 L 22 120 Z"),
        vector("M 13 109 L 43 94 L 58 110 L 77 95 L 101 103 L 72 118 L 62 130 L 47 130 L 55 117 Z")
    };
    private static final Path COLLAR_STITCH = vector("M 13 113 Q 29 105 40 103 M 79 100 Q 91 101 102 108");

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path frame = new Path();
    private final int[] style;
    private final int age;
    private final Shader backdrop;
    private final EyePower eyePower;
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
        this.eyePower = eyePower;
        this.previewSlot = previewSlot;
        this.previewValue = previewValue;
        backdrop = new LinearGradient(105, 0, 26, 124, tone(backdropColor, 0xFFFFEDC9, .15f),
                tone(backdropColor, 0xFF444B64, .54f), Shader.TileMode.CLAMP);
        frame.addRoundRect(1, 1, 119, 119, 15, 15, Path.Direction.CW);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        setContentDescription(eyePower == null ? label : label + ", " + eyePower.title + " awakened");
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    static boolean isColor(int slot) { return slot >= 1 && slot <= 3; }

    static int optionColor(int slot, int variant) {
        return switch (slot) {
            case 1 -> HAIR[variant];
            case 2 -> EYES[variant];
            case 3 -> SKIN[variant];
            default -> throw new IllegalArgumentException("No color palette for slot " + slot);
        };
    }

    private int part(int slot) { return slot == previewSlot ? previewValue : style[slot]; }

    @Override protected void onDraw(Canvas c) {
        float scale = Math.min(getWidth(), getHeight()) / 120f;
        c.save();
        c.translate((getWidth() - 120 * scale) / 2, (getHeight() - 120 * scale) / 2);
        c.scale(scale, scale);
        c.clipPath(frame);
        paint.setShader(backdrop);
        c.drawRect(0, 0, 120, 120, paint);
        paint.setShader(null);
        paint.setColor(0x55FFF0CE);
        c.drawCircle(89, 39, 28, paint);
        segment(c, 104, 16, 114, 16, 0x92FFE7C4, 1.1f);
        segment(c, 109, 11, 109, 21, 0x92FFE7C4, 1.1f);
        segment(c, 7, 90, 33, 73, 0x26FBE3C9, 1f);

        int hair = HAIR[age >= 65 ? 2 : part(1)];
        int skin = SKIN[part(3)];
        int outfit = part(4);
        int hairStyle = part(0);
        int lining = tone(CLOTH[outfit], INK, .56f);
        if (hairStyle == 4) layer(c, LONG_HAIR, tone(hair, INK, .22f), 1.8f);
        if (hairStyle == 6) layer(c, PONYTAIL, tone(hair, INK, .22f), 1.8f);
        if (hairStyle == 2) {
            paint.setColor(INK);
            c.drawOval(50, 5, 73, 28, paint);
            paint.setColor(hair);
            c.drawOval(52, 6, 71, 25, paint);
        }
        layer(c, COAT, CLOTH[outfit], 2f);
        layer(c, NECK, skin, 1.55f);
        layer(c, NECK_SHADE, tone(skin, 0xFF884D57, .40f), 0);
        layer(c, COLLARS[outfit], outfit == 4 ? 0xFF824452 : lining, 1.35f);
        stroke(c, COLLAR_STITCH, tone(CLOTH[outfit], 0xFFF3D9B1, .38f), 1.25f);

        layer(c, EAR, tone(skin, 0xFFA05B5E, .22f), 1.5f);
        stroke(c, EAR_LINE, tone(skin, INK, .43f), 1.13f);
        layer(c, FACE, skin, 0);
        c.save();
        c.clipPath(FACE);
        layer(c, FACE_SHADE, tone(skin, 0xFF9C595E, .37f), 0);
        layer(c, CHEEK_LIGHT, tone(skin, 0xFFF8D3B0, .29f), 0);
        c.restore();
        stroke(c, FACE, INK, 1.5f);
        face(c, hair, skin);
        hair(c, hair, hairStyle);
        accessories(c, skin);
        c.restore();
    }

    private void face(Canvas c, int hair, int skin) {
        int brow = tone(hair, INK, .58f);
        layer(c, LEFT_BROW, brow, 0);
        layer(c, RIGHT_BROW, brow, 0);
        int eye = EYES[part(2)];
        iris(c, LEFT_EYE, 56.1f, 56.8f, eye);
        iris(c, RIGHT_EYE, 81.5f, 54.5f, eye);
        stroke(c, LEFT_LID, INK, 1.35f);
        stroke(c, RIGHT_LID, INK, 1.25f);
        stroke(c, NOSE, tone(skin, INK, .42f), 1.07f);
        stroke(c, MOUTH, tone(skin, INK, .68f), 1.15f);
        segment(c, 70, 81, 76, 81, tone(skin, INK, .33f), .85f);
        if (age >= 55) {
            segment(c, 49, 65, 54, 66, tone(skin, INK, .42f), .8f);
            segment(c, 78, 62, 84, 61, tone(skin, INK, .42f), .8f);
            segment(c, 60, 77, 57, 81, tone(skin, INK, .37f), .8f);
        }
    }

    private void iris(Canvas c, Path eyeShape, float x, float y, int normal) {
        layer(c, eyeShape, 0xFFEDE0D6, .66f);
        c.save();
        c.clipPath(eyeShape);
        int iris = eyePower == EyePower.SPARKGLASS ? 0xFFC53F35
                : eyePower == EyePower.HORIZON ? 0xFF7449AB : normal;
        paint.setColor(iris);
        c.drawOval(x - 3.6f, y - 3.7f, x + 3.6f, y + 3.8f, paint);
        paint.setColor(INK);
        c.drawOval(x - 1.35f, y - 2.45f, x + 1.35f, y + 2.55f, paint);
        if (eyePower == EyePower.SPARKGLASS) segment(c, x - 1.7f, y + 1.6f, x + 1.8f, y - 1.9f, 0xFFFFDB94, 1.15f);
        else if (eyePower == EyePower.HORIZON) segment(c, x - 2.9f, y, x + 2.9f, y, 0xFFF3CCFE, .85f);
        else {
            paint.setColor(0xFFF6E4D1);
            c.drawCircle(x + 1.1f, y - 1.35f, .85f, paint);
        }
        c.restore();
    }

    private void hair(Canvas c, int color, int hairStyle) {
        Path silhouette = HAIRSTYLES[hairStyle];
        layer(c, silhouette, color, 0);
        c.save();
        c.clipPath(silhouette);
        layer(c, HAIR_SHADE, tone(color, INK, .44f), 0);
        layer(c, HAIR_LIGHT, tone(color, 0xFFDABCC3, .28f), 0);
        if (hairStyle == 0) layer(c, HAIR_FLASH, tone(color, 0xFFF8EEE3, .82f), 0);
        c.restore();
        stroke(c, silhouette, INK, 1.65f);
        if (hairStyle == 8) {
            layer(c, BRAID, color, 1.6f);
            for (int i = 0; i < 4; i++) segment(c, 25, 73 + i * 7, 34, 77 + i * 7, tone(color, INK, .65f), .9f);
        }
    }

    private void accessories(Canvas c, int skin) {
        int scar = tone(skin, 0xFF9F414F, .58f);
        if (part(5) == 1) segment(c, 52, 66, 58, 75, scar, 1.35f);
        if (part(5) == 2) segment(c, 49, 44, 52, 52, scar, 1.2f);
        if (part(6) == 1) {
            paint.setColor(0xFFE3B668);
            c.drawCircle(35, 70, 2.3f, paint);
        } else if (part(6) == 2) {
            segment(c, 29, 34, 35, 39, 0xFFB84751, 3.6f);
            segment(c, 33, 31, 30, 42, 0xFFBB4351, 2.5f);
        } else if (part(6) == 3) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(INK);
            paint.setStrokeWidth(1.10f);
            c.drawOval(44, 50, 66, 64, paint);
            c.drawOval(72, 48, 90, 62, paint);
            paint.setStyle(Paint.Style.FILL);
            segment(c, 66, 54, 72, 53, INK, 1.1f);
            segment(c, 36, 53, 44, 53, INK, 1.1f);
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

    private void segment(Canvas c, float x, float y, float x2, float y2, int color, float width) {
        paint.setColor(color);
        paint.setStrokeWidth(width);
        c.drawLine(x, y, x2, y2, paint);
    }

    private static int tone(int from, int to, float amount) {
        return Color.rgb((int) (Color.red(from) + (Color.red(to) - Color.red(from)) * amount),
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
