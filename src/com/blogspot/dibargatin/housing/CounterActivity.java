
package com.blogspot.dibargatin.housing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

        TextView title = (TextView)findViewById(R.id.counters_edit_form_title);
        Intent intent = getIntent();

        if (intent.getAction().equals(Intent.ACTION_INSERT)) {
            title.setText(getResources().getString(R.string.counters_edit_form_title_add));
        } else {
            title.setText(getResources().getString(R.string.counters_edit_form_title_edit));
            mCounterId = intent.getLongExtra(EXTRA_COUNTER_ID, -1);

            if (mCounterId != -1) {
                EditText name = (EditText)findViewById(R.id.counters_edit_form_et_name);
                EditText note = (EditText)findViewById(R.id.counters_edit_form_et_note);

                Cursor c = mDbHelper.fetchCounterById(mCounterId);
                c.moveToFirst();
                name.setText(c.getString(c.getColumnIndex("name")));
                note.setText(c.getString(c.getColumnIndex("note")));
            }
        }

        View color = (View)findViewById(R.id.vColor1);
        color.setOnClickListener(this);

        Button ok = (Button)findViewById(R.id.counters_edit_form_btn_ok);
        ok.setOnClickListener(this);

        Button cancel = (Button)findViewById(R.id.counters_edit_form_btn_cancel);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.counters_edit_form_btn_ok:
                EditText name = (EditText)findViewById(R.id.counters_edit_form_et_name);
                EditText note = (EditText)findViewById(R.id.counters_edit_form_et_note);

                if (getIntent().getAction().equals(Intent.ACTION_INSERT)) {
                    mDbHelper.insertCounter(name.getText().toString(), note.getText().toString());
                } else {
                    mDbHelper.updateCounter(mCounterId, name.getText().toString(), note.getText()
                            .toString());
                }

                setResult(RESULT_OK);
                finish();
                break;

            case R.id.counters_edit_form_btn_cancel:
                finish();
                break;

            case R.id.vColor1:

                AlertDialog.Builder builder = new AlertDialog.Builder(CounterActivity.this);
                builder.setTitle(R.string.choise);
                builder.setItems(R.array.colors, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        String[] colors = getResources().getStringArray(R.array.colors);
                        View c = (View)CounterActivity.this.findViewById(R.id.vColor1);
                        c.setBackgroundColor(Color.parseColor(colors[which]));
                    }
                });
                builder.create().show();
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
