
package com.blogspot.dibargatin.housing;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class EntryActivity extends Activity implements OnClickListener {
    // ===========================================================
    // Constants
    // ===========================================================
    private final static int REQUEST_EDIT_COUNTER = 1;

    private final static int REQUEST_ADD_ENTRY = 2;

    private final static int REQUEST_EDIT_ENTRY = 3;

    // ===========================================================
    // Fields
    // ===========================================================
    DBHelper mDbHelper;

    SimpleCursorAdapter mAdapter;

    long mCounterId;

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
        setContentView(R.layout.entries_form);

        mDbHelper = new DBHelper(this);

        TextView name = (TextView)findViewById(R.id.tvCounterName);
        TextView note = (TextView)findViewById(R.id.tvCounterNote);
        View color = (View)findViewById(R.id.vColor);

        ImageView ivEdit = (ImageView)findViewById(R.id.ivEdit);
        ivEdit.setOnClickListener(this);

        Intent intent = getIntent();
        mCounterId = intent.getLongExtra(CounterActivity.EXTRA_COUNTER_ID, -1);

        Cursor c = mDbHelper.fetchCounterById(mCounterId);
        c.moveToFirst();
        name.setText(c.getString(c.getColumnIndex("name")));
        note.setText(c.getString(c.getColumnIndex("note")));
        color.setBackgroundColor(c.getInt(c.getColumnIndex("color")));

        String[] from = new String[] {
                "entry_date", "value", "delta", "cost", "rate"
        };
        int[] to = new int[] {
                R.id.tvDate, R.id.tvCurrentValue, R.id.tvDelta, R.id.tvCost, R.id.tvRateValue
        };

        mAdapter = new EntriesCursorAdapter(this, R.layout.entry_list_item,
                mDbHelper.fetchEntriesByCounterId(mCounterId), from, to);

        ListView list = (ListView)findViewById(R.id.listView1);
        list.setAdapter(mAdapter);

        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                Intent intent = new Intent(EntryActivity.this, EntryEditActivity.class);
                intent.setAction(Intent.ACTION_EDIT);
                intent.putExtra(EntryEditActivity.EXTRA_ENTRY_ID, id);

                startActivityForResult(intent, REQUEST_EDIT_ENTRY);
            }
        });

        list.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, int pos, long id) {
                mDbHelper.deleteEntry(id);
                mAdapter.getCursor().requery();
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivEdit:
                Intent intent = new Intent(EntryActivity.this, EntryEditActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
                intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, mCounterId);
                startActivityForResult(intent, REQUEST_ADD_ENTRY);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.entry_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_entry:
                Intent intent = new Intent(EntryActivity.this, EntryEditActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
                intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, mCounterId);
                startActivityForResult(intent, REQUEST_ADD_ENTRY);
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_ENTRY:
            case REQUEST_EDIT_ENTRY:
                if (resultCode == RESULT_OK) {
                    mAdapter.getCursor().requery();
                }
                break;

            case REQUEST_EDIT_COUNTER:
                if (resultCode == RESULT_OK) {
                    TextView name = (TextView)findViewById(R.id.tvCounterName);
                    TextView note = (TextView)findViewById(R.id.tvCounterNote);

                    Cursor c = mDbHelper.fetchCounterById(mCounterId);
                    c.moveToFirst();
                    name.setText(c.getString(c.getColumnIndex("name")));
                    note.setText(c.getString(c.getColumnIndex("note")));
                    setResult(RESULT_OK);
                }
                break;
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
    private class EntriesCursorAdapter extends SimpleCursorAdapter {

        public EntriesCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            
            try {
                TextView measure = (TextView)view.findViewById(R.id.tvMeasure);
                String m = cursor.getString(cursor.getColumnIndex("measure"));
                measure.setText(Html.fromHtml(m));
            } catch (Exception e) {
                // Нет единицы измерения
            }

            try {
                TextView date = (TextView)view.findViewById(R.id.tvDate);
                TextView month = (TextView)view.findViewById(R.id.tvMonth);
                
                String[] m = getResources().getStringArray(R.array.month_list);
                
                String entryDate = cursor.getString(cursor.getColumnIndex("entry_date"));
                Date d = new Date(java.sql.Date.valueOf(entryDate).getTime());
                
                date.setText(DateFormat.format("dd.MM.yyyy", d));
                month.setText(m[d.getMonth()]);
            } catch (Exception e) {
                // Нет показаний
            }
        }

    }
}
