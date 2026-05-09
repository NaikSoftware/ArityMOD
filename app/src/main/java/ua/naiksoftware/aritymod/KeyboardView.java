// Copyright (C) 2009 Mihai Preda
package ua.naiksoftware.aritymod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

public class KeyboardView extends View {

    private static final float CELL_GAP_DP = 4f;
    private static final float CELL_RADIUS_DP = 12f;
    private static final float PRESS_STROKE_DP = 2f;
    private static final float DELTAY = 8f;

    private static final int ROLE_LETTER = 0;
    private static final int ROLE_DIGIT = 1;
    private static final int ROLE_OPERATOR = 2;
    private static final int ROLE_ACTION_SECONDARY = 3;
    private static final int ROLE_ACTION_PRIMARY = 4;

    private final Context context;
    private final Paint cellBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pressFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pressStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cellRect = new RectF();

    private char[][] keys;
    private int nLine, nCol;
    private int width, height;
    private float cellw, cellh;
    private float density, scaledDensity;
    private float gapPx, radiusPx;
    private Bitmap bitmap;
    private boolean isDown;
    private float downX, downY;
    private int downLine, downCol;
    private float downCW, downCH;
    private boolean isLarge, isBottom;
    private KeyboardView aboveView;
    private KeyboardListener keyboardListener;

    private int[] roleBg;
    private int[] roleFg;
    private int surfaceColor;
    private int pressFillColor;
    private int pressStrokeColor;

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        pressStrokePaint.setStyle(Paint.Style.STROKE);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    void init(char[][] keys, boolean isLarge, boolean isBottom, KeyboardListener listener) {
        this.keys = keys;
        nLine = keys.length;
        nCol = keys[0].length;
        this.isLarge = isLarge;
        this.isBottom = isBottom;
        keyboardListener = listener;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        density = dm.density;
        scaledDensity = dm.scaledDensity;
        gapPx = density * CELL_GAP_DP;
        radiusPx = density * CELL_RADIUS_DP;
        resolveThemeColors();
        pressStrokePaint.setStrokeWidth(density * PRESS_STROKE_DP);
    }

    void setAboveView(KeyboardView aboveView) {
        this.aboveView = aboveView;
    }

