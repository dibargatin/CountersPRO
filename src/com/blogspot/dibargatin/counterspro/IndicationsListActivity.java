
package com.blogspot.dibargatin.counterspro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.blogspot.dibargatin.counterspro.database.Counter;
import com.blogspot.dibargatin.counterspro.database.CounterDAO;
import com.blogspot.dibargatin.counterspro.database.CountersCollection;
import com.blogspot.dibargatin.counterspro.database.CountersListAdapter;
import com.blogspot.dibargatin.counterspro.database.DBHelper;
import com.blogspot.dibargatin.counterspro.database.Indication;
import com.blogspot.dibargatin.counterspro.database.IndicationDAO;
import com.blogspot.dibargatin.counterspro.database.IndicationsCollection;
import com.blogspot.dibargatin.counterspro.database.IndicationsExpandableListAdapter;
import com.blogspot.dibargatin.counterspro.graph.GraphSeries;
import com.blogspot.dibargatin.counterspro.graph.GraphSeries.GraphData;
import com.blogspot.dibargatin.counterspro.graph.GraphSeries.GraphSeriesStyle;
import com.blogspot.dibargatin.counterspro.graph.LineGraph;
import com.blogspot.dibargatin.counterspro.util.CopyIndicationUtils;
import com.blogspot.dibargatin.counterspro.util.CsvUtils;

public class IndicationsListActivity extends SherlockActivity implements OnClickListener {
    // ===========================================================
    // Constants
    // ===========================================================
    private final static int REQUEST_EDIT_COUNTER = 1;

    private final static int REQUEST_ADD_INDICATION = 2;

    private final static int REQUEST_EDIT_INDICATION = 3;

    private static final String LIST_STATE_KEY = "listState";

    private static final String LIST_POSITION_KEY = "listPosition";

    private static final String ITEM_POSITION_KEY = "itemPosition";

    private final static int MENU_EXPORT = 10;

    private final static int MENU_IMPORT = 15;

    private final static int MENU_COPY = 20;

    private final static int MENU_DELETE = 25;

    // ===========================================================
    // Fields
    // ===========================================================
    SQLiteDatabase mDatabase;

    IndicationsExpandableListAdapter mGroupAdapter;

    Counter mCounter;

    LineGraph mLineGraph;

    GraphSeriesStyle mLineGraphStyle;

    ExpandableListView mExpandableList;

    private Parcelable mListState = null;

    private int mListPosition = 0;

    private int mItemPosition = 0;

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
        setContentView(R.layout.indication_list_form);

