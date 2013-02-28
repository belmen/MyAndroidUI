package belmen.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private SliderView mSlider;
	private ImageView mHandle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mSlider = (SliderView) findViewById(R.id.main_slider);
		mHandle = (ImageView) findViewById(R.id.main_handle);
	}
}
