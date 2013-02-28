package belmen.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewStub;
import android.widget.ImageView;
import belmen.ui.SliderView.OnSlideToEndListener;

public class MainActivity extends Activity {

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
//		ViewStub slidingDrawerStub = (ViewStub) findViewById(R.id.sliding_drawer);
//		slidingDrawerStub.inflate();
		
		sliderLeft.inflate();
//		sliderRight.inflate();
//		sliderTop.inflate();
		mSlider = (SliderView) findViewById(R.id.main_slider);
		mHandle = (ImageView) findViewById(R.id.main_handle);
		mSlider.setOnSlideToEndListener(new OnSlideToEndListener() {
			@Override
			public void onSlideToEnd() {
				mHandle.setImageResource(R.drawable.grabber_left);
			}
			
			@Override
			public void onLeaveEnd() {
				mHandle.setImageResource(R.drawable.grabber_right);
			}
		});
		
//		mSlider.setOnSlideToEndListener(new OnSlideToEndListener() {
//			@Override
//			public void onSlideToEnd() {
//				mHandle.setImageResource(R.drawable.grabber_right);
//			}
//			
//			@Override
//			public void onLeaveEnd() {
//				mHandle.setImageResource(R.drawable.grabber_left);
//			}
//		});
		
//		mSlider.setOnSlideToEndListener(new OnSlideToEndListener() {
//			@Override
//			public void onSlideToEnd() {
//				mHandle.setImageResource(R.drawable.grabber_up);
//			}
//			
//			@Override
//			public void onLeaveEnd() {
//				mHandle.setImageResource(R.drawable.grabber_down);
//			}
//		});
	}
}
