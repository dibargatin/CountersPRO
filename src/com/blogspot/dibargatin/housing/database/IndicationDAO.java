
package com.blogspot.dibargatin.housing.database;

import java.sql.Timestamp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class IndicationDAO {
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
    public IndicationsCollection getAllByCounter(SQLiteDatabase db, Counter counter) {
        final IndicationsCollection result = new IndicationsCollection();

        /*
         * Cursor c = db.query(DBHelper.TABLE_INDICATION, new String[] {},
         * DBHelper.INDICATION_COUNTER_ID + " = ?", new String[] {
         * Long.toString(counter.getId()) }, null, null,
         * DBHelper.INDICATION_DATE + " DESC");
         */

        final StringBuilder query = new StringBuilder();

        query.append("SELECT i._id AS _id"); // TODO use constant for field name
        query.append("      ,i.counter_id AS counter_id");
        query.append("      ,i.entry_date AS entry_date");
        query.append("      ,i.value AS value");
        query.append("      ,i.rate AS rate");
        query.append("      ,IFNULL((");
        query.append("         SELECT SUM(value)");
        query.append("           FROM Indications");
        query.append("          WHERE counter_id = i.counter_id");
        query.append("            AND entry_date <= i.entry_date");
        query.append("       ) , 0) AS total");
        query.append("  FROM Indications AS i");
        query.append(" WHERE i.counter_id = ? ");
        query.append(" ORDER BY i.entry_date DESC, i._id DESC");

        Cursor c = db.rawQuery(query.toString(), new String[] {
            Long.toString(counter.getId())
        });

        if (c != null) {
            while (c.moveToNext()) {
                Indication ind = new Indication(counter);

                ind.setId(c.getLong(c.getColumnIndex(DBHelper.INDICATION_ID)));
                ind.setDate(Timestamp.valueOf(c.getString(c
                        .getColumnIndex(DBHelper.INDICATION_DATE))));
                ind.setValue(c.getDouble(c.getColumnIndex(DBHelper.INDICATION_VALUE)));
                ind.setRateValue(c.getDouble(c.getColumnIndex(DBHelper.INDICATION_RATE)));
                ind.setTotal(c.getDouble(c.getColumnIndex("total")));

                result.add(ind);
            }

            c.close();
        }

        return result;
    }

    public void deleteById(SQLiteDatabase db, long id) {
        db.delete(DBHelper.TABLE_INDICATION, DBHelper.INDICATION_ID + " = ?", new String[] {
            Long.toString(id)
        });
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
