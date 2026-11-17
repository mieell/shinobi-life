package com.shinobilife.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

@android.annotation.SuppressLint("ViewConstructor")
final class Marks extends View {
    private static final int INK = 0xFF1F1A28;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final String mark;
    private final int color;

    Marks(Context context, String mark, int color) {
        super(context);
        this.mark = mark;
        this.color = color;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override protected void onDraw(Canvas c) {
        c.save();
        float size = Math.min(getWidth(), getHeight());
        c.translate((getWidth() - size) / 2f, (getHeight() - size) / 2f);
        c.scale(size / 24f, size / 24f);
        paint.setColor(color);
        paint.setStrokeWidth(1.65f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);

        switch (mark) {
            case "life" -> line(c, 3, 10, 12, 3, 21, 10, 19, 10, 19, 20, 14, 20, 14, 14, 10, 14, 10, 20, 5, 20, 5, 10, 3, 10);
            case "missions" -> {
                c.drawRoundRect(5, 4, 19, 21, 2, 2, paint);
                line(c, 9, 8, 15, 8);
                line(c, 9, 12, 15, 12);
                line(c, 9, 16, 13, 16);
            }
            case "clans" -> {
                line(c, 12, 2, 21, 7, 21, 17, 12, 22, 3, 17, 3, 7, 12, 2);
                line(c, 12, 7, 17, 12, 12, 17, 7, 12, 12, 7);
            }
            case "people" -> {
                c.drawCircle(9, 8, 3, paint);
                c.drawArc(3, 13, 15, 23, 180, 180, false, paint);
                c.drawArc(13, 5, 20, 11, 270, 180, false, paint);
                c.drawArc(13, 13, 22, 22, 270, 90, false, paint);
            }
            case "training" -> {
                line(c, 3, 18, 18, 3, 21, 3, 21, 6, 6, 21, 3, 18);
                line(c, 6, 15, 9, 18);
            }
            case "meditate" -> {
                // Stylized chakra lotus & flame
                path.rewind();
                path.moveTo(12, 3);
                path.quadTo(17, 9, 15, 14);
                path.quadTo(12, 16, 9, 14);
                path.quadTo(7, 9, 12, 3);
                c.drawPath(path, paint);
                c.drawArc(4, 13, 12, 20, 180, 150, false, paint);
                c.drawArc(12, 13, 20, 20, -150, 150, false, paint);
                line(c, 6, 20, 18, 20);
            }
            case "wardrobe" -> {
                line(c, 7, 4, 12, 6, 17, 4, 20, 9, 17, 12, 16, 20, 8, 20, 7, 12, 4, 9, 7, 4);
                line(c, 9, 5, 14, 12);
                line(c, 15, 5, 10, 12);
            }
            case "trophy" -> {
                line(c, 7, 4, 17, 4, 16, 12, 12, 15, 8, 12, 7, 4);
                c.drawArc(4, 5, 8, 11, 90, 180, false, paint);
                c.drawArc(16, 5, 20, 11, -90, 180, false, paint);
                line(c, 12, 15, 12, 20);
                line(c, 8, 20, 16, 20);
            }
            case "scroll" -> {
                c.drawRoundRect(5, 5, 19, 19, 2.5f, 2.5f, paint);
                line(c, 3, 7, 21, 7);
                line(c, 3, 17, 21, 17);
                line(c, 9, 11, 15, 11);
                line(c, 9, 14, 14, 14);
            }
            case "spark" -> line(c, 12, 2, 14.8f, 9, 22, 12, 14.8f, 15, 12, 22, 9, 15, 2, 12, 9, 9, 12, 2);
            case "coin" -> {
                c.drawCircle(12, 12, 9, paint);
                line(c, 12, 7, 16, 12, 12, 17, 8, 12, 12, 7);
            }
            case "summons" -> {
                c.drawOval(6, 12, 18, 21, paint);
                c.drawOval(3, 7, 7, 12, paint);
                c.drawOval(8, 3, 11, 9, paint);
                c.drawOval(13, 3, 16, 9, paint);
                c.drawOval(17, 7, 21, 12, paint);
            }
            case "SHRIKE" -> drawShrike(c);
            case "TORTOISE" -> drawTortoise(c);
            case "OTTER" -> drawOtter(c);
            case "MOTH" -> drawMoth(c);
            case "LYNX" -> drawLynx(c);
            default -> drawClanSeal(c, Integer.parseInt(mark));
        }
        c.restore();
    }

    private void drawShrike(Canvas c) {
        // Spread wings
        fillPoly(c, 0xFF5A3F32, true, 10, 11, 2, 5, 4, 11, 8, 14);
        fillPoly(c, 0xFF5A3F32, true, 14, 11, 22, 5, 20, 11, 16, 14);
        // Wing accent feathers
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFFD8B084);
        paint.setStrokeWidth(0.9f);
        c.drawLine(4, 7.5f, 8.5f, 11, paint);
        c.drawLine(20, 7.5f, 15.5f, 11, paint);

        // Tail feathers
        fillPoly(c, 0xFF473026, true, 10, 16, 8, 22, 12, 20, 16, 22, 14, 16);
        // Body + cream breast
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF6B4C3B);
        c.drawOval(8.5f, 7, 15.5f, 17, paint);
        paint.setColor(0xFFF6E2BE);
        c.drawOval(9.5f, 9.5f, 14.5f, 16.5f, paint);
        strokeOval(c, 8.5f, 7, 15.5f, 17, 1.0f);

