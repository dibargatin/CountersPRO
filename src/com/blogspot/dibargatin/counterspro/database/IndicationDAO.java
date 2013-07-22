
package com.blogspot.dibargatin.counterspro.database;

import java.sql.Timestamp;

import com.blogspot.dibargatin.counterspro.CountersListActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
        query.append("      ,i.note AS note");
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
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    Indication ind = new Indication(counter);

                    ind.setId(c.getLong(c.getColumnIndex(DBHelper.INDICATION_ID)));
                    ind.setDate(Timestamp.valueOf(c.getString(c
                            .getColumnIndex(DBHelper.INDICATION_DATE))));
                    ind.setValue(c.getDouble(c.getColumnIndex(DBHelper.INDICATION_VALUE)));
                    ind.setRateValue(c.getDouble(c.getColumnIndex(DBHelper.INDICATION_RATE)));
                    ind.setNote(c.getString(c.getColumnIndex(DBHelper.INDICATION_NOTE)));
                    ind.setTotal(c.getDouble(c.getColumnIndex("total")));

                    result.add(ind);
                }
            }
            c.close();
        }

        return result;
    }

    public Indication getById(SQLiteDatabase db, Counter counter, long id) {
        Indication result = null;

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
        query.append("      ,i.note AS note");
        query.append("      ,IFNULL((");
        query.append("         SELECT SUM(value)");
        query.append("           FROM Indications");
        query.append("          WHERE counter_id = i.counter_id");
        query.append("            AND entry_date <= i.entry_date");
        query.append("       ) , 0) AS total");
        query.append("  FROM Indications AS i");
        query.append(" WHERE i._id = ? ");
        query.append(" ORDER BY i.entry_date DESC, i._id DESC");

        Cursor c = db.rawQuery(query.toString(), new String[] {
            Long.toString(id)
        });

        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                result = new Indication(counter);

                result.setId(c.getLong(c.getColumnIndex(DBHelper.INDICATION_ID)));
                result.setDate(Timestamp.valueOf(c.getString(c
                        .getColumnIndex(DBHelper.INDICATION_DATE))));
                result.setValue(c.getDouble(c.getColumnIndex(DBHelper.INDICATION_VALUE)));
                result.setRateValue(c.getDouble(c.getColumnIndex(DBHelper.INDICATION_RATE)));
                result.setNote(c.getString(c.getColumnIndex(DBHelper.INDICATION_NOTE)));
                result.setTotal(c.getDouble(c.getColumnIndex("total")));
            }
            c.close();
        }

        return result;
    }

    public double getLastRateByCounterId(SQLiteDatabase db, long counterId) {
        final StringBuilder query = new StringBuilder();

        query.append("SELECT rate ");
        query.append("  FROM Indications ");
        query.append(" WHERE counter_id = ? ");
        query.append(" ORDER BY entry_date DESC ");
        query.append(" LIMIT 1 ");

        Cursor c = db.rawQuery(query.toString(), new String[] {
            Long.toString(counterId)
        });

        double result = 0.0;

        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                result = c.getDouble(c.getColumnIndex("rate"));
            }
            c.close();
        }

        return result;
    }

    public double getPrevValueByCounterId(SQLiteDatabase db, long counterId, Timestamp date) {
        final StringBuilder query = new StringBuilder();

        query.append("SELECT ");
        query.append("  IFNULL((SELECT value ");
        query.append("            FROM Indications ");
        query.append("           WHERE counter_id = ? ");
        query.append("             AND entry_date < ? ");
        query.append("           ORDER BY entry_date DESC ");
        query.append("           LIMIT 1 ");
        query.append("        ), 0) AS prev_value");

        Cursor c = db.rawQuery(query.toString(), new String[] {
                Long.toString(counterId), date.toString()
        });

        double result = 0;

        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                result = c.getDouble(c.getColumnIndex("prev_value"));
            }
            c.close();
        }

        return result;
    }

    public double getPrevTotalByCounterId(SQLiteDatabase db, long counterId, long indicationId,
            Timestamp date) {
        final StringBuilder query = new StringBuilder();

        query.append("SELECT ");
        query.append("  IFNULL((SELECT SUM(value)");
        query.append("            FROM Indications");
        query.append("           WHERE counter_id = ?");
        query.append("             AND entry_date < ?");
        query.append("             AND _id <> ?");
        query.append("        ) , 0) AS prev_total");

        Cursor c = db.rawQuery(query.toString(), new String[] {
                Long.toString(counterId), date.toString(), Long.toString(indicationId)
        });

        double result = 0;

        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                result = c.getDouble(c.getColumnIndex("prev_total"));
            }
            c.close();
        }

        return result;
    }

    public long insert(SQLiteDatabase db, Indication object) {
        ContentValues cv = new ContentValues();

        cv.put(DBHelper.INDICATION_COUNTER_ID, object.getCounter().getId());
        cv.put(DBHelper.INDICATION_DATE, object.getDate().toString());
        cv.put(DBHelper.INDICATION_VALUE, object.getValue());
        cv.put(DBHelper.INDICATION_RATE, object.getRateValue());
        cv.put(DBHelper.INDICATION_NOTE, object.getNote());

        object.setId(db.insert(DBHelper.TABLE_INDICATION, null, cv));

        return object.getId();
    }

    public boolean massInsert(SQLiteDatabase db, IndicationsCollection objects) {

        boolean result = true;

        db.beginTransaction();

        try {
            for (Indication obj : objects) {
                insert(db, obj);
            }

            db.setTransactionSuccessful();

        } catch (Exception e) {

            result = false;

        } finally {
            db.endTransaction();
        }

        return result;
    }

    public void update(SQLiteDatabase db, Indication object) {
        ContentValues cv = new ContentValues();

        cv.put(DBHelper.INDICATION_DATE, object.getDate().toString());
        cv.put(DBHelper.INDICATION_VALUE, object.getValue());
        cv.put(DBHelper.INDICATION_RATE, object.getRateValue());
        cv.put(DBHelper.INDICATION_NOTE, object.getNote());

        db.update(DBHelper.TABLE_INDICATION, cv, DBHelper.INDICATION_ID + " = ?", new String[] {
            Long.toString(object.getId())
        });
    }

    public void deleteById(SQLiteDatabase db, long id) {
        db.delete(DBHelper.TABLE_INDICATION, DBHelper.INDICATION_ID + " = ?", new String[] {
            Long.toString(id)
        });
    }

    public boolean isEntryExists(SQLiteDatabase db, long counterId, Timestamp entryDate,
            long entryId) {
        final StringBuilder query = new StringBuilder();

        query.append("SELECT ");
        query.append("  EXISTS (SELECT 1");
        query.append("            FROM Indications");
        query.append("           WHERE counter_id = ?");
        query.append("             AND entry_date = ?");
        query.append("             AND _id <> ?");
        query.append("           LIMIT 1");
        query.append("         ) AS cnt");

        final Cursor c = db.rawQuery(query.toString(), new String[] {
                Long.toString(counterId), entryDate.toString(), Long.toString(entryId)
        });

        boolean result = false;

        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                result = c.getInt(c.getColumnIndex("cnt")) != 0;
            }
            c.close();
        }

        return result;
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
