package belmen.ui;

import android.app.Activity;
import android.os.Bundle;
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
		mSlider = (SliderView) findViewById(R.id.main_slider);
		mHandle = (ImageView) findViewById(R.id.main_handle);
		mSlider.setOnSlideToEndListener(new OnSlideToEndListener() {
			@Override
			public void onSlideToEnd() {
				mHandle.setImageResource(R.drawable.grabber_reverse);
			}
			
			@Override
			public void onLeaveEnd() {
				mHandle.setImageResource(R.drawable.grabber);
			}
		});
	}
}
