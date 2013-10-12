
package com.blogspot.dibargatin.counterspro.database;

import java.sql.Timestamp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class EventDAO {
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
    public EventsCollection getAll(SQLiteDatabase db) {
        final EventsCollection result = new EventsCollection();

        Cursor c = db.query(DBHelper.TABLE_EVENT, new String[] {
                DBHelper.EVENT_ID, DBHelper.EVENT_COUNTER_ID, DBHelper.EVENT_DATE,
                DBHelper.EVENT_REPEAT_TYPE, DBHelper.EVENT_TITLE, DBHelper.EVENT_NOTE
        }, null, null, null, null, null);
        
        final CounterDAO counterDao = new CounterDAO();
        
        if (c != null) {
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    int counterId = c.getInt(c.getColumnIndex(DBHelper.EVENT_COUNTER_ID));
                    
                    Event ev = new Event(counterDao.getById(db, counterId, false));

                    ev.setId(c.getLong(c.getColumnIndex(DBHelper.EVENT_ID)));
                    ev.setDate(Timestamp.valueOf(c.getString(c
                            .getColumnIndex(DBHelper.EVENT_DATE))));
                    ev.setRepeatType(Event.RepeatType.values()[c.getInt(c
                            .getColumnIndex(DBHelper.EVENT_REPEAT_TYPE))]);                    
                    ev.setTitle(c.getString(c.getColumnIndex(DBHelper.EVENT_TITLE)));
                    ev.setNote(c.getString(c.getColumnIndex(DBHelper.EVENT_NOTE)));
                    
                    result.add(ev);
                }
            }
            c.close();
        }

        return result;
    }
    
    public EventsCollection getAllByCounter(SQLiteDatabase db, Counter counter) {
        final EventsCollection result = new EventsCollection();

        Cursor c = db.query(DBHelper.TABLE_EVENT, new String[] {
                DBHelper.EVENT_ID, DBHelper.EVENT_COUNTER_ID, DBHelper.EVENT_DATE,
                DBHelper.EVENT_REPEAT_TYPE, DBHelper.EVENT_TITLE, DBHelper.EVENT_NOTE
        }, DBHelper.EVENT_COUNTER_ID + " = ?", new String[] {
                Long.toString(counter.getId())
            }, null, null, null);
        
        if (c != null) {
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    Event ev = new Event(counter);

                    ev.setId(c.getLong(c.getColumnIndex(DBHelper.EVENT_ID)));
                    ev.setDate(Timestamp.valueOf(c.getString(c
                            .getColumnIndex(DBHelper.EVENT_DATE))));
                    ev.setRepeatType(Event.RepeatType.values()[c.getInt(c
                            .getColumnIndex(DBHelper.EVENT_REPEAT_TYPE))]);                    
                    ev.setTitle(c.getString(c.getColumnIndex(DBHelper.EVENT_TITLE)));
                    ev.setNote(c.getString(c.getColumnIndex(DBHelper.EVENT_NOTE)));
                    
                    result.add(ev);
                }
            }
            c.close();
        }

        return result;
    }
    
    public EventsCollection getById(SQLiteDatabase db, Counter counter, long id) {
        final EventsCollection result = new EventsCollection();

        Cursor c = db.query(DBHelper.TABLE_EVENT, new String[] {
                DBHelper.EVENT_ID, DBHelper.EVENT_COUNTER_ID, DBHelper.EVENT_DATE,
                DBHelper.EVENT_REPEAT_TYPE, DBHelper.EVENT_TITLE, DBHelper.EVENT_NOTE
        }, DBHelper.EVENT_ID + " = ?", new String[] {
                Long.toString(id)
            }, null, null, null);
        
        if (c != null) {
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    Event ev = new Event(counter);

                    ev.setId(c.getLong(c.getColumnIndex(DBHelper.EVENT_ID)));
                    ev.setDate(Timestamp.valueOf(c.getString(c
                            .getColumnIndex(DBHelper.EVENT_DATE))));
                    ev.setRepeatType(Event.RepeatType.values()[c.getInt(c
                            .getColumnIndex(DBHelper.EVENT_REPEAT_TYPE))]);                    
                    ev.setTitle(c.getString(c.getColumnIndex(DBHelper.EVENT_TITLE)));
                    ev.setNote(c.getString(c.getColumnIndex(DBHelper.EVENT_NOTE)));
                    
                    result.add(ev);
                }
            }
            c.close();
        }

        return result;
    }
    
    public long insert(SQLiteDatabase db, Event object) {
        ContentValues cv = new ContentValues();

        cv.put(DBHelper.EVENT_COUNTER_ID, object.getCounter().getId());
        cv.put(DBHelper.EVENT_DATE, object.getDate().toString());
        cv.put(DBHelper.EVENT_REPEAT_TYPE, object.getRepeatType().ordinal());
        cv.put(DBHelper.EVENT_TITLE, object.getTitle());
        cv.put(DBHelper.EVENT_NOTE, object.getNote());
        
        long id = db.insert(DBHelper.TABLE_EVENT, null, cv);
        object.setId(id);

        return id;
    }
    
    public boolean massInsert(SQLiteDatabase db, EventsCollection objects) {

        boolean result = true;

        db.beginTransaction();

        try {
            for (Event obj : objects) {
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
    
    public void update(SQLiteDatabase db, Event object) {
        ContentValues cv = new ContentValues();
        
        cv.put(DBHelper.EVENT_DATE, object.getDate().toString());
        cv.put(DBHelper.EVENT_REPEAT_TYPE, object.getRepeatType().ordinal());
        cv.put(DBHelper.EVENT_TITLE, object.getTitle());
        cv.put(DBHelper.EVENT_NOTE, object.getNote());
        
        db.update(DBHelper.TABLE_EVENT, cv, DBHelper.EVENT_ID + " = ?", new String[] {
                Long.toString(object.getId())
            });
    }
    
    public void deleteById(SQLiteDatabase db, long id) {
        db.delete(DBHelper.TABLE_EVENT, DBHelper.EVENT_ID + " = ?", new String[] {
            Long.toString(id)
        });
    }
    
    public void deleteByCounterId(SQLiteDatabase db, long counterId) {
        db.delete(DBHelper.TABLE_EVENT, DBHelper.EVENT_COUNTER_ID + " = ?", new String[] {
            Long.toString(counterId)
        });
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
