package belmen.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
import belmen.view.PullToRefreshLinearLayout;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

public class LinearLayoutActivity extends Activity {

//	private PullToRefreshLinearLayout mLayout;
	private PullToRefreshLinearLayout mView;
	private TextView mTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ll_activity);
		mView = (PullToRefreshLinearLayout) findViewById(R.id.ll_layout);
		mTextView = (TextView) findViewById(R.id.ll_text);
		mView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				mTextView.setText("刷新");
			}
		});
	}

	
}
