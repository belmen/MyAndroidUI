package belmen.ui;

import belmen.util.Logger;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HomeActivity extends ListActivity {

	private ListView mListView;
	private String[] mItems = new String[] {"Top", "Bottom", "Left", "Right",
			"LinearLayout", "Scroll", "Sliders", "Gallery"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.setDebug(true);
		
		mListView = getListView();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mItems);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(onItemClick);
	}
	
	private OnItemClickListener onItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = null;
			if(position == 4) {
				intent = new Intent(HomeActivity.this, LinearLayoutActivity.class);
			} else if(position == 5) {
				intent = new Intent(HomeActivity.this, ScrollActivity.class);
			} else if(position == 6) {
				intent = new Intent(HomeActivity.this, SlidersActivity.class);
			} else if(position == 7) {
				intent = new Intent(HomeActivity.this, GalleryActivity.class);
			}  else {
				intent = new Intent(HomeActivity.this, MainActivity.class);
				intent.putExtra("position", position);
			}
			if(intent != null) {
				startActivity(intent);
			}
		}
	};
}
