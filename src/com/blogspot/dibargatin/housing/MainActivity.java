
package com.blogspot.dibargatin.housing;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockListActivity {

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

        getSupportActionBar().setIcon(R.drawable.ic_menu_home);
        getSupportActionBar().setTitle(getResources().getString(R.string.counters));

        String[] from = new String[] {
                "name", "note", "value", "measure", "entry_date"
        };

        int[] to = new int[] {
                R.id.tvCounterName, R.id.tvCounterNote, R.id.tvValue, R.id.tvMeasure, R.id.tvPeriod
        };

        mAdapter = new CountersCursorAdapter(this, R.layout.counters_list_item,
                mDbHelper.fetchAllCounters(), from, to);
        getListView().setAdapter(mAdapter);

        final View ev = View.inflate(this, R.layout.counters_list_empty, null);
        ev.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        ev.setVisibility(View.GONE);
        ((ViewGroup)getListView().getParent()).addView(ev);
        getListView().setEmptyView(ev);

        // При нажатии на счетчик, перейдем к списку показаний
        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                Intent intent = new Intent(MainActivity.this, EntryActivity.class);

                intent.setAction(Intent.ACTION_EDIT);
                intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, id);

                startActivityForResult(intent, REQUEST_EDIT_COUNTER);
            }
        });

        // При долгом нажатии на счетчик отобразим диалог выбора
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

                                confirm.setTitle(R.string.action_counter_del_confirm);
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
        getSupportMenuInflater().inflate(R.menu.main, menu);
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
            case REQUEST_ADD_ENTRY:
                if (resultCode == RESULT_OK) {
                    mAdapter.getCursor().requery();
                }
                break;

            case REQUEST_EDIT_COUNTER:
                mAdapter.getCursor().requery();
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

            // Цвет ярлыка
            try {
                int color = cursor.getInt(cursor.getColumnIndex("color"));
                view.findViewById(R.id.vColor).setBackgroundColor(color);
            } catch (Exception e) {
                // Нет цвета
            }

            // Значение и единица измерения
            try {
                TextView value = (TextView)view.findViewById(R.id.tvValue);
                double v = cursor.getDouble(cursor.getColumnIndex("value"));

                TextView measure = (TextView)view.findViewById(R.id.tvMeasure);
                String m = cursor.getString(cursor.getColumnIndex("measure"));
                
                Currency cur = null;
                
                try {
                    cur = Currency.getInstance(m);
                } catch (Exception e) {
                    // Не в формате ISO
                }
                
                if (cur != null) {
                    NumberFormat cnf = NumberFormat.getCurrencyInstance(context.getResources()
                            .getConfiguration().locale);
                    cnf.setCurrency(cur);
                    value.setText(cnf.format(v));
                    measure.setVisibility(View.GONE);
                } else {
                    NumberFormat nf = NumberFormat
                            .getNumberInstance(context.getResources().getConfiguration().locale);
                    value.setText(nf.format(v));
                    measure.setText(Html.fromHtml(m));   
                    measure.setVisibility(View.VISIBLE);
                }
                
            } catch (Exception e) {
                // Нет значения
            }
            
            // Дата последнего показания
            try {
                TextView period = (TextView)view.findViewById(R.id.tvPeriod);
                String date = cursor.getString(cursor.getColumnIndex("entry_date"));
                Date d = new Date(java.sql.Timestamp.valueOf(date).getTime());

                final java.text.DateFormat df = DateFormat.getDateFormat(MainActivity.this);
                period.setText(df.format(d));
            } catch (Exception e) {
                // Нет показаний
            }
        }

    }
}
