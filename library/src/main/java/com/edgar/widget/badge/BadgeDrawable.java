package com.edgar.widget.badge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.annotation.XmlRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;

/**
 * Created by xiexiang on 2020/6/9.
 * View badge drawable
 **/
public class BadgeDrawable extends Drawable {

    private static final int DEFAULT_STYLE = R.style.DefaultBadge;
    private static final int DEFAULT_THEME_ATTR = R.attr.badgeDrawableStyle;

    private static final int MAX_CIRCULAR_BADGE_NUMBER_COUNT = 9;
    private static final int DEFAULT_MAX_BADGE_NUMBER = 99;
    private static final String DEFAULT_EXCEED_MAX_BADGE_NUMBER_SUFFIX = "+";
    private static final int DEFAULT_BADGE_BACKGROUND_COLOR = 0xFFFF5471;
    private static final int DEFAULT_BADGE_TEXT_COLOR = Color.WHITE;

    public static final int TOP_END = Gravity.TOP | Gravity.END;
    public static final int TOP_START = Gravity.TOP | Gravity.START;
    public static final int BOTTOM_END = Gravity.BOTTOM | Gravity.END;
    public static final int BOTTOM_START = Gravity.BOTTOM | Gravity.START;

    @IntDef({
            TOP_END,
            TOP_START,
            BOTTOM_END,
            BOTTOM_START,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface BadgeGravity {}

    public static final int STYLE_RECTANGLE = 0;
    public static final int STYLE_DOT = 1;

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.FIELD,ElementType.PARAMETER})
    @IntDef({
            STYLE_DOT,
            STYLE_RECTANGLE
    })
    public @interface ShapeStyle {}

    private RectF mBadgeBounds;
    @ColorInt private int mBackgroundColor = DEFAULT_BADGE_BACKGROUND_COLOR;
    private GradientDrawable mShapeDrawable;
    private final TextPaint mBadgeTextPaint;
    @Nullable private WeakReference<View> mAnchorViewRef;

    private Rect mAnchorBounds = new Rect();
    private int mAlpha = 255;
    private float mBadgeRadii;
    private int mBadgeSize;
    private int mBadgeWidth;
    private int mBadgeHeight;
    private boolean mSizeAdjustRadius;
    private int mNumber = 0;
    private int mMaxNumber = DEFAULT_MAX_BADGE_NUMBER;
    @ShapeStyle private int mShapeStyle = STYLE_DOT;
    private int mHorizontalPadding;
    private int mHorizontalOffset;
    private int mVerticalOffset;
    private int mGravity = TOP_END;

    @NonNull
    public static BadgeDrawable create(@NonNull Context context) {
        return createFromAttributes(context, /* attrs= */ null, DEFAULT_THEME_ATTR, DEFAULT_STYLE);
    }

    @NonNull
    public static BadgeDrawable create(@NonNull Context context, @StyleRes int id) {
        return createFromAttributes(context, /* attrs= */ null, DEFAULT_THEME_ATTR, id);
    }

    @SuppressLint("RestrictedApi")
    public static BadgeDrawable createFromResource(@NonNull Context context, @XmlRes int id) {
        AttributeSet attrs = BadgeUtils.parseDrawableXml(context, id, "badge");
        @StyleRes int style = attrs.getStyleAttribute();
        if (style == 0) {
            style = DEFAULT_STYLE;
        }
        return createFromAttributes(context, attrs, DEFAULT_THEME_ATTR, style);
    }

    @NonNull
    private static BadgeDrawable createFromAttributes(
            @NonNull Context context,
            AttributeSet attrs,
            @AttrRes int defStyleAttr,
            @StyleRes int defStyleRes) {
        BadgeDrawable badge = new BadgeDrawable(context);
        badge.loadDefaultStateFromAttributes(context, attrs, defStyleAttr, defStyleRes);
        return badge;
    }

    private BadgeDrawable(Context context) {
        Resources resources = context.getResources();
        mBadgeRadii = resources.getDimensionPixelOffset(R.dimen.default_badge_radius);
        mHorizontalPadding = resources.getDimensionPixelOffset(R.dimen.default_badge_long_text_horizontal_padding);
        mBadgeSize = resources.getDimensionPixelSize(R.dimen.default_badge_size);

        mBadgeBounds = new RectF();
        mShapeDrawable = new GradientDrawable();

        mBadgeTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mBadgeTextPaint.setTextAlign(Paint.Align.CENTER);
        mBadgeTextPaint.setColor(DEFAULT_BADGE_TEXT_COLOR);
        mBadgeTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        setTextSize(resources.getDimensionPixelSize(R.dimen.default_badge_text_size));
    }

    private float calculateTextWidth(@Nullable CharSequence charSequence) {
        if (charSequence == null) {
            return 0f;
        }
        return mBadgeTextPaint.measureText(charSequence, 0, charSequence.length());
    }

    private void loadDefaultStateFromAttributes(
            Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.BadgeDrawable,
                defStyleAttr,defStyleRes);
        setBackgroundColor(ta.getColor(R.styleable.BadgeDrawable_badge_backgroundColor,DEFAULT_BADGE_BACKGROUND_COLOR));

