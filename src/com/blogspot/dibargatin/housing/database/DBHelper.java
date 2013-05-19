
package com.blogspot.dibargatin.housing.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.blogspot.dibargatin.housing.CountersListActivity;

public class DBHelper extends SQLiteOpenHelper {

    // ===========================================================
    // Constants
    // ===========================================================
    public static String DB_NAME = "counterspro.sqlite3";
    public static int DB_VERSION = 1;
    
    // Таблица счетчиков
    public final static String TABLE_COUNTER = "Counters";

    public final static String COUNTER_ID = "_id";

    public final static String COUNTER_NAME = "name";

    public final static String COUNTER_NOTE = "note";

    public final static String COUNTER_MEASURE = "measure";

    public final static String COUNTER_COLOR = "color";

    public final static String COUNTER_CURRENCY = "currency";

    public final static String COUNTER_RATE_TYPE = "rate_type";

    public final static String COUNTER_PERIOD_TYPE = "period_type";

    public final static String COUNTER_FORMULA = "formula";
    
    // Таблица показаний
    public final static String TABLE_INDICATION = "Indications";
    
    public final static String INDICATION_ID = "_id";
    
    public final static String INDICATION_COUNTER_ID = "counter_id";    
    
    public final static String INDICATION_DATE = "entry_date";
    
    public final static String INDICATION_VALUE = "value"; 
    
    public final static String INDICATION_RATE = "rate";
    
    // Миграции БД    
    private static final Patch[] PATCHES = new Patch[] {
        // №1 Таблицы для храненения счетчиков и показаний
        new Patch() {
           public void apply(SQLiteDatabase db) {
               db.beginTransaction();
               
               try {
                   db.execSQL("PRAGMA foreign_keys = ON;");
                   
                   final StringBuilder queryCounters = new StringBuilder();
                   
                   queryCounters.append("CREATE TABLE " + TABLE_COUNTER +" ("); 
                   queryCounters.append(COUNTER_ID + " INTEGER PRIMARY KEY,"); 
                   queryCounters.append(COUNTER_NAME + " TEXT,");
                   queryCounters.append(COUNTER_NOTE + " TEXT,");
                   queryCounters.append(COUNTER_MEASURE + " TEXT,");
                   queryCounters.append(COUNTER_COLOR + " INTEGER,");
                   queryCounters.append(COUNTER_CURRENCY + " TEXT,");
                   queryCounters.append(COUNTER_RATE_TYPE + " INTEGER,");
                   queryCounters.append(COUNTER_PERIOD_TYPE + " INTEGER,");
                   queryCounters.append(COUNTER_FORMULA + " TEXT");
                   queryCounters.append(");");
                   
                   db.execSQL(queryCounters.toString());
                   
                   final StringBuilder queryIndications = new StringBuilder();
                   
                   queryIndications.append("CREATE TABLE " + TABLE_INDICATION + " (");
                   queryIndications.append(INDICATION_ID + " INTEGER PRIMARY KEY,");
                   queryIndications.append(INDICATION_COUNTER_ID + " INTEGER REFERENCES " + TABLE_COUNTER + "(" + COUNTER_ID + ") ON DELETE CASCADE,"); 
                   queryIndications.append(INDICATION_DATE + " DATE,");
                   queryIndications.append(INDICATION_VALUE + " REAL,"); 
                   queryIndications.append(INDICATION_RATE + " REAL");
                   queryIndications.append(");");
                   
                   db.execSQL(queryIndications.toString());
                   
                   db.setTransactionSuccessful();
                   
               } catch (Exception e) {
                   Log.d(CountersListActivity.LOG_TAG, e.getStackTrace().toString());
               } finally {
                   db.endTransaction();
               }
           }
      
           public void revert(SQLiteDatabase db) {
               db.beginTransaction();
               
               try {
                   db.execSQL("DROP TABLE IF EXISTS " + TABLE_COUNTER + ";");
                   db.execSQL("DROP TABLE IF EXISTS " + TABLE_INDICATION + ";");
                   
                   db.setTransactionSuccessful();
                   
               } catch (Exception e) {
                   Log.d(CountersListActivity.LOG_TAG, e.getStackTrace().toString());
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
    
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i=oldVersion; i>newVersion; i++) {
          PATCHES[i-1].revert(db);
        }
      }

    // ===========================================================
    // Methods
    // ===========================================================  
    
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
    private static class Patch {
        public void apply(SQLiteDatabase db) {}
       
        public void revert(SQLiteDatabase db) {}
    }
}
