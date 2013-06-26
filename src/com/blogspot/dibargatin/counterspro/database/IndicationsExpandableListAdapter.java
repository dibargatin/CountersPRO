
package com.blogspot.dibargatin.counterspro.database;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blogspot.dibargatin.counterspro.R;
import com.blogspot.dibargatin.counterspro.database.Counter.IndicationsGroupType;
import com.blogspot.dibargatin.counterspro.database.Counter.PeriodType;
import com.blogspot.dibargatin.counterspro.database.Counter.RateType;
import com.blogspot.dibargatin.counterspro.util.Span;

public class IndicationsExpandableListAdapter extends BaseExpandableListAdapter {
    // ===========================================================
    // Constants
    // ===========================================================
    private final static int COST_PRECISION = 2;

    // ===========================================================
    // Fields
    // ===========================================================
    Context mContext;

    LayoutInflater mInflater;

    String[] mFormulaValueAliases;

    String[] mFormulaTotalAliases;

    String[] mFormulaRateAliases;

    IndicationsCollection mSource;

    Counter mCounter;

    int mGroupItemColor = Color.WHITE;

    IndicationsGroupType mGroupType;

    PeriodType mPeriodType;

    ArrayList<Span> mGroups;

    LinkedHashMap<Span, IndicationsCollection> mGroupItems;