    @SuppressLint("ResourceType")
    private void resolveThemeColors() {
        int[] attrs = {
                com.google.android.material.R.attr.colorSurfaceContainer,        // 0 letter bg
                com.google.android.material.R.attr.colorPrimaryContainer,        // 1 digit bg
                com.google.android.material.R.attr.colorSecondaryContainer,      // 2 operator bg
                com.google.android.material.R.attr.colorTertiaryContainer,       // 3 action_secondary bg
                com.google.android.material.R.attr.colorPrimary,                 // 4 action_primary bg
                com.google.android.material.R.attr.colorOnSurface,               // 5 letter fg
                com.google.android.material.R.attr.colorOnPrimaryContainer,      // 6 digit fg
                com.google.android.material.R.attr.colorOnSecondaryContainer,    // 7 operator fg
                com.google.android.material.R.attr.colorOnTertiaryContainer,     // 8 action_secondary fg
                com.google.android.material.R.attr.colorOnPrimary,               // 9 action_primary fg
                com.google.android.material.R.attr.colorSurface,                 // 10 backdrop
        };
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);
        roleBg = new int[5];
        roleFg = new int[5];
        for (int i = 0; i < 5; i++) {
            roleBg[i] = ta.getColor(i, 0xff808080);
            roleFg[i] = ta.getColor(i + 5, 0xffffffff);
        }
        surfaceColor = ta.getColor(10, 0xffffffff);
        ta.recycle();
        int onSurface = roleFg[ROLE_LETTER];
        pressFillColor = (onSurface & 0x00FFFFFF) | 0x1F000000;     // ~12% alpha
        pressStrokeColor = (onSurface & 0x00FFFFFF) | 0x66000000;   // ~40% alpha
        pressStrokePaint.setColor(pressStrokeColor);
        pressFillPaint.setColor(pressFillColor);
    }

    private int roleFor(char c) {
        if (c == 'E') return ROLE_ACTION_PRIMARY;
        if (c == 'C' || c == 'A') return ROLE_ACTION_SECONDARY;
        if (('0' <= c && c <= '9') || c == '.') return ROLE_DIGIT;
        if (('a' <= c && c <= 'z') || c == ' ' || c == MainActivity.PI) return ROLE_LETTER;
        return ROLE_OPERATOR;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w;
        height = h;
        if (w <= 0 || h <= 0 || keys == null) return;

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(surfaceColor);

        cellw = width / (float) nCol;
        cellh = height / (float) nLine;

        textPaint.setTextSize((isLarge ? 22f : 18f) * scaledDensity);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textCenterOffset = -(fm.ascent + fm.descent) / 2f;

        for (int line = 0; line < nLine; ++line) {
            float y1 = getY(line);
            char[] lineKeys = keys[line];
            for (int col = 0; col < nCol; ++col) {
                float x1 = getX(col);
                char c = lineKeys[col];
                if ((col > 0 && c == lineKeys[col - 1]) || (line > 0 && c == keys[line - 1][col])) {
                    continue;
                }
                float cw = col < nCol - 1 && c == lineKeys[col + 1] ? cellw + cellw : cellw;
                float ch = line < nLine - 1 && c == keys[line + 1][col] ? cellh + cellh : cellh;
                int role = roleFor(c);

                cellBgPaint.setColor(roleBg[role]);
                cellRect.set(x1 + gapPx / 2f, y1 + gapPx / 2f,
                        x1 + cw - gapPx / 2f, y1 + ch - gapPx / 2f);
                canvas.drawRoundRect(cellRect, radiusPx, radiusPx, cellBgPaint);

                int fgColor = roleFg[role];
                switch (c) {
                    case 'E':
                        drawIconTinted(canvas, R.drawable.ic_kb_enter, x1, y1, cw, ch, fgColor);
                        break;
                    case 'C':
                        drawIconTinted(canvas, R.drawable.ic_kb_backspace, x1, y1, cw, ch, fgColor);
                        break;
                    case 'A':
                        drawIconTinted(canvas, R.drawable.ic_kb_arrow_up, x1, y1, cw, ch, fgColor);
                        break;
                    default:
                        textPaint.setColor(fgColor);
                        canvas.drawText(lineKeys, col, 1,
                                x1 + cw / 2f, y1 + ch / 2f + textCenterOffset, textPaint);
                        break;
                }
            }
        }
    }

    private void drawIconTinted(Canvas canvas, int id, float x, float y, float cw, float ch, int tint) {
        Drawable d = AppCompatResources.getDrawable(context, id);
        if (d == null) return;
        d = d.mutate();
        DrawableCompat.setTint(d, tint);
        int target = (int) (Math.min(cw, ch) * 0.45f);
        int x1 = Math.round(x + (cw - target) / 2f);
        int y1 = Math.round(y + (ch - target) / 2f);
        d.setBounds(x1, y1, x1 + target, y1 + target);
        d.draw(canvas);
    }

    private float getY(int line) {
        return line * height / (float) nLine;
    }

    private float getX(int col) {
        return col * width / (float) nCol;
    }

    private int getLine(float y) {
        int line = (int) (y * nLine / height);
        if (line < 0) line = 0;
        else if (line >= nLine) line = nLine - 1;
        return line;
    }

    private int getCol(float x) {
        int col = (int) (x * nCol / width);
        if (col < 0) col = 0;
        else if (col >= nCol) col = nCol - 1;
        return col;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
        if (isDown) {
            float x1 = getX(downCol);
            float y1 = getY(downLine);
            cellRect.set(x1 + gapPx / 2f, y1 + gapPx / 2f,
                    x1 + downCW - gapPx / 2f, y1 + downCH - gapPx / 2f);
            canvas.drawRoundRect(cellRect, radiusPx, radiusPx, pressFillPaint);
            canvas.drawRoundRect(cellRect, radiusPx, radiusPx, pressStrokePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            float y = event.getY();
            if (y < DELTAY && aboveView != null) {
                event.offsetLocation(0, aboveView.getHeight() - DELTAY);
                aboveView.onTouchEvent(event);
            } else {
                isDown = true;
                downY = y >= DELTAY ? y - DELTAY : 0;
                downLine = getLine(downY);
                downCol = getCol(downX);
                downCW = cellw;
                downCH = cellh;
                /* Merged-cell touch normalization (large '0' and large Backspace). */
                if (downLine == 3 && downCol <= 1 && isLarge) { // Large '0' button
                    downCol = 0;
                    downCW = cellw + cellw;
                } else if (downCol == 5 && downLine >= 1 && downLine <= 2 && isLarge) { // Large remove
                    downLine = 1;
                    downCH = cellh + cellh;
                }
                invalidateCell(downLine, downCol);
                char key = keys[downLine][downCol];
                keyboardListener.onKeyboardClicked(key);
            }
        } else if (action == MotionEvent.ACTION_UP) {
            if (isDown) {
                isDown = false;
                invalidateCell(downLine, downCol);
            } else if (aboveView != null) {
                aboveView.onTouchEvent(event);
            }
        } else {
            return false;
        }
        return true;
    }

    private void invalidateCell(int line, int col) {
        float x1 = getX(col);
        float y1 = getY(line);
        int x2 = (int) (x1 + downCW + 1);
        int y2 = (int) (y1 + downCH + 1);
        invalidate((int) x1, (int) y1, x2, y2);
    }

    public interface KeyboardListener {
        void onKeyboardClicked(char key);
    }
}
