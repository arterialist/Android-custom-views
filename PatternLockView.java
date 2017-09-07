import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PatternLockView extends View {

    private static final int DOT_RADIUS = 5;
    private static final int BACKGROUND_COLOR = Color.parseColor("#ee222222");
    private static final int CIRCLE_RADIUS = 35;
    private static final int PATTERN_RESET_DELAY = 1000;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect mRect = new Rect();
    private final Handler handler = new Handler();
    private boolean mPressed;
    private boolean mPatternCorrect = true;
    private boolean mInputLocked;
    private boolean mTouchCoordinatesTrackingEnabled;
    private List<Pair<Integer, Integer>> mDotsCoordinates;
    private List<Integer> mCheckedCircles = new ArrayList<>();
    private List<Integer> mCorrectPattern = new ArrayList<>();
    private List<Vector> mCheckedDotsLines = new ArrayList<>();
    private final Runnable resetPatternField = new Runnable() {
        @Override
        public void run() {
            mCheckedCircles = new ArrayList<>();
            mCheckedDotsLines = new ArrayList<>();
            mPatternCorrect = true;
            mInputLocked = false;
            invalidate();
        }
    };
    private Pair<Integer, Integer> mLastCheckedCircle = new Pair<>(-5, -5);
    private OnPatternTypedListener mOnPatternTypedListener;
    private Pair<Integer, Integer> mTouchCoordinates = new Pair<>(0, 0);
    private int mDotSensitivity;

    public PatternLockView(Context context) {
        super(context);
    }

    public PatternLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PatternLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        Pair<Integer, Integer> coordinate;

        mDotsCoordinates = new ArrayList<>();

        coordinate = new Pair<>(mRect.width() / 6, mRect.width() / 6); // dot at 0, 0
        mDotsCoordinates.add(coordinate);
        coordinate = new Pair<>(mRect.centerX(), mRect.width() / 6); // dot at 1, 0
        mDotsCoordinates.add(coordinate);
        coordinate = new Pair<>(mRect.width() / 6 * 5, mRect.width() / 6); // dot at 2, 0
        mDotsCoordinates.add(coordinate);

        coordinate = new Pair<>(mRect.width() / 6, mRect.centerY()); // dot at 0, 1
        mDotsCoordinates.add(coordinate);
        coordinate = new Pair<>(mRect.centerX(), mRect.centerY()); // center dot
        mDotsCoordinates.add(coordinate);
        coordinate = new Pair<>(mRect.width() / 6 * 5, mRect.centerY()); // dot at 1, 2
        mDotsCoordinates.add(coordinate);

        coordinate = new Pair<>(mRect.width() / 6, mRect.width() / 6 * 5); // dot at 0, 2
        mDotsCoordinates.add(coordinate);
        coordinate = new Pair<>(mRect.centerX(), mRect.width() / 6 * 5); // dot at 1, 2
        mDotsCoordinates.add(coordinate);
        coordinate = new Pair<>(mRect.width() / 6 * 5, mRect.width() / 6 * 5); // dot at 2, 2
        mDotsCoordinates.add(coordinate);

        setBackgroundColor(BACKGROUND_COLOR);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getDrawingRect(mRect);
        init();
        float dotRadius = dpToPx(DOT_RADIUS);
        float circleRadius = dpToPx(CIRCLE_RADIUS);

        for (int checkedCircleIndex : mCheckedCircles) {
            mPaint.setColor(mPatternCorrect ? Color.GREEN : Color.RED);
            Integer circleX = mDotsCoordinates.get(checkedCircleIndex).first;
            Integer circleY = mDotsCoordinates.get(checkedCircleIndex).second;
            canvas.drawCircle(circleX, circleY, circleRadius, mPaint);
            mPaint.setColor(BACKGROUND_COLOR);
            canvas.drawCircle(circleX, circleY, circleRadius - dotRadius, mPaint);
        }

        for (int i = 0; i < mDotsCoordinates.size(); i++) {
            Pair coordinate = mDotsCoordinates.get(i);
            mPaint.setColor(Color.WHITE);
            canvas.drawCircle((int) coordinate.first, (int) coordinate.second, dotRadius, mPaint);
            if (!mCheckedCircles.contains(i)) {
                mPaint.setColor(BACKGROUND_COLOR);
                canvas.drawCircle((int) coordinate.first, (int) coordinate.second, dotRadius - dotRadius / 3, mPaint);
            }
        }

        mPaint.setStrokeWidth(10);
        mPaint.setColor(Color.WHITE);

        for (Vector vector : mCheckedDotsLines) {
            if (vector.getStartX() != -5) {
                canvas.drawLine(vector.getStartX(), vector.getStartY(), vector.getEndX(), vector.getEndY(), mPaint);
            }
        }

        if (mPressed && mCheckedCircles.size() > 0) {
            Integer lastCheckedCircleIndex = mCheckedCircles.get(mCheckedCircles.size() - 1);
            Pair<Integer, Integer> lastCheckedCircle = mDotsCoordinates.get(lastCheckedCircleIndex);
            canvas.drawLine(lastCheckedCircle.first, lastCheckedCircle.second, mTouchCoordinates.first, mTouchCoordinates.second, mPaint);
        }

        if (mTouchCoordinatesTrackingEnabled) {
            mPaint.setTextSize(30);
            mPaint.setColor(Color.RED);
            canvas.drawText(String.format(Locale.getDefault(), "X %d, Y %d", mTouchCoordinates.first, mTouchCoordinates.second), mTouchCoordinates.first - 90, mTouchCoordinates.second - 100, mPaint);
            mPaint.setStrokeWidth(3);
            canvas.drawLine(0, mTouchCoordinates.second, mRect.width(), mTouchCoordinates.second, mPaint);
            canvas.drawLine(mTouchCoordinates.first, 0, mTouchCoordinates.first, mRect.height(), mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        if (!mInputLocked) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    mPressed = true;
                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mPressed = false;
                    onPatternTyped();
                    break;
            }

            mTouchCoordinates = new Pair<>(((int) event.getX()), (int) event.getY());

            int touchX = mTouchCoordinates.first;
            int touchY = mTouchCoordinates.second;
            float dotRadius = dpToPx(DOT_RADIUS);

            for (int index = 0; index < mDotsCoordinates.size(); index++) {
                Pair<Integer, Integer> coord = mDotsCoordinates.get(index);
                mDotSensitivity = 6;
                if (touchX < coord.first + dotRadius * mDotSensitivity &&
                        touchX > coord.first - dotRadius * mDotSensitivity &&
                        touchY > coord.second - dotRadius * mDotSensitivity &&
                        touchY < coord.second + dotRadius * mDotSensitivity &&
                        !mCheckedCircles.contains(index)) {
                    mCheckedDotsLines.add(new Vector(mLastCheckedCircle.first, mLastCheckedCircle.second, coord.first, coord.second));
                    mLastCheckedCircle = coord;
                    mCheckedCircles.add(index);
                }
            }

            invalidate();
        }

        return true;
    }

    public void setDotSensitivity(int dotSensitivity) {
        mDotSensitivity = Math.abs(dotSensitivity);
    }

    @SuppressWarnings("unused")
    public void setCorrectPattern(@NonNull List<Integer> pattern) {
        if (pattern.size() > 9 || pattern.size() < 4) {
            throw new IllegalArgumentException("Pattern length must be bigger than 4 and smaller than 9.");
        }
        mCorrectPattern.clear();
        for (Integer circleIndex : pattern) {
            if (circleIndex > 8 || circleIndex < 0) {
                throw new IllegalArgumentException("Circle index must be bigger than 0 and smaller than 9.");
            }
            mCorrectPattern.add(circleIndex);
        }
    }

    public void setCorrectPattern(int[] pattern) {
        if (pattern.length > 9 || pattern.length < 4) {
            throw new IllegalArgumentException("Pattern length must be bigger than 4 and smaller than 9.");
        }
        mCorrectPattern.clear();
        for (int circleIndex : pattern) {
            if (circleIndex > 8 || circleIndex < 0) {
                throw new IllegalArgumentException("Circle index must be bigger than 0 and smaller than 9.");
            }
            mCorrectPattern.add(circleIndex);
        }
    }

    @SuppressWarnings("unused")
    public void setOnPatternTypedListener(OnPatternTypedListener onPatternTypedListener) {
        if (mOnPatternTypedListener != onPatternTypedListener) {
            mOnPatternTypedListener = onPatternTypedListener;
        }
    }

    public void onPatternTyped() {
        mPatternCorrect = mCorrectPattern != null && mCheckedCircles.size() > 3 && mCorrectPattern.equals(mCheckedCircles);
        handler.postDelayed(resetPatternField, PATTERN_RESET_DELAY);
        mLastCheckedCircle = new Pair<>(-5, -5);
        mInputLocked = true;
        if (mOnPatternTypedListener != null) {
            mOnPatternTypedListener.onPatternTyped(this, mPatternCorrect);
        }
        invalidate();
    }

    private float dpToPx(float dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    @SuppressWarnings("unused")
    public void setTouchCoordinatesTrackingEnabled(boolean touchCoordinatesTrackingEnabled) {
        mTouchCoordinatesTrackingEnabled = touchCoordinatesTrackingEnabled;
    }

    @SuppressWarnings("unused")
    public interface OnPatternTypedListener {
        void onPatternTyped(PatternLockView lockView, boolean isPatternCorrect);
    }

    private class Vector {

        private final int startX;
        private final int startY;
        private final int endX;
        private final int endY;

        Vector(int startX, int startY, int endX, int endY) {
            this.endX = endX;
            this.endY = endY;
            this.startX = startX;
            this.startY = startY;
        }

        int getStartX() {
            return startX;
        }

        int getStartY() {
            return startY;
        }

        int getEndX() {
            return endX;
        }

        int getEndY() {
            return endY;
        }
    }
}
