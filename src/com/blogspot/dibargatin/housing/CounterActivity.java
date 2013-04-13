
package com.blogspot.dibargatin.housing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.larswerkman.colorpicker.ColorPicker;
import com.larswerkman.colorpicker.SaturationBar;

public class CounterActivity extends Activity implements OnClickListener {
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

        TextView title = (TextView)findViewById(R.id.tvEntryEditTitle);

        mColor = (View)findViewById(R.id.vColor);
        mColor.setOnClickListener(this);

        mName = (EditText)findViewById(R.id.etName);
        mNote = (EditText)findViewById(R.id.etNote);
        mMeasure = (EditText)findViewById(R.id.etMeasure);
        mCurrency = (EditText)findViewById(R.id.etCurrency);

        Button ok = (Button)findViewById(R.id.btnOk);
        ok.setOnClickListener(this);

        Button cancel = (Button)findViewById(R.id.btnCancel);
        cancel.setOnClickListener(this);

        Intent intent = getIntent();

        if (intent.getAction().equals(Intent.ACTION_INSERT)) {
            title.setText(getResources().getString(R.string.counters_edit_form_title_add));
        } else {
            title.setText(getResources().getString(R.string.counters_edit_form_title_edit));
            mCounterId = intent.getLongExtra(EXTRA_COUNTER_ID, -1);

            if (mCounterId != -1) {
                Cursor c = mDbHelper.fetchCounterById(mCounterId);
                c.moveToFirst();

                mPickedColor = c.getInt(c.getColumnIndex("color"));
                mName.setText(c.getString(c.getColumnIndex("name")));
                mNote.setText(c.getString(c.getColumnIndex("note")));
                mMeasure.setText(c.getString(c.getColumnIndex("measure")));
                mCurrency.setText(c.getString(c.getColumnIndex("currency")));
            }
        }

        mColor.setBackgroundColor(mPickedColor);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOk:

                if (getIntent().getAction().equals(Intent.ACTION_INSERT)) {
                    mDbHelper.insertCounter(mName.getText().toString(), mNote.getText().toString(),
                            mPickedColor, mMeasure.getText().toString(), mCurrency.getText()
                                    .toString());
                } else {
                    mDbHelper.updateCounter(mCounterId, mName.getText().toString(), mNote.getText()
                            .toString(), mPickedColor, mMeasure.getText().toString(), mCurrency
                            .getText().toString());
                }

                setResult(RESULT_OK);
                finish();
                break;

            case R.id.btnCancel:
                finish();
                break;

            case R.id.vColor:
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

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

}
