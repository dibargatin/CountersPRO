
package com.blogspot.dibargatin.counterspro.database;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;

import android.content.Context;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.blogspot.dibargatin.counterspro.R;
import com.blogspot.dibargatin.counterspro.database.Counter.ViewValueType;

public class CountersListAdapter extends BaseAdapter {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================
    Context mContext;

    LayoutInflater mInflater;

    CountersCollection mItems;

    String[] mFormulaValueAliases;

    String[] mFormulaTotalAliases;

    String[] mFormulaRateAliases;

    // ===========================================================
    // Constructors
    // ===========================================================
    public CountersListAdapter(Context context, CountersCollection items) {
        mContext = context;
        mItems = items;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mFormulaValueAliases = mContext.getResources().getStringArray(
                R.array.formula_var_value_aliases);
        mFormulaTotalAliases = mContext.getResources().getStringArray(
                R.array.formula_var_total_aliases);
        mFormulaRateAliases = mContext.getResources().getStringArray(
                R.array.formula_var_tariff_aliases);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public Counter getCounter(int position) {
        return mItems != null ? mItems.get(position) : null;
    }

    public void setItems(CountersCollection items) {
        mItems = items;
        notifyDataSetChanged();
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getCounter(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.counters_list_item, parent, false);
        }

        Counter cnt = getCounter(position);

        if (cnt == null) {
            return view;
        }

        // Цвет ярлыка
        try {
            view.findViewById(R.id.vColor).setBackgroundColor(cnt.getColor());
        } catch (Exception e) {
            // Нет ярлыка
        }

        // Название
        try {
            ((TextView)view.findViewById(R.id.tvCounterName)).setText(Html.fromHtml(cnt.getName()
                    .replace("\n", "<br/>")));
        } catch (Exception e) {
            // Нет названия
        }

        // Описание
        try {
            ((TextView)view.findViewById(R.id.tvCounterNote)).setText(Html.fromHtml(cnt.getNote()
                    .replace("\n", "<br/>")));
        } catch (Exception e) {
            // Нет описания
        }

        // Значение, единица измерения, дата
        TextView value = (TextView)view.findViewById(R.id.tvValue);
        TextView measure = (TextView)view.findViewById(R.id.tvMeasure);
        TextView period = (TextView)view.findViewById(R.id.tvPeriod);

        NumberFormat nf = NumberFormat
                .getNumberInstance(mContext.getResources().getConfiguration().locale);

        NumberFormat cnf = NumberFormat.getCurrencyInstance(mContext.getResources()
                .getConfiguration().locale);

        Currency cur = null;
        double val = 0;

        // Расчет значения
        // Если показаний нет
        if (cnt.getIndications().size() == 0) {

            period.setVisibility(View.GONE);
            val = 0;

        } else { // Показания есть

            final IndicationsCollection inds = cnt.getIndications();

            try {
                final Indication ind = inds.get(0); // Последнее значение
                Date d = new Date(ind.getDate().getTime());
                final java.text.DateFormat df = DateFormat.getDateFormat(mContext);

                period.setText(df.format(d));
                period.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                // Нет даты показаний
            }

            if (cnt.getViewValueType() == ViewValueType.DELTA) {

                // Дельта
                val = inds.get(0).getValue();

            } else if (cnt.getViewValueType() == ViewValueType.TOTAL) {

                // Итог по значению
                val = inds.get(0).getTotal();

            } else if (cnt.getViewValueType() == ViewValueType.COST) {

                // Затраты
                val = inds.get(0).calcCost(Indication.COST_PRECISION, mFormulaTotalAliases,
                        mFormulaValueAliases, mFormulaRateAliases);

            } else if (cnt.getViewValueType() == ViewValueType.TOTAL_COST) {

                // Итог по затратам
                val = 0;
                for (Indication i : inds) {
                    val += i.calcCost(Indication.COST_PRECISION, mFormulaTotalAliases,
                            mFormulaValueAliases, mFormulaRateAliases);
                }
            }
        }

        // Вывод значения
        if (cnt.getViewValueType() == ViewValueType.DELTA
                || cnt.getViewValueType() == ViewValueType.TOTAL) {
            // Дельта (относительное значение) или Итог по значению

            try {
                cur = Currency.getInstance(cnt.getMeasure());
            } catch (Exception e) {
                // Не валюта в формате ISO
            }

            if (cur == null) {
                value.setText(nf.format(val));
                measure.setText(Html.fromHtml(cnt.getMeasure()));
                measure.setVisibility(View.VISIBLE);
            } else {
                cnf.setCurrency(cur);
                value.setText(cnf.format(val));
                measure.setVisibility(View.GONE);
            }

        } else if (cnt.getViewValueType() == ViewValueType.COST
                || cnt.getViewValueType() == ViewValueType.TOTAL_COST) {
            // Затраты или Итог по затратам

            try {
                cur = Currency.getInstance(cnt.getCurrency());
            } catch (Exception e) {
                // Не валюта в формате ISO
            }

            if (cur == null) {
                value.setText(nf.format(val));
                measure.setText(Html.fromHtml(cnt.getCurrency()));
                measure.setVisibility(View.VISIBLE);
            } else {
                cnf.setCurrency(cur);
                value.setText(cnf.format(val));
                measure.setVisibility(View.GONE);
            }
        }

        return view;
    }
    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
