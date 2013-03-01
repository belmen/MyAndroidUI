package belmen.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewStub;
import android.widget.ImageView;
import belmen.ui.SliderView.OnSlideToEndListener;

public class MainActivity extends Activity {
	
	public static final int TOP = 0;
	public static final int BOTTOM = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;

	private SliderView mSlider;
	private ImageView mHandle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Logger.setDebug(true);
		
		ViewStub sliderLeft = (ViewStub) findViewById(R.id.slider_left);
		ViewStub sliderRight = (ViewStub) findViewById(R.id.slider_right);
		ViewStub sliderTop = (ViewStub) findViewById(R.id.slider_top);
		ViewStub sliderBottom = (ViewStub) findViewById(R.id.slider_bottom);
//		ViewStub slidingDrawerStub = (ViewStub) findViewById(R.id.sliding_drawer);
//		slidingDrawerStub.inflate();
		
		int position = getIntent().getIntExtra("position", TOP);
		OnSlideToEndListener listener = null;
		switch (position) {
		case TOP:
			sliderTop.inflate();
			listener = mTopListener;
			break;
		case BOTTOM:
			sliderBottom.inflate();
			listener = mBottomListener;
			break;
		case LEFT:
			sliderLeft.inflate();
			listener = mLeftListener;
			break;
		case RIGHT:
			sliderRight.inflate();
//			listener = mRightListener;
			break;
		}
		mSlider = (SliderView) findViewById(R.id.main_slider);
		mHandle = (ImageView) findViewById(R.id.main_handle);
		mSlider.setOnSlideToEndListener(listener);
	}
	
	private OnSlideToEndListener mTopListener = new OnSlideToEndListener() {
		@Override
		public void onSlideToEnd() {
			mHandle.setImageResource(R.drawable.grabber_up);
		}
		
		@Override
		public void onLeaveEnd() {
			mHandle.setImageResource(R.drawable.grabber_down);
		}
	};
	
	private OnSlideToEndListener mBottomListener = new OnSlideToEndListener() {
		@Override
		public void onSlideToEnd() {
			mHandle.setImageResource(R.drawable.grabber_down);
		}
		
		@Override
		public void onLeaveEnd() {
			mHandle.setImageResource(R.drawable.grabber_up);
		}
	};
	
	private OnSlideToEndListener mLeftListener = new OnSlideToEndListener() {
		@Override
		public void onSlideToEnd() {
			mHandle.setImageResource(R.drawable.grabber_left);
		}
		
		@Override
		public void onLeaveEnd() {
			mHandle.setImageResource(R.drawable.grabber_right);
		}
	};
	
	private OnSlideToEndListener mRightListener = new OnSlideToEndListener() {
		@Override
		public void onSlideToEnd() {
			mHandle.setImageResource(R.drawable.grabber_right);
		}
		
		@Override
		public void onLeaveEnd() {
			mHandle.setImageResource(R.drawable.grabber_left);
		}
	};
}
