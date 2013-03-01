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
	private String[] mItems = new String[] {"Top", "Bottom", "Left", "Right"};
	
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
			Intent intent = new Intent(HomeActivity.this, MainActivity.class);
			intent.putExtra("position", position);
			startActivity(intent);
		}
	};
}
