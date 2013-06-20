
package com.blogspot.dibargatin.counterspro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.blogspot.dibargatin.counterspro.R;
import com.blogspot.dibargatin.counterspro.database.Counter;
import com.blogspot.dibargatin.counterspro.database.Counter.ViewValueType;
import com.blogspot.dibargatin.counterspro.database.CounterDAO;
import com.blogspot.dibargatin.counterspro.database.DBHelper;
import com.blogspot.dibargatin.counterspro.database.Counter.IndicationsGroupType;
import com.blogspot.dibargatin.counterspro.database.Counter.PeriodType;
import com.blogspot.dibargatin.counterspro.database.Counter.RateType;
import com.blogspot.dibargatin.counterspro.util.FormulaEvaluator;
import com.larswerkman.colorpicker.ColorPicker;
import com.larswerkman.colorpicker.SaturationBar;

public class CounterActivity extends SherlockActivity implements OnClickListener {
    // ===========================================================
    // Constants
    // ===========================================================
    public final static String EXTRA_COUNTER_ID = "com.blogspot.dibargatin.housing.CounterActivity.COUNTER_ID";

    private final static int DEFAULT_COLOR = -20480;

    // ===========================================================
    // Fields
    // ===========================================================
    SQLiteDatabase mDatabase;

    CounterDAO mCounterDao;

    Counter mCounter;

    View mColor;

    EditText mName;

    EditText mNote;

    EditText mMeasure;

    EditText mCurrency;

    Spinner mRateType;

    EditText mFormula;

    Spinner mPeriodType;
    
    Spinner mGroupType;
    
    Spinner mViewValueType;

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
        setContentView(R.layout.counters_edit_form);

