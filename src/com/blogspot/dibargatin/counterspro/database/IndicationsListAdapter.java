
package com.blogspot.dibargatin.counterspro.database;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;

import com.blogspot.dibargatin.counterspro.R;
import com.blogspot.dibargatin.counterspro.database.Counter.RateType;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IndicationsListAdapter extends BaseAdapter {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================
    Context mContext;

    LayoutInflater mInflater;

    IndicationsCollection mItems;

    String[] mFormulaValueAliases;

    String[] mFormulaTotalAliases;

    String[] mFormulaRateAliases;

    // ===========================================================
    // Constructors
    // ===========================================================
    public IndicationsListAdapter(Context context, IndicationsCollection items) {
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
    public Indication getIndication(int position) {
        return mItems != null ? mItems.get(position) : null;
    }

    public void setItems(IndicationsCollection items) {
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
        return getIndication(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.indication_list_item, parent, false);
        }

        Indication ind = getIndication(position);

        if (ind == null) {
            return view;
        }

        NumberFormat nf = NumberFormat
                .getNumberInstance(mContext.getResources().getConfiguration().locale);

        NumberFormat cnf = NumberFormat.getCurrencyInstance(mContext.getResources()
                .getConfiguration().locale);

        Currency cur = null;
        try {
            cur = Currency.getInstance(ind.getCounter().getMeasure());
        } catch (Exception e) {
            // Не валюта в формате ISO
        }

        // Устанавливаем единицу измерения
        try {
            final TextView measure = (TextView)view.findViewById(R.id.tvMeasure);

            if (cur == null) {
                measure.setText(Html.fromHtml(ind.getCounter().getMeasure()));
            } else {
                measure.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            // Нет единицы измерения
        }

        // Читаем и устанавливаем значение
        try {
            final TextView value = (TextView)view.findViewById(R.id.tvValue);

            if (ind.getValue() < 0) {
                value.setText(nf.format(ind.getValue()));
            } else {
                value.setText("+" + nf.format(ind.getValue()));
            }
        } catch (Exception e) {
            // Нет значения
        }

        // Читаем и устанавливаем сумму
        try {
            TextView total = (TextView)view.findViewById(R.id.tvTotal);

            if (cur == null) {
                total.setText(nf.format(ind.getTotal()));
            } else {
                cnf.setCurrency(cur);
                total.setText(cnf.format(ind.getTotal()));
            }
        } catch (Exception e) {
            // Нет суммы
        }

        // Выводим инфорацию о тарифе и о затратах
        if (ind.getCounter().getRateType() == RateType.SIMPLE) { // Простой
                                                                 // тариф
            try {
                LinearLayout f = (LinearLayout)view.findViewById(R.id.lFormula);
                f.setVisibility(View.GONE);

                TextView c = (TextView)view.findViewById(R.id.tvCurrency);
                TextView c2 = (TextView)view.findViewById(R.id.tvCostCurrency);

                Currency rcur = null;
                try {
                    rcur = Currency.getInstance(ind.getCounter().getCurrency());
                } catch (Exception e) {
                    // Не в формате ISO
                }

                if (rcur == null) {
                    c.setText(Html.fromHtml(ind.getCounter().getCurrency()));
                    c2.setText(Html.fromHtml(ind.getCounter().getCurrency()));
                } else {
                    c.setVisibility(View.GONE);
                    c2.setVisibility(View.GONE);
                }

                TextView rate = (TextView)view.findViewById(R.id.tvRateValue);
                TextView cost = (TextView)view.findViewById(R.id.tvCost);

                if (rcur == null) {
                    rate.setText(nf.format(ind.getRateValue()));
                    cost.setText(nf.format(ind.getRateValue() * ind.getValue()));
                } else {
                    cnf.setCurrency(rcur);
                    rate.setText(cnf.format(ind.getRateValue()));
                    cost.setText(cnf.format(ind.getRateValue() * ind.getValue()));
                }

            } catch (Exception e) {
                // Не вышло прочитать тариф
            }

        } else if (ind.getCounter().getRateType() == RateType.FORMULA) { // Формула
            LinearLayout lri = (LinearLayout)view.findViewById(R.id.lRateInfo);
            lri.setVisibility(View.GONE);

            TextView cc = (TextView)view.findViewById(R.id.tvCostCurrency);

            Currency rcur = null;
            try {
                rcur = Currency.getInstance(ind.getCounter().getCurrency());
            } catch (Exception e) {
                // Не в формате ISO
            }

            if (rcur == null) {
                cc.setText(Html.fromHtml(ind.getCounter().getCurrency()));
            } else {
                cc.setVisibility(View.GONE);
            }

            TextView formula = (TextView)view.findViewById(R.id.tvFormula);
            TextView cost = (TextView)view.findViewById(R.id.tvCost);

            try {
                formula.setText(ind.getCounter().getFormula());
                double res = ind.calcCost(mFormulaTotalAliases, mFormulaValueAliases,
                        mFormulaRateAliases);

                if (rcur == null) {
                    cost.setText(nf.format(res));
                } else {
                    cnf.setCurrency(rcur);
                    cost.setText(cnf.format(res));
                }
            } catch (IllegalArgumentException e) {
                cost.setText(mContext.getResources().getString(R.string.error_evaluator_expression));
            }

        } else { // Без тарифа
            LinearLayout l1 = (LinearLayout)view.findViewById(R.id.lRateInfo);
            LinearLayout l2 = (LinearLayout)view.findViewById(R.id.lCost);
            LinearLayout l3 = (LinearLayout)view.findViewById(R.id.lFormula);

            l1.setVisibility(View.GONE);
            l2.setVisibility(View.GONE);
            l3.setVisibility(View.GONE);
        }

        // Выводим дату и время показания
        try {
            GregorianCalendar c = (GregorianCalendar)Calendar.getInstance();
            c.setTimeInMillis(ind.getDate().getTime());

            TextView date = (TextView)view.findViewById(R.id.tvDate);
            TextView month = (TextView)view.findViewById(R.id.tvMonth);

            final java.text.DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, mContext
                    .getResources().getConfiguration().locale);
            final java.text.DateFormat tf = android.text.format.DateFormat.getTimeFormat(mContext);

            switch (ind.getCounter().getPeriodType().ordinal()) {
                case 0: // Год
                    month.setText(Integer.toString(c.get(Calendar.YEAR)) + " "
                            + mContext.getResources().getString(R.string.year));
                    date.setText(df.format(c.getTime()) + " " + tf.format(c.getTime()));
                    break;

                case 1: // Месяц
                    String[] ml = mContext.getResources().getStringArray(R.array.month_list);
                    month.setText(ml[c.get(Calendar.MONTH)]);
                    date.setText(df.format(c.getTime()) + " " + tf.format(c.getTime()));
                    break;

                case 2: // День
                    month.setText(new SimpleDateFormat("EEEEEEE", mContext.getResources()
                            .getConfiguration().locale).format(c.getTime()));
                    date.setText(df.format(c.getTime()) + " " + tf.format(c.getTime()));
                    break;

                case 3: // Час
                case 4: // Минута
                    month.setText(tf.format(c.getTime()));
                    date.setText(df.format(c.getTime()));
                    break;

                default:
                    String[] ml2 = mContext.getResources().getStringArray(R.array.month_list);
                    month.setText(ml2[c.get(Calendar.MONTH)]);
                    date.setText(df.format(c.getTime()) + " " + tf.format(c.getTime()));
            }
        } catch (Exception e) {
            // Нет даты
        }

        // Примечание к показанию
        try {
            TextView note = (TextView)view.findViewById(R.id.tvNote);
            final String n = ind.getNote().trim();

            if (n.length() == 0) {
                note.setVisibility(View.GONE);
            } else {
                note.setVisibility(View.VISIBLE);
                note.setText(Html.fromHtml(n.replace("\n", "<br/>")));
            }
        } catch (Exception e) {
            // Нет примечания
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