        // Head + crest + beak
        paint.setColor(0xFF5A3F32);
        c.drawCircle(12, 6.5f, 3.3f, paint);
        fillPoly(c, 0xFFE8A44A, true, 14.5f, 6, 18.5f, 7, 14.5f, 8.2f);
        strokeCircle(c, 12, 6.5f, 3.3f, 0.9f);
        paint.setColor(0xFFFFF4D6);
        c.drawCircle(13, 6.1f, 0.85f, paint);

        // Glowing miniature lantern
        paint.setColor(0x55FFD166);
        c.drawCircle(18, 16, 3.2f, paint);
        paint.setColor(0xFFFFB733);
        c.drawRoundRect(16.3f, 13.8f, 19.7f, 18.2f, 1, 1, paint);
        paint.setColor(0xFFFFF6C9);
        c.drawCircle(18, 16, 1.0f, paint);
    }

    private void drawTortoise(Canvas c) {
        // Legs & head
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF7B9E7E);
        c.drawRoundRect(5.5f, 14.5f, 8.5f, 19.5f, 1, 1, paint);
        c.drawRoundRect(14.5f, 14.5f, 17.5f, 19.5f, 1, 1, paint);
        c.drawOval(17, 10.5f, 22.5f, 15, paint);
        strokeOval(c, 17, 10.5f, 22.5f, 15, 0.9f);
        paint.setColor(0xFFFFE59D);
        c.drawCircle(20.5f, 12.2f, 0.7f, paint);

        // Carapace dome
        path.rewind();
        path.moveTo(3, 15);
        path.quadTo(4, 4.5f, 11.5f, 4.5f);
        path.quadTo(18.5f, 4.5f, 18.5f, 15);
        path.close();
        paint.setColor(0xFF2D5B47);
        c.drawPath(path, paint);
        // Hexagonal armor plates
        paint.setColor(0xFF5FA67F);
        c.drawOval(8.2f, 6.3f, 14.3f, 11.5f, paint);
        c.drawOval(4.8f, 9.8f, 9, 14, paint);
        c.drawOval(13.5f, 9.8f, 17.5f, 14, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(INK);
        paint.setStrokeWidth(1.0f);
        c.drawPath(path, paint);

        // Plastron rim + glowing jade ward sigil
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFE7D3AF);
        c.drawRoundRect(2.5f, 14, 19, 16.5f, 1, 1, paint);
        paint.setColor(0xFFA7F3D0);
        c.drawCircle(11.3f, 8.8f, 1.0f, paint);
    }

    private void drawOtter(Canvas c) {
        // Water aura arc
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFF7AD6EE);
        paint.setStrokeWidth(1.2f);
        c.drawArc(2.5f, 3, 21.5f, 21, 150, 240, false, paint);

        // Ears & head
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF3E788A);
        c.drawCircle(6.2f, 6.2f, 2.5f, paint);
        c.drawCircle(17.8f, 6.2f, 2.5f, paint);
        strokeCircle(c, 6.2f, 6.2f, 2.5f, 0.85f);
        strokeCircle(c, 17.8f, 6.2f, 2.5f, 0.85f);

        paint.setColor(0xFF4C8C9E);
        c.drawOval(4.5f, 5.2f, 19.5f, 18.5f, paint);
        strokeOval(c, 4.5f, 5.2f, 19.5f, 18.5f, 0.95f);

        // Cream muzzle
        paint.setColor(0xFFF5E6CC);
        c.drawOval(7.2f, 11.2f, 16.8f, 17.6f, paint);
        paint.setColor(INK);
        c.drawCircle(9.2f, 10, 1.0f, paint);
        c.drawCircle(14.8f, 10, 1.0f, paint);
        paint.setColor(0xFF38242B);
        c.drawOval(10.8f, 12.1f, 13.2f, 13.7f, paint);

        // Whiskers
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFFE6D4B8);
        paint.setStrokeWidth(0.75f);
        c.drawLine(4.5f, 13, 8, 13.5f, paint);
        c.drawLine(4.5f, 15, 8, 14.8f, paint);
        c.drawLine(19.5f, 13, 16, 13.5f, paint);
        c.drawLine(19.5f, 15, 16, 14.8f, paint);

        // Glowing water pearl
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x559AEFFF);
        c.drawCircle(12, 19.2f, 3.2f, paint);
        paint.setColor(0xFF8BE8FF);
        c.drawCircle(12, 19.2f, 2.0f, paint);
        paint.setColor(0xFFFFFFFF);
        c.drawCircle(11.3f, 18.5f, 0.7f, paint);
    }

    private void drawMoth(Canvas c) {
        // Upper & lower wings
        fillPoly(c, 0xFF6A498D, true, 11, 10, 3, 3, 2, 9, 6, 14, 11, 13);
        fillPoly(c, 0xFF6A498D, true, 13, 10, 21, 3, 22, 9, 18, 14, 13, 13);
        fillPoly(c, 0xFF543673, true, 11, 13, 5, 15, 7, 21, 11, 17);
        fillPoly(c, 0xFF543673, true, 13, 13, 19, 15, 17, 21, 13, 17);

        // Eye-spots
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFE8C87A);
        c.drawCircle(6.2f, 8.2f, 1.7f, paint);
        c.drawCircle(17.8f, 8.2f, 1.7f, paint);
        paint.setColor(0xFF9C76C8);
        c.drawCircle(6.2f, 8.2f, 0.85f, paint);
        c.drawCircle(17.8f, 8.2f, 0.85f, paint);
        paint.setColor(0xFFFFEEAA);
        c.drawCircle(8.5f, 17, 0.8f, paint);
        c.drawCircle(15.5f, 17, 0.8f, paint);

        // Fuzzy body & golden antennae
        paint.setColor(0xFFF6E7CA);
        c.drawOval(10.2f, 7.5f, 13.8f, 18, paint);
        strokeOval(c, 10.2f, 7.5f, 13.8f, 18, 0.85f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFFFFD87D);
        paint.setStrokeWidth(1.0f);
        c.drawLine(11.3f, 7.5f, 8.5f, 3.8f, paint);
        c.drawLine(12.7f, 7.5f, 15.5f, 3.8f, paint);
    }

    private void drawLynx(Canvas c) {
        // Ember aura wisps
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x66FF8533);
        c.drawCircle(12, 13, 8.5f, paint);

        // Head & tufted ears
        fillPoly(c, 0xFFD96D3B, true, 5, 12, 4, 3.5f, 9.5f, 7.5f, 14.5f, 7.5f, 20, 3.5f, 19, 12, 20.5f, 16.5f, 12, 20.5f, 3.5f, 16.5f);
        // Dark ear tips & cheek stripes
        paint.setColor(0xFF2B1C24);
        fillPoly(c, 0xFF2B1C24, false, 4, 3.5f, 6.2f, 5.2f, 4.5f, 7.2f);
        fillPoly(c, 0xFF2B1C24, false, 20, 3.5f, 17.8f, 5.2f, 19.5f, 7.2f);

        // Cream muzzle
        paint.setColor(0xFFF8E6C8);
        c.drawOval(8, 13.5f, 16, 19.5f, paint);
        paint.setColor(0xFF9E2B2B);
        fillPoly(c, 0xFF9E2B2B, false, 10.8f, 14.8f, 13.2f, 14.8f, 12, 16.4f);

        // Golden eyes & forehead flame stripe
        paint.setColor(0xFFFFDC6B);
        c.drawOval(7.2f, 11.2f, 10.2f, 13.2f, paint);
        c.drawOval(13.8f, 11.2f, 16.8f, 13.2f, paint);
        paint.setColor(INK);
        c.drawLine(8.7f, 11.4f, 8.7f, 13.0f, paint);
        c.drawLine(15.3f, 11.4f, 15.3f, 13.0f, paint);
        paint.setColor(0xFFFFBE5C);
        fillPoly(c, 0xFFFFBE5C, false, 12, 8.5f, 13.4f, 11, 12, 12.2f, 10.6f, 11);
    }

    private void drawClanSeal(Canvas c, int index) {
        // Ornate Kamon Crest with outer ring and clan-specific inner emblem
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(1.35f);
        c.drawCircle(12, 12, 9.6f, paint);
        paint.setStrokeWidth(0.75f);
        c.drawCircle(12, 12, 7.8f, paint);

        paint.setStyle(Paint.Style.FILL);
        switch (index) {
            case 21 -> { // Kagaribi — Sparkglass triple prism
                for (int i = 0; i < 3; i++) {
                    c.save();
                    c.rotate(i * 120f, 12, 12);
                    c.drawCircle(12, 7.2f, 1.7f, paint);
                    line(c, 12, 7.2f, 14.8f, 10, 12, 9.2f);
                    c.restore();
                }
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.0f);
                c.drawCircle(12, 12, 4.8f, paint);
            }
            case 22 -> { // Shinkai — Horizon concentric gravity rings
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.1f);
                c.drawCircle(12, 12, 5.4f, paint);
                c.drawCircle(12, 12, 3.1f, paint);
                paint.setStyle(Paint.Style.FILL);
                c.drawCircle(12, 12, 1.3f, paint);
            }
            case 20 -> { // Clanless — Academy triple diamond
                for (int i = 0; i < 3; i++) {
                    c.save();
                    c.rotate(i * 120f, 12, 12);
                    fillPoly(c, color, false, 12, 5.2f, 14.3f, 8.2f, 12, 10.6f, 9.7f, 8.2f);
                    c.restore();
                }
            }
            default -> {
                int pattern = index % 6;
                int petals = 3 + (index % 3);
                switch (pattern) {
                    case 0 -> { // Radiant blades / rays
                        for (int i = 0; i < petals; i++) {
                            c.save();
                            c.rotate(i * (360f / petals) + index * 10f, 12, 12);
                            fillPoly(c, color, false, 12, 5, 14.5f, 8.8f, 12, 11.2f, 9.5f, 8.8f);
                            c.restore();
                        }
                        c.drawCircle(12, 12, 1.4f, paint);
                    }
                    case 1 -> { // Blossom petals
                        for (int i = 0; i < 4; i++) {
                            c.save();
                            c.rotate(i * 90f + index * 15f, 12, 12);
                            c.drawOval(10.3f, 5.4f, 13.7f, 10.4f, paint);
                            c.restore();
                        }
                        c.drawCircle(12, 12, 1.5f, paint);
                    }
                    case 2 -> { // Crescent / Wave arcs
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(1.45f);
                        c.drawArc(6.5f, 6.5f, 17.5f, 17.5f, 200 + index * 25f, 220, false, paint);
                        c.drawArc(8.8f, 8.8f, 15.2f, 15.2f, 30 + index * 25f, 190, false, paint);
                        paint.setStyle(Paint.Style.FILL);
                        c.drawCircle(12, 12, 1.5f, paint);
                    }
                    case 3 -> { // Twin guard shields / squares
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(1.3f);
                        c.drawRect(8.2f, 8.2f, 15.8f, 15.8f, paint);
                        c.save();
                        c.rotate(45, 12, 12);
                        c.drawRect(8.6f, 8.6f, 15.4f, 15.4f, paint);
                        c.restore();
                    }
                    case 4 -> { // Mountain / Flame peak
                        fillPoly(c, color, false, 12, 5.5f, 17.5f, 15, 14, 15, 12, 11.5f, 10, 15, 6.5f, 15);
                        c.drawCircle(12, 15.8f, 1.3f, paint);
                    }
                    case 5 -> { // Crossed kunai / star knot
                        for (int i = 0; i < 6; i++) {
                            c.save();
                            c.rotate(i * 60f, 12, 12);
                            c.drawRoundRect(11, 5.5f, 13, 10.2f, 0.8f, 0.8f, paint);
                            c.restore();
                        }
                        c.drawCircle(12, 12, 1.7f, paint);
                    }
                }
            }
        }
    }

    private void fillPoly(Canvas c, int fillColor, boolean outline, float... pts) {
        path.rewind();
        path.moveTo(pts[0], pts[1]);
        for (int i = 2; i < pts.length; i += 2) path.lineTo(pts[i], pts[i + 1]);
        path.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fillColor);
        c.drawPath(path, paint);
        if (outline) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(INK);
            paint.setStrokeWidth(0.9f);
            c.drawPath(path, paint);
        }
    }

    private void strokeCircle(Canvas c, float x, float y, float r, float w) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(INK);
        paint.setStrokeWidth(w);
        c.drawCircle(x, y, r, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void strokeOval(Canvas c, float l, float t, float r, float b, float w) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(INK);
        paint.setStrokeWidth(w);
        c.drawOval(l, t, r, b, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void line(Canvas c, float... points) {
        path.rewind();
        path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) path.lineTo(points[i], points[i + 1]);
        c.drawPath(path, paint);
    }
}
