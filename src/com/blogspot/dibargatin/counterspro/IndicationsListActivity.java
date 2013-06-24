
package com.blogspot.dibargatin.counterspro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.blogspot.dibargatin.counterspro.database.Counter;
import com.blogspot.dibargatin.counterspro.database.CounterDAO;
import com.blogspot.dibargatin.counterspro.database.DBHelper;
import com.blogspot.dibargatin.counterspro.database.Indication;
import com.blogspot.dibargatin.counterspro.database.IndicationDAO;
import com.blogspot.dibargatin.counterspro.database.IndicationsExpandableListAdapter;
import com.blogspot.dibargatin.counterspro.graph.GraphSeries;
import com.blogspot.dibargatin.counterspro.graph.GraphSeries.GraphData;
import com.blogspot.dibargatin.counterspro.graph.GraphSeries.GraphSeriesStyle;
import com.blogspot.dibargatin.counterspro.graph.LineGraph;

public class IndicationsListActivity extends SherlockActivity implements OnClickListener {
    // ===========================================================
    // Constants
    // ===========================================================
    private final static int REQUEST_EDIT_COUNTER = 1;

    private final static int REQUEST_ADD_INDICATION = 2;

    private final static int REQUEST_EDIT_INDICATION = 3;

    // ===========================================================
    // Fields
    // ===========================================================
    SQLiteDatabase mDatabase;

    IndicationsExpandableListAdapter mGroupAdapter;

    Counter mCounter;

    LineGraph mLineGraph;

    GraphSeriesStyle mLineGraphStyle;

    ExpandableListView mExpandableList;

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
                mCounter.getIndicationsGroupType(), getGroupItemColor(), mCounter.getPeriodType());
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.action_add_entry:
                showAddIndicationDialog();
                break;
        }

        return true;
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

                    mGroupAdapter.setGroupItemColor(getGroupItemColor());
                    mGroupAdapter.setGroupType(mCounter.getIndicationsGroupType());
                    mGroupAdapter.setPeriodType(mCounter.getPeriodType());
                    mGroupAdapter.setSource(mCounter.getIndications(), true);
                    
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
    }

    private synchronized void refreshLineGraphStyle() {
        float[] hsv = new float[3];
        Color.colorToHSV(mCounter.getColor(), hsv);

        mLineGraphStyle.pointsColor = Color.HSVToColor(hsv);

        hsv[1] = 0.2f;
        mLineGraphStyle.graphColor = Color.HSVToColor(hsv);
    }

    private int getGroupItemColor() {
        float[] hsv = new float[3];

        Color.colorToHSV(mCounter.getColor(), hsv);
        hsv[1] = 0.1f;

        return Color.HSVToColor(hsv);
    }

    private void showAddIndicationDialog() {
        Intent intent = new Intent(IndicationsListActivity.this, IndicationActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, mCounter.getId());
        startActivityForResult(intent, REQUEST_ADD_INDICATION);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