        mDatabase = new DBHelper(this).getWritableDatabase();
        mCounterDao = new CounterDAO();
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_menu_home);

        // Определим список элементов диалога
        mName = (EditText)findViewById(R.id.etName);
        mNote = (EditText)findViewById(R.id.etNote);
        mMeasure = (EditText)findViewById(R.id.etMeasure);
        mCurrency = (EditText)findViewById(R.id.etCurrency);
        mFormula = (EditText)findViewById(R.id.etFormula);

        // Контрол для выбора вида тарифа
        mRateType = (Spinner)findViewById(R.id.sRateType);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(
                        R.array.rate_type_list));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mRateType.setAdapter(adapter);
        mRateType.setSelection(1);

        mRateType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setCurrencyVisibility(RateType.values()[(int)id]);
                setFormulaVisibility(RateType.values()[(int)id]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        // Контрол для выбора вида периода
        mPeriodType = (Spinner)findViewById(R.id.sPeriodType);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.period_type_list));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mPeriodType.setAdapter(adapter);
        mPeriodType.setSelection(1);
        
        // Контрол для выбора вида группировки по периоду
        mGroupType = (Spinner)findViewById(R.id.sGroupType);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.indication_groups));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mGroupType.setAdapter(adapter);
        mGroupType.setSelection(0);
        
        // Контрол для выбора вида отображаемого значения
        mViewValueType = (Spinner)findViewById(R.id.sViewValueType);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.view_value_type_list));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mViewValueType.setAdapter(adapter);
        mViewValueType.setSelection(0);
        
        // Контрол для выбора цвета
        mColor = (View)findViewById(R.id.vColor);
        mColor.setOnClickListener(this);

        // Прочитаем переданные параметры
        Intent intent = getIntent();

        // Создание нового счетчика
        if (intent.getAction().equals(Intent.ACTION_INSERT)) {

            getSupportActionBar().setTitle(
                    getResources().getString(R.string.counters_edit_form_title_add));

            mCounter = new Counter();

            mCounter.setColor(DEFAULT_COLOR);
            mCounter.setRateType(RateType.SIMPLE);
            mCounter.setPeriodType(PeriodType.MONTH);
            mCounter.setViewValueType(ViewValueType.DELTA);

        } else { // Редактирование счетчика

            getSupportActionBar().setTitle(
                    getResources().getString(R.string.counters_edit_form_title_edit));

            long counterId = intent.getLongExtra(EXTRA_COUNTER_ID, -1);

            if (counterId != -1 && mDatabase != null) {

                mCounter = mCounterDao.getById(mDatabase, counterId, false);

                if (mCounter != null) {
                    mName.setText(mCounter.getName());
                    mNote.setText(mCounter.getNote());
                    mMeasure.setText(mCounter.getMeasure());
                    mCurrency.setText(mCounter.getCurrency());
                    mRateType.setSelection(mCounter.getRateType().ordinal());
                    mPeriodType.setSelection(mCounter.getPeriodType().ordinal());
                    mFormula.setText(mCounter.getFormula());
                    mViewValueType.setSelection(mCounter.getViewValueType().ordinal());
                    mGroupType.setSelection(mCounter.getIndicationsGroupType().ordinal());

                    setCurrencyVisibility(RateType.values()[(int)mRateType.getSelectedItemId()]);
                    setFormulaVisibility(RateType.values()[(int)mRateType.getSelectedItemId()]);
                }
            } else {
                mCounter = new Counter();
                mCounter.setId(-1);
            }
        }

        mColor.setBackgroundColor(mCounter.getColor());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.vColor:
                // Сформируем диалог для выбора цвета
                AlertDialog.Builder alert = new AlertDialog.Builder(CounterActivity.this);
                alert.setTitle(getResources().getString(R.string.pick_the_color));

                final LinearLayout layout = new LinearLayout(CounterActivity.this);
                final SaturationBar sb = new SaturationBar(CounterActivity.this);
                final ColorPicker cp = new ColorPicker(CounterActivity.this);

                cp.addSaturationBar(sb);
                cp.setColor(mCounter.getColor());
                cp.setOldCenterColor(mCounter.getColor());
                cp.setNewCenterColor(mCounter.getColor());

                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(sb);
                layout.addView(cp);

                alert.setView(layout);

                alert.setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mCounter.setColor(cp.getColor());
                                mColor.setBackgroundColor(mCounter.getColor());
                            }
                        });

                alert.setNegativeButton(getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // На нет и суда нет
                            }
                        });

                alert.show();

                break;

            default:
                return;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.counter_edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.action_save_counter:
                // Проверим корректность введенной формулы
                if (mRateType.getSelectedItemId() == 2) { // Формула
                    String[] val = getResources().getStringArray(R.array.formula_var_value_aliases);
                    String[] ttl = getResources().getStringArray(R.array.formula_var_total_aliases);
                    String[] trf = getResources()
                            .getStringArray(R.array.formula_var_tariff_aliases);

                    final FormulaEvaluator eval = new FormulaEvaluator(val, 1.0, ttl, 11.0, trf,
                            1.0);
                    String expression = mFormula.getText().toString();

                    try {
                        eval.evaluate(expression);
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(
                                CounterActivity.this,
                                (expression + " " + getResources().getString(
                                        R.string.error_evaluator_expression)).trim(),
                                Toast.LENGTH_SHORT).show();
                        mFormula.requestFocus();

                        return true;
                    }
                }

                // Результат в объект
                mCounter.setName(mName.getText().toString());
                mCounter.setNote(mNote.getText().toString());
                mCounter.setMeasure(mMeasure.getText().toString());
                mCounter.setCurrency(mCurrency.getText().toString());
                mCounter.setRateType(RateType.values()[mRateType.getSelectedItemPosition()]);
                mCounter.setPeriodType(PeriodType.values()[mPeriodType.getSelectedItemPosition()]);
                mCounter.setFormula(mFormula.getText().toString());
                mCounter.setViewValueType(ViewValueType.values()[mViewValueType.getSelectedItemPosition()]);
                mCounter.setIndicationsGroupType(IndicationsGroupType.values()[mGroupType.getSelectedItemPosition()]);

                // Сохраним результат
                if (getIntent().getAction().equals(Intent.ACTION_INSERT)) {
                    mCounterDao.insert(mDatabase, mCounter);
                } else {
                    mCounterDao.update(mDatabase, mCounter);
                }

                // Завершим диалог
                setResult(RESULT_OK);
                finish();
                break;
        }

        return true;
    }

    // ===========================================================
    // Methods
    // ===========================================================
    private void setCurrencyVisibility(RateType type) {
        LinearLayout l = (LinearLayout)CounterActivity.this.findViewById(R.id.lCurrency);

        if (l != null) {
            if (type == RateType.WITHOUT) { // Без тарифа
                l.setVisibility(View.GONE);
            } else {
                l.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setFormulaVisibility(RateType type) {
        final LinearLayout lf = (LinearLayout)CounterActivity.this.findViewById(R.id.lFormula);

        if (lf != null) {
            if (type == RateType.FORMULA) { // Формула
                lf.setVisibility(View.VISIBLE);
            } else {
                lf.setVisibility(View.GONE);
            }
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

}
