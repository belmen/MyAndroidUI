package belmen.view;

import belmen.util.Logger;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {
	
	public static final String TAG = MyScrollView.class.getSimpleName();

	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(context, new YScrollDetector());
	}
	
	// 拦截listview的左右滑动事件
	private GestureDetector mGestureDetector;
	
	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
    }

    // Return false if we're scrolling in the x direction  
    static class YScrollDetector extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(Math.abs(distanceY) > Math.abs(distanceX)) {
            	Logger.i(TAG, "onScroll true");
                return true;
            }
            Logger.i(TAG, "onScroll false");
            return false;
        }
    }
}
