
package com.blogspot.dibargatin.housing;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.blogspot.dibargatin.housing.database.Counter;
import com.blogspot.dibargatin.housing.database.CounterDAO;
import com.blogspot.dibargatin.housing.database.DBHelper;
import com.blogspot.dibargatin.housing.database.Indication;
import com.blogspot.dibargatin.housing.database.IndicationDAO;
import com.blogspot.dibargatin.housing.database.Counter.RateType;
import com.blogspot.dibargatin.housing.util.DecimalKeyListener;

public class IndicationActivity extends SherlockActivity {
    // ===========================================================
    // Constants
    // ===========================================================
    public static final String EXTRA_INDICATION_ID = "com.blogspot.dibargatin.housing.IndicationActivity.INDICATION_ID";

    // ===========================================================
    // Fields
    // ===========================================================
    SQLiteDatabase mDatabase;

    IndicationDAO mIndicationDao;

    Indication mIndication;

    double mPrevValue;

    TextView mPrevValueView;

    TextView mMeasure;

    EditText mValue;

    Spinner mValueType;

    EditText mRate;

    EditText mNote;

    EditText mEditDate;

    EditText mEditTime;

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
        setContentView(R.layout.indication_edit_form);

        mDatabase = new DBHelper(this).getWritableDatabase();
        mIndicationDao = new IndicationDAO();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_menu_home);

        // Прочитаем параметры вызова
        Intent intent = getIntent();

        final long counterId = intent.getLongExtra(CounterActivity.EXTRA_COUNTER_ID, -1);
        final long indicationId = intent.getLongExtra(IndicationActivity.EXTRA_INDICATION_ID, -1);

        final GregorianCalendar c = (GregorianCalendar)Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        // Если регистрация нового показания
        if (intent.getAction().equals(Intent.ACTION_INSERT)) {
            if (mDatabase != null && counterId != -1) {
                Counter counter = new CounterDAO().getById(mDatabase, counterId, false);
                mIndication = new Indication(counter);
                mIndication.setDate(new Timestamp(c.getTimeInMillis()));
            }
        } else { // Редактирование показания
            Counter counter = new CounterDAO().getById(mDatabase, counterId, false);
            mIndication = mIndicationDao.getById(mDatabase, counter, indicationId);
        }

        // Контрол для ввода значения показания
        mValue = (EditText)findViewById(R.id.etValue);
        mValue.setKeyListener(new DecimalKeyListener(this));

        // Контрол для выбора вида значения
        mValueType = (Spinner)findViewById(R.id.sValueType);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(
                        R.array.value_type_list));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mValueType.setAdapter(adapter);
        mValueType.setSelection(1);

        // Контрол для ввода тарифа
        mRate = (EditText)findViewById(R.id.etRate);
        mRate.setKeyListener(new DecimalKeyListener(this));

        // Контрол для ввода примечания
        mNote = (EditText)findViewById(R.id.etNote);

        // Контрол выбора даты показания
        final java.text.DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, getResources()
                .getConfiguration().locale);

        mEditDate = (EditText)findViewById(R.id.etDate);
        mEditDate.setText(df.format(c.getTime()));
        mEditDate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(IndicationActivity.this);
                alert.setTitle(getResources().getString(R.string.date));

                final DatePicker date = new DatePicker(IndicationActivity.this);

                c.setTime(mIndication.getDate());
                date.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH));

                alert.setView(date);

                alert.setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                c.setTime(mIndication.getDate());
                                c.set(date.getYear(), date.getMonth(), date.getDayOfMonth());

                                mIndication.getDate().setTime(c.getTimeInMillis());
                                mEditDate.setText(df.format(c.getTime()));

                                refreshPrevValue();
                            }
                        });

                alert.setNegativeButton(getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // На нет и суда нет
                            }
                        });

                alert.show();

            }
        });

        // Контрол выбора времени показания
        final DateFormat tf = android.text.format.DateFormat.getTimeFormat(IndicationActivity.this);

        // Формат времени 12 или 24 часовой
        final int timeType = android.text.format.DateFormat.is24HourFormat(IndicationActivity.this) ? 24
                : 12;

        mEditTime = (EditText)findViewById(R.id.etTime);
        mEditTime.setText(tf.format(c.getTime()));
        mEditTime.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(IndicationActivity.this);
                alert.setTitle(getResources().getString(R.string.time));

                final TimePicker time = new TimePicker(IndicationActivity.this);

                if (timeType == 24) {
                    time.setIs24HourView(true);
                } else { // Если 12 часовой формат
                    time.setIs24HourView(false);
                }

                c.setTime(mIndication.getDate());
                time.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
                time.setCurrentMinute(c.get(Calendar.MINUTE));

                alert.setView(time);

                alert.setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                c.setTime(mIndication.getDate());
                                c.set(Calendar.HOUR_OF_DAY, time.getCurrentHour());
                                c.set(Calendar.MINUTE, time.getCurrentMinute());
                                c.set(Calendar.SECOND, 0);
                                c.set(Calendar.MILLISECOND, 0);

                                mIndication.getDate().setTime(c.getTimeInMillis());
                                mEditTime.setText(tf.format(c.getTime()));

                                refreshPrevValue();
                            }
                        });

                alert.setNegativeButton(getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // На нет и суда нет
                            }
                        });

                alert.show();

            }
        });

        final NumberFormat nf = NumberFormat
                .getNumberInstance(getResources().getConfiguration().locale);

        // Если регистрация нового показания
        if (intent.getAction().equals(Intent.ACTION_INSERT)) {
            getSupportActionBar().setTitle(
                    getResources().getString(R.string.entry_edit_form_title_add));

            final double rateValue = mIndicationDao.getLastRateByCounterId(mDatabase, mIndication
                    .getCounter().getId());
            mRate.setText(nf.format(rateValue));

        } else { // Редактирование показания
            getSupportActionBar().setTitle(
                    getResources().getString(R.string.entry_edit_form_title_edit));

            c.setTime(mIndication.getDate());
            mEditDate.setText(df.format(c.getTime()));
            mEditTime.setText(tf.format(c.getTime()));

            mValue.setText(nf.format(mIndication.getValue()));
            mRate.setText(nf.format(mIndication.getRateValue()));

            mNote.setText(mIndication.getNote());
        }

        if (mIndication.getCounter().getRateType() == RateType.WITHOUT) {
            LinearLayout l = (LinearLayout)findViewById(R.id.lRate);
            l.setVisibility(View.GONE);
        }

        // Контрол для отображения последнего значения
        mPrevValueView = (TextView)findViewById(R.id.tvPrevValue);
        mMeasure = (TextView)findViewById(R.id.tvMeasure);

        refreshPrevValue();
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
        getSupportMenuInflater().inflate(R.menu.entry_edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.action_save_entry:
                // Проверим на наличие показания на указанные дату и время
                boolean exists = mIndicationDao.isEntryExists(mDatabase, mIndication.getCounter()
                        .getId(), mIndication.getDate(), mIndication.getId());

                if (exists) {
                    Toast.makeText(IndicationActivity.this,
                            getResources().getString(R.string.error_entry_datetime),
                            Toast.LENGTH_SHORT).show();
                    return true;
                }

                final DecimalFormatSymbols dfs = new DecimalFormatSymbols(getResources()
                        .getConfiguration().locale);
                final DecimalFormat df = new DecimalFormat();
                df.setDecimalFormatSymbols(dfs);

                // Прочитаем значение и тариф
                try {
                    // Если вводили абсолютное значение, то расчитаем
                    // относительное
                    if (mValueType.getSelectedItemId() == 0) { // Абсолютное
                        mIndication.setValue(df.parse(mValue.getText().toString()).doubleValue()
                                - mPrevValue);
                    } else {
                        mIndication.setValue(df.parse(mValue.getText().toString()).doubleValue());
                    }
                } catch (ParseException e) {
                    Toast.makeText(IndicationActivity.this,
                            getResources().getString(R.string.error_entry_value),
                            Toast.LENGTH_SHORT).show();
                    mValue.requestFocus();
                    return true;
                }

                try {
                    mIndication.setRateValue(df.parse(mRate.getText().toString()).doubleValue());
                } catch (ParseException e) {
                    if (mIndication.getCounter().getRateType() != RateType.WITHOUT) {
                        Toast.makeText(IndicationActivity.this,
                                getResources().getString(R.string.error_entry_rate),
                                Toast.LENGTH_SHORT).show();
                        mRate.requestFocus();
                        return true;
                    } else { // Счетчик без тарифа
                        mIndication.setRateValue(0);
                    }
                }

                // Примечание
                mIndication.setNote(mNote.getText().toString());

                // Сохраним результат
                if (getIntent().getAction().equals(Intent.ACTION_INSERT)) {
                    mIndicationDao.insert(mDatabase, mIndication);
                } else {
                    mIndicationDao.update(mDatabase, mIndication);
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
    private void refreshPrevValue() {
        mPrevValue = mIndicationDao.getPrevTotalByCounterId(mDatabase, mIndication.getCounter()
                .getId(), mIndication.getDate());

        Currency cur = null;
        CharSequence m = Html.fromHtml(mIndication.getCounter().getMeasure());

        try {
            cur = Currency.getInstance(m.toString());
        } catch (Exception e) {
            // Не в формате ISO
        }

        if (cur == null) {
            NumberFormat nf = NumberFormat
                    .getNumberInstance(getResources().getConfiguration().locale);
            mPrevValueView.setText(nf.format(mPrevValue));
            mMeasure.setText(m);

        } else {
            NumberFormat cnf = NumberFormat
                    .getCurrencyInstance(getResources().getConfiguration().locale);
            cnf.setCurrency(cur);

            mPrevValueView.setText(cnf.format(mPrevValue));
            mMeasure.setVisibility(View.GONE);
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
