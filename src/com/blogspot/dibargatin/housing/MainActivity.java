
package com.blogspot.dibargatin.housing;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends ListActivity {

    // ===========================================================
    // Constants
    // ===========================================================
    public final static String LOG_TAG = "Housing";

    private final static int REQUEST_ADD_COUNTER = 1;

    private final static int REQUEST_EDIT_COUNTER = 2;

    private final static int REQUEST_ADD_ENTRY = 3;

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
        
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.close();

        String[] from = new String[] {
                "name", "note", "value", "entry_date"
        };

        int[] to = new int[] {
                R.id.tvCounterName, R.id.tvCounterNote, R.id.tvValue, R.id.tvPeriod
        };

        mAdapter = new CountersCursorAdapter(this, R.layout.counters_list_item,
                mDbHelper.fetchAllCounters(), from, to);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final long itemId = id;

                builder.setTitle(R.string.action);
                builder.setItems(R.array.counter_actions, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent;

                        switch (which) {
                            case 0: // Добавить показания
                                intent = new Intent(MainActivity.this, EntryEditActivity.class);

                                intent.setAction(Intent.ACTION_INSERT);
                                intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, itemId);

                                startActivityForResult(intent, REQUEST_ADD_ENTRY);

                                break;

                            case 1: // Редактировать счетчик
                                intent = new Intent(MainActivity.this, CounterActivity.class);

                                intent.setAction(Intent.ACTION_EDIT);
                                intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, itemId);

                                startActivityForResult(intent, REQUEST_EDIT_COUNTER);

                                break;

                            case 2: // Удалить счетчик
                                AlertDialog.Builder confirm = new AlertDialog.Builder(
                                        MainActivity.this);

                                confirm.setMessage(R.string.action_counter_del_confirm);
                                confirm.setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                mDbHelper.deleteCounter(itemId);
                                                mAdapter.getCursor().requery();

                                            }

                                        });
                                confirm.setNegativeButton(R.string.no,
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                // На нет и суда нет

                                            }

                                        });
                                confirm.show();
                        }
                    }
                });
                builder.create().show();

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
            case REQUEST_EDIT_COUNTER:
            case REQUEST_ADD_ENTRY:
                if (resultCode == RESULT_OK) {
                    mAdapter.getCursor().requery();
                }
                break;

            default:
                break;
        }
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
    private class CountersCursorAdapter extends SimpleCursorAdapter {

        public CountersCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);

            try {
                int color = cursor.getInt(cursor.getColumnIndex("color"));
                view.findViewById(R.id.vColor).setBackgroundColor(color);
            } catch (Exception e) {
                // Нет цвета
            }
        }

    }
}
