package com.blingbling.trianglebuttonview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by BlingBling on 2017/12/4.
 */

public class TriangleButtonView extends View {

    private static final String DEFAULT_SPLIT = ",";
    private static final int DEFAULT_TEXT_SIZE = 14;
    private static final int DEFAULT_TEXT_PADDING = 6;
    private static final int DEFAULT_BUTTON_WIDTH = 100;
    private static final int DEFAULT_BUTTON_HEIGHT = 60;
    //    //按钮文字大小、颜色
    private ColorStateList mTextColor;
    private int mCurTextColor;
    //按钮背景
    private Drawable mButtonBackground;
    private Drawable mLeftButtonBackground;
    private Drawable mRightButtonBackground;
    //按钮大小
    private int mButtonWidth;
    private int mButtonHeight;
    private int mTextPadding;

    private String[] mButton;

    private Paint mTextPaint;
    private Rect mButtonRect = new Rect();
    private Path mTrianglePath = new Path();
    private int mSelectedIndex = -1;//选中位置
    private int mPressedIndex = -1;//按下位置
    private OnItemClickListener mOnItemClickListener;

    public TriangleButtonView(Context context) {
        this(context, null);
    }

    public TriangleButtonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TriangleButtonView);
        final String buttonStr = a.getString(R.styleable.TriangleButtonView_tbv_button);
        final int textSize = a.getDimensionPixelSize(R.styleable.TriangleButtonView_tbv_textSize, sp2Px(DEFAULT_TEXT_SIZE, metrics));
        mTextColor = a.getColorStateList(R.styleable.TriangleButtonView_tbv_textColor);
        mTextPadding = a.getDimensionPixelSize(R.styleable.TriangleButtonView_tbv_textPadding, dp2Px(DEFAULT_TEXT_PADDING, metrics));
        mButtonWidth = a.getDimensionPixelSize(R.styleable.TriangleButtonView_tbv_buttonWidth, dp2Px(DEFAULT_BUTTON_WIDTH, metrics));
        mButtonHeight = a.getDimensionPixelSize(R.styleable.TriangleButtonView_tbv_buttonHeight, dp2Px(DEFAULT_BUTTON_HEIGHT, metrics));

        mButtonBackground = a.getDrawable(R.styleable.TriangleButtonView_tbv_buttonBackground);
        mLeftButtonBackground = a.getDrawable(R.styleable.TriangleButtonView_tbv_leftButtonBackground);
        mRightButtonBackground = a.getDrawable(R.styleable.TriangleButtonView_tbv_rightButtonBackground);
        a.recycle();
        initButton(buttonStr);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    private int sp2Px(int sp, DisplayMetrics metrics) {
        return (int) (sp * metrics.scaledDensity);
    }

    private int dp2Px(int dp, DisplayMetrics metrics) {
        return (int) (dp * metrics.density);
    }

    private void initButton(String button) {
        if (!TextUtils.isEmpty(button)) {
            String[] btnArray = button.split(DEFAULT_SPLIT);
            setButton(btnArray);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mButton != null && mButton.length != 0) {
            setMeasuredDimension(mButtonWidth, mButtonHeight + mButtonHeight / 2 * (mButton.length - 1));
        } else {
            setMeasuredDimension(0, 0);
        }
    }

    public void setButton(String... button) {
        mButton = button;
        requestLayout();
    }

    public void setSelected(int index) {
        mSelectedIndex = index;
        invalidate();
    }

    /**
     * 设置按钮文字大小（单位sp）
     *
     * @param textSize
     */
    public void setTextSize(int textSize) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    public void setTextColor(@ColorInt int color) {
        mTextColor = ColorStateList.valueOf(color);
        invalidate();
    }

    public void setTextColor(ColorStateList colors) {
        if (colors == null) {
            throw new NullPointerException();
        }
        mTextColor = colors;
        invalidate();
    }

    private void setTextSize(int unit, int textSize) {
        if (mTextPaint == null) {
            mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        mTextPaint.setTextSize(TypedValue.applyDimension(unit, textSize, getResources().getDisplayMetrics()));
        invalidate();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int len = mButton == null ? 0 : mButton.length;
        if (len == 0) {
            return;
        }
        final float textMoveY = (mButtonHeight - (mTextPaint.descent() - mTextPaint.ascent())) / 2 - mTextPaint.ascent();
        for (int i = 0; i < len; i++) {
            final boolean left = i % 2 == 0;
            Drawable drawable = left ? mLeftButtonBackground : mRightButtonBackground;
            if (drawable == null) {
                drawable = mButtonBackground;
            }
            final Rect buttonRect = getButtonRect(i);
            final int[] state = getButtonState(i);
            if (drawable != null) {
                canvas.save();
                canvas.clipPath(getTrianglePath(i));
                drawable.setState(state);
                drawable.setBounds(buttonRect);
                drawable.draw(canvas);
                canvas.restore();
            }
            //draw text
            if (mTextColor != null) {
                mCurTextColor = mTextColor.getColorForState(state, 0);
                mTextPaint.setColor(mCurTextColor);
            }
            float x = mTextPadding;
            final String text = mButton[i];
            if (!left) {
                x = mButtonWidth - mTextPadding - mTextPaint.measureText(text);
            }
            canvas.drawText(text, x, buttonRect.top + textMoveY, mTextPaint);
        }
    }

    /**
     * 按钮位置
     *
     * @param index
     * @return
     */
    private Rect getButtonRect(int index) {
        mButtonRect.left = 0;
        mButtonRect.right = mButtonWidth;
        mButtonRect.top = mButtonHeight / 2 * index;
        mButtonRect.bottom = mButtonRect.top + mButtonHeight;
        return mButtonRect;
    }

    /**
     * 按钮裁剪区域
     *
     * @param index
     * @return
     */
    private Path getTrianglePath(int index) {
        mTrianglePath.reset();
        final int half = mButtonHeight / 2;
        final int start = index * half;
        if (index % 2 == 0) {
            mTrianglePath.moveTo(0, start);
            mTrianglePath.lineTo(0, start + mButtonHeight);
            mTrianglePath.lineTo(mButtonWidth, start + half);
            mTrianglePath.close();
        } else {
            mTrianglePath.moveTo(mButtonWidth, start);
            mTrianglePath.lineTo(mButtonWidth, start + mButtonHeight);
            mTrianglePath.lineTo(0, start + half);
            mTrianglePath.close();
        }
        return mTrianglePath;
    }

    private int[] getButtonState(int index) {
        if (index == mSelectedIndex) {
            return ENABLED_SELECTED_STATE_SET;
        }
        if (index == mPressedIndex) {
            return PRESSED_ENABLED_STATE_SET;
        }
        return EMPTY_STATE_SET;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mPressedIndex = getPressedIndex((int) event.getX(),
                        (int) event.getY());
                if (mPressedIndex != -1) {
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mPressedIndex != -1) {
                    final int upIndex = getPressedIndex((int) event.getX(),
                            (int) event.getY());
                    if (mPressedIndex == upIndex && mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(this, mPressedIndex);
                    }
                    mPressedIndex = -1;
                    invalidate();
                }
                break;
        }
        return true;
    }

    /**
     * 获取点击的位置
     *
     * @param x
     * @param y
     * @return
     */
    private int getPressedIndex(int x, int y) {
        final int count = mButton == null ? 0 : mButton.length;
        if (count == 0) {
            return -1;
        }
        final Point p = new Point(x, y);
        int index = y / (mButtonHeight / 2);
        //点击的位置可能为  index-1 或 index
        if (!isInTriangle(index, p)) {
            index = index - 1;
        }
        if (index == count) {
            index = -1;
        }
        return index;
    }

    /**
     * 判断点击的位置是否在三角形点击区域内
     *
     * @param index
     * @param p
     * @return
     */
    private boolean isInTriangle(int index, Point p) {
        final int half = mButtonHeight / 2;
        final int start = index * half;
        if (index % 2 == 0) {
            return isInTriangle(new Point(0, start),
                    new Point(0, start + mButtonHeight),
                    new Point(mButtonWidth, start + half),
                    p);
        } else {
            return isInTriangle(new Point(mButtonWidth, start),
                    new Point(mButtonWidth, start + mButtonHeight),
                    new Point(0, start + half),
                    p);
        }
    }

    private boolean isInTriangle(Point A, Point B, Point C, Point P) {
         /*利用叉乘法进行判断,假设P点就是M点*/
        int a, b, c;

        /*向量减法*/
        Point MA = new Point(P.x - A.x, P.y - A.y);
        Point MB = new Point(P.x - B.x, P.y - B.y);
        Point MC = new Point(P.x - C.x, P.y - C.y);

        /*向量叉乘*/
        a = MA.x * MB.y - MA.y * MB.x;
        b = MB.x * MC.y - MB.y * MC.x;
        c = MC.x * MA.y - MC.y * MA.x;

        if ((a <= 0 && b <= 0 && c <= 0) || (a > 0 && b > 0 && c > 0)) {
            return true;
        }
        return false;
    }

    public interface OnItemClickListener {
        void onItemClick(TriangleButtonView view, int index);
    }
}