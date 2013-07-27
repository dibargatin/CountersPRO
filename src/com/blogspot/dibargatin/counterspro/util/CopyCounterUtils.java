
package com.blogspot.dibargatin.counterspro.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.dibargatin.counterspro.R;
import com.blogspot.dibargatin.counterspro.database.Counter;
import com.blogspot.dibargatin.counterspro.database.CounterDAO;
import com.blogspot.dibargatin.counterspro.database.Indication;
import com.blogspot.dibargatin.counterspro.database.IndicationDAO;
import com.blogspot.dibargatin.counterspro.database.IndicationsCollection;

public class CopyCounterUtils {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
    public static void showCopyDialog(final Context context, final SQLiteDatabase db,
            final Counter source, final ICopyCounterListener listener) {

        // Если нечего копировать
        if (source.getId() == Indication.EMPTY_ID) {
            Toast.makeText(context, context.getResources().getString(R.string.save_before_copying),
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Готовим диалог
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        dialog.setTitle(R.string.copy_counter_title);

        final LinearLayout layout = new LinearLayout(context);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(8, 0, 8, 0);

        final TextView tvName = new TextView(context);
        final EditText etName = new EditText(context);

        tvName.setText(context.getResources().getString(R.string.copy_counter_name));
        layout.addView(tvName);

        etName.setHint(context.getResources().getString(R.string.copy_counter_name));
        etName.setText(source.getName());
        layout.addView(etName);

        final TextView tvNote = new TextView(context);
        final EditText etNote = new EditText(context);

        tvNote.setText(context.getResources().getString(R.string.copy_counter_note));
        layout.addView(tvNote);

        etNote.setHint(context.getResources().getString(R.string.copy_counter_note));
        etNote.setText(source.getNote());
        layout.addView(etNote);

        final CheckBox cb = new CheckBox(context);

        cb.setText(context.getResources().getString(R.string.copy_counter_indications));
        cb.setChecked(true);
        layout.addView(cb);

        dialog.setView(layout).setInverseBackgroundForced(true);

        dialog.setPositiveButton(R.string.continue_action, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                CounterDAO cdao = new CounterDAO();
                Counter newCounter = new Counter(source);

                newCounter.setName(etName.getText().toString());
                newCounter.setNote(etNote.getText().toString());

                // Если необходимо копировать показания
                if (cb.isChecked()) {

                    final IndicationDAO idao = new IndicationDAO();
                    final IndicationsCollection indSrc = idao.getAllByCounter(db, source);
                    final IndicationsCollection result = new IndicationsCollection();

                    for (Indication ind : indSrc) {
                        result.add(new Indication(ind, newCounter));
                    }

                    db.beginTransaction();

                    try {
                        long counterId = cdao.insert(db, newCounter);

                        if (counterId == Counter.EMPTY_ID) {
                            throw new RuntimeException("Counter copying error");
                        } else {
                            if (!idao.massInsert(db, result)) {
                                throw new RuntimeException(
                                        "Counter copying: Indications copying error.");
                            }
                        }

                        db.setTransactionSuccessful();

                    } catch (Exception e) {

                        Toast.makeText(context,
                                context.getResources().getString(R.string.copy_to_trouble),
                                Toast.LENGTH_LONG).show();
                        return;

                    } finally {
                        db.endTransaction();
                    }

                } else {
                    cdao.insert(db, newCounter);
                }

                Toast.makeText(context, context.getResources().getString(R.string.copy_to_ok),
                        Toast.LENGTH_LONG).show();

                if (listener != null) {
                    listener.OnCopyComplatedListener();
                }
            }

        });

        dialog.setNegativeButton(R.string.cancel_action, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                Toast.makeText(context,
                        context.getResources().getString(R.string.copy_to_canceled),
                        Toast.LENGTH_LONG).show();

            }

        });

        dialog.show();
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
    public interface ICopyCounterListener {
        void OnCopyComplatedListener();
    }
}
