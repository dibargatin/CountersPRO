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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends ListActivity implements OnClickListener {

	// ===========================================================
	// Constants
	// ===========================================================
	public final static String LOG_TAG = "Housing";
	private final static int REQUEST_ADD_COUNTER = 1;
	private final static int REQUEST_EDIT_COUNTER = 2;

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
		
		String[] from = new String[] { "name", "note" };
		int[] to = new int[] { R.id.tvCounterName, R.id.tvCounterNote };

		mAdapter = new SimpleCursorAdapter(this, R.layout.counters_list_item, mDbHelper.fetchAllCounters(), from, to);
		getListView().setAdapter(mAdapter);

		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
				Intent intent = new Intent(MainActivity.this, EntryActivity.class);
				
				intent.setAction(Intent.ACTION_EDIT);
				intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, id);

				startActivityForResult(intent, REQUEST_EDIT_COUNTER);
			}
		});
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> a, View v, int pos, long id) {
				mDbHelper.deleteCounter(id);
				mAdapter.getCursor().requery();
				return true;
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
		
		case REQUEST_EDIT_COUNTER:
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
	
	@Override
	protected void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
