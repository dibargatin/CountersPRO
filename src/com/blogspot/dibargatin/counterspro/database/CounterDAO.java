
package com.blogspot.dibargatin.counterspro.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CounterDAO {
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
    public CountersCollection getAll(SQLiteDatabase db) {
        final CountersCollection result = new CountersCollection();

        Cursor c = db.query(DBHelper.TABLE_COUNTER, new String[] {
                DBHelper.COUNTER_ID, DBHelper.COUNTER_NAME, DBHelper.COUNTER_NOTE,
                DBHelper.COUNTER_MEASURE, DBHelper.COUNTER_COLOR, DBHelper.COUNTER_CURRENCY,
                DBHelper.COUNTER_RATE_TYPE, DBHelper.COUNTER_PERIOD_TYPE, DBHelper.COUNTER_FORMULA, DBHelper.COUNTER_VIEW_VALUE_TYPE
        }, null, null, null, null, DBHelper.COUNTER_NOTE + ", " + DBHelper.COUNTER_NAME);

        if (c != null) {
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    Counter cnt = new Counter();

                    cnt.setId(c.getLong(c.getColumnIndex(DBHelper.COUNTER_ID)));
                    cnt.setName(c.getString(c.getColumnIndex(DBHelper.COUNTER_NAME)));
                    cnt.setNote(c.getString(c.getColumnIndex(DBHelper.COUNTER_NOTE)));
                    cnt.setColor(c.getInt(c.getColumnIndex(DBHelper.COUNTER_COLOR)));

                    cnt.setMeasure(c.getString(c.getColumnIndex(DBHelper.COUNTER_MEASURE)));
                    cnt.setCurrency(c.getString(c.getColumnIndex(DBHelper.COUNTER_CURRENCY)));

                    cnt.setRateType(Counter.RateType.values()[c.getInt(c
                            .getColumnIndex(DBHelper.COUNTER_RATE_TYPE))]);
                    cnt.setFormula(c.getString(c.getColumnIndex(DBHelper.COUNTER_FORMULA)));

                    cnt.setPeriodType(Counter.PeriodType.values()[c.getInt(c
                            .getColumnIndex(DBHelper.COUNTER_PERIOD_TYPE))]);
                    
                    cnt.setViewValueType(Counter.ViewValueType.values()[c.getInt(c
                            .getColumnIndex(DBHelper.COUNTER_VIEW_VALUE_TYPE))]);

                    // Получим показания
                    cnt.setIndications(new IndicationDAO().getAllByCounter(db, cnt));

                    result.add(cnt);
                }
            }
            c.close();
        }

        return result;
    }

    public Counter getById(SQLiteDatabase db, long id, boolean isEagerLoad) {
        Counter cnt = null;

        Cursor c = db.query(DBHelper.TABLE_COUNTER, new String[] {
                DBHelper.COUNTER_ID, DBHelper.COUNTER_NAME, DBHelper.COUNTER_NOTE,
                DBHelper.COUNTER_MEASURE, DBHelper.COUNTER_COLOR, DBHelper.COUNTER_CURRENCY,
                DBHelper.COUNTER_RATE_TYPE, DBHelper.COUNTER_PERIOD_TYPE, DBHelper.COUNTER_FORMULA, DBHelper.COUNTER_VIEW_VALUE_TYPE
        }, DBHelper.COUNTER_ID + " = ?", new String[] {
            Long.toString(id)
        }, null, null, DBHelper.COUNTER_NOTE + ", " + DBHelper.COUNTER_NAME);

        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                cnt = new Counter();

                cnt.setId(c.getLong(c.getColumnIndex(DBHelper.COUNTER_ID)));
                cnt.setName(c.getString(c.getColumnIndex(DBHelper.COUNTER_NAME)));
                cnt.setNote(c.getString(c.getColumnIndex(DBHelper.COUNTER_NOTE)));
                cnt.setColor(c.getInt(c.getColumnIndex(DBHelper.COUNTER_COLOR)));

                cnt.setMeasure(c.getString(c.getColumnIndex(DBHelper.COUNTER_MEASURE)));
                cnt.setCurrency(c.getString(c.getColumnIndex(DBHelper.COUNTER_CURRENCY)));

                cnt.setRateType(Counter.RateType.values()[c.getInt(c
                        .getColumnIndex(DBHelper.COUNTER_RATE_TYPE))]);
                cnt.setFormula(c.getString(c.getColumnIndex(DBHelper.COUNTER_FORMULA)));

                cnt.setPeriodType(Counter.PeriodType.values()[c.getInt(c
                        .getColumnIndex(DBHelper.COUNTER_PERIOD_TYPE))]);
                
                cnt.setViewValueType(Counter.ViewValueType.values()[c.getInt(c
                        .getColumnIndex(DBHelper.COUNTER_VIEW_VALUE_TYPE))]);
                
                // Получим показания
                if (isEagerLoad)
                    cnt.setIndications(new IndicationDAO().getAllByCounter(db, cnt));
            }
            c.close();            
        }

        return cnt;
    }

    public Counter getById(SQLiteDatabase db, long id) {
        return getById(db, id, true);
    }

    public long insert(SQLiteDatabase db, Counter object) {
        ContentValues cv = new ContentValues();

        cv.put(DBHelper.COUNTER_NAME, object.getName().toString());
        cv.put(DBHelper.COUNTER_NOTE, object.getNote().toString());
        cv.put(DBHelper.COUNTER_COLOR, object.getColor());
        cv.put(DBHelper.COUNTER_MEASURE, object.getMeasure().toString());
        cv.put(DBHelper.COUNTER_CURRENCY, object.getCurrency().toString());
        cv.put(DBHelper.COUNTER_RATE_TYPE, object.getRateType().ordinal());
        cv.put(DBHelper.COUNTER_PERIOD_TYPE, object.getPeriodType().ordinal());
        cv.put(DBHelper.COUNTER_FORMULA, object.getFormula());
        cv.put(DBHelper.COUNTER_VIEW_VALUE_TYPE, object.getViewValueType().ordinal());

        long id = db.insert(DBHelper.TABLE_COUNTER, null, cv);
        object.setId(id);

        return id;
    }

    public void update(SQLiteDatabase db, Counter object) {
        ContentValues cv = new ContentValues();

        cv.put(DBHelper.COUNTER_NAME, object.getName().toString());
        cv.put(DBHelper.COUNTER_NOTE, object.getNote().toString());
        cv.put(DBHelper.COUNTER_COLOR, object.getColor());
        cv.put(DBHelper.COUNTER_MEASURE, object.getMeasure().toString());
        cv.put(DBHelper.COUNTER_CURRENCY, object.getCurrency().toString());
        cv.put(DBHelper.COUNTER_RATE_TYPE, object.getRateType().ordinal());
        cv.put(DBHelper.COUNTER_PERIOD_TYPE, object.getPeriodType().ordinal());
        cv.put(DBHelper.COUNTER_FORMULA, object.getFormula());
        cv.put(DBHelper.COUNTER_VIEW_VALUE_TYPE, object.getViewValueType().ordinal());

        db.update(DBHelper.TABLE_COUNTER, cv, DBHelper.COUNTER_ID + " = ?", new String[] {
            Long.toString(object.getId())
        });
    }

    public void deleteById(SQLiteDatabase db, long id) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        db.delete(DBHelper.TABLE_COUNTER, DBHelper.COUNTER_ID + " = ?", new String[] {
            Long.toString(id)
        });
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
