package com.blogspot.dibargatin.housing;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	final static String LOG_TAG = "Housing";

	DBHelper mDbHelper;
	SimpleCursorAdapter mCountersAdapter;
	EditText mEtCounterName, mEtCounterNote;
	ListView mLvCounters;
	Long mCurrentId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mDbHelper = new DBHelper(this);

		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();

		TabHost.TabSpec tabSpec;

		// Счетчики
		tabSpec = tabHost.newTabSpec("counters_tab");
		tabSpec.setIndicator(getResources().getText(R.string.counters_tab_name));
		tabSpec.setContent(R.id.counters_tab);
		tabHost.addTab(tabSpec);

		// Поля для ввода данных
		mEtCounterName = (EditText) findViewById(R.id.counterName);
		mEtCounterNote = (EditText) findViewById(R.id.counterNote);

		// Находим кнопку для добавления нового счетчика
		Button btnAdd = (Button) findViewById(R.id.btnCreateCounter);
		btnAdd.setOnClickListener(this);
		
		Button btnDeleteAll = (Button) findViewById(R.id.btnDeleteAll);
		btnDeleteAll.setOnClickListener(this);

		// Выводим список
		displayCountersList();

		// Показания
		tabSpec = tabHost.newTabSpec("statements_tab");
		tabSpec.setIndicator(getResources().getText(R.string.journal_tab_name), getResources().getDrawable(R.drawable.tab_icon_selector));
		tabSpec.setContent(R.id.journal_tab);
		tabHost.addTab(tabSpec);

		tabHost.setCurrentTabByTag("counters_tab");
	}

	private void displayCountersList() {
		String[] from = new String[] { "name", "note" };
		int[] to = new int[] { R.id.tvCounterName, R.id.tvCounterNote };

		mCountersAdapter = new SimpleCursorAdapter(this, R.layout.counters_list_item, mDbHelper.fetchAllCounters(), from, to);

		mLvCounters = (ListView) findViewById(R.id.countersListView);
		mLvCounters.setAdapter(mCountersAdapter);
		mLvCounters.requestFocus();

		mLvCounters.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
				Cursor cursor = (Cursor) mLvCounters.getItemAtPosition(pos);
				
				mEtCounterName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
				mEtCounterNote.setText(cursor.getString(cursor.getColumnIndexOrThrow("note")));
				mCurrentId = id;
			}

		});

		mLvCounters.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> a, View v, int pos, long id) {
				mDbHelper.deleteCounter(id);
				mCountersAdapter.getCursor().requery();
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.btnCreateCounter) {
			String name = mEtCounterName.getText().toString();
			String note = mEtCounterNote.getText().toString();

			if (name.length() == 0) {
				Toast.makeText(this, "Укажите название счетчика", Toast.LENGTH_LONG).show();
				return;
			}

			if (mCurrentId == null) {
				mDbHelper.insertCounter(name, note);

			} else {
				mDbHelper.updateCounter(mCurrentId, name, note);
				mCurrentId = null;
			}
		} else {
			mDbHelper.deleteAllCounters();
		}
		mCountersAdapter.getCursor().requery();
	}

}