    // ===========================================================
    // Constructors
    // ===========================================================
    public IndicationsExpandableListAdapter(Context context, IndicationsCollection source,
            Counter counter) {
        mContext = context;

        mFormulaValueAliases = mContext.getResources().getStringArray(
                R.array.formula_var_value_aliases);
        mFormulaTotalAliases = mContext.getResources().getStringArray(
                R.array.formula_var_total_aliases);
        mFormulaRateAliases = mContext.getResources().getStringArray(
                R.array.formula_var_tariff_aliases);

        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSource = source;
        setSettings(counter);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public Counter getSettings() {
        return mCounter;
    }

    public void resetSettings() {
        mGroupType = mCounter.getIndicationsGroupType();
        mGroupItemColor = calcGroupItemColor();
        mPeriodType = mCounter.getPeriodType();

        rebuildGroups();
    }

    public void setSettings(Counter counter) {
        this.mCounter = counter;
        resetSettings();
    }

    public PeriodType getPeriodType() {
        return mPeriodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.mPeriodType = periodType;
    }

    public IndicationsGroupType getGroupType() {
        return mGroupType;
    }

    public void setGroupType(IndicationsGroupType groupType) {
        mGroupType = groupType;
    }

    public int getGroupItemColor() {
        return mGroupItemColor;
    }

    public void setGroupItemColor(int groupItemColor) {
        this.mGroupItemColor = groupItemColor;
    }

    public IndicationsCollection getSource() {
        return mSource;
    }

    public void setSource(IndicationsCollection source, boolean isNeedRebuildGroups) {
        this.mSource = source;

        if (isNeedRebuildGroups)
            rebuildGroups();
    }

    public Indication getIndication(int group, int position) {
        return mGroupItems != null ? mGroupItems.get(mGroups.get(group)) != null ? mGroupItems.get(
                mGroups.get(group)).get(position) : null : null;
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return getIndication(groupPosition, childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        final Indication ind = getIndication(groupPosition, childPosition);
        return ind == null ? Indication.EMPTY_ID : ind.getId();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mGroupItems.get(mGroups.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mGroups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.indication_list_item_group, null);
        }

        convertView.setBackgroundColor(mGroupItemColor);

        TextView period = (TextView)convertView.findViewById(R.id.tvPeriod);
        period.setText(mGroups.get(groupPosition).getCaption());

        IndicationsCollection ic = mGroupItems.get(mGroups.get(groupPosition));

        final NumberFormat nf = NumberFormat.getNumberInstance(mContext.getResources()
                .getConfiguration().locale);

        TextView total = (TextView)convertView.findViewById(R.id.tvValueTotal);

        if (ic.getTotal() < 0) {
            total.setText(nf.format(ic.getTotal()));
        } else {
            total.setText("+" + nf.format(ic.getTotal()));
        }

        TextView totalCost = (TextView)convertView.findViewById(R.id.tvCostTotal);
        TextView costCurrency = (TextView)convertView.findViewById(R.id.tvCostCurrency);

        if (mCounter.getRateType() != RateType.WITHOUT) {

            Currency cur = null;
            try {
                cur = Currency.getInstance(mCounter.getCurrency());
            } catch (Exception e) {
                // Не валюта в формате ISO
            }

            totalCost.setVisibility(View.VISIBLE);

            if (cur == null) {
                totalCost.setText(nf.format(ic.getTotalCost()));

                costCurrency.setVisibility(View.VISIBLE);
                costCurrency.setText(Html.fromHtml(mCounter.getCurrency()));

            } else {
                final NumberFormat cnf = NumberFormat.getCurrencyInstance(mContext.getResources()
                        .getConfiguration().locale);
                cnf.setCurrency(cur);

                totalCost.setText(cnf.format(ic.getTotalCost()));
                costCurrency.setVisibility(View.GONE);
            }

        } else {
            totalCost.setVisibility(View.GONE);
            costCurrency.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.indication_list_item, parent, false);
        }

        Indication ind = getIndication(groupPosition, childPosition);

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
                
                double res = ind.calcCost(COST_PRECISION, mFormulaTotalAliases, mFormulaValueAliases,
                        mFormulaRateAliases);

                if (rcur == null) {
                    rate.setText(nf.format(ind.getRateValue()));
                    cost.setText(nf.format(res));
                } else {
                    cnf.setCurrency(rcur);
                    rate.setText(cnf.format(ind.getRateValue()));
                    cost.setText(cnf.format(res));
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
                double res = ind.calcCost(COST_PRECISION, mFormulaTotalAliases, mFormulaValueAliases,
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

            final PeriodType periodType = convertGroupType2PeriodType(mGroupType, mPeriodType);

            switch (periodType.ordinal()) {
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
    private void rebuildGroups() {
        final IndicationsCollection source = mSource;
        mGroupItems = new LinkedHashMap<Span, IndicationsCollection>();

        if (mSource.size() == 0) {
            mGroups = new ArrayList<Span>();
            notifyDataSetChanged();

            return;
        }

        final long minTime = source.getMinTime();
        final long maxTime = source.getMaxTime();

        final Calendar c = GregorianCalendar.getInstance();

        // Без группировки
        if (mGroupType == IndicationsGroupType.WITHOUT) {
            final Span s = new Span(minTime, maxTime, mContext.getResources().getString(
                    R.string.indication_group_period_without));

            if (!source.checkCostCalculatorState()) {
                source.initCostCalculator(COST_PRECISION, mFormulaTotalAliases,
                        mFormulaValueAliases, mFormulaRateAliases);
            }

            mGroupItems.put(s, source);
            mGroups = new ArrayList<Span>(mGroupItems.keySet());
        }
        // Группировка по годам
        else if (mGroupType == IndicationsGroupType.YEAR) {
            c.setTimeInMillis(minTime);
            final int minYear = c.get(Calendar.YEAR);

            c.setTimeInMillis(maxTime);
            final int maxYear = c.get(Calendar.YEAR);

            final String period = mContext.getResources().getString(
                    R.string.indication_group_period_year);

            for (int i = maxYear; i >= minYear; i--) {
                c.clear();
                c.set(i, Calendar.JANUARY, 1);
                long l = c.getTimeInMillis();

                c.set(i, Calendar.DECEMBER, 31, 23, 59, 59);
                long r = c.getTimeInMillis();

                IndicationsCollection ic = new IndicationsCollection();
                ic.initCostCalculator(COST_PRECISION, mFormulaTotalAliases, mFormulaValueAliases,
                        mFormulaRateAliases);

                final Span s = new Span(l, r, String.format(period, i));
                mGroupItems.put(s, ic);
            }

            mGroups = new ArrayList<Span>(mGroupItems.keySet());
        }
        // Группировка по месяцам
        else if (mGroupType == IndicationsGroupType.MONTH) {
            c.setTimeInMillis(minTime);
            final int minYear = c.get(Calendar.YEAR);
            final int minMonth = c.get(Calendar.MONTH);

            c.setTimeInMillis(maxTime);
            final int maxYear = c.get(Calendar.YEAR);
            final int maxMonth = c.get(Calendar.MONTH);

            final String period = mContext.getResources().getString(
                    R.string.indication_group_period_month);

            String[] ml = mContext.getResources().getStringArray(R.array.month_list);

            for (int i = maxYear; i >= minYear; i--) {
                for (int j = (i == maxYear ? maxMonth : Calendar.DECEMBER); j >= (i == minYear ? minMonth
                        : Calendar.JANUARY); j--) {

                    c.clear();
                    c.set(i, j, 1);
                    long l = c.getTimeInMillis();

                    c.set(i, j, c.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
                    long r = c.getTimeInMillis();

                    IndicationsCollection ic = new IndicationsCollection();
                    ic.initCostCalculator(COST_PRECISION, mFormulaTotalAliases,
                            mFormulaValueAliases, mFormulaRateAliases);

                    final Span s = new Span(l, r, String.format(period, ml[j], i));
                    mGroupItems.put(s, ic);
                }
            }

            mGroups = new ArrayList<Span>(mGroupItems.keySet());
        }
        // Группировка по дням
        else if (mGroupType == IndicationsGroupType.DAY) {
            c.setTimeInMillis(minTime);
            final int minYear = c.get(Calendar.YEAR);
            final int minMonth = c.get(Calendar.MONTH);
            final int minDay = c.get(Calendar.DAY_OF_MONTH);

            c.setTimeInMillis(maxTime);
            final int maxYear = c.get(Calendar.YEAR);
            final int maxMonth = c.get(Calendar.MONTH);
            final int maxDay = c.get(Calendar.DAY_OF_MONTH);

            final String period = mContext.getResources().getString(
                    R.string.indication_group_period_day);

            final java.text.DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, mContext
                    .getResources().getConfiguration().locale);

            for (int i = maxYear; i >= minYear; i--) {
                for (int j = (i == maxYear ? maxMonth : Calendar.DECEMBER); j >= (i == minYear ? minMonth
                        : Calendar.JANUARY); j--) {

                    c.clear();
                    c.set(i, j, 1);

                    for (int d = (i == maxYear && j == maxMonth ? maxDay : c
                            .getActualMaximum(Calendar.DAY_OF_MONTH)); d >= (i == minYear
                            && j == minMonth ? minDay : 1); d--) {

                        c.clear();
                        c.set(i, j, d);
                        long l = c.getTimeInMillis();

                        c.set(i, j, d, 23, 59, 59);
                        long r = c.getTimeInMillis();

                        IndicationsCollection ic = new IndicationsCollection();
                        ic.initCostCalculator(COST_PRECISION, mFormulaTotalAliases,
                                mFormulaValueAliases, mFormulaRateAliases);

                        final Span s = new Span(l, r, String.format(period, df.format(c.getTime())));
                        mGroupItems.put(s, ic);
                    }
                }
            }

            mGroups = new ArrayList<Span>(mGroupItems.keySet());
        }
        // Группировка по часам
        else if (mGroupType == IndicationsGroupType.HOUR) {
            c.setTimeInMillis(minTime);
            final int minYear = c.get(Calendar.YEAR);
            final int minMonth = c.get(Calendar.MONTH);
            final int minDay = c.get(Calendar.DAY_OF_MONTH);
            final int minHour = c.get(Calendar.HOUR_OF_DAY);

            c.setTimeInMillis(maxTime);
            final int maxYear = c.get(Calendar.YEAR);
            final int maxMonth = c.get(Calendar.MONTH);
            final int maxDay = c.get(Calendar.DAY_OF_MONTH);
            final int maxHour = c.get(Calendar.HOUR_OF_DAY);

            final String period = mContext.getResources().getString(
                    R.string.indication_group_period_day);

            final java.text.DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, mContext
                    .getResources().getConfiguration().locale);

            final java.text.DateFormat tf = android.text.format.DateFormat.getTimeFormat(mContext);

            for (int i = maxYear; i >= minYear; i--) {
                for (int j = (i == maxYear ? maxMonth : Calendar.DECEMBER); j >= (i == minYear ? minMonth
                        : Calendar.JANUARY); j--) {

                    c.clear();
                    c.set(i, j, 1);

                    for (int d = (i == maxYear && j == maxMonth ? maxDay : c
                            .getActualMaximum(Calendar.DAY_OF_MONTH)); d >= (i == minYear
                            && j == minMonth ? minDay : 1); d--) {

                        for (int h = (i == maxYear && j == maxMonth && d == maxDay ? maxHour : 23); h >= (i == minYear
                                && j == minMonth && d == minDay ? minHour : 0); h--) {

                            c.clear();
                            c.set(i, j, d, h, 0, 0);
                            long l = c.getTimeInMillis();

                            c.set(i, j, d, h, 59, 59);
                            long r = c.getTimeInMillis();

                            IndicationsCollection ic = new IndicationsCollection();
                            ic.initCostCalculator(COST_PRECISION, mFormulaTotalAliases,
                                    mFormulaValueAliases, mFormulaRateAliases);

                            c.clear();
                            c.set(i, j, d, h, 0, 0);

                            final Span s = new Span(l, r, String.format(period,
                                    df.format(c.getTime()) + " " + tf.format(c.getTime())));

                            mGroupItems.put(s, ic);
                        }
                    }
                }
            }

            mGroups = new ArrayList<Span>(mGroupItems.keySet());
        }

        // Заполняем группы
        // TODO Оптимизация распределения показаний по группам
        if (mGroupType != IndicationsGroupType.WITHOUT) {
            for (Indication i : source) {
                for (Span s : mGroupItems.keySet()) {
                    if (s.contains(i.getDate().getTime())) {
                        mGroupItems.get(s).add(i);
                        break;
                    }
                }
            }
        }

        notifyDataSetChanged();
    }

    private PeriodType convertGroupType2PeriodType(IndicationsGroupType groupType,
            PeriodType defaultPeriodType) {

        PeriodType result = defaultPeriodType;

        if (groupType == IndicationsGroupType.YEAR) {
            result = PeriodType.MONTH;
        } else if (groupType == IndicationsGroupType.MONTH) {
            result = PeriodType.DAY;
        } else if (groupType == IndicationsGroupType.DAY) {
            result = PeriodType.HOUR;
        } else if (groupType == IndicationsGroupType.HOUR) {
            result = PeriodType.MINUTE;
        }

        return result;
    }

    private int calcGroupItemColor() {
        float[] hsv = new float[3];

        Color.colorToHSV(mCounter.getColor(), hsv);
        hsv[1] = 0.1f;

        return Color.HSVToColor(hsv);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
