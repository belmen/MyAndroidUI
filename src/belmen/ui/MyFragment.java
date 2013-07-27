package belmen.ui;

import belmen.util.Logger;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyFragment extends Fragment {
	
	public static final String TAG = MyFragment.class.getSimpleName();

	private String title;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if(args != null) {
			title = getArguments().getString("title");
		}
		if(title == null) {
			title = "MyFragment";
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.d(TAG, title + " onCreateView");
		View view = inflater.inflate(R.layout.one_slider, container, false);
		TextView tvTitle = (TextView) view.findViewById(R.id.slider_title);
		tvTitle.setText(title);
		return view;
	}

	private OnClickListener onHeaderClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
		}
	};
}
