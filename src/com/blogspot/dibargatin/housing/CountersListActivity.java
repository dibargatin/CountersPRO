
package com.blogspot.dibargatin.housing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.blogspot.dibargatin.housing.database.CounterDAO;
import com.blogspot.dibargatin.housing.database.CountersListAdapter;
import com.blogspot.dibargatin.housing.database.DBHelper;

public class CountersListActivity extends SherlockListActivity {

    // ===========================================================
    // Constants
    // ===========================================================
    public final static String LOG_TAG = "CountersPRO";

    private final static int REQUEST_ADD_COUNTER = 1;

    private final static int REQUEST_EDIT_COUNTER = 2;

    private final static int REQUEST_ADD_ENTRY = 3;

    // ===========================================================
    // Fields
    // ===========================================================
    SQLiteDatabase mDatabase;

    CounterDAO mCounterDao;

    CountersListAdapter mAdapter;

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

        mDatabase = new DBHelper(this).getWritableDatabase();
        mCounterDao = new CounterDAO();

        getSupportActionBar().setIcon(R.drawable.ic_menu_home);
        getSupportActionBar().setTitle(getResources().getString(R.string.counters));

        // Фон пустого списка счетчиков
        final View ev = View.inflate(this, R.layout.counters_list_empty, null);
        ev.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        ev.setVisibility(View.GONE);
        ((ViewGroup)getListView().getParent()).addView(ev);
        getListView().setEmptyView(ev);

        // Данные для списка
        if (mDatabase != null) {
            mAdapter = new CountersListAdapter(this, mCounterDao.getAll(mDatabase));
        } else {
            mAdapter = new CountersListAdapter(this, null);
        }
        getListView().setAdapter(mAdapter);

        // При нажатии на счетчик, перейдем к списку показаний
        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                Intent intent = new Intent(CountersListActivity.this, IndicationsListActivity.class);

                intent.setAction(Intent.ACTION_EDIT);
                intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, id);

                startActivityForResult(intent, REQUEST_EDIT_COUNTER);
            }
        });

        // При долгом нажатии на счетчик отобразим диалог выбора
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, int pos, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(CountersListActivity.this);
                final long itemId = id;

                builder.setTitle(R.string.action);
                builder.setItems(R.array.counter_actions, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent;

                        switch (which) {
                            case 0: // Добавить показания
                                intent = new Intent(CountersListActivity.this, IndicationActivity.class);

                                intent.setAction(Intent.ACTION_INSERT);
                                intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, itemId);

                                startActivityForResult(intent, REQUEST_ADD_ENTRY);

                                break;

                            case 1: // Редактировать счетчик
                                intent = new Intent(CountersListActivity.this, CounterActivity.class);

                                intent.setAction(Intent.ACTION_EDIT);
                                intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, itemId);

                                startActivityForResult(intent, REQUEST_EDIT_COUNTER);

                                break;

                            case 2: // Удалить счетчик
                                AlertDialog.Builder confirm = new AlertDialog.Builder(
                                        CountersListActivity.this);

                                confirm.setTitle(R.string.action_counter_del_confirm);
                                confirm.setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                if (mDatabase != null) {
                                                    mCounterDao.deleteById(mDatabase, itemId);
                                                    mAdapter.setItems(mCounterDao.getAll(mDatabase));
                                                }
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
                Intent intent = new Intent(CountersListActivity.this, CounterActivity.class);

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
                    if (mDatabase != null) {
                        mAdapter.setItems(mCounterDao.getAll(mDatabase));
                    }
                }
                break;

            case REQUEST_EDIT_COUNTER:
                if (mDatabase != null)
                    mAdapter.setItems(mCounterDao.getAll(mDatabase));
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
        super.onDestroy();
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
