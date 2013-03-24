package com.blogspot.dibargatin.housing;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MainActivity extends ListActivity implements OnClickListener {

	// ===========================================================
	// Constants
	// ===========================================================
	public final static String LOG_TAG = "Housing";
	private final static int REQUEST_ADD_COUNTER = 1;

	// ===========================================================
	// Fields
	// ===========================================================
	DBHelper mDbHelper;
	SimpleCursorAdapter mAdapter;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDbHelper = new DBHelper(this);
		// mDbHelper.insertCounter("Холодная вода", "В ванной");
		//mDbHelper.insertEntry((long)1, "2013-03-23", 254.34);

		String[] from = new String[] { "name", "note" };
		int[] to = new int[] { R.id.tvCounterName, R.id.tvCounterNote };

		mAdapter = new SimpleCursorAdapter(this, R.layout.counters_list_item, mDbHelper.fetchAllCounters(), from, to);
		getListView().setAdapter(mAdapter);

		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
				Intent intent = new Intent(MainActivity.this, EntryActivity.class);
							
				TextView name = (TextView) v.findViewById(R.id.tvCounterName);
				TextView note = (TextView) v.findViewById(R.id.tvCounterNote);
				
				intent.setAction(Intent.ACTION_EDIT);
				intent.putExtra(EntryActivity.EXTRA_COUNTER_ID, id);
				intent.putExtra(EntryActivity.EXTRA_COUNTER_NAME, name.getText().toString());
				intent.putExtra(EntryActivity.EXTRA_COUNTER_NOTE, note.getText().toString());

				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		boolean result = true;

		switch (item.getItemId()) {
		case R.id.action_add_counter:
			Intent intent = new Intent(MainActivity.this, CounterActivity.class);

			intent.setAction(Intent.ACTION_INSERT);
			startActivityForResult(intent, REQUEST_ADD_COUNTER);
			break;

		case R.id.action_del_counter:
			// TODO Удалить
			String[] from = new String[] { "name" };
			int[] to = new int[] { android.R.id.text1 };

			ListView list = getListView();
			mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_multiple_choice, mDbHelper.fetchAllCounters(), from, to);

			list.setAdapter(mAdapter);
			list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			break;

		default:
			result = super.onOptionsItemSelected(item);
		}

		return result;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ADD_COUNTER:
			if (resultCode == RESULT_OK) {
				mAdapter.getCursor().requery();
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {

	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
