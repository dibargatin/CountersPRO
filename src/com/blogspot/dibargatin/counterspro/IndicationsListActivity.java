
package com.blogspot.dibargatin.counterspro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.blogspot.dibargatin.counterspro.R;
import com.blogspot.dibargatin.counterspro.database.Counter;
import com.blogspot.dibargatin.counterspro.database.CounterDAO;
import com.blogspot.dibargatin.counterspro.database.DBHelper;
import com.blogspot.dibargatin.counterspro.database.Indication;
import com.blogspot.dibargatin.counterspro.database.IndicationDAO;
import com.blogspot.dibargatin.counterspro.database.IndicationsListAdapter;
import com.blogspot.dibargatin.counterspro.graph.GraphSeries;
import com.blogspot.dibargatin.counterspro.graph.LineGraph;
import com.blogspot.dibargatin.counterspro.graph.GraphSeries.GraphData;
import com.blogspot.dibargatin.counterspro.graph.GraphSeries.GraphSeriesStyle;

public class IndicationsListActivity extends SherlockActivity {
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

    IndicationsListAdapter mAdapter;

    Counter mCounter;

    LineGraph mLineGraph;

    GraphSeriesStyle mLineGraphStyle;

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

        // Инициализация списка показаний
        mAdapter = new IndicationsListAdapter(this, mCounter.getIndications());
        ListView list = (ListView)findViewById(R.id.lvIndications);
        list.setAdapter(mAdapter);

