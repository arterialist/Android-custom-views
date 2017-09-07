import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.merch.sandbox.R;

public class SlideLockView extends View {

    public static final int SMALL_LOCK_CIRCLE_RADIUS_DP = 35;
    public static final int BACKGROUND_COLOR = Color.parseColor("#ee222222");
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect mRect = new Rect();
    private Bitmap mLockBitmap;
    private OnUnlockListener mOnUnlockListener;
    private float mTouchX;
    private float mTouchY;
    private boolean mPressed;
    private boolean mUnlocked;

    public SlideLockView(Context context) {
        super(context);
        init();
    }

    public SlideLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        int lockIconXY = (int) dpToPx(50);
        mLockBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_lock); // change to your icon, my icon can be found in this repo
        mLockBitmap = Bitmap.createScaledBitmap(mLockBitmap, lockIconXY, lockIconXY, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getDrawingRect(mRect);

        mPaint.setColor(Color.WHITE);
        float radius = dpToPx(SMALL_LOCK_CIRCLE_RADIUS_DP);

        float lockDrawX = mPressed ? mTouchX : mRect.centerX();
        float lockDrawY = mPressed ? mTouchY : mRect.centerY();

        if (mPressed) {
            canvas.drawCircle(mRect.centerX(), mRect.centerY(), mRect.height() / 2 - 5, mPaint);
            mPaint.setColor(BACKGROUND_COLOR);
            canvas.drawCircle(mRect.centerX(), mRect.centerY(), mRect.height() / 2 - 10, mPaint);
            mPaint.setColor(Color.WHITE);
        }

        canvas.drawCircle(lockDrawX, lockDrawY, radius, mPaint);
        mPaint.setColor(mUnlocked ? Color.GREEN : BACKGROUND_COLOR);
        canvas.drawCircle(lockDrawX, lockDrawY, radius - 5, mPaint);
        canvas.drawBitmap(mLockBitmap, lockDrawX - dpToPx(25), lockDrawY - dpToPx(25), mPaint);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        float lastTouchX = mTouchX;
        float lastTouchY = mTouchY;

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mPressed = true;
                mTouchX = event.getX();
                mTouchY = event.getY();
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mPressed = false;
                mTouchX = mRect.centerX();
                mTouchY = mRect.centerY();
                break;
        }

        double lengthFromCenterToTouchPoint = Math.pow(mTouchX - mRect.centerX(), 2) + Math.pow(mTouchY - mRect.centerY(), 2);
        double radius = Math.pow(mRect.height() / 2 - 10, 2);

        if (lengthFromCenterToTouchPoint > radius) {
            mTouchX = lastTouchX;
            mTouchY = lastTouchY;
            if (!mUnlocked) {
                onUnlock();
            }
            mUnlocked = true;
        } else {
            mUnlocked = false;
        }

        invalidate();

        return true;
    }

    public void onUnlock() {
        if (mOnUnlockListener != null) {
            mOnUnlockListener.onUnlock(this);
        }
        invalidate();
    }

    private float dpToPx(float dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    @SuppressWarnings("unused")
    public void setOnUnlockListener(OnUnlockListener onUnlockListener) {
        if (mOnUnlockListener != onUnlockListener) {
            mOnUnlockListener = onUnlockListener;
        }
        float width = getLayoutParams().width;
        float height = getLayoutParams().height;
        mTouchX = width / 2;
        mTouchY = height / 2;
    }

    @SuppressWarnings("unused")
    public interface OnUnlockListener {
        void onUnlock(SlideLockView slideLockView);
    }
}
