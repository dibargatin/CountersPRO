
package com.blogspot.dibargatin.housing;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class EntryEditActivity extends Activity implements OnClickListener {
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

    EditText mValue;

    EditText mRate;

    int mRateType = 1;
    
    EditText mEditDateTime;
    
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

        // Прочитаем параметры вызова
        Intent intent = getIntent();
        mDbHelper = new DBHelper(this);

        mCounterId = intent.getLongExtra(CounterActivity.EXTRA_COUNTER_ID, -1);
        mEntryId = intent.getLongExtra(EntryEditActivity.EXTRA_ENTRY_ID, -1);

        // Определим элементы диалога
        Button ok = (Button)findViewById(R.id.btn_entry_edit_form_btn_ok);
        ok.setOnClickListener(this);

        Button cancel = (Button)findViewById(R.id.btn_entry_edit_form_btn_cancel);
        cancel.setOnClickListener(this);

        TextView title = (TextView)findViewById(R.id.tvEntryEditTitle);
        mValue = (EditText)findViewById(R.id.etValue);
        mRate = (EditText)findViewById(R.id.etRate);
        
        final GregorianCalendar c = (GregorianCalendar)Calendar.getInstance();
        mDateTime = new Timestamp(c.getTimeInMillis());
        
        mEditDateTime = (EditText)findViewById(R.id.etDateTime);        
        mEditDateTime.setText(new SimpleDateFormat(getResources().getString(R.string.date_time_format)).format(c.getTime()));
        mEditDateTime.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(EntryEditActivity.this);
                alert.setTitle(getResources().getString(R.string.date_time));
                
                final LinearLayout layout = new LinearLayout(EntryEditActivity.this);
                final DatePicker date = new DatePicker(EntryEditActivity.this);
                final TimePicker time = new TimePicker(EntryEditActivity.this);
                
                time.setIs24HourView(true); // TODO: preference
                
                c.setTime(mDateTime);

                date.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH));
                time.setCurrentHour(c.get(Calendar.HOUR_OF_DAY)); // TODO: если 12-часовой формат, то использовать HOUR
                time.setCurrentMinute(c.get(Calendar.MINUTE));
                
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(date);
                layout.addView(time);
                                                
                alert.setView(layout);
                
                alert.setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                c.set(date.getYear(), date.getMonth(), date.getDayOfMonth(),
                                        time.getCurrentHour(), time.getCurrentMinute(), 0);
                                c.set(Calendar.MILLISECOND, 0);
                                
                                mDateTime.setTime(c.getTimeInMillis());
                                mEditDateTime.setText(new SimpleDateFormat(getResources().getString(R.string.date_time_format)).format(c.getTime()));
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

        // Если регистрация нового показания
        if (intent.getAction().equals(Intent.ACTION_INSERT)) {
            title.setText(getResources().getString(R.string.entry_edit_form_title_add));
            Cursor cur = mDbHelper.fetchLastRateByCounterId(mCounterId);

            if (cur.getCount() > 0) {
                cur.moveToFirst();
                mRate.setText(cur.getString(cur.getColumnIndex("rate")));
                mRateType = cur.getInt(cur.getColumnIndex("rate_type"));
            }

        } else { // Редактирование показания
            title.setText(getResources().getString(R.string.entry_edit_form_title_edit));
            Cursor cur = mDbHelper.fetchEntryById(mEntryId);

            if (cur.getCount() > 0) {
                cur.moveToFirst();

                String d = cur.getString(cur.getColumnIndex("entry_date"));
                mDateTime = java.sql.Timestamp.valueOf(d);
                
                c.setTime(mDateTime);
                mEditDateTime.setText(new SimpleDateFormat(getResources().getString(R.string.date_time_format)).format(c.getTime()));
                
                mValue.setText(cur.getString(cur.getColumnIndex("value")));
                mRate.setText(cur.getString(cur.getColumnIndex("rate")));
                mRateType = cur.getInt(cur.getColumnIndex("rate_type"));
            }
        }

        if (mRateType == 0) { // Счетчик без тарифа
            LinearLayout l = (LinearLayout)findViewById(R.id.lRate);
            l.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_entry_edit_form_btn_ok:
                                                
                boolean exists = mDbHelper.isEntryExists(mCounterId, mDateTime.toString());                
                
                if (exists) {
                    Toast.makeText(EntryEditActivity.this,
                            getResources().getString(R.string.error_entry_datetime),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                
                double va = 0;
                double r = 0;

                try {
                    va = Double.parseDouble(mValue.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(EntryEditActivity.this,
                            getResources().getString(R.string.error_entry_value),
                            Toast.LENGTH_SHORT).show();
                    mValue.requestFocus();
                    return;
                }

                try {
                    r = Double.parseDouble(mRate.getText().toString());
                } catch (NumberFormatException e) {
                    if (mRateType > 0) {
                        Toast.makeText(EntryEditActivity.this,
                                getResources().getString(R.string.error_entry_rate),
                                Toast.LENGTH_SHORT).show();
                        mRate.requestFocus();
                        return;
                    } else { // Счетчик без тарифа
                        r = 0;
                    }
                }

                if (getIntent().getAction().equals(Intent.ACTION_INSERT)) {
                    mDbHelper.insertEntry(mCounterId, mDateTime.toString(), va, r);
                } else {
                    mDbHelper.updateEntry(mEntryId, mDateTime.toString(), va, r);
                }

                setResult(RESULT_OK);
                finish();
                break;

            case R.id.btn_entry_edit_form_btn_cancel:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
