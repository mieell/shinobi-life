package com.shinobilife.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
    private static final int INK = 0xFF292335;
    private static final int[] HAIR = {0xFF2A314B, 0xFF623E38, 0xFFB2A9A8, 0xFF984940, 0xFF5A3C63, 0xFFC48339};
    private static final int[] EYES = {0xFFAA6436, 0xFF72674C, 0xFF56648C, 0xFF4C8069, 0xFF745188, 0xFF543535};
    private static final int[] SKIN = {0xFFF7D8BF, 0xFFF2BF9F, 0xFFDC9B6B, 0xFFBF7954, 0xFF96583E, 0xFF6D4136};
    private static final int[] CLOTH = {0xFF357B73, 0xFFA94C3F, 0xFF576A8E, 0xFF6B5178, 0xFFEEE0C9};

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clip = new Path(), outline = new Path();
    private final int[] style;
    private final int age;
    private final int backdrop;
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

    private Portrait(Context context, int[] style, int age, int backdrop, String label,
                     EyePower eyePower, int previewSlot, int previewValue) {
        super(context);
        this.style = style;
        this.age = age;
        this.backdrop = backdrop;
        this.eyePower = eyePower;
        this.previewSlot = previewSlot;
        this.previewValue = previewValue;
        clip.addCircle(48, 48, 46, Path.Direction.CW);
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

    @Override protected void onDraw(Canvas canvas) {
        float scale = Math.min(getWidth(), getHeight()) / 96f;
        canvas.save();
        canvas.translate((getWidth() - 96 * scale) / 2, (getHeight() - 96 * scale) / 2);
        canvas.scale(scale, scale);
        canvas.clipPath(clip);
        circle(canvas, 48, 48, 46, backdrop);
        circle(canvas, 48, 56, 33, 0x25FFFFFF);
        shape(canvas, 0x30FFFFFF, false, 7, 32, 14, 27, 18, 33, 13, 38);
        shape(canvas, 0x36FFFFFF, false, 79, 16, 84, 22, 79, 27, 74, 22);
        int hair = age >= 65 ? HAIR[2] : HAIR[part(1)];
        int skin = SKIN[part(3)];
        int cloth = CLOTH[part(4)];
        if (part(0) == 4) oval(canvas, 18, 16, 78, 82, hair, true);
        if (part(0) == 2) oval(canvas, 49, 6, 68, 26, hair, true);
        if (part(0) == 6) oval(canvas, 62, 48, 82, 79, hair, true);
        shape(canvas, cloth, true, 9, 97, 17, 81, 38, 73, 60, 73, 79, 81, 88, 97);
        oval(canvas, 40, 60, 59, 84, skin, true);
        shape(canvas, part(4) == 4 ? 0xFFAA4D47 : INK, false, 19, 81, 37, 74, 50, 88, 62, 75, 76, 81, 53, 96, 47, 96);
        oval(canvas, 20, 43, 33, 58, skin, true);
        oval(canvas, 63, 43, 76, 58, skin, true);
        oval(canvas, 26, 22, 70, 73, skin, true);
        hair(canvas, hair);
        int eye = EYES[part(2)];
        line(canvas, 33, 44, 40, 43, INK, 1.8f);
        line(canvas, 55, 43, 62, 44, INK, 1.8f);
        oval(canvas, 33, 47, 42, 54, Color.WHITE, false);
        oval(canvas, 54, 47, 63, 54, Color.WHITE, false);
        iris(canvas, 38, eye);
        iris(canvas, 58, eye);
        line(canvas, 47, 55, 46, 58, 0x887C4C3C, 1.4f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.6f);
        paint.setColor(INK);
        canvas.drawArc(42, 57, 55, 66, 20, 129, false, paint);
        paint.setStyle(Paint.Style.FILL);
        oval(canvas, 29, 54, 40, 59, 0x40F15C62, false);
        oval(canvas, 56, 54, 67, 59, 0x40F15C62, false);
        if (part(5) == 1) line(canvas, 62, 54, 57, 62, 0xFF9E4F4D, 1.6f);
        if (part(5) == 2) line(canvas, 35, 41, 39, 46, 0xFF9E4F4D, 1.6f);
        if (part(6) == 1) circle(canvas, 69, 56, 2.6f, 0xFFEABB59);
        if (part(6) == 2) shape(canvas, 0xFFCF4D50, true, 67, 35, 74, 33, 76, 40, 69, 42);
        if (part(6) == 3) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            paint.setColor(INK);
            canvas.drawCircle(37, 50, 9, paint);
            canvas.drawCircle(59, 50, 9, paint);
            paint.setStyle(Paint.Style.FILL);
            line(canvas, 46, 49, 50, 49, INK, 1.6f);
        }
        if (age >= 55) {
            line(canvas, 35, 57, 39, 58, 0x887C4C3C, 1);
            line(canvas, 58, 58, 62, 57, 0x887C4C3C, 1);
        }
        canvas.restore();
    }

    private void iris(Canvas c, float x, int normalColor) {
        if (eyePower == EyePower.SPARKGLASS) {
            shape(c, 0xFFBE4537, false, x, 45.5f, x + 4, 50, x, 54.5f, x - 4, 50);
            line(c, x - 1.6f, 51.2f, x + 1.6f, 48.8f, 0xFFFFE4A4, 1.4f);
        } else if (eyePower == EyePower.HORIZON) {
            oval(c, x - 4.5f, 46, x + 4.5f, 54, 0xFF6542A1, false);
            line(c, x - 3, 50, x + 3, 50, 0xFFF3CDFB, 1.2f);
            shape(c, INK, false, x, 47, x + 1.2f, 50, x, 53, x - 1.2f, 50);
        } else {
            circle(c, x, 50, 3, normalColor);
            circle(c, x, 50, 1.5f, INK);
            circle(c, x + .7f, 49, .8f, Color.WHITE);
        }
    }

    private void hair(Canvas c, int color) {
        switch (part(0)) {
            case 1:
                shape(c, color, true, 23, 45, 22, 29, 29, 21, 38, 15, 55, 15, 69, 24, 74, 34, 70, 45, 62, 35, 53, 36, 47, 33, 37, 36, 30, 35);
                break;
            case 2:
                shape(c, color, true, 23, 45, 21, 33, 29, 21, 42, 17, 58, 17, 70, 26, 74, 39, 68, 45, 63, 33, 58, 28, 45, 37, 31, 36);
                break;
            case 3:
                shape(c, color, true, 23, 48, 19, 29, 28, 18, 45, 15, 62, 18, 72, 28, 72, 47, 65, 33, 56, 27, 49, 37, 37, 42, 39, 34, 30, 39);
                break;
            case 4:
                shape(c, color, true, 19, 57, 17, 39, 21, 25, 32, 17, 46, 15, 59, 17, 72, 26, 78, 41, 77, 64, 68, 70, 69, 49, 59, 37, 50, 28, 40, 39, 29, 46, 27, 65);
                break;
            case 5:
                oval(c, 19, 16, 76, 43, color, true);
                for (int i = 0; i < 5; i++) circle(c, 27 + i * 10, 26 - (i % 2) * 4, 10, color);
                for (int i = 0; i < 5; i++) circle(c, 26 + i * 11, 36 + (i % 2) * 3, 7, color);
                break;
            case 6:
                shape(c, color, true, 23, 45, 19, 35, 27, 23, 42, 16, 57, 16, 70, 23, 76, 35, 69, 50, 65, 38, 57, 27, 46, 29, 36, 38, 28, 38);
                shape(c, 0xFFBC5350, false, 71, 49, 77, 47, 80, 51, 73, 54);
                break;
            case 7:
                oval(c, 25, 19, 71, 51, 0x484B3A45, false);
                shape(c, color, true, 25, 37, 26, 27, 33, 19, 44, 15, 58, 16, 68, 22, 71, 35, 63, 30, 48, 29, 34, 31);
                break;
            case 8:
                shape(c, color, true, 22, 48, 19, 31, 28, 20, 43, 14, 56, 16, 69, 23, 75, 33, 71, 47, 65, 34, 54, 26, 48, 35, 36, 35, 30, 41);
                for (int i = 0; i < 4; i++) oval(c, 66 + i, 43 + i * 7, 76 + i, 54 + i * 7, color, true);
                break;
            case 9:
                shape(c, color, true, 22, 47, 17, 36, 24, 27, 19, 24, 35, 20, 35, 12, 45, 17, 60, 15, 57, 21, 73, 21, 68, 27, 77, 33, 70, 46, 63, 35, 57, 41, 53, 36, 46, 46, 42, 37, 33, 45, 31, 38);
                break;
            default:
                shape(c, color, true, 22, 46, 17, 33, 24, 28, 19, 25, 37, 16, 32, 11, 49, 15, 62, 12, 62, 18, 77, 24, 72, 30, 78, 38, 69, 43, 64, 31, 52, 29, 37, 43, 39, 34, 30, 40);
                shape(c, part(1) == 2 ? 0xFFFAE1C5 : 0xFFE5DBDD, false, 45, 19, 53, 17, 47, 28, 32, 37, 40, 27);
        }
    }

    private void circle(Canvas c, float x, float y, float r, int color) {
        paint.setColor(color);
        c.drawCircle(x, y, r, paint);
    }

    private void oval(Canvas c, float l, float t, float r, float b, int color, boolean outline) {
        paint.setColor(color);
        c.drawOval(l, t, r, b, paint);
        if (outline) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(INK);
            c.drawOval(l, t, r, b, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private void line(Canvas c, float x, float y, float x2, float y2, int color, float width) {
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND);
        c.drawLine(x, y, x2, y2, paint);
    }

    private void shape(Canvas c, int color, boolean hasStroke, float... points) {
        outline.rewind();
        outline.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) outline.lineTo(points[i], points[i + 1]);
        outline.close();
        paint.setColor(color);
        c.drawPath(outline, paint);
        if (hasStroke) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(INK);
            c.drawPath(outline, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }
}
