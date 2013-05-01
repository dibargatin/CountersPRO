
package com.blogspot.dibargatin.housing;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.blogspot.dibargatin.housing.graph.GraphSeries;
import com.blogspot.dibargatin.housing.graph.GraphSeries.GraphData;
import com.blogspot.dibargatin.housing.graph.GraphSeries.GraphSeriesStyle;
import com.blogspot.dibargatin.housing.graph.LineGraph;
import com.blogspot.dibargatin.housing.util.FormulaEvaluator;

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

    String[] mFormulaValueAliases;

    String[] mFormulaDeltaAliases;

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
                final long itemId = id;

                AlertDialog.Builder confirm = new AlertDialog.Builder(EntryActivity.this);

                confirm.setTitle(R.string.action_entry_del_confirm);
                confirm.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mDbHelper.deleteEntry(itemId);
                        mAdapter.getCursor().requery();
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
        final int entryCount = ec.getCount();

        if (entryCount > 1) {
            RelativeLayout rl = (RelativeLayout)findViewById(R.id.rlHeader);

            LineGraph lg = new LineGraph(this);
            GraphSeriesStyle s = new GraphSeriesStyle();
            
            float[] hsv = new float[3];
            Color.colorToHSV(c.getInt(c.getColumnIndex("color")), hsv);
            hsv[1] = 0.2f;            
            s.graphColor = Color.HSVToColor(hsv);

            GraphData[] gd = new GraphData[entryCount];
            int indx = 0;

            ec.moveToFirst();
            do {
                String entryDate = ec.getString(ec.getColumnIndex("entry_date"));
                float x = java.sql.Timestamp.valueOf(entryDate).getTime();
                float y = ec.getFloat(ec.getColumnIndex("delta"));
                gd[indx++] = new GraphData(x, y);
            } while (ec.moveToNext());            

            lg.addSeries(new GraphSeries(gd, "", "", "", s));
            lg.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            rl.addView(lg, 0);
        }
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

            CharSequence m = "";
            CharSequence r = "";

            int rateType = 0;
            int periodType = 1;

            double v = 0;
            double d = 0;

            NumberFormat nf = NumberFormat.getNumberInstance(context.getResources()
                    .getConfiguration().locale);

            // Читаем и устанавливаем значение
            try {
                TextView value = (TextView)view.findViewById(R.id.tvValue);
                v = cursor.getDouble(cursor.getColumnIndex("value"));

                value.setText(nf.format(v));

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

            // Устанавливаем единицу измерения
            try {
                TextView measure = (TextView)view.findViewById(R.id.tvMeasure);
                TextView measure2 = (TextView)view.findViewById(R.id.tvMeasure2);
                TextView measure3 = (TextView)view.findViewById(R.id.tvMeasure3);

                measure.setText(m);
                measure2.setText(m);

                if (rateType == 1) { // Простой тариф
                    measure3.setText(m);
                } else {
                    measure3.setText("");
                }
            } catch (Exception e) {
                // Нет единицы измерения
            }

            // Выводим инфорацию о тарифе и о затратах
            if (rateType == 1) { // Простой тариф
                try {
                    TextView c = (TextView)view.findViewById(R.id.tvCurrency);
                    TextView c2 = (TextView)view.findViewById(R.id.tvCurrency2);

                    r = Html.fromHtml(cursor.getString(cursor.getColumnIndex("currency")));
                    c.setText(r);
                    c2.setText(r);

                    double rv = cursor.getDouble(cursor.getColumnIndex("rate"));

                    TextView rate = (TextView)view.findViewById(R.id.tvRateValue);
                    rate.setText(nf.format(rv));

                    double res = cursor.getDouble(cursor.getColumnIndex("cost"));

                    TextView cost = (TextView)view.findViewById(R.id.tvCost);
                    cost.setText(nf.format(res));

                } catch (Exception e) {
                    // Не вышло прочитать тариф
                }

            } else if (rateType == 2) { // Формула
                TextView c = (TextView)view.findViewById(R.id.tvCurrency);
                c.setText("");

                TextView rn = (TextView)view.findViewById(R.id.tvRateName);
                rn.setText(getResources().getString(R.string.formula));

                TextView rv = (TextView)view.findViewById(R.id.tvRateValue);

                TextView cost = (TextView)view.findViewById(R.id.tvCost);
                final FormulaEvaluator eval = new FormulaEvaluator(mFormulaValueAliases, v,
                        mFormulaDeltaAliases, d);

                try {
                    String expression = cursor.getString(cursor.getColumnIndex("formula"));
                    rv.setText(expression);

                    double res = eval.evaluate(expression);
                    cost.setText(nf.format(res));
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

                switch (periodType) {
                    case 0: // Год
                        month.setText(Integer.toString(c.get(Calendar.YEAR)) + " "
                                + getResources().getString(R.string.year));
                        date.setText(new SimpleDateFormat(getResources().getString(
                                R.string.date_time_format)).format(c.getTime()));
                        break;

                    case 1: // Месяц
                        String[] ml = getResources().getStringArray(R.array.month_list);
                        month.setText(ml[c.get(Calendar.MONTH)]);
                        date.setText(new SimpleDateFormat(getResources().getString(
                                R.string.date_time_format)).format(c.getTime()));

                        break;

                    case 2: // День
                        month.setText(new SimpleDateFormat("EEEEEEE", context.getResources()
                                .getConfiguration().locale).format(c.getTime()));
                        date.setText(new SimpleDateFormat(getResources().getString(
                                R.string.date_time_format)).format(c.getTime()));
                        break;

                    case 3: // Час
                    case 4: // Минута
                        month.setText(new SimpleDateFormat(getResources().getString(
                                R.string.time_format)).format(c.getTime()));
                        date.setText(new SimpleDateFormat(getResources().getString(
                                R.string.date_format)).format(c.getTime()));
                        break;

                    default:
                        String[] ml2 = getResources().getStringArray(R.array.month_list);
                        month.setText(ml2[c.get(Calendar.MONTH)]);
                        date.setText(new SimpleDateFormat(getResources().getString(
                                R.string.date_time_format)).format(c.getTime()));
                }
            } catch (Exception e) {
                // Нет даты
            }
        }

    }
}
