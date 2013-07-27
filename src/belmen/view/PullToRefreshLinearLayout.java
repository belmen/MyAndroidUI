package belmen.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

public class PullToRefreshLinearLayout extends PullToRefreshBase<ScrollView> {

	public PullToRefreshLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullToRefreshLinearLayout(
			Context context,
			com.handmark.pulltorefresh.library.PullToRefreshBase.Mode mode,
			com.handmark.pulltorefresh.library.PullToRefreshBase.AnimationStyle animStyle) {
		super(context, mode, animStyle);
	}

	public PullToRefreshLinearLayout(Context context,
			com.handmark.pulltorefresh.library.PullToRefreshBase.Mode mode) {
		super(context, mode);
	}

	public PullToRefreshLinearLayout(Context context) {
		super(context);
	}

	@Override
	public com.handmark.pulltorefresh.library.PullToRefreshBase.Orientation getPullToRefreshScrollDirection() {
		return Orientation.VERTICAL;
	}

	@Override
	protected ScrollView createRefreshableView(Context context,
			AttributeSet attrs) {
//		LinearLayout ll;
//		if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
//			ll = new InternalLinearLayoutSDK9(context, attrs);
//		} else {
//			ll = new LinearLayout(context, attrs);
//		}
//		ll.setId(R.id.linearlayout);
//		return ll;
		return new ScrollView(context, attrs);
		
	}

	@Override
	protected boolean isReadyForPullEnd() {
		return false;
	}

	@Override
	protected boolean isReadyForPullStart() {
		return true;
	}
}
