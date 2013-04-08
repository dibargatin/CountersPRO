
package com.blogspot.dibargatin.housing;

import java.sql.Date;

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

        TextView title = (TextView)findViewById(R.id.tv_entry_form_title);
        EditText rate = (EditText)findViewById(R.id.et_entry_form_rate);

        if (intent.getAction().equals(Intent.ACTION_INSERT)) {
            title.setText(getResources().getString(R.string.entry_edit_form_title_add));
            Cursor cur = mDbHelper.fetchLastRateByCounterId(mCounterId);

            if (cur.getCount() > 0) {
                cur.moveToFirst();
                rate.setText(cur.getString(cur.getColumnIndex("rate")));
            }

        } else {
            title.setText(getResources().getString(R.string.entry_edit_form_title_edit));
            Cursor cur = mDbHelper.fetchEntryById(mEntryId);

            if (cur.getCount() > 0) {
                cur.moveToFirst();

                DatePicker date = (DatePicker)findViewById(R.id.dp_entry_form_period);
                Date d = Date.valueOf(cur.getString(cur.getColumnIndex("entry_date")));
                date.updateDate(d.getYear() + 1900, d.getMonth(), d.getDay());

                EditText value = (EditText)findViewById(R.id.et_entry_form_value);
                value.setText(cur.getString(cur.getColumnIndex("value")));

                rate.setText(cur.getString(cur.getColumnIndex("rate")));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_entry_edit_form_btn_ok:
                DatePicker date = (DatePicker)findViewById(R.id.dp_entry_form_period);
                Date d = new Date(date.getYear() - 1900, date.getMonth(), date.getDayOfMonth());

                EditText value = (EditText)findViewById(R.id.et_entry_form_value);
                double va = Double.parseDouble(value.getText().toString());

                EditText rate = (EditText)findViewById(R.id.et_entry_form_rate);
                double r = Double.parseDouble(rate.getText().toString());

                if (getIntent().getAction().equals(Intent.ACTION_INSERT)) {
                    mDbHelper.insertEntry(mCounterId, d.toString(),
                            Double.parseDouble(value.getText().toString()),
                            Double.parseDouble(rate.getText().toString()));
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
