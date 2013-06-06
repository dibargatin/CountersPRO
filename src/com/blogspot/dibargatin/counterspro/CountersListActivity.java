
package com.blogspot.dibargatin.counterspro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.blogspot.dibargatin.counterspro.database.CounterDAO;
import com.blogspot.dibargatin.counterspro.database.CountersListAdapter;
import com.blogspot.dibargatin.counterspro.database.DBHelper;
import com.blogspot.dibargatin.counterspro.util.BackupUtils;

public class CountersListActivity extends SherlockListActivity implements OnClickListener {

    // ===========================================================
    // Constants
    // ===========================================================
    public final static String APP_NAME = "com.blogspot.dibargatin.counterspro";

    public final static String LOG_TAG = "CountersPRO";

    private final static int REQUEST_ADD_COUNTER = 1;

    private final static int REQUEST_EDIT_COUNTER = 2;

    private final static int REQUEST_ADD_ENTRY = 3;

    private final static int REQUEST_RESTORE = 4;

    private final static int MENU_BACKUP = 10;

    private final static int MENU_RESTORE = 15;

    private final static int MENU_FEEDBACK = 20;

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

        final ImageView iv = (ImageView)ev.findViewById(R.id.ivCounter);
        iv.setOnClickListener(this);

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
                                intent = new Intent(CountersListActivity.this,
                                        IndicationActivity.class);

                                intent.setAction(Intent.ACTION_INSERT);
                                intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, itemId);

                                startActivityForResult(intent, REQUEST_ADD_ENTRY);

                                break;

                            case 1: // Редактировать счетчик
                                intent = new Intent(CountersListActivity.this,
                                        CounterActivity.class);

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

        SubMenu sm = menu.addSubMenu(0, Menu.FIRST, Menu.NONE, R.string.menu_more);
        sm.add(0, MENU_BACKUP, Menu.NONE, R.string.menu_backup).setIcon(R.drawable.backup);
        sm.add(0, MENU_RESTORE, Menu.NONE, R.string.menu_restore).setIcon(R.drawable.reload);
        sm.add(0, MENU_FEEDBACK, Menu.NONE, R.string.menu_feedback).setIcon(
                android.R.drawable.star_on);

        MenuItem subMenu1Item = sm.getItem();
        subMenu1Item.setIcon(R.drawable.abs__ic_menu_moreoverflow_holo_light);
        subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean result = true;

        switch (item.getItemId()) {
            case R.id.action_add_counter:
                showAddCounterDialog();
                break;

            case MENU_BACKUP:
                if (checkSdCard()) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);

                    alert.setIcon(R.drawable.backup);
                    alert.setTitle(R.string.backup_form_title);
                    alert.setMessage(R.string.backup_form_description);

                    alert.setPositiveButton(getResources().getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    new BackupUtils(CountersListActivity.this).backup();
                                }
                            });

                    alert.setNegativeButton(getResources().getString(R.string.no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // На нет и суда нет
                                }
                            });

                    alert.show();
                }
                break;

            case MENU_RESTORE:
                if (checkSdCard()) {
                    Intent intent = new Intent(CountersListActivity.this, RestoreActivity.class);
                    startActivityForResult(intent, REQUEST_RESTORE);
                }
                break;

            case MENU_FEEDBACK:
                gotoStore(APP_NAME);
                break;

            default:
                result = super.onOptionsItemSelected(item);
        }

        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_RESTORE:
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivCounter:
                showAddCounterDialog();
                break;
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================
    private void showAddCounterDialog() {
        Intent intent = new Intent(CountersListActivity.this, CounterActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        startActivityForResult(intent, REQUEST_ADD_COUNTER);
    }

    private boolean checkSdCard() {
        boolean result = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        if (!result) {
            Toast.makeText(this, this.getResources().getString(R.string.sdcard_not_available),
                    Toast.LENGTH_LONG).show();
        }

        return result;
    }
    
    private void gotoStore(String appName) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse("market://details?id=" + appName)));
        } catch (android.content.ActivityNotFoundException anfe) {

            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id="
                            + appName)));
        }
    }
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
