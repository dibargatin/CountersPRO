
package com.blogspot.dibargatin.housing;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.blogspot.dibargatin.housing.graph.GraphSeries;
import com.blogspot.dibargatin.housing.graph.GraphSeries.GraphData;
import com.blogspot.dibargatin.housing.graph.GraphSeries.GraphSeriesStyle;
import com.blogspot.dibargatin.housing.graph.LineGraph;
import com.blogspot.dibargatin.housing.util.FormulaEvaluator;

public class EntryActivity extends SherlockActivity {
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

    String[] mFormulaValueAliases;

    String[] mFormulaDeltaAliases;
    
    String[] mFormulaTariffAliases;
    
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
        setContentView(R.layout.entries_form);

        mDbHelper = new DBHelper(this);

        mFormulaValueAliases = getResources().getStringArray(R.array.formula_var_value_aliases);
        mFormulaDeltaAliases = getResources().getStringArray(R.array.formula_var_delta_aliases);
        mFormulaTariffAliases = getResources().getStringArray(R.array.formula_var_tariff_aliases);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_menu_home);

        TextView name = (TextView)findViewById(R.id.tvCounterName);
        TextView note = (TextView)findViewById(R.id.tvCounterNote);
        View color = (View)findViewById(R.id.vColor);

        Intent intent = getIntent();
        mCounterId = intent.getLongExtra(CounterActivity.EXTRA_COUNTER_ID, -1);

        Cursor c = mDbHelper.fetchCounterById(mCounterId);
        c.moveToFirst();
        name.setText(c.getString(c.getColumnIndex("name")));
        note.setText(c.getString(c.getColumnIndex("note")));
        color.setBackgroundColor(c.getInt(c.getColumnIndex("color")));

        String[] from = new String[] {
                "entry_date", "value", "delta", "cost", "rate", "measure", "measure", "measure"
        };
        int[] to = new int[] {
                R.id.tvDate, R.id.tvValue, R.id.tvDelta, R.id.tvCost, R.id.tvRateValue,
                R.id.tvMeasure, R.id.tvMeasure2, R.id.tvMeasure3
        };

        final Cursor ec = mDbHelper.fetchEntriesByCounterId(mCounterId);
        mAdapter = new EntriesCursorAdapter(this, R.layout.entry_list_item, ec, from, to);

        ListView list = (ListView)findViewById(R.id.listView1);
        list.setAdapter(mAdapter);
        
        final View ev = View.inflate(this, R.layout.entry_list_empty, null);        
        ev.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        ev.setVisibility(View.GONE);
        ((ViewGroup)list.getParent()).addView(ev);
        list.setEmptyView(ev);
                
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                Intent intent = new Intent(EntryActivity.this, EntryEditActivity.class);
                intent.setAction(Intent.ACTION_EDIT);
                intent.putExtra(EntryEditActivity.EXTRA_ENTRY_ID, id);
                intent.putExtra(CounterActivity.EXTRA_COUNTER_ID, mCounterId);

                startActivityForResult(intent, REQUEST_EDIT_ENTRY);
            }
        });

        list.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, int pos, long id) {
                final long itemId = id;

                AlertDialog.Builder confirm = new AlertDialog.Builder(EntryActivity.this);

                confirm.setTitle(R.string.action_entry_del_confirm);
                confirm.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mDbHelper.deleteEntry(itemId);

                        final Cursor c = mAdapter.getCursor();
                        c.requery();
                        refreshLineGraphData(c);
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
        Color.colorToHSV(c.getInt(c.getColumnIndex("color")), hsv);
        
        mLineGraphStyle.pointsColor = Color.HSVToColor(hsv);
        
        hsv[1] = 0.2f;
        mLineGraphStyle.graphColor = Color.HSVToColor(hsv);

        refreshLineGraphData(ec);
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
                    final Cursor c = mAdapter.getCursor();
                    c.requery();
                    refreshLineGraphData(c);
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
    private synchronized void refreshLineGraphData(Cursor c) {
        
        mLineGraph.clearSeries();
        
        final int entryCount = c.getCount();        

        if (entryCount > 1) {
            GraphData[] gd = new GraphData[entryCount];
            int indx = 0;

            c.moveToFirst();
            do {
                String entryDate = c.getString(c.getColumnIndex("entry_date"));
                double x = java.sql.Timestamp.valueOf(entryDate).getTime();
                double y = c.getDouble(c.getColumnIndex("value"));
                gd[indx++] = new GraphData(x, y);
            } while (c.moveToNext());
            
            mLineGraph.addSeries(new GraphSeries(gd, "", "", "", mLineGraphStyle));
        }
    }

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

            CharSequence m = "";
            CharSequence r = "";

            int rateType = 0;
            int periodType = 1;

            double v = 0;
            double d = 0;

            NumberFormat nf = NumberFormat.getNumberInstance(context.getResources()
                    .getConfiguration().locale);
            
            NumberFormat cnf = NumberFormat.getCurrencyInstance(context.getResources()
                    .getConfiguration().locale);
            
            Currency cur = null;
            
            // Читаем вид тарифа
            try {
                rateType = cursor.getInt(cursor.getColumnIndex("rate_type"));
            } catch (Exception e) {
                // Не вышло прочитать вид тарифа
            }
            
            // Читаем единицу измерения
            try {
                m = Html.fromHtml(cursor.getString(cursor.getColumnIndex("measure")));
            } catch (Exception e) {
                // Не вышло прочитать единицу измерения
            }
            
            try {
                cur = Currency.getInstance(m.toString());
            } catch (Exception e) {
                // Не валюта в формате ISO
            }
            
            // Устанавливаем единицу измерения
            try {
                TextView measure = (TextView)view.findViewById(R.id.tvMeasure);
                TextView measure2 = (TextView)view.findViewById(R.id.tvMeasure2);
                TextView measure3 = (TextView)view.findViewById(R.id.tvMeasure3);
                
                if (cur == null) {
                    measure.setText(m);
                    measure2.setText(m);
                } else {
                    measure.setVisibility(View.GONE);
                    measure2.setVisibility(View.GONE);
                }

                if (rateType == 1) { // Простой тариф
                    if (cur == null) {
                        measure3.setText(m);
                    } else {
                        measure3.setText(cur.getSymbol());
                    }
                } else {
                    measure3.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                // Нет единицы измерения
            }
            
            // Читаем и устанавливаем значение
            try {
                TextView value = (TextView)view.findViewById(R.id.tvValue);
                v = cursor.getDouble(cursor.getColumnIndex("value"));
                
                if (cur == null) {
                    value.setText(nf.format(v));
                } else {
                    cnf.setCurrency(cur);
                    value.setText(cnf.format(v));
                } 

            } catch (Exception e) {
                // Нет значения
            }

            // Читаем и устанавливаем дельту
            try {
                TextView delta = (TextView)view.findViewById(R.id.tvDelta);
                d = cursor.getDouble(cursor.getColumnIndex("delta"));
                
                if (d < 0) {
                    delta.setText(nf.format(d));
                } else {
                    delta.setText("+" + nf.format(d));
                }
            } catch (Exception e) {
                // Нет дельты
            }
            
            // Выводим инфорацию о тарифе и о затратах
            if (rateType == 1) { // Простой тариф
                try {
                    TextView c = (TextView)view.findViewById(R.id.tvCurrency);
                    TextView c2 = (TextView)view.findViewById(R.id.tvCurrency2);
                    
                    Currency rcur = null;
                    r = Html.fromHtml(cursor.getString(cursor.getColumnIndex("currency")));
                    
                    try {
                        rcur = Currency.getInstance(r.toString());
                    } catch (Exception e) {
                        // Не в формате ISO
                    }
                    
                    if (rcur == null) {
                        c.setText(r);
                        c2.setText(r);
                    } else {
                        c.setVisibility(View.GONE);
                        c2.setVisibility(View.GONE);
                    }

                    double rv = cursor.getDouble(cursor.getColumnIndex("rate"));
                    TextView rate = (TextView)view.findViewById(R.id.tvRateValue);
                    
                    double res = cursor.getDouble(cursor.getColumnIndex("cost"));
                    TextView cost = (TextView)view.findViewById(R.id.tvCost);
                    
                    if (rcur == null) {
                        rate.setText(nf.format(rv));
                        cost.setText(nf.format(res));
                    } else {
                        cnf.setCurrency(rcur);
                        rate.setText(cnf.format(rv));
                        cost.setText(cnf.format(res));
                    }
                    
                } catch (Exception e) {
                    // Не вышло прочитать тариф
                }

            } else if (rateType == 2) { // Формула
                TextView c = (TextView)view.findViewById(R.id.tvCurrency);
                c.setVisibility(View.GONE);
                
                TextView c2 = (TextView)view.findViewById(R.id.tvCurrency2);
                
                Currency rcur = null;
                r = Html.fromHtml(cursor.getString(cursor.getColumnIndex("currency")));
                
                try {
                    rcur = Currency.getInstance(r.toString());
                } catch (Exception e) {
                    // Не в формате ISO
                }
                
                if (rcur == null) {                    
                    c2.setText(r);
                } else {                    
                    c2.setVisibility(View.GONE);
                }

                TextView rn = (TextView)view.findViewById(R.id.tvRateName);
                rn.setText(getResources().getString(R.string.formula));

                TextView rv = (TextView)view.findViewById(R.id.tvRateValue);
                TextView cost = (TextView)view.findViewById(R.id.tvCost);
                
                final FormulaEvaluator eval = new FormulaEvaluator(mFormulaValueAliases, v,
                        mFormulaDeltaAliases, d, mFormulaTariffAliases, cursor.getDouble(cursor.getColumnIndex("rate")));

                try {
                    String expression = cursor.getString(cursor.getColumnIndex("formula"));
                    rv.setText(expression);

                    double res = eval.evaluate(expression);
                    
                    if (rcur == null) {
                        cost.setText(nf.format(res));
                    } else {
                        cnf.setCurrency(rcur);
                        cost.setText(cnf.format(res));
                    }                        
                } catch (IllegalArgumentException e) {
                    cost.setText(getResources().getString(R.string.error_evaluator_expression));
                }

            } else { // Без тарифа
                LinearLayout l1 = (LinearLayout)view.findViewById(R.id.lRateInfo);
                LinearLayout l2 = (LinearLayout)view.findViewById(R.id.lCost);

                l1.setVisibility(View.GONE);
                l2.setVisibility(View.GONE);
            }

            // Читаем вид периода
            try {
                periodType = cursor.getInt(cursor.getColumnIndex("period_type"));
            } catch (Exception e) {
                // Не вышло прочитать вид периода
            }

            // Выводим дату и время показания
            try {
                String entryDate = cursor.getString(cursor.getColumnIndex("entry_date"));
                GregorianCalendar c = (GregorianCalendar)Calendar.getInstance();
                c.setTimeInMillis(java.sql.Timestamp.valueOf(entryDate).getTime());

                TextView date = (TextView)view.findViewById(R.id.tvDate);
                TextView month = (TextView)view.findViewById(R.id.tvMonth);
                 
                final java.text.DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, getResources().getConfiguration().locale);
                final java.text.DateFormat tf = android.text.format.DateFormat.getTimeFormat(EntryActivity.this);                
                
                switch (periodType) {
                    case 0: // Год
                        month.setText(Integer.toString(c.get(Calendar.YEAR)) + " "
                                + getResources().getString(R.string.year));
                        date.setText(df.format(c.getTime()) + " " + tf.format(c.getTime()));
                        break;

                    case 1: // Месяц
                        String[] ml = getResources().getStringArray(R.array.month_list);
                        month.setText(ml[c.get(Calendar.MONTH)]);
                        date.setText(df.format(c.getTime()) + " " + tf.format(c.getTime()));
                        break;

                    case 2: // День
                        month.setText(new SimpleDateFormat("EEEEEEE", context.getResources()
                                .getConfiguration().locale).format(c.getTime()));
                        date.setText(df.format(c.getTime()) + " " + tf.format(c.getTime()));
                        break;

                    case 3: // Час
                    case 4: // Минута
                        month.setText(tf.format(c.getTime()));
                        date.setText(df.format(c.getTime()));
                        break;

                    default:
                        String[] ml2 = getResources().getStringArray(R.array.month_list);
                        month.setText(ml2[c.get(Calendar.MONTH)]);
                        date.setText(df.format(c.getTime()) + " " + tf.format(c.getTime()));
                }
            } catch (Exception e) {
                // Нет даты
            }
        }

    }
}
