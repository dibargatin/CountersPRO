
package com.blogspot.dibargatin.counterspro.database;

import java.sql.Timestamp;

public class Event {
    // ===========================================================
    // Constants
    // ===========================================================
    public final static long EMPTY_ID = -1;

    // ===========================================================
    // Fields
    // ===========================================================
    long mId;
    
    Counter mCounter;

    Timestamp mDate;
    
    RepeatType mRepeatType;
    
    String mTitle;
    
    String mNote;

    // ===========================================================
    // Constructors
    // ===========================================================
    public Event(Counter pCounter) {
        mId = EMPTY_ID;
        mRepeatType = RepeatType.ONCE;
        mCounter = pCounter;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public long getId() {
        return mId;
    }

    public Timestamp getDate() {
        return mDate;
    }

    public RepeatType getRepeatType() {
        return mRepeatType;
    }

    public void setId(long pId) {
        this.mId = pId;
    }

    public void setDate(Timestamp pDate) {
        this.mDate = pDate;
    }

    public void setRepeatType(RepeatType pRepeatType) {
        this.mRepeatType = pRepeatType;
    }
    
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String pTitle) {
        this.mTitle = pTitle;
    }
    
    public String getNote() {
        return mNote;
    }

    public void setNote(String pNote) {
        this.mNote = pNote;
    }
    
    public Counter getCounter() {
        return mCounter;
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
    public enum RepeatType {
        /**
         * Один раз
         */
        ONCE,
        /**
         * Ежедневно
         */
        EVERYDAY,
        /**
         * Ежедневно по рабочим дням (Пример: для РФ с пн. по пт.)
         */
        EVERY_WORKDAY,
        /**
         * Каждую неделю (Например: во вторник)
         */
        EVERY_WEEK,
        /**
         * Ежемесячно (Например: в 17 день)
         */
        EVERY_MONTH,
        /**
         * Ежемесячно (Например: каждый третий вторник)
         */
        EVERY_MONTH_DAY_OF_WEEK,
        /*
         * Ежегодно (Например: 17 сентября)
         */
        EVERY_YEAR
    }
}