        mDatabase = new DBHelper(this).getWritableDatabase();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_menu_home);

        Intent intent = getIntent();
        final long counterId = intent.getLongExtra(CounterActivity.EXTRA_COUNTER_ID, -1);

        if (mDatabase != null) {
            mCounter = new CounterDAO().getById(mDatabase, counterId);
        }

        // Заполним заголовок
        TextView name = (TextView)findViewById(R.id.tvCounterName);
        TextView note = (TextView)findViewById(R.id.tvCounterNote);
        View color = (View)findViewById(R.id.vColor);

        name.setText(Html.fromHtml(mCounter.getName()));
        note.setText(Html.fromHtml(mCounter.getNote()));
        color.setBackgroundColor(mCounter.getColor());

        final LinearLayout mainLayout = (LinearLayout)findViewById(R.id.lMain);

        // Инициализация списка показаний
        mExpandableList = new ExpandableListView(this);
        mExpandableList.setLayoutParams(new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mExpandableList.setGroupIndicator(getResources().getDrawable(
                R.drawable.list_view_group_indicator));

        mExpandableList.setDivider(getResources().getDrawable(R.drawable.list_view_item_divider));
        mExpandableList.setDividerHeight(1);

        mainLayout.addView(mExpandableList);

        // Фон пустого списка показаний
        final View ev = View.inflate(this, R.layout.indication_list_empty, null);

        final ImageView iv = (ImageView)ev.findViewById(R.id.ivCounter);
        iv.setOnClickListener(this);

        ev.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        ev.setVisibility(View.GONE);
        ((ViewGroup)mExpandableList.getParent()).addView(ev);
        mExpandableList.setEmptyView(ev);

        // Адаптер для списка
        mGroupAdapter = new IndicationsExpandableListAdapter(this, mCounter.getIndications(),
                mCounter);
        mExpandableList.setAdapter(mGroupAdapter);

        if (mGroupAdapter.getGroupCount() > 0) {
            mExpandableList.expandGroup(0);
        }

        // Обработчики нажатий на элементы списка
        mExpandableList.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                    int childPosition, long id) {

                Intent intent = new Intent(IndicationsListActivity.this, IndicationActivity.class);
                intent.setAction(Intent.ACTION_EDIT);
                intent.putExtra(IndicationActivity.EXTRA_INDICATION_ID, id);
                intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, mCounter.getId());

                startActivityForResult(intent, REQUEST_EDIT_INDICATION);
                return true;
            }
        });

        mExpandableList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, int pos, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    long packedPos = ((ExpandableListView)a).getExpandableListPosition(pos);
                    int groupPosition = ExpandableListView.getPackedPositionGroup(packedPos);
                    int childPosition = ExpandableListView.getPackedPositionChild(packedPos);

                    final Indication ind = mGroupAdapter
                            .getIndication(groupPosition, childPosition);

                    if (ind != null) {

                        final long itemId = ind.getId();

                        AlertDialog.Builder confirm = new AlertDialog.Builder(
                                IndicationsListActivity.this);

                        confirm.setTitle(R.string.action_entry_del_confirm);
                        confirm.setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        IndicationDAO dao = new IndicationDAO();
                                        dao.deleteById(mDatabase, itemId);

                                        mCounter.setIndications(dao.getAllByCounter(mDatabase,
                                                mCounter));

                                        mGroupAdapter.setSource(mCounter.getIndications(), true);

                                        refreshLineGraphData();
                                        mLineGraph.postInvalidate();
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

                return true;
            }
        });

        // Кнопка "Настроки счетчика"
        ImageView btnSettings = (ImageView)findViewById(R.id.ivSettings);
        btnSettings.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.setBackgroundColor(0x770000FF);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        v.setBackgroundColor(0x00000000);
                        v.invalidate();
                        break;
                    }
                }

                return false;
            }
        });

        btnSettings.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IndicationsListActivity.this, CounterActivity.class);

                intent.setAction(Intent.ACTION_EDIT);
                intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, mCounter.getId());
                startActivityForResult(intent, REQUEST_EDIT_COUNTER);
            }
        });

        // Рисуем график
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.rlHeader);
        mLineGraph = new LineGraph(this);

        mLineGraph.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        rl.addView(mLineGraph, 0);

        mLineGraphStyle = new GraphSeriesStyle();

        refreshLineGraphStyle();
        refreshLineGraphData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.entry_form, menu);

        SubMenu sm = menu.addSubMenu(0, Menu.FIRST, Menu.NONE, R.string.menu_more);
        sm.add(0, MENU_EXPORT, Menu.NONE, R.string.menu_export).setIcon(R.drawable.ic_export);
        // sm.add(0, MENU_IMPORT, Menu.NONE,
        // R.string.menu_import).setIcon(R.drawable.ic_import); // TODO import
        // function
        sm.add(0, MENU_COPY, Menu.NONE, R.string.menu_copy_to).setIcon(R.drawable.ic_copy);
        sm.add(0, MENU_DELETE, Menu.NONE, R.string.menu_delete_all).setIcon(R.drawable.ic_delete);

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
            case android.R.id.home:
                finish();
                break;

            case R.id.action_add_entry:
                showAddIndicationDialog();
                break;

            case MENU_EXPORT:
                final CsvUtils csv = new CsvUtils(this);
                csv.export(mCounter, mCounter.getIndications());
                break;

            case MENU_IMPORT:
                // TODO import function
                break;

            case MENU_COPY:
                showCopyToDialog();
                break;

            case MENU_DELETE:
                showDeleteAllDialog();
                break;

            default:
                result = super.onOptionsItemSelected(item);
        }

        return result;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_INDICATION:
            case REQUEST_EDIT_INDICATION:
                if (resultCode == RESULT_OK) {
                    IndicationDAO dao = new IndicationDAO();
                    mCounter.setIndications(dao.getAllByCounter(mDatabase, mCounter));
                    mGroupAdapter.setSource(mCounter.getIndications(), true);

                    if (mGroupAdapter.getGroupCount() == 1) {
                        mExpandableList.expandGroup(0);
                    }

                    refreshLineGraphData();
                }
                break;

            case REQUEST_EDIT_COUNTER:
                if (resultCode == RESULT_OK) {
                    if (mDatabase != null) {
                        mCounter = new CounterDAO().getById(mDatabase, mCounter.getId());
                    }

                    // Заполним заголовок
                    TextView name = (TextView)findViewById(R.id.tvCounterName);
                    TextView note = (TextView)findViewById(R.id.tvCounterNote);
                    View color = (View)findViewById(R.id.vColor);

                    name.setText(Html.fromHtml(mCounter.getName()));
                    note.setText(Html.fromHtml(mCounter.getNote()));
                    color.setBackgroundColor(mCounter.getColor());

                    mGroupAdapter.setSource(mCounter.getIndications(), false);
                    mGroupAdapter.setSettings(mCounter);

                    refreshLineGraphStyle();
                    refreshLineGraphData();

                    setResult(RESULT_OK);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivCounter:
                showAddIndicationDialog();
                break;
        }
    }

    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        mListState = state.getParcelable(LIST_STATE_KEY);
        mListPosition = state.getInt(LIST_POSITION_KEY);
        mItemPosition = state.getInt(ITEM_POSITION_KEY);
    }

    protected void onResume() {
        super.onResume();

        IndicationDAO dao = new IndicationDAO();
        mCounter.setIndications(dao.getAllByCounter(mDatabase, mCounter));
        mGroupAdapter.setSource(mCounter.getIndications(), true);

        if (mListState != null)
            mExpandableList.onRestoreInstanceState(mListState);

        mExpandableList.setSelectionFromTop(mListPosition, mItemPosition);
    }

    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        mListState = mExpandableList.onSaveInstanceState();
        state.putParcelable(LIST_STATE_KEY, mListState);

        mListPosition = mExpandableList.getFirstVisiblePosition();
        state.putInt(LIST_POSITION_KEY, mListPosition);

        View itemView = mExpandableList.getChildAt(0);
        mItemPosition = itemView == null ? 0 : itemView.getTop();
        state.putInt(ITEM_POSITION_KEY, mItemPosition);
    }

    // ===========================================================
    // Methods
    // ===========================================================
    private synchronized void refreshLineGraphData() {

        mLineGraph.clearSeries();

        final int entryCount = mCounter.getIndications().size();

        if (entryCount > 1) {
            GraphData[] gd = new GraphData[entryCount];
            int indx = 0;

            for (Indication i : mCounter.getIndications()) {
                double x = i.getDate().getTime();
                double y = i.getTotal();
                gd[indx++] = new GraphData(x, y);
            }

            mLineGraph.addSeries(new GraphSeries(gd, "", "", "", mLineGraphStyle));
        }

        mLineGraph.invalidate();
    }

    private synchronized void refreshLineGraphStyle() {
        float[] hsv = new float[3];
        Color.colorToHSV(mCounter.getColor(), hsv);

        mLineGraphStyle.pointsColor = Color.HSVToColor(hsv);

        hsv[1] = 0.2f;
        mLineGraphStyle.graphColor = Color.HSVToColor(hsv);
    }

    private void showAddIndicationDialog() {
        Intent intent = new Intent(IndicationsListActivity.this, IndicationActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, mCounter.getId());
        startActivityForResult(intent, REQUEST_ADD_INDICATION);
    }

    private void showCopyToDialog() {
        // Если нечего копировать
        if (mCounter.getIndications().size() < 1) {
            Toast.makeText(IndicationsListActivity.this,
                    getResources().getString(R.string.list_of_indications_is_empty),
                    Toast.LENGTH_LONG).show();
            return;
        }
        
        // Готовим данные для диалога
        final CounterDAO dao = new CounterDAO();
        final CountersCollection counters = dao.getAll(mDatabase);

        if (counters.size() < 2) {
            Toast.makeText(IndicationsListActivity.this,
                    getResources().getString(R.string.copy_to_empty_destination), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        for (Counter cnt : counters) {
            if (cnt.getId() == mCounter.getId()) {
                counters.remove(cnt);
                break;
            }
        }

        final CountersListAdapter adapter = new CountersListAdapter(this, counters, true, false);

        // Формируем диалог
        AlertDialog.Builder dialog = new AlertDialog.Builder(IndicationsListActivity.this);

        final ListView lv = new ListView(IndicationsListActivity.this);
        lv.setAdapter(adapter);

        dialog.setTitle(R.string.copy_to_choice_counter);
        dialog.setView(lv).setInverseBackgroundForced(true);

        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                CountersCollection choice = new CountersCollection();

                // Формируем список выбранных счетчиков
                for (int i = 0; i < lv.getChildCount(); i++) {
                    CheckBox cb = (CheckBox)lv.getChildAt(i).findViewById(R.id.checkBox);

                    if (cb != null) {
                        if (cb.isChecked()) {
                            choice.add((Counter)lv.getItemAtPosition(i));
                        }
                    }
                }

                if (choice.size() > 0) {
                    final IndicationsCollection result = new IndicationsCollection();
                    final CountersCollection trouble = new CountersCollection();

                    for (Counter destination : choice) {
                        IndicationsCollection withEqualDate = CopyIndicationUtils
                                .checkForEqualDate(mCounter.getIndications(),
                                        destination.getIndications());

                        // Если есть показания с одинаковыми периодами, то
                        // пропускаем счетчик
                        if (withEqualDate.size() > 0) {
                            trouble.add(destination);
                            continue;
                        }

                        // Копируем показания в буфер результата
                        for (Indication ind : mCounter.getIndications()) {
                            result.add(new Indication(ind, destination));
                        }
                    }

                    // Если есть счетчики в которые не выйдет скопировать
                    // показания, спросим пользователя как нам быть
                    if (trouble.size() > 0) {
                        AlertDialog.Builder confirm = new AlertDialog.Builder(
                                IndicationsListActivity.this);

                        final CountersListAdapter adapterTrouble = new CountersListAdapter(
                                IndicationsListActivity.this, trouble, false, false);
                        final ListView lvTrouble = new ListView(IndicationsListActivity.this);
                        lvTrouble.setAdapter(adapterTrouble);

                        confirm.setTitle(R.string.copy_to_cant_be_completed);
                        confirm.setView(lvTrouble).setInverseBackgroundForced(true);

                        if (counters.size() > trouble.size()) {
                            confirm.setPositiveButton(R.string.continue_action,
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Копируем показания
                                            IndicationDAO idao = new IndicationDAO();

                                            if (idao.massInsert(mDatabase, result)) {
                                                Toast.makeText(
                                                        IndicationsListActivity.this,
                                                        getResources().getString(
                                                                R.string.copy_to_ok),
                                                        Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(
                                                        IndicationsListActivity.this,
                                                        getResources().getString(
                                                                R.string.copy_to_trouble),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }

                        final int labelId = counters.size() > trouble.size() ? R.string.cancel_action
                                : R.string.ok;

                        confirm.setNegativeButton(labelId, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Toast.makeText(IndicationsListActivity.this,
                                        getResources().getString(R.string.copy_to_canceled),
                                        Toast.LENGTH_LONG).show();

                            }
                        });

                        confirm.show();

                    } else {

                        // Копируем показания
                        IndicationDAO idao = new IndicationDAO();

                        if (idao.massInsert(mDatabase, result)) {
                            Toast.makeText(IndicationsListActivity.this,
                                    getResources().getString(R.string.copy_to_ok),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(IndicationsListActivity.this,
                                    getResources().getString(R.string.copy_to_trouble),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

        });

        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // На нет и суда нет

            }

        });

        dialog.show();
    }

    private void showDeleteAllDialog() {
        // Если нечего удалять
        if (mCounter.getIndications().size() < 1) {
            Toast.makeText(IndicationsListActivity.this,
                    getResources().getString(R.string.list_of_indications_is_empty),
                    Toast.LENGTH_LONG).show();
            return;
        }
        
        // Готовим диалог
        AlertDialog.Builder dialog = new AlertDialog.Builder(IndicationsListActivity.this);
        
        dialog.setTitle(R.string.delete_all_confirm);
        
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                
                IndicationDAO dao = new IndicationDAO();
                dao.deleteByCounterId(mDatabase, mCounter.getId());

                mCounter.setIndications(new IndicationsCollection());

                mGroupAdapter.setSource(mCounter.getIndications(), true);

                refreshLineGraphData();
                mLineGraph.postInvalidate();                
            }
            
        });
        
        dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                
                // На нет и суда нет
                
            }
            
        });
        
        dialog.show();
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
