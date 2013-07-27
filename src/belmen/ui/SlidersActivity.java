package belmen.ui;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;

public class SlidersActivity extends FragmentActivity {

	private static final int CLOSED = -1;
	private static final int HIDED = -2;
	private List<Fragment> mFragments = new ArrayList<Fragment>();
	private int mCurrent = CLOSED;
	
	private int mContentWidth;
	private int mContentHeight;
	
	private Animation mShowAnim;
	private Animation mHideAnim;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sliders_activity);
		FragmentManager fm = getSupportFragmentManager();
		
		View header;
		int id = R.id.my_fragment1;
		Fragment fragment1 = fm.findFragmentById(id);
		header = fragment1.getView().findViewById(R.id.slider_header);
		header.setOnClickListener(new OnHeaderClickListener(0));
		mFragments.add(fragment1);
		
		id = R.id.my_fragment2;
		Fragment fragment2 = fm.findFragmentById(id);
		header = fragment2.getView().findViewById(R.id.slider_header);
		header.setOnClickListener(new OnHeaderClickListener(1));
		mFragments.add(fragment2);
		
		id = R.id.my_fragment3;
		Fragment fragment3 = fm.findFragmentById(id);
		header = fragment3.getView().findViewById(R.id.slider_header);
		header.setOnClickListener(new OnHeaderClickListener(2));
		mFragments.add(fragment3);
		
		id = R.id.my_fragment4;
		Fragment fragment4 = fm.findFragmentById(id);
		header = fragment4.getView().findViewById(R.id.slider_header);
		header.setOnClickListener(new OnHeaderClickListener(3));
		mFragments.add(fragment4);
		
		id = R.id.my_fragment5;
		Fragment fragment5 = fm.findFragmentById(id);
		header = fragment5.getView().findViewById(R.id.slider_header);
		header.setOnClickListener(new OnHeaderClickListener(4));
		mFragments.add(fragment5);
		
		getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				View view = mFragments.get(0).getView();
				mContentWidth = view.getWidth();
				mContentHeight = view.getHeight();
				View slider;
				FrameLayout.LayoutParams lp;
				for(int i = 0; i < mFragments.size(); i++) {
					slider = mFragments.get(i).getView();
					lp = new FrameLayout.LayoutParams(mContentWidth, mContentHeight, Gravity.CENTER_HORIZONTAL);
					lp.topMargin = 100 * (i+1);
					slider.setLayoutParams(lp);
				}
				getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
		
		mShowAnim = AnimationUtils.loadAnimation(this, R.anim.fragment_show);
		mHideAnim = AnimationUtils.loadAnimation(this, R.anim.fragment_hide);
	}

	public static class MyFragment1 extends MyFragment {
		public MyFragment1() {
			Bundle args = new Bundle();
			args.putString("title", "MyFragment1");
			setArguments(args);
		}
	}
	public static class MyFragment2 extends MyFragment {
		public MyFragment2() {
			Bundle args = new Bundle();
			args.putString("title", "MyFragment2");
			setArguments(args);
		}
	}
	public static class MyFragment3 extends MyFragment {
		public MyFragment3() {
			Bundle args = new Bundle();
			args.putString("title", "MyFragment3");
			setArguments(args);
		}
	}
	public static class MyFragment4 extends MyFragment {
		public MyFragment4() {
			Bundle args = new Bundle();
			args.putString("title", "MyFragment4");
			setArguments(args);
		}
	}
	public static class MyFragment5 extends MyFragment {
		public MyFragment5() {
			Bundle args = new Bundle();
			args.putString("title", "MyFragment5");
			setArguments(args);
		}
	}
	
	public void onHideClick(View v) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment fragment;
		View view;
		int position;
		Animation anim;
		Button btn = (Button) v;
		if(mCurrent == CLOSED) {
			for(int i = 0; i < mFragments.size(); i++) {
				fragment = mFragments.get(i);
				view = fragment.getView();
				position = 100 * (i+1);
				anim = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
						Animation.ABSOLUTE, 0, Animation.ABSOLUTE, mContentHeight - position);
				anim.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
				ft.hide(fragment);
				view.startAnimation(anim);
			}
			btn.setText("Show!");
			mCurrent = HIDED;
		} else if(mCurrent == HIDED) {
			for(int i = 0; i < mFragments.size(); i++) {
				fragment = mFragments.get(i);
				view = fragment.getView();
				position = 100 * (i+1);
				anim = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
						Animation.ABSOLUTE, mContentHeight - position, Animation.ABSOLUTE, 0);
				anim.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
				ft.show(fragment);
				view.startAnimation(anim);
			}
			btn.setText("Hide!");
			mCurrent = CLOSED;
		}
		ft.commit();
	}
	
	class OnHeaderClickListener implements OnClickListener {

		private int fragmentId;
		
		public OnHeaderClickListener(int fragmentId) {
			this.fragmentId = fragmentId;
		}

		@Override
		public void onClick(View v) {
			if(mCurrent == CLOSED) {
				open(fragmentId);
			} else if(fragmentId == mCurrent) {
				close();
			}
		}
	}
	
	private void open(int id) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment fragment;
		View view;
		int position;
		for(int i = 0; i < mFragments.size(); i++) {
			fragment = mFragments.get(i);
			view = fragment.getView();
			position = 100 * (i+1);
			Animation move = null;
			if(i < id) {
				move = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
						Animation.ABSOLUTE, 0, Animation.ABSOLUTE, -position);
				ft.hide(fragment);
			} else if(i == id) {
				move = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
						Animation.ABSOLUTE, position, Animation.ABSOLUTE, 0);
				LayoutParams lp = new FrameLayout.LayoutParams(mContentWidth, mContentHeight, Gravity.CENTER_HORIZONTAL);
				view.setLayoutParams(lp);
			} else if(i > id) {
				move = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
						Animation.ABSOLUTE, 0, Animation.ABSOLUTE, mContentHeight - position);
				ft.hide(fragment);
			}
			if(move != null) {
				move.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
//				move.setDuration(3000);
				view.startAnimation(move);
			}
		}
		mCurrent = id;
		ft.commit();
	}
	
	private void close() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment fragment;
		for(int i = 0; i < mFragments.size(); i++) {
			fragment = mFragments.get(i);
			View view = fragment.getView();
			int position = 100 * (i+1);
			Animation move = null;
			if(i < mCurrent) {
				move = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
						Animation.ABSOLUTE, -position, Animation.ABSOLUTE, 0);
				ft.show(fragment);
			} else if(i == mCurrent) {
				move = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
						Animation.ABSOLUTE, -position, Animation.ABSOLUTE, 0);
				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(mContentWidth, mContentHeight, Gravity.CENTER_HORIZONTAL);
				lp.topMargin = position;
				view.setLayoutParams(lp);
				ft.show(fragment);
			} else if(i > mCurrent) {
				move = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
						Animation.ABSOLUTE, mContentHeight - position, Animation.ABSOLUTE, 0);
				ft.show(fragment);
			}
			if(move != null) {
				move.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
//				move.setDuration(3000);
				view.startAnimation(move);
			}
		}
		mCurrent = CLOSED;
		ft.commit();
	}
}