        setBadgeRadii(ta.getDimension(R.styleable.BadgeDrawable_badge_radii,mBadgeRadii));
        setBadgeSize(ta.getDimensionPixelSize(R.styleable.BadgeDrawable_badge_size,mBadgeSize));
        setBadgeWidth(ta.getDimensionPixelOffset(R.styleable.BadgeDrawable_badge_width,-1));
        setBadgeHeight(ta.getDimensionPixelOffset(R.styleable.BadgeDrawable_badge_height,-1));

        setSizeAdjustRadius(ta.getBoolean(R.styleable.BadgeDrawable_sizeAdjustRadii,false));
        setBadgeGravity(ta.getInt(R.styleable.BadgeDrawable_badge_gravity,TOP_END));

        setTextSize(ta.getDimension(R.styleable.BadgeDrawable_badge_textSize,mBadgeTextPaint.getTextSize()));
        setTextColor(ta.getColor(R.styleable.BadgeDrawable_badge_textColor,DEFAULT_BADGE_TEXT_COLOR));

        setHorizontalOffset(ta.getDimensionPixelOffset(R.styleable.BadgeDrawable_badge_horizontalOffset,0));
        setVerticalOffset(ta.getDimensionPixelOffset(R.styleable.BadgeDrawable_badge_verticalOffset,0));
        setHorizontalPadding(ta.getDimensionPixelOffset(R.styleable.BadgeDrawable_badge_horizontalPadding,mHorizontalPadding));

        setNumber(ta.getInt(R.styleable.BadgeDrawable_badge_number,0));
        setMaxNumber(ta.getInt(R.styleable.BadgeDrawable_badge_maxNumber,DEFAULT_MAX_BADGE_NUMBER));

