
package com.blogspot.dibargatin.housing;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    // ===========================================================
    // Constants
    // ===========================================================
    public static String DB_NAME = "housing_db.sqlite3";

    public static int DB_VERSION = 4;
    
    private static final Patch[] PATCHES = new Patch[] {
        // №1 Таблицы для храненения счетчиков и показаний
        new Patch() {
           public void apply(SQLiteDatabase db) {
               db.beginTransaction();
               
               try {
                   db.execSQL("PRAGMA foreign_keys = ON;");
                   
                   final String queryCounters = ""
                           + "CREATE TABLE Counters (" 
                           + " _id INTEGER PRIMARY KEY" 
                           + ",name TEXT" 
                           + ",note TEXT" 
                           + ",measure TEXT" 
                           + ",color INTEGER"
                           + ",currency TEXT"
                           + ");";
                   
                   db.execSQL(queryCounters);
                   
                   final String queryEntries = ""
                           + "CREATE TABLE Entries (" 
                           + " _id INTEGER PRIMARY KEY" 
                           + ",counter_id INTEGER REFERENCES Counters(_id) ON DELETE CASCADE" 
                           + ",entry_date DATE" 
                           + ",value REAL" 
                           + ",rate REAL"
                           + ");";
                   
                   db.execSQL(queryEntries);
                   
                   db.execSQL("INSERT INTO Counters (_id, name, note, measure, color, currency) VALUES (1, 'Холодная вода', 'Дом', 'м<small><sup>3</sup></small>', -4000, 'руб');");
                   db.execSQL("INSERT INTO Entries (counter_id, entry_date, value, rate) VALUES (1, '2013-04-16', 123, 2);");
                   
                   db.setTransactionSuccessful();
                   
               } catch (Exception e) {
                   Log.d(MainActivity.LOG_TAG, e.getStackTrace().toString());
               } finally {
                   db.endTransaction();
               }
           }
      
           public void revert(SQLiteDatabase db) {
               db.beginTransaction();
               
               try {
                   db.execSQL("DROP TABLE IF EXISTS Counters;");
                   db.execSQL("DROP TABLE IF EXISTS Entries;");
                   
                   db.setTransactionSuccessful();
                   
               } catch (Exception e) {
                   Log.d(MainActivity.LOG_TAG, e.getStackTrace().toString());
               } finally {
                   db.endTransaction();
               }
           }
        }
        
        // №2 Вид тарифа в таблицу счетчиков
        ,new Patch() {
            public void apply(SQLiteDatabase db) {
                db.beginTransaction();
                
                try {
                    db.execSQL("ALTER TABLE Counters RENAME TO Counters_old;");
                                    
                    final String queryCounters = ""
                            + "CREATE TABLE Counters (" 
                            + " _id INTEGER PRIMARY KEY" 
                            + ",name TEXT" 
                            + ",note TEXT" 
                            + ",measure TEXT" 
                            + ",color INTEGER"
                            + ",currency TEXT"
                            + ",rate_type INTEGER"
                            + ");";
                    
                    db.execSQL(queryCounters);
                    
                    final String queryCopyData = ""
                            + "INSERT INTO Counters (_id, name, note, measure, color, currency, rate_type) "
                            + "SELECT _id, name, note, measure, color, currency, 1 AS rate_type FROM Counters_old;";
                    
                    db.execSQL(queryCopyData);
                    
                    db.execSQL("DROP TABLE IF EXISTS Counters_old;");
                    
                    db.setTransactionSuccessful();
                    
                } catch (Exception e) {
                    Log.d(MainActivity.LOG_TAG, e.getStackTrace().toString());
                } finally {
                    db.endTransaction();
                }                
            }
            
            public void revert(SQLiteDatabase db) { 
                db.beginTransaction();
                
                try {
                    db.execSQL("ALTER TABLE Counters RENAME TO Counters_old");
                                    
                    final String queryCounters = ""
                            + "CREATE TABLE Counters (" 
                            + " _id INTEGER PRIMARY KEY" 
                            + ",name TEXT" 
                            + ",note TEXT" 
                            + ",measure TEXT" 
                            + ",color INTEGER"
                            + ",currency TEXT"
                            + ");";
                    
                    db.execSQL(queryCounters);
                    
                    final String queryCopyData = ""
                            + "INSERT INTO Counters (_id, name, note, measure, color, currency) "
                            + "SELECT _id, name, note, measure, color, currency FROM Counters_old";
                    
                    db.execSQL(queryCopyData);
                    
                    db.execSQL("DROP TABLE IF EXISTS Counters_old;");
                    
                    db.setTransactionSuccessful();
                    
                } catch (Exception e) {
                    Log.d(MainActivity.LOG_TAG, e.getStackTrace().toString());
                } finally {
                    db.endTransaction();
                }
            }
         }
        
        // №3 Вид периода в таблицу счетчиков
        ,new Patch() {
            public void apply(SQLiteDatabase db) {
                db.beginTransaction();
                
                try {
                    db.execSQL("ALTER TABLE Counters RENAME TO Counters_old;");
                                    
                    final String queryCounters = ""
                            + "CREATE TABLE Counters (" 
                            + " _id INTEGER PRIMARY KEY" 
                            + ",name TEXT" 
                            + ",note TEXT" 
                            + ",measure TEXT" 
                            + ",color INTEGER"
                            + ",currency TEXT"
                            + ",rate_type INTEGER"
                            + ",period_type INTEGER"
                            + ");";
                    
                    db.execSQL(queryCounters);
                    
                    final String queryCopyData = ""
                            + "INSERT INTO Counters (_id, name, note, measure, color, currency, rate_type, period_type) "
                            + "SELECT _id, name, note, measure, color, currency, 1 AS rate_type, 1 AS period_type FROM Counters_old;";
                    
                    db.execSQL(queryCopyData);
                    
                    db.execSQL("DROP TABLE IF EXISTS Counters_old;");
                    
                    db.setTransactionSuccessful();
                    
                } catch (Exception e) {
                    Log.d(MainActivity.LOG_TAG, e.getStackTrace().toString());
                } finally {
                    db.endTransaction();
                }                
            }
            
            public void revert(SQLiteDatabase db) { 
                db.beginTransaction();
                
                try {
                    db.execSQL("ALTER TABLE Counters RENAME TO Counters_old");
                                    
                    final String queryCounters = ""
                            + "CREATE TABLE Counters (" 
                            + " _id INTEGER PRIMARY KEY" 
                            + ",name TEXT" 
                            + ",note TEXT" 
                            + ",measure TEXT" 
                            + ",color INTEGER"
                            + ",currency TEXT"
                            + ",rate_type INTEGER"
                            + ");";
                    
                    db.execSQL(queryCounters);
                    
                    final String queryCopyData = ""
                            + "INSERT INTO Counters (_id, name, note, measure, color, currency, rate_type) "
                            + "SELECT _id, name, note, measure, color, currency, rate_type FROM Counters_old";
                    
                    db.execSQL(queryCopyData);
                    
                    db.execSQL("DROP TABLE IF EXISTS Counters_old;");
                    
                    db.setTransactionSuccessful();
                    
                } catch (Exception e) {
                    Log.d(MainActivity.LOG_TAG, e.getStackTrace().toString());
                } finally {
                    db.endTransaction();
                }
            }
         }
        
        // №4 Формула в таблицу счетчиков
        ,new Patch() {
            public void apply(SQLiteDatabase db) {
                db.beginTransaction();
                
                try {
                    db.execSQL("ALTER TABLE Counters RENAME TO Counters_old;");
                                    
                    final String queryCounters = ""
                            + "CREATE TABLE Counters (" 
                            + " _id INTEGER PRIMARY KEY" 
                            + ",name TEXT" 
                            + ",note TEXT" 
                            + ",measure TEXT" 
                            + ",color INTEGER"
                            + ",currency TEXT"
                            + ",rate_type INTEGER"
                            + ",period_type INTEGER"
                            + ",formula TEXT"
                            + ");";
                    
                    db.execSQL(queryCounters);
                    
                    final String queryCopyData = ""
                            + "INSERT INTO Counters (_id, name, note, measure, color, currency, rate_type, period_type, formula) "
                            + "SELECT _id, name, note, measure, color, currency, 1 AS rate_type, 1 AS period_type, '' AS formula FROM Counters_old;";
                    
                    db.execSQL(queryCopyData);
                    
                    db.execSQL("DROP TABLE IF EXISTS Counters_old;");
                    
                    db.setTransactionSuccessful();
                    
                } catch (Exception e) {
                    Log.d(MainActivity.LOG_TAG, e.getStackTrace().toString());
                } finally {
                    db.endTransaction();
                }                
            }
            
            public void revert(SQLiteDatabase db) { 
                db.beginTransaction();
                
                try {
                    db.execSQL("ALTER TABLE Counters RENAME TO Counters_old");
                                    
                    final String queryCounters = ""
                            + "CREATE TABLE Counters (" 
                            + " _id INTEGER PRIMARY KEY" 
                            + ",name TEXT" 
                            + ",note TEXT" 
                            + ",measure TEXT" 
                            + ",color INTEGER"
                            + ",currency TEXT"
                            + ",rate_type INTEGER"
                            + ",period_type INTEGER"
                            + ");";
                    
                    db.execSQL(queryCounters);
                    
                    final String queryCopyData = ""
                            + "INSERT INTO Counters (_id, name, note, measure, color, currency, rate_type, period_type) "
                            + "SELECT _id, name, note, measure, color, currency, rate_type, period_type FROM Counters_old";
                    
                    db.execSQL(queryCopyData);
                    
                    db.execSQL("DROP TABLE IF EXISTS Counters_old;");
                    
                    db.setTransactionSuccessful();
                    
                } catch (Exception e) {
                    Log.d(MainActivity.LOG_TAG, e.getStackTrace().toString());
                } finally {
                    db.endTransaction();
                }
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
                + "         ON e.counter_id = c._id"
                + " ORDER BY note, name";
        
        return getReadableDatabase().rawQuery(query, null);
    }

    public Cursor fetchCounterById(long id) {
        return getReadableDatabase().rawQuery("SELECT * FROM Counters WHERE _id = ?", new String[] {
            Long.toString(id)
        });
    }

    public long insertCounter(String name, String note, int color, String measure, String currency, int rateType, int periodType, String formula) {
        ContentValues cv = new ContentValues();

        cv.put("name", name);
        cv.put("note", note);
        cv.put("color", color);
        cv.put("measure", measure);
        cv.put("currency", currency);
        cv.put("rate_type", rateType);
        cv.put("period_type", periodType);
        cv.put("formula", formula);

        return getWritableDatabase().insert("Counters", null, cv);
    }

    public void updateCounter(long id, String name, String note, int color, String measure, String currency, int rateType, int periodType, String formula) {
        ContentValues cv = new ContentValues();

        cv.put("name", name);
        cv.put("note", note);
        cv.put("color", color);
        cv.put("measure", measure);
        cv.put("currency", currency);
        cv.put("rate_type", rateType);
        cv.put("period_type", periodType);
        cv.put("formula", formula);
        
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
        final String query = ""
                + "SELECT e.*" 
                + "      ,c.rate_type " 
                + "  FROM Entries AS e " 
                + " INNER JOIN Counters AS c " 
                + "         ON c._id = e.counter_id " 
                + " WHERE e._id = ?;";
        
        return getReadableDatabase().rawQuery(query, new String[] {
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
                + "      ,c.rate_type AS rate_type"
                + "      ,c.period_type AS period_type"
                + "      ,c.formula AS formula"
                + "  FROM (SELECT _id" 
                + "              ,counter_id"
                + "              ,entry_date" 
                + "              ,value"
                + "              ,ifnull((SELECT value"
                + "                         FROM Entries " 
                + "                        WHERE counter_id = en.counter_id " 
                + "                          AND entry_date < en.entry_date "
                + "                        ORDER BY entry_date DESC, _id DESC "
                + "                        LIMIT 1), 0)"
                + "               AS prev_value"
                + "              ,rate"
                + "          FROM Entries AS en "
                + "         WHERE counter_id = ? "
                + "         ORDER BY entry_date DESC, _id DESC"
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
        final String query = ""
                + "SELECT c.rate_type AS rate_type "
                + "      ,(SELECT rate "
                + "          FROM Entries "
                + "         WHERE counter_id = c._id "
                + "         ORDER BY entry_date DESC "
                + "         LIMIT 1 "
                + "       ) AS rate"
                + "  FROM Counters AS c "
                + " WHERE _id = ?";
        
        return getReadableDatabase().rawQuery(
                query,
                new String[] {
                    Long.toString(counterId)
                });
    }

    public Cursor fetchRateByCounterId(long counterId, String date) {
        final String query = ""
                + "SELECT e.rate AS rate "
                + "      ,c.rate_type AS rate_type "
                + "  FROM Entries AS e "
                + " INNER JOIN Counters AS c "
                + "         ON c._id = e.counter_id "
                + "WHERE e.counter_id = ? "
                + "  AND e.entry_date < ? "
                + "ORDER BY e.entry_date DESC "
                + "LIMIT 1; ";
        
        return getReadableDatabase().rawQuery(
                query,
                new String[] {
                        Long.toString(counterId), date
                });
    }
    
    public boolean isEntryExists(long counterId, String entryDate) {
        final String query = ""
                + "SELECT EXISTS ("
                + "         SELECT 1"
                + "           FROM Entries"
                + "          WHERE counter_id = ?"
                + "            AND entry_date = ?"
                + "          LIMIT 1"
                + "         ) AS cnt;";
        
        final Cursor c = getReadableDatabase().rawQuery(
                query,
                new String[] {
                        Long.toString(counterId), entryDate
                });
        c.moveToFirst();
        
        return c.getInt(c.getColumnIndex("cnt")) != 0;
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
