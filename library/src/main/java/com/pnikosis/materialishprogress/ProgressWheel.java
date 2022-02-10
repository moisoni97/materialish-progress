package com.pnikosis.materialishprogress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

public class ProgressWheel extends View {
    private final int barLength = 16;

    private int circleRadius = 28;
    private int barWidth = 4;
    private int rimWidth = 4;
    private boolean fillRadius = false;
    private double timeStartGrowing = 0;
    private double barSpinCycleTime = 460;
    private float barExtraLength = 0;
    private boolean barGrowingFromFront = true;
    private long pausedTimeWithoutGrowing = 0;
    private int barColor = 0xAA000000;
    private int rimColor = 0x00FFFFFF;

    private final Paint barPaint = new Paint();
    private final Paint rimPaint = new Paint();

    private RectF circleBounds = new RectF();

    private float spinSpeed = 230.0f;

    private long lastTimeAnimated = 0;

    private boolean linearProgress;

    private float mProgress = 0.0f;
    private float mTargetProgress = 0.0f;
    private boolean isSpinning = false;

    private ProgressCallback callback;

    private boolean shouldAnimate;

    public ProgressWheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context.obtainStyledAttributes(attrs, R.styleable.ProgressWheel));
        setAnimationEnabled();
    }

    public ProgressWheel(Context context) {
        super(context);
        setAnimationEnabled();
    }

    private void setAnimationEnabled() {
        float animationValue;
        animationValue = Settings.Global.getFloat(getContext().getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1);
        shouldAnimate = animationValue != 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int viewWidth = circleRadius + this.getPaddingLeft() + this.getPaddingRight();
        int viewHeight = circleRadius + this.getPaddingTop() + this.getPaddingBottom();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(viewWidth, widthSize);
        } else {
            width = viewWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(viewHeight, heightSize);
        } else {
            height = viewHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        setupBounds(width, height);
        setupPaints();
        invalidate();
    }

    private void setupPaints() {
        barPaint.setColor(barColor);
        barPaint.setAntiAlias(true);
        barPaint.setStyle(Style.STROKE);
        barPaint.setStrokeWidth(barWidth);

        rimPaint.setColor(rimColor);
        rimPaint.setAntiAlias(true);
        rimPaint.setStyle(Style.STROKE);
        rimPaint.setStrokeWidth(rimWidth);
    }

    private void setupBounds(int layout_width, int layout_height) {
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();

        if (!fillRadius) {
            int minValue = Math.min(layout_width - paddingLeft - paddingRight, layout_height - paddingBottom - paddingTop);

            int circleDiameter = Math.min(minValue, circleRadius * 2 - barWidth * 2);

            int xOffset = (layout_width - paddingLeft - paddingRight - circleDiameter) / 2 + paddingLeft;
            int yOffset = (layout_height - paddingTop - paddingBottom - circleDiameter) / 2 + paddingTop;

            circleBounds = new RectF(xOffset + barWidth, yOffset + barWidth, xOffset + circleDiameter - barWidth,
                    yOffset + circleDiameter - barWidth);
        } else {
            circleBounds = new RectF(paddingLeft + barWidth, paddingTop + barWidth,
                    layout_width - paddingRight - barWidth, layout_height - paddingBottom - barWidth);
        }
    }

    private void parseAttributes(TypedArray array) {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();

        barWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, barWidth, metrics);
        rimWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, rimWidth, metrics);
        circleRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, circleRadius, metrics);

        circleRadius = (int) array.getDimension(R.styleable.ProgressWheel_matProg_circleRadius, circleRadius);

        fillRadius = array.getBoolean(R.styleable.ProgressWheel_matProg_fillRadius, false);

        barWidth = (int) array.getDimension(R.styleable.ProgressWheel_matProg_barWidth, barWidth);

        rimWidth = (int) array.getDimension(R.styleable.ProgressWheel_matProg_rimWidth, rimWidth);

        float baseSpinSpeed = array.getFloat(R.styleable.ProgressWheel_matProg_spinSpeed, spinSpeed / 360.0f);

        spinSpeed = baseSpinSpeed * 360;

        barSpinCycleTime = array.getInt(R.styleable.ProgressWheel_matProg_barSpinCycleTime, (int) barSpinCycleTime);

        barColor = array.getColor(R.styleable.ProgressWheel_matProg_barColor, barColor);

        rimColor = array.getColor(R.styleable.ProgressWheel_matProg_rimColor, rimColor);

        linearProgress = array.getBoolean(R.styleable.ProgressWheel_matProg_linearProgress, false);

        if (array.getBoolean(R.styleable.ProgressWheel_matProg_progressIndeterminate, false)) {
            spin();
        }

        array.recycle();
    }

    public void setCallback(ProgressCallback progressCallback) {
        callback = progressCallback;

        if (!isSpinning) {
            runCallback();
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawArc(circleBounds, 360, 360, false, rimPaint);

        boolean mustInvalidate = false;

        if (!shouldAnimate) {
            return;
        }

        if (isSpinning) {
            mustInvalidate = true;

            long deltaTime = (SystemClock.uptimeMillis() - lastTimeAnimated);
            float deltaNormalized = deltaTime * spinSpeed / 1000.0f;

            updateBarLength(deltaTime);

            mProgress += deltaNormalized;
            if (mProgress > 360) {
                mProgress -= 360f;

                runCallbackValue();
            }
            lastTimeAnimated = SystemClock.uptimeMillis();

            float from = mProgress - 90;
            float length = barLength + barExtraLength;

            if (isInEditMode()) {
                from = 0;
                length = 135;
            }

            canvas.drawArc(circleBounds, from, length, false, barPaint);
        } else {
            float oldProgress = mProgress;

            if (mProgress != mTargetProgress) {
                mustInvalidate = true;

                float deltaTime = (float) (SystemClock.uptimeMillis() - lastTimeAnimated) / 1000;
                float deltaNormalized = deltaTime * spinSpeed;

                mProgress = Math.min(mProgress + deltaNormalized, mTargetProgress);
                lastTimeAnimated = SystemClock.uptimeMillis();
            }

            if (oldProgress != mProgress) {
                runCallback();
            }

            float offset = 0.0f;
            float progress = mProgress;
            if (!linearProgress) {
                float factor = 2.0f;
                offset = (float) (1.0f - Math.pow(1.0f - mProgress / 360.0f, 2.0f * factor)) * 360.0f;
                progress = (float) (1.0f - Math.pow(1.0f - mProgress / 360.0f, factor)) * 360.0f;
            }

            if (isInEditMode()) {
                progress = 360;
            }

            canvas.drawArc(circleBounds, offset - 90, progress, false, barPaint);
        }

        if (mustInvalidate) {
            invalidate();
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if (visibility == VISIBLE) {
            lastTimeAnimated = SystemClock.uptimeMillis();
        }
    }

    private void updateBarLength(long deltaTimeInMilliSeconds) {
        long pauseGrowingTime = 200;
        if (pausedTimeWithoutGrowing >= pauseGrowingTime) {
            timeStartGrowing += deltaTimeInMilliSeconds;

            if (timeStartGrowing > barSpinCycleTime) {
                timeStartGrowing -= barSpinCycleTime;
                pausedTimeWithoutGrowing = 0;
                barGrowingFromFront = !barGrowingFromFront;
            }

            float distance = (float) Math.cos((timeStartGrowing / barSpinCycleTime + 1) * Math.PI) / 2 + 0.5f;
            int barMaxLength = 270;
            float destLength = (barMaxLength - barLength);

            if (barGrowingFromFront) {
                barExtraLength = distance * destLength;
            } else {
                float newLength = destLength * (1 - distance);
                mProgress += (barExtraLength - newLength);
                barExtraLength = newLength;
            }
        } else {
            pausedTimeWithoutGrowing += deltaTimeInMilliSeconds;
        }
    }

    public boolean isSpinning() {
        return isSpinning;
    }

    public void resetCount() {
        mProgress = 0.0f;
        mTargetProgress = 0.0f;
        invalidate();
    }

    public void stopSpinning() {
        isSpinning = false;
        mProgress = 0.0f;
        mTargetProgress = 0.0f;
        invalidate();
    }

    public void spin() {
        lastTimeAnimated = SystemClock.uptimeMillis();
        isSpinning = true;
        invalidate();
    }

    private void runCallbackValue() {
        if (callback != null) {
            callback.onProgressUpdate((float) -1.0);
        }
    }

    private void runCallback() {
        if (callback != null) {
            float normalizedProgress = (float) Math.round(mProgress * 100 / 360.0f) / 100;
            callback.onProgressUpdate(normalizedProgress);
        }
    }

    public void setInstantProgress(float progress) {
        if (isSpinning) {
            mProgress = 0.0f;
            isSpinning = false;
        }

        if (progress > 1.0f) {
            progress -= 1.0f;
        } else if (progress < 0) {
            progress = 0;
        }

        if (progress == mTargetProgress) {
            return;
        }

        mTargetProgress = Math.min(progress * 360.0f, 360.0f);
        mProgress = mTargetProgress;
        lastTimeAnimated = SystemClock.uptimeMillis();
        invalidate();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        WheelSavedState wheelSavedState = new WheelSavedState(superState);
        wheelSavedState.mProgress = this.mProgress;
        wheelSavedState.mTargetProgress = this.mTargetProgress;
        wheelSavedState.isSpinning = this.isSpinning;
        wheelSavedState.spinSpeed = this.spinSpeed;
        wheelSavedState.barWidth = this.barWidth;
        wheelSavedState.barColor = this.barColor;
        wheelSavedState.rimWidth = this.rimWidth;
        wheelSavedState.rimColor = this.rimColor;
        wheelSavedState.circleRadius = this.circleRadius;
        wheelSavedState.linearProgress = this.linearProgress;
        wheelSavedState.fillRadius = this.fillRadius;

        return wheelSavedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof WheelSavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        WheelSavedState ss = (WheelSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        this.mProgress = ss.mProgress;
        this.mTargetProgress = ss.mTargetProgress;
        this.isSpinning = ss.isSpinning;
        this.spinSpeed = ss.spinSpeed;
        this.barWidth = ss.barWidth;
        this.barColor = ss.barColor;
        this.rimWidth = ss.rimWidth;
        this.rimColor = ss.rimColor;
        this.circleRadius = ss.circleRadius;
        this.linearProgress = ss.linearProgress;
        this.fillRadius = ss.fillRadius;

        this.lastTimeAnimated = SystemClock.uptimeMillis();
    }

    public float getProgress() {
        return isSpinning ? -1 : mProgress / 360.0f;
    }

    public void setProgress(float progress) {
        if (isSpinning) {
            mProgress = 0.0f;
            isSpinning = false;

            runCallback();
        }

        if (progress > 1.0f) {
            progress -= 1.0f;
        } else if (progress < 0) {
            progress = 0;
        }

        if (progress == mTargetProgress) {
            return;
        }

        if (mProgress == mTargetProgress) {
            lastTimeAnimated = SystemClock.uptimeMillis();
        }

        mTargetProgress = Math.min(progress * 360.0f, 360.0f);

        invalidate();
    }

    public void setLinearProgress(boolean isLinear) {
        linearProgress = isLinear;
        if (!isSpinning) {
            invalidate();
        }
    }

    public int getCircleRadius() {
        return circleRadius;
    }

    public void setCircleRadius(int circleRadius) {
        this.circleRadius = circleRadius;
        if (!isSpinning) {
            invalidate();
        }
    }

    public int getBarWidth() {
        return barWidth;
    }

    public void setBarWidth(int barWidth) {
        this.barWidth = barWidth;
        if (!isSpinning) {
            invalidate();
        }
    }

    public int getBarColor() {
        return barColor;
    }

    public void setBarColor(int barColor) {
        this.barColor = barColor;
        setupPaints();
        if (!isSpinning) {
            invalidate();
        }
    }

    public int getRimColor() {
        return rimColor;
    }

    public void setRimColor(int rimColor) {
        this.rimColor = rimColor;
        setupPaints();
        if (!isSpinning) {
            invalidate();
        }
    }

    public float getSpinSpeed() {
        return spinSpeed / 360.0f;
    }

    public void setSpinSpeed(float spinSpeed) {
        this.spinSpeed = spinSpeed * 360.0f;
    }

    public int getRimWidth() {
        return rimWidth;
    }

    public void setRimWidth(int rimWidth) {
        this.rimWidth = rimWidth;
        if (!isSpinning) {
            invalidate();
        }
    }

    public interface ProgressCallback {
        void onProgressUpdate(float progress);
    }

    static class WheelSavedState extends BaseSavedState {
        public static final Parcelable.Creator<WheelSavedState> CREATOR = new Parcelable.Creator<WheelSavedState>() {
            public WheelSavedState createFromParcel(Parcel in) {
                return new WheelSavedState(in);
            }

            public WheelSavedState[] newArray(int size) {
                return new WheelSavedState[size];
            }
        };

        float mProgress;
        float mTargetProgress;
        boolean isSpinning;
        float spinSpeed;
        int barWidth;
        int barColor;
        int rimWidth;
        int rimColor;
        int circleRadius;
        boolean linearProgress;
        boolean fillRadius;

        WheelSavedState(Parcelable superState) {
            super(superState);
        }

        private WheelSavedState(Parcel in) {
            super(in);
            this.mProgress = in.readFloat();
            this.mTargetProgress = in.readFloat();
            this.isSpinning = in.readByte() != 0;
            this.spinSpeed = in.readFloat();
            this.barWidth = in.readInt();
            this.barColor = in.readInt();
            this.rimWidth = in.readInt();
            this.rimColor = in.readInt();
            this.circleRadius = in.readInt();
            this.linearProgress = in.readByte() != 0;
            this.fillRadius = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(this.mProgress);
            out.writeFloat(this.mTargetProgress);
            out.writeByte((byte) (isSpinning ? 1 : 0));
            out.writeFloat(this.spinSpeed);
            out.writeInt(this.barWidth);
            out.writeInt(this.barColor);
            out.writeInt(this.rimWidth);
            out.writeInt(this.rimColor);
            out.writeInt(this.circleRadius);
            out.writeByte((byte) (linearProgress ? 1 : 0));
            out.writeByte((byte) (fillRadius ? 1 : 0));
        }
    }
}
