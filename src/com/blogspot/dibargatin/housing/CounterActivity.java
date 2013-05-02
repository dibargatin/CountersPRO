
package com.blogspot.dibargatin.housing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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

import com.blogspot.dibargatin.housing.util.FormulaEvaluator;
import com.larswerkman.colorpicker.ColorPicker;
import com.larswerkman.colorpicker.SaturationBar;

public class CounterActivity extends SherlockActivity implements OnClickListener {
    // ===========================================================
    // Constants
    // ===========================================================
    public final static String EXTRA_COUNTER_ID = "com.blogspot.dibargatin.housing.CounterActivity.COUNTER_ID";

    // ===========================================================
    // Fields
    // ===========================================================
    DBHelper mDbHelper;

    long mCounterId;

    View mColor;

    EditText mName;

    EditText mNote;

    EditText mMeasure;

    EditText mCurrency;

    Spinner mRateType;

    EditText mFormula;

    Spinner mPeriodType;

    int mPickedColor = -20480;

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

        mDbHelper = new DBHelper(this);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        
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
                final LinearLayout l = (LinearLayout)CounterActivity.this
                        .findViewById(R.id.lCurrency);

                if (id == 0) { // Без тарифа
                    l.setVisibility(View.GONE);
                } else {
                    l.setVisibility(View.VISIBLE);
                }

                final LinearLayout lf = (LinearLayout)CounterActivity.this
                        .findViewById(R.id.lFormula);

                if (id == 2) { // Формула
                    lf.setVisibility(View.VISIBLE);
                } else {
                    lf.setVisibility(View.GONE);
                }
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

        // Контрол для выбора цвета
        mColor = (View)findViewById(R.id.vColor);
        mColor.setOnClickListener(this);

        // Прочитаем переданные параметры
        Intent intent = getIntent();

        // Создание нового счетчика
        if (intent.getAction().equals(Intent.ACTION_INSERT)) {
            getSupportActionBar().setTitle(getResources().getString(R.string.counters_edit_form_title_add));
        } else { // Редактирование счетчика
            getSupportActionBar().setTitle(getResources().getString(R.string.counters_edit_form_title_edit));
            mCounterId = intent.getLongExtra(EXTRA_COUNTER_ID, -1);

            if (mCounterId != -1) {
                Cursor c = mDbHelper.fetchCounterById(mCounterId);
                c.moveToFirst();

                mPickedColor = c.getInt(c.getColumnIndex("color"));
                mName.setText(c.getString(c.getColumnIndex("name")));
                mNote.setText(c.getString(c.getColumnIndex("note")));
                mMeasure.setText(c.getString(c.getColumnIndex("measure")));
                mCurrency.setText(c.getString(c.getColumnIndex("currency")));
                mRateType.setSelection(c.getInt(c.getColumnIndex("rate_type")));
                mPeriodType.setSelection(c.getInt(c.getColumnIndex("period_type")));
                mFormula.setText(c.getString(c.getColumnIndex("formula")));

                LinearLayout l = (LinearLayout)CounterActivity.this.findViewById(R.id.lCurrency);

                if (mRateType.getSelectedItemId() == 0) { // Без тарифа
                    l.setVisibility(View.GONE);
                } else {
                    l.setVisibility(View.VISIBLE);
                }

                final LinearLayout lf = (LinearLayout)CounterActivity.this
                        .findViewById(R.id.lFormula);

                if (mRateType.getSelectedItemId() == 2) { // Формула
                    lf.setVisibility(View.VISIBLE);
                } else {
                    lf.setVisibility(View.GONE);
                }
            }
        }

        mColor.setBackgroundColor(mPickedColor);
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
                cp.setColor(mPickedColor);
                cp.setOldCenterColor(mPickedColor);
                cp.setNewCenterColor(mPickedColor);

                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(sb);
                layout.addView(cp);

                alert.setView(layout);

                alert.setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mPickedColor = cp.getColor();
                                mColor.setBackgroundColor(mPickedColor);
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
        mDbHelper.close();
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
                String[] dlt = getResources().getStringArray(R.array.formula_var_delta_aliases);
                
                final FormulaEvaluator eval = new FormulaEvaluator(val, 1.0, dlt, 11.0);
                String expression = mFormula.getText().toString();

                try {                        
                    /*
                    Toast.makeText(
                            CounterActivity.this,
                            expression + " = " + Double.toString(eval.evaluate(expression)),
                            Toast.LENGTH_SHORT).show();
                    //*/
                    eval.evaluate(expression);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(
                            CounterActivity.this,
                            (expression
                                    + " "
                                    + getResources().getString(
                                            R.string.error_evaluator_expression)).trim(),
                            Toast.LENGTH_SHORT).show();
                    mFormula.requestFocus();
                    
                    return true;
                }
            }

            // Сохраним результат
            if (getIntent().getAction().equals(Intent.ACTION_INSERT)) {
                mDbHelper.insertCounter(mName.getText().toString(), mNote.getText().toString(),
                        mPickedColor, mMeasure.getText().toString(), mCurrency.getText()
                                .toString(), mRateType.getSelectedItemPosition(), mPeriodType
                                .getSelectedItemPosition(), mFormula.getText().toString());
            } else {
                mDbHelper.updateCounter(mCounterId, mName.getText().toString(), mNote.getText()
                        .toString(), mPickedColor, mMeasure.getText().toString(), mCurrency
                        .getText().toString(), mRateType.getSelectedItemPosition(), mPeriodType
                        .getSelectedItemPosition(), mFormula.getText().toString());
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

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

}