        // Фон пустого списка показаний
        final View ev = View.inflate(this, R.layout.indication_list_empty, null);
        ev.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        ev.setVisibility(View.GONE);
        ((ViewGroup)list.getParent()).addView(ev);
        list.setEmptyView(ev);

        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                Intent intent = new Intent(IndicationsListActivity.this, IndicationActivity.class);
                intent.setAction(Intent.ACTION_EDIT);
                intent.putExtra(IndicationActivity.EXTRA_INDICATION_ID, id);
                intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, mCounter.getId());

                startActivityForResult(intent, REQUEST_EDIT_INDICATION);
            }
        });

        list.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, int pos, long id) {
                final long itemId = id;

                AlertDialog.Builder confirm = new AlertDialog.Builder(IndicationsListActivity.this);

                confirm.setTitle(R.string.action_entry_del_confirm);
                confirm.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        IndicationDAO dao = new IndicationDAO();
                        dao.deleteById(mDatabase, itemId);

                        mCounter.setIndications(dao.getAllByCounter(mDatabase, mCounter));
                        mAdapter.setItems(mCounter.getIndications());

                        refreshLineGraphData();
                        mLineGraph.postInvalidate();
                    }

                });
                confirm.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // На нет и суда нет

                    }

                });
                confirm.show();

                return true;
            }
        });

        // Рисуем график
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.rlHeader);
        mLineGraph = new LineGraph(this);

        mLineGraph.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        rl.addView(mLineGraph, 0);

        mLineGraphStyle = new GraphSeriesStyle();

        float[] hsv = new float[3];
        Color.colorToHSV(mCounter.getColor(), hsv);

        mLineGraphStyle.pointsColor = Color.HSVToColor(hsv);

        hsv[1] = 0.2f;
        mLineGraphStyle.graphColor = Color.HSVToColor(hsv);

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
                Intent intent = new Intent(IndicationsListActivity.this, IndicationActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
                intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, mCounter.getId());
                startActivityForResult(intent, REQUEST_ADD_INDICATION);
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
                    mAdapter.setItems(mCounter.getIndications());
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

                    setResult(RESULT_OK);
                }
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

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
    /*
     * private class EntriesCursorAdapter extends SimpleCursorAdapter { public
     * EntriesCursorAdapter(Context context, int layout, Cursor c, String[]
     * from, int[] to) { super(context, layout, c, from, to); }
     * @Override public void bindView(View view, Context context, Cursor cursor)
     * { // super.bindView(view, context, cursor); CharSequence m = "";
     * CharSequence r = ""; int rateType = 0; int periodType = 1; double v = 0;
     * double t = 0; NumberFormat nf =
     * NumberFormat.getNumberInstance(context.getResources()
     * .getConfiguration().locale); NumberFormat cnf =
     * NumberFormat.getCurrencyInstance(context.getResources()
     * .getConfiguration().locale); Currency cur = null; // Читаем вид тарифа
     * try { rateType = cursor.getInt(cursor.getColumnIndex("rate_type")); }
     * catch (Exception e) { // Не вышло прочитать вид тарифа } // Читаем
     * единицу измерения try { m =
     * Html.fromHtml(cursor.getString(cursor.getColumnIndex("measure"))); }
     * catch (Exception e) { // Не вышло прочитать единицу измерения } try { cur
     * = Currency.getInstance(m.toString()); } catch (Exception e) { // Не
     * валюта в формате ISO } // Устанавливаем единицу измерения try { TextView
     * measure = (TextView)view.findViewById(R.id.tvMeasure); if (cur == null) {
     * measure.setText(m); } else { measure.setVisibility(View.GONE); } } catch
     * (Exception e) { // Нет единицы измерения } // Читаем и устанавливаем
     * значение try { TextView value =
     * (TextView)view.findViewById(R.id.tvValue); v =
     * cursor.getDouble(cursor.getColumnIndex("value")); if (v < 0) {
     * value.setText(nf.format(v)); } else { value.setText("+" + nf.format(v));
     * } } catch (Exception e) { // Нет значения } // Читаем и устанавливаем
     * сумму try { TextView total = (TextView)view.findViewById(R.id.tvTotal); t
     * = cursor.getDouble(cursor.getColumnIndex("total")); if (cur == null) {
     * total.setText(nf.format(t)); } else { cnf.setCurrency(cur);
     * total.setText(cnf.format(t)); } } catch (Exception e) { // Нет суммы } //
     * Выводим инфорацию о тарифе и о затратах if (rateType == 1) { // Простой
     * тариф try { LinearLayout f =
     * (LinearLayout)view.findViewById(R.id.lFormula);
     * f.setVisibility(View.GONE); TextView c =
     * (TextView)view.findViewById(R.id.tvCurrency); TextView c2 =
     * (TextView)view.findViewById(R.id.tvCostCurrency); Currency rcur = null; r
     * = Html.fromHtml(cursor.getString(cursor.getColumnIndex("currency"))); try
     * { rcur = Currency.getInstance(r.toString()); } catch (Exception e) { //
     * Не в формате ISO } if (rcur == null) { c.setText(r); c2.setText(r); }
     * else { c.setVisibility(View.GONE); c2.setVisibility(View.GONE); }
     * TextView rate = (TextView)view.findViewById(R.id.tvRateValue); TextView
     * cost = (TextView)view.findViewById(R.id.tvCost); double rv =
     * cursor.getDouble(cursor.getColumnIndex("rate")); if (rcur == null) {
     * rate.setText(nf.format(rv)); cost.setText(nf.format(rv * v)); } else {
     * cnf.setCurrency(rcur); rate.setText(cnf.format(rv));
     * cost.setText(cnf.format(rv * v)); } } catch (Exception e) { // Не вышло
     * прочитать тариф } } else if (rateType == 2) { // Формула LinearLayout lri
     * = (LinearLayout)view.findViewById(R.id.lRateInfo);
     * lri.setVisibility(View.GONE); TextView cc =
     * (TextView)view.findViewById(R.id.tvCostCurrency); Currency rcur = null; r
     * = Html.fromHtml(cursor.getString(cursor.getColumnIndex("currency"))); try
     * { rcur = Currency.getInstance(r.toString()); } catch (Exception e) { //
     * Не в формате ISO } if (rcur == null) { cc.setText(r); } else {
     * cc.setVisibility(View.GONE); } TextView formula =
     * (TextView)view.findViewById(R.id.tvFormula); TextView cost =
     * (TextView)view.findViewById(R.id.tvCost); final FormulaEvaluator eval =
     * new FormulaEvaluator(mFormulaTotalAliases, t, mFormulaValueAliases, v,
     * mFormulaTariffAliases, cursor.getDouble(cursor .getColumnIndex("rate")));
     * try { String expression =
     * cursor.getString(cursor.getColumnIndex("formula"));
     * formula.setText(expression); double res = eval.evaluate(expression); if
     * (rcur == null) { cost.setText(nf.format(res)); } else {
     * cnf.setCurrency(rcur); cost.setText(cnf.format(res)); } } catch
     * (IllegalArgumentException e) {
     * cost.setText(getResources().getString(R.string
     * .error_evaluator_expression)); } } else { // Без тарифа LinearLayout l1 =
     * (LinearLayout)view.findViewById(R.id.lRateInfo); LinearLayout l2 =
     * (LinearLayout)view.findViewById(R.id.lCost); LinearLayout l3 =
     * (LinearLayout)view.findViewById(R.id.lFormula);
     * l1.setVisibility(View.GONE); l2.setVisibility(View.GONE);
     * l3.setVisibility(View.GONE); } // Читаем вид периода try { periodType =
     * cursor.getInt(cursor.getColumnIndex("period_type")); } catch (Exception
     * e) { // Не вышло прочитать вид периода } // Выводим дату и время
     * показания try { String entryDate =
     * cursor.getString(cursor.getColumnIndex("entry_date")); GregorianCalendar
     * c = (GregorianCalendar)Calendar.getInstance();
     * c.setTimeInMillis(java.sql.Timestamp.valueOf(entryDate).getTime());
     * TextView date = (TextView)view.findViewById(R.id.tvDate); TextView month
     * = (TextView)view.findViewById(R.id.tvMonth); final java.text.DateFormat
     * df = DateFormat.getDateInstance(DateFormat.SHORT,
     * getResources().getConfiguration().locale); final java.text.DateFormat tf
     * = android.text.format.DateFormat .getTimeFormat(EntryActivity.this);
     * switch (periodType) { case 0: // Год
     * month.setText(Integer.toString(c.get(Calendar.YEAR)) + " " +
     * getResources().getString(R.string.year));
     * date.setText(df.format(c.getTime()) + " " + tf.format(c.getTime()));
     * break; case 1: // Месяц String[] ml =
     * getResources().getStringArray(R.array.month_list);
     * month.setText(ml[c.get(Calendar.MONTH)]);
     * date.setText(df.format(c.getTime()) + " " + tf.format(c.getTime()));
     * break; case 2: // День month.setText(new SimpleDateFormat("EEEEEEE",
     * context.getResources() .getConfiguration().locale).format(c.getTime()));
     * date.setText(df.format(c.getTime()) + " " + tf.format(c.getTime()));
     * break; case 3: // Час case 4: // Минута
     * month.setText(tf.format(c.getTime()));
     * date.setText(df.format(c.getTime())); break; default: String[] ml2 =
     * getResources().getStringArray(R.array.month_list);
     * month.setText(ml2[c.get(Calendar.MONTH)]);
     * date.setText(df.format(c.getTime()) + " " + tf.format(c.getTime())); } }
     * catch (Exception e) { // Нет даты } } }
     */
}
