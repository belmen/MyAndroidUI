package belmen.ui;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import belmen.view.ImageViewTouchViewPager;

public class GalleryActivity extends Activity {

	private ImageViewTouchViewPager mViewPager;
	
	private MyAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		mViewPager = (ImageViewTouchViewPager) findViewById(R.id.gallery_viewpager);
		mAdapter = new MyAdapter(this);
		mViewPager.setAdapter(mAdapter);
	}
	
	public static class MyAdapter extends PagerAdapter {
		
		private static final int[] PICS = {R.drawable.chrysanthemum,
											R.drawable.desert,
											R.drawable.hydrangeas,
											R.drawable.penguins};
		
		private Context mContext;

		public MyAdapter(Context context) {
			this.mContext = context;
		}
		
		@Override
		public int getCount() {
			return PICS.length;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageViewTouch view = new ImageViewTouch(mContext, null);
			view.setDisplayType(DisplayType.FIT_IF_BIGGER);
			view.setImageResource(PICS[position]);
			container.addView(view);
			return view;
		}
	}
}
