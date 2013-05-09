
package com.blogspot.dibargatin.housing;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import com.blogspot.dibargatin.housing.util.DecimalKeyListener;

public class EntryEditActivity extends SherlockActivity {
    // ===========================================================
    // Constants
    // ===========================================================
    public static final String EXTRA_ENTRY_ID = "com.blogspot.dibargatin.housing.EntryEditActivity.ENTRY_ID";

    // ===========================================================
    // Fields
    // ===========================================================
    DBHelper mDbHelper;

    long mEntryId;

    long mCounterId;

    double mPrevValue;

    TextView mPrevValueView;

    TextView mMeasure;

    EditText mValue;

    Spinner mValueType;

    EditText mRate;

    int mRateType = 1;

    EditText mEditDate;

    EditText mEditTime;

    Timestamp mDateTime;

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
        setContentView(R.layout.entry_edit_form);

        mDbHelper = new DBHelper(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_menu_home);

        // Прочитаем параметры вызова
        Intent intent = getIntent();

        mCounterId = intent.getLongExtra(CounterActivity.EXTRA_COUNTER_ID, -1);
        mEntryId = intent.getLongExtra(EntryEditActivity.EXTRA_ENTRY_ID, -1);

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
        mValueType.setSelection(0);

        // Контрол для ввода тарифа
        mRate = (EditText)findViewById(R.id.etRate);
        mRate.setKeyListener(new DecimalKeyListener(this));

        final GregorianCalendar c = (GregorianCalendar)Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        mDateTime = new Timestamp(c.getTimeInMillis());

        // Контрол выбора даты показания
        final java.text.DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, getResources().getConfiguration().locale);
        
        mEditDate = (EditText)findViewById(R.id.etDate);
        mEditDate.setText(df.format(c.getTime()));
        mEditDate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(EntryEditActivity.this);
                alert.setTitle(getResources().getString(R.string.date));

                final DatePicker date = new DatePicker(EntryEditActivity.this);

                c.setTime(mDateTime);
                date.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH));

                alert.setView(date);

                alert.setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                c.setTime(mDateTime);
                                c.set(date.getYear(), date.getMonth(), date.getDayOfMonth());

                                mDateTime.setTime(c.getTimeInMillis());
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
        final DateFormat tf = android.text.format.DateFormat.getTimeFormat(EntryEditActivity.this);
                //.getTimeInstance(DateFormat.SHORT, getResources().getConfiguration().locale);
        
        // Формат времени 12 или 24 часовой
        final int timeType = android.text.format.DateFormat.is24HourFormat(EntryEditActivity.this) ? 24 : 12;
        
        mEditTime = (EditText)findViewById(R.id.etTime);
        mEditTime.setText(tf.format(c.getTime()));
        mEditTime.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(EntryEditActivity.this);
                alert.setTitle(getResources().getString(R.string.time));

                final TimePicker time = new TimePicker(EntryEditActivity.this);
                
                if (timeType == 24) {
                    time.setIs24HourView(true);
                } else { // Если 12 часовой формат
                    time.setIs24HourView(false);
                }
                
                c.setTime(mDateTime);
                time.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
                time.setCurrentMinute(c.get(Calendar.MINUTE));

                alert.setView(time);

                alert.setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                c.setTime(mDateTime);
                                c.set(Calendar.HOUR_OF_DAY, time.getCurrentHour());
                                c.set(Calendar.MINUTE, time.getCurrentMinute());
                                c.set(Calendar.SECOND, 0);
                                c.set(Calendar.MILLISECOND, 0);

                                mDateTime.setTime(c.getTimeInMillis());
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
            Cursor cur = mDbHelper.fetchLastRateByCounterId(mCounterId);

            if (cur.getCount() > 0) {
                cur.moveToFirst();
                mRate.setText(nf.format(cur.getDouble(cur.getColumnIndex("rate"))));
                mRateType = cur.getInt(cur.getColumnIndex("rate_type"));
            }

        } else { // Редактирование показания
            getSupportActionBar().setTitle(
                    getResources().getString(R.string.entry_edit_form_title_edit));
            Cursor cur = mDbHelper.fetchEntryById(mEntryId);

            if (cur.getCount() > 0) {
                cur.moveToFirst();

                String d = cur.getString(cur.getColumnIndex("entry_date"));
                mDateTime = java.sql.Timestamp.valueOf(d);

                c.setTime(mDateTime);
                mEditDate.setText(df.format(c.getTime()));
                mEditTime.setText(tf.format(c.getTime()));
                
                double v = cur.getDouble(cur.getColumnIndex("value"));
                double r = cur.getDouble(cur.getColumnIndex("rate"));

                mValue.setText(nf.format(v));
                mRate.setText(nf.format(r));

                mRateType = cur.getInt(cur.getColumnIndex("rate_type"));
            }
        }

        if (mRateType == 0) { // Счетчик без тарифа
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
        mDbHelper.close();
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
                boolean exists = mDbHelper
                        .isEntryExists(mCounterId, mDateTime.toString(), mEntryId);

                if (exists) {
                    Toast.makeText(EntryEditActivity.this,
                            getResources().getString(R.string.error_entry_datetime),
                            Toast.LENGTH_SHORT).show();
                    return true;
                }

                final DecimalFormatSymbols dfs = new DecimalFormatSymbols(getResources()
                        .getConfiguration().locale);
                final DecimalFormat df = new DecimalFormat();
                df.setDecimalFormatSymbols(dfs);

                // Прочитаем значение и тариф
                double va = 0;
                double r = 0;

                try {
                    va = df.parse(mValue.getText().toString()).doubleValue();
                } catch (ParseException e) {
                    Toast.makeText(EntryEditActivity.this,
                            getResources().getString(R.string.error_entry_value),
                            Toast.LENGTH_SHORT).show();
                    mValue.requestFocus();
                    return true;
                }

                try {
                    r = df.parse(mRate.getText().toString()).doubleValue();
                } catch (ParseException e) {
                    if (mRateType > 0) {
                        Toast.makeText(EntryEditActivity.this,
                                getResources().getString(R.string.error_entry_rate),
                                Toast.LENGTH_SHORT).show();
                        mRate.requestFocus();
                        return true;
                    } else { // Счетчик без тарифа
                        r = 0;
                    }
                }

                // Если вводили относительное значение, то расчитаем абсолютное
                if (mValueType.getSelectedItemId() == 1) { // Относительное
                    va += mPrevValue;
                }

                // Сохраним результат
                if (getIntent().getAction().equals(Intent.ACTION_INSERT)) {
                    mDbHelper.insertEntry(mCounterId, mDateTime.toString(), va, r);
                } else {
                    mDbHelper.updateEntry(mEntryId, mDateTime.toString(), va, r);
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
        Cursor c = mDbHelper.fetchValueByCounterId(mCounterId, mDateTime.toString());

        if (!c.moveToFirst()) {
            mPrevValueView.setText("---");
            mMeasure.setText("");
            return;
        }

        NumberFormat nf = NumberFormat.getNumberInstance(getResources().getConfiguration().locale);

        mPrevValue = c.getDouble(c.getColumnIndex("value"));
        mPrevValueView.setText(nf.format(mPrevValue));
        mMeasure.setText(Html.fromHtml(c.getString(c.getColumnIndex("measure"))));

        c.close();
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