        setShapeStyle(ta.getInt(R.styleable.BadgeDrawable_badge_shape,mShapeStyle));
        setAlpha(ta.getInt(R.styleable.BadgeDrawable_badge_alpha,mAlpha));
        ta.recycle();
    }

    public void setBadgeWidth(int badgeWidth) {
        if (mBadgeWidth != badgeWidth) {
            mBadgeWidth = badgeWidth;
            updateBadgeBounds();
            invalidateSelf();
        }
    }

    public void setBadgeHeight(int badgeHeight) {
        if (mBadgeHeight != badgeHeight) {
            mBadgeHeight = badgeHeight;
            updateBadgeBounds();
            invalidateSelf();
        }
    }

    public void setBadgeGravity(@BadgeGravity int gravity) {
        if (mGravity != gravity) {
            mGravity = gravity;
            updateBadgeBounds();
            invalidateSelf();
        }
    }

    public void setHorizontalPadding(int horizontalPadding) {
        if (mHorizontalPadding != horizontalPadding) {
            mHorizontalPadding = horizontalPadding;
            updateBadgeBounds();
            invalidateSelf();
        }
    }

    public void setHorizontalOffset(int horizontalOffset) {
        if (mHorizontalOffset != horizontalOffset) {
            mHorizontalOffset = horizontalOffset;
            updateBadgeBounds();
            invalidateSelf();
        }
    }

    public void setVerticalOffset(int verticalOffset) {
        if (mVerticalOffset != verticalOffset) {
            mVerticalOffset = verticalOffset;
            updateBadgeBounds();
            invalidateSelf();
        }
    }

    public void setSizeAdjustRadius(boolean sizeAdjustRadius) {
        if (mSizeAdjustRadius != sizeAdjustRadius) {
            mSizeAdjustRadius = sizeAdjustRadius;
            invalidateSelf();
        }
    }

    /**
     * Set badge background shape style
     * @param shapeStyle background shape style
     */
    public void setShapeStyle(@ShapeStyle int shapeStyle) {
        if (mShapeStyle != shapeStyle) {
            mShapeStyle = shapeStyle;
            invalidateSelf();
        }
    }

    /**
     * Set or clear the typeface object.
     * @param typeface May be null. The typeface to be installed in the paint
     */
    public void setTypeface(Typeface typeface) {
        Paint paint = mBadgeTextPaint;
        if (paint.getTypeface() != typeface) {
            paint.setTypeface(typeface);
            invalidateSelf();
        }
    }

    public void setBadgeSize(int badgeSize) {
        if (mBadgeSize != badgeSize) {
            mBadgeSize = badgeSize;
            updateBadgeBounds();
            invalidateSelf();
        }
    }

    /**
     * Set badge radius, badge size radius
     * @param badgeRadii badge radius
     */
    public void setBadgeRadii(float badgeRadii) {
        if (mBadgeRadii != badgeRadii) {
            mBadgeRadii = badgeRadii;
            updateBadgeBounds();
            invalidateSelf();
        }
    }

    public void setMaxNumber(int maxNumber) {
        maxNumber = Math.max(0,maxNumber);
        if (mMaxNumber != maxNumber) {
            mMaxNumber = maxNumber;
            updateBadgeBounds();
            invalidateSelf();
        }
    }

    /**
     * Set badge number
     * @param number number
     */
    public void setNumber(int number) {
        number = Math.max(0, number);
        if (number != mNumber) {
            mNumber = number;
            updateBadgeBounds();
            invalidateSelf();
        }
    }

    /**
     * Set badge text size
     * @param textSize text size
     */
    public void setTextSize(float textSize) {
        if (mBadgeTextPaint.getTextSize() != textSize) {
            mBadgeTextPaint.setTextSize(textSize);
            updateBadgeBounds();
            invalidateSelf();
        }
    }

    /**
     * Set badge text color, regardless of the values of r,g,b
     * @param color text color
     */
    public void setTextColor(@ColorInt int color) {
        Paint textPaint = mBadgeTextPaint;
        if (textPaint.getColor() != color) {
            textPaint.setColor(color);
            invalidateSelf();
        }
    }

    /**
     * Set badge background color
     * @param color background color
     */
    public void setBackgroundColor(@ColorInt int color) {
        if (mBackgroundColor != color) {
            mBackgroundColor = color;
            invalidateSelf();
        }
    }

    public void setVisible(boolean visible) {
        setVisible(visible, /* restart= */ false);
    }

    public void updateBadgeCoordinates(View anchorView) {
        if (mAnchorViewRef == null || mAnchorViewRef.get() != anchorView) {
            mAnchorViewRef = new WeakReference<>(anchorView);
        }
        updateBadgeBounds();
        invalidateSelf();
    }

    private void updateBadgeBounds() {
        View anchorView = mAnchorViewRef == null ? null : mAnchorViewRef.get();
        if (anchorView == null) {
            return;
        }
        anchorView.getDrawingRect(mAnchorBounds);
        int width = mBadgeWidth > 0 ? mBadgeWidth : mBadgeSize;
        int height = mBadgeHeight > 0 ? mBadgeHeight : mBadgeSize;
        if (mBadgeWidth <= 0 && hasNumber() && mNumber > MAX_CIRCULAR_BADGE_NUMBER_COUNT) {
            width = (int) (calculateTextWidth(getNumberText()) + mHorizontalPadding);
        }

        int left;
        int top;
        switch (mGravity) {
            case TOP_START:
            case BOTTOM_START:
                left = mAnchorBounds.left + mHorizontalOffset;
                break;
            case TOP_END:
            case BOTTOM_END:
            default:
                left = mAnchorBounds.right - width + mHorizontalOffset;
                break;
        }
        switch (mGravity) {
            case BOTTOM_START:
            case BOTTOM_END:
                top = mAnchorBounds.bottom - height + mVerticalOffset;
                break;
            case TOP_START:
            case TOP_END:
            default:
                top =  mAnchorBounds.top + mVerticalOffset;
                break;
        }
        mBadgeBounds.set(left, top, left+width, top+height);
    }

    private String getNumberText() {
        String numberText = String.valueOf(mNumber);
        if (mNumber > mMaxNumber) {
            numberText = mMaxNumber+DEFAULT_EXCEED_MAX_BADGE_NUMBER_SUFFIX;
        }
        return numberText;
    }

    private boolean hasNumber() {
        return mNumber > 0;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (!isVisible()) {
            return;
        }
        mShapeDrawable.setAlpha(mAlpha);
        mShapeDrawable.setColor(mBackgroundColor);
        mShapeDrawable.setBounds(
                (int) mBadgeBounds.left,
                (int) mBadgeBounds.top,
                (int) mBadgeBounds.right,
                (int) mBadgeBounds.bottom);
        switch (mShapeStyle) {
            case STYLE_DOT:
                mShapeDrawable.setShape(GradientDrawable.OVAL);
                break;
            case STYLE_RECTANGLE:
                mShapeDrawable.setShape(GradientDrawable.RECTANGLE);
                break;
            default:
                break;
        }
        float radii = mBadgeRadii;
        if (mSizeAdjustRadius) {
            radii = Math.min(mBadgeBounds.width(), mBadgeBounds.height())/2f;
        }
        mShapeDrawable.setCornerRadius(radii);
        mShapeDrawable.draw(canvas);
        if (hasNumber()) {
            drawText(canvas);
        }
    }

    private void drawText(@NonNull Canvas canvas) {
        final RectF bounds = mBadgeBounds;
        final Paint textPaint = mBadgeTextPaint;
        textPaint.setAlpha(mAlpha);
        String numberText = getNumberText();
        textPaint.measureText(numberText,0,numberText.length());
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float centerY = bounds.centerY()- fontMetrics.descent + (fontMetrics.descent-fontMetrics.ascent)/2f;
        canvas.drawText(numberText, bounds.centerX(), centerY,textPaint);
    }

    /**
     * Specify an alpha value for the drawable. 0 means fully transparent, and
     * 255 means fully opaque.
     * @param alpha set the alpha component [0..255] of the paint's color.
     */
    @Override
    public void setAlpha(@IntRange(from = 0,to = 255) int alpha) {
        if (mAlpha != alpha) {
            mAlpha = alpha;
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        // Intentionally empty.
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public boolean onStateChange(int[] state) {
        return super.onStateChange(state);
    }
}
