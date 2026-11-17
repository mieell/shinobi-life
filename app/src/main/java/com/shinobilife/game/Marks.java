package com.shinobilife.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

@android.annotation.SuppressLint("ViewConstructor")
final class Marks extends View {
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
        c.scale(size / 24, size / 24);
        paint.setColor(color);
        paint.setStrokeWidth(1.65f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        switch (mark) {
            case "life":
                line(c, 3, 10, 12, 3, 21, 10, 19, 10, 19, 20, 14, 20, 14, 14, 10, 14, 10, 20, 5, 20, 5, 10, 3, 10);
                break;
            case "missions":
                c.drawRoundRect(5, 4, 19, 21, 2, 2, paint);
                line(c, 9, 8, 15, 8);
                line(c, 9, 12, 15, 12);
                line(c, 9, 16, 13, 16);
                break;
            case "clans":
                line(c, 12, 2, 21, 7, 21, 17, 12, 22, 3, 17, 3, 7, 12, 2);
                line(c, 12, 7, 17, 12, 12, 17, 7, 12, 12, 7);
                break;
            case "people":
                c.drawCircle(9, 8, 3, paint);
                c.drawArc(3, 13, 15, 23, 180, 180, false, paint);
                c.drawArc(13, 5, 20, 11, 270, 180, false, paint);
                c.drawArc(13, 13, 22, 22, 270, 90, false, paint);
                break;
            case "training":
                line(c, 3, 18, 18, 3, 21, 3, 21, 6, 6, 21, 3, 18);
                line(c, 6, 15, 9, 18);
                break;
            case "spark":
                line(c, 12, 2, 14.8f, 9, 22, 12, 14.8f, 15, 12, 22, 9, 15, 2, 12, 9, 9, 12, 2);
                break;
            case "coin":
                c.drawCircle(12, 12, 9, paint);
                line(c, 12, 7, 16, 12, 12, 17, 8, 12, 12, 7);
                break;
            default:
                int index = Integer.parseInt(mark);
                paint.setStyle(Paint.Style.FILL);
                for (int i = 0; i < 3; i++) {
                    c.save();
                    c.rotate(i * 120f + index * 15f, 12, 12);
                    if (index % 3 == 0) line(c, 12, 2.5f, 15.6f, 8, 12, 11, 8.4f, 8, 12, 2.5f);
                    if (index % 3 == 1) c.drawRoundRect(10 - index % 2, 3, 14 + index % 2, 10, 1, 1, paint);
                    if (index % 3 == 2) line(c, 12, 2.5f, 16.5f, 8, 12, 7, 7.5f, 8, 12, 2.5f);
                    c.restore();
                }
                c.drawCircle(12, 12, 1.2f + index % 2 * .5f, paint);
        }
        c.restore();
    }

    private void line(Canvas c, float... points) {
        path.rewind();
        path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) path.lineTo(points[i], points[i + 1]);
        c.drawPath(path, paint);
    }
}
