import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class DotIndicatorView extends View{

	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Rect rect = new Rect();
	private int selectedDot;
	private int dotsCount;
	private int defaultDotColor = Color.parseColor("#5A393939");
	private int activeDotColor = Color.parseColor("#0404B4");
	private int dotSize = 11;
	private int activeDotSize = 14;

	public DotIndicatorView(Context context) {
		super(context);
	}

	public DotIndicatorView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public DotIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		getDrawingRect(rect);
		int radius = dpToPx(dotSize) / 2;
		int dotDistance = radius*2;
		int dotsOffset = (rect.width() - dpToPx(dotSize) * dotsCount - (dotsCount - 1)*dotDistance)/2;

		for(int i = 0; i < dotsCount; i++) {
			paint.setColor(defaultDotColor);
			if (selectedDot == i) {
				paint.setColor(activeDotColor);
			}
			canvas.drawCircle(dotsOffset+dpToPx(dotSize)*i+dotDistance*i + radius, rect.height()/2, (i == selectedDot)?dpToPx(activeDotSize)/2:radius, paint);
		}
	}

	public int dpToPx(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}

	public void setSelectedDot(int a){
		selectedDot = a;
		invalidate();
	}

	public void setDotsCount(int a) {
		dotsCount = a;
		invalidate();
	}

	public void setDefaultDotColor(int a) {
		defaultDotColor = a;
		invalidate();
	}

	public void setActiveDotColor(int a) {
		activeDotColor = a;
		invalidate();
	}

	public void setDotSize(int a) {
		dotSize = a;
		invalidate();
	}

	public void setActiveDotSize(int a) {
		activeDotSize = a;
		invalidate();
	}
}
