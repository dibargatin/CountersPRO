
package com.blogspot.dibargatin.housing;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    // ===========================================================
    // Constants
    // ===========================================================
    public static String DB_NAME = "housing_db.sqlite3";

    public static int DB_VERSION = 1;
    
    private static final Patch[] PATCHES = new Patch[] {
        // №1
        new Patch() {
           public void apply(SQLiteDatabase db) {
               db.execSQL("PRAGMA foreign_keys = ON;");
               
               final String queryCounters = ""
                       + "CREATE TABLE Counters (" 
                       + " _id INTEGER PRIMARY KEY AUTOINCREMENT" 
                       + ",name TEXT" 
                       + ",note TEXT" 
                       + ",measure TEXT" 
                       + ",color INTEGER"
                       + ",currency TEXT"
                       + ");";
               
               db.execSQL(queryCounters);
               
               final String queryEntries = ""
                       + "CREATE TABLE Entries (" 
                       + " _id INTEGER PRIMARY KEY AUTOINCREMENT" 
                       + ",counter_id INTEGER REFERENCES Counters(_id) ON DELETE CASCADE" 
                       + ",entry_date DATE" 
                       + ",value REAL" 
                       + ",rate REAL"
                       + ");";
               
               db.execSQL(queryEntries);
           }
      
           public void revert(SQLiteDatabase db) {
               db.execSQL("DROP TABLE IF EXISTS Counters;");
               db.execSQL("DROP TABLE IF EXISTS Entries;");
           }
        }
        /*
        , new Patch() {
           public void apply(SQLiteDatabase db) { ... }
           public void revert(SQLiteDatabase db) { ... }
        }
        */
     };
    
    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================
    public DBHelper(Context pContext) {
        super(pContext, DB_NAME, null, DB_VERSION);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================
    @Override
    public void onCreate(SQLiteDatabase db) {
        for (int i=0; i<PATCHES.length; i++) {
            PATCHES[i].apply(db);
          }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i=oldVersion; i<newVersion; i++) {
            PATCHES[i].apply(db);
          }                
    }
    
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i=oldVersion; i>newVersion; i++) {
          PATCHES[i-1].revert(db);
        }
      }

    // ===========================================================
    // Counter Methods
    // ===========================================================
    public Cursor fetchAllCounters() {
        final String query = ""
                + "SELECT *"
                + "      ,CAST(IFNULL(e.value, 0) AS TEXT) AS value"
                + "      ,entry_date"
                + "  FROM Counters AS c" 
                + "  LEFT JOIN (SELECT counter_id"
                + "                   ,value"
                + "                   ,entry_date"
                + "               FROM Entries AS e"
                + "              WHERE _id = (SELECT _id"
                + "                             FROM Entries"
                + "                            WHERE counter_id = e.counter_id"
                + "                            ORDER BY entry_date DESC"
                + "                            LIMIT 1)"
                + "            ) AS e"
                + "         ON e.counter_id = c._id";
        
        return getReadableDatabase().rawQuery(query, null);
    }

    public Cursor fetchCounterById(long id) {
        return getReadableDatabase().rawQuery("SELECT * FROM Counters WHERE _id = ?", new String[] {
            Long.toString(id)
        });
    }

    public long insertCounter(String name, String note, int color, String measure, String currency) {
        ContentValues cv = new ContentValues();

        cv.put("name", name);
        cv.put("note", note);
        cv.put("color", color);
        cv.put("measure", measure);
        cv.put("currency", currency);

        return getWritableDatabase().insert("Counters", null, cv);
    }

    public void updateCounter(long id, String name, String note, int color, String measure, String currency) {
        ContentValues cv = new ContentValues();

        cv.put("name", name);
        cv.put("note", note);
        cv.put("color", color);
        cv.put("measure", measure);
        cv.put("currency", currency);

        getWritableDatabase().update("Counters", cv, "_id = ?", new String[] {
            Long.toString(id)
        });
    }

    public void deleteCounter(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("PRAGMA foreign_keys = ON;");
        db.delete("Counters", "_id = ?", new String[] {
            Long.toString(id)
        });
    }

    public void deleteAllCounters() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("PRAGMA foreign_keys = ON;");
        db.delete("Counters", null, null);
    }

    // ===========================================================
    // Entries Methods
    // ===========================================================
    public Cursor fetchEntryById(long id) {
        return getReadableDatabase().rawQuery("SELECT * FROM Entries WHERE _id = ?", new String[] {
            Long.toString(id)
        });
    }

    public Cursor fetchEntriesByCounterId(long counterId) {                
        final String query = ""
                + "SELECT e._id AS _id"
                + "      ,e.counter_id AS counter_id"
                + "      ,e.entry_date AS entry_date" 
                + "      ,e.value AS value"
                + "      ,e.prev_value AS prev_value"
                + "      ,e.value - e.prev_value AS delta"
                + "      ,round(e.rate * (e.value - e.prev_value), 2) AS cost"
                + "      ,e.rate AS rate"
                + "      ,c.measure AS measure"
                + "      ,c.currency AS currency"
                + "  FROM (SELECT _id" 
                + "              ,counter_id"
                + "              ,entry_date" 
                + "              ,value"
                + "              ,ifnull((SELECT value"
                + "                         FROM Entries " 
                + "                        WHERE counter_id = en.counter_id " 
                + "                          AND entry_date < en.entry_date "
                + "                        ORDER BY entry_date DESC "
                + "                        LIMIT 1), 0)"
                + "               AS prev_value"
                + "              ,rate"
                + "          FROM Entries AS en "
                + "         WHERE counter_id = ? "
                + "         ORDER BY entry_date DESC"
                + "       ) AS e"
                + " INNER JOIN Counters AS c"
                + "         ON c._id = e.counter_id";
        
        return getReadableDatabase().rawQuery(
                        query, 
                        new String[] {
                            Long.toString(counterId)
                        });
    }

    public Cursor fetchLastRateByCounterId(long counterId) {
        return getReadableDatabase().rawQuery(
                "SELECT rate FROM Entries WHERE counter_id = ? ORDER BY entry_date DESC LIMIT 1",
                new String[] {
                    Long.toString(counterId)
                });
    }

    public Cursor fetchRateByCounterId(long counterId, String date) {
        return getReadableDatabase()
                .rawQuery(
                        "SELECT rate FROM Entries WHERE counter_id = ? AND entry_date < ? ORDER BY entry_date DESC LIMIT 1",
                        new String[] {
                                Long.toString(counterId), date
                        });
    }

    public long insertEntry(long counterId, String entryDate, double value, double rate) {
        ContentValues cv = new ContentValues();

        cv.put("counter_id", counterId);
        cv.put("entry_date", entryDate);
        cv.put("value", value);
        cv.put("rate", rate);

        return getWritableDatabase().insert("Entries", null, cv);
    }

    public void updateEntry(long id, String entryDate, double value, double rate) {
        ContentValues cv = new ContentValues();

        cv.put("entry_date", entryDate);
        cv.put("value", value);
        cv.put("rate", rate);

        getWritableDatabase().update("Entries", cv, "_id = ?", new String[] {
            Long.toString(id)
        });
    }

    public void deleteEntry(long id) {
        getWritableDatabase().delete("Entries", "_id = ?", new String[] {
            Long.toString(id)
        });
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
    private static class Patch {
        public void apply(SQLiteDatabase db) {}
       
        public void revert(SQLiteDatabase db) {}
    }
}
