
package com.blogspot.dibargatin.housing;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
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

    DatePicker mPeriod;

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

        Intent intent = getIntent();
        mDbHelper = new DBHelper(this);

        mCounterId = intent.getLongExtra(CounterActivity.EXTRA_COUNTER_ID, -1);
        mEntryId = intent.getLongExtra(EntryEditActivity.EXTRA_ENTRY_ID, -1);

        Button ok = (Button)findViewById(R.id.btn_entry_edit_form_btn_ok);
        ok.setOnClickListener(this);

        Button cancel = (Button)findViewById(R.id.btn_entry_edit_form_btn_cancel);
        cancel.setOnClickListener(this);

        TextView title = (TextView)findViewById(R.id.tvEntryEditTitle);
        mValue = (EditText)findViewById(R.id.etValue);
        mRate = (EditText)findViewById(R.id.etRate);
        mPeriod = (DatePicker)findViewById(R.id.dpPeriod);

        if (intent.getAction().equals(Intent.ACTION_INSERT)) {
            title.setText(getResources().getString(R.string.entry_edit_form_title_add));
            Cursor cur = mDbHelper.fetchLastRateByCounterId(mCounterId);

            if (cur.getCount() > 0) {
                cur.moveToFirst();
                mRate.setText(cur.getString(cur.getColumnIndex("rate")));
            }

        } else {
            title.setText(getResources().getString(R.string.entry_edit_form_title_edit));
            Cursor cur = mDbHelper.fetchEntryById(mEntryId);

            if (cur.getCount() > 0) {
                cur.moveToFirst();
                
                String d = cur.getString(cur.getColumnIndex("entry_date"));
                Date date = java.sql.Date.valueOf(d);
                
                GregorianCalendar c = (GregorianCalendar)Calendar.getInstance();
                c.setTime(date);
                
                mPeriod.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                
                mValue.setText(cur.getString(cur.getColumnIndex("value")));
                mRate.setText(cur.getString(cur.getColumnIndex("rate")));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_entry_edit_form_btn_ok:
                Date d = new Date(mPeriod.getYear() - 1900, mPeriod.getMonth(),
                        mPeriod.getDayOfMonth());
                
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
                    Toast.makeText(EntryEditActivity.this,
                            getResources().getString(R.string.error_entry_rate), Toast.LENGTH_SHORT)
                            .show();
                    mRate.requestFocus();
                    return;
                }

                if (getIntent().getAction().equals(Intent.ACTION_INSERT)) {
                    mDbHelper.insertEntry(mCounterId, d.toString(), va, r);
                } else {
                    mDbHelper.updateEntry(mEntryId, d.toString(), va, r);
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
