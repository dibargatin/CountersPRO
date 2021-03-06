
package com.blogspot.dibargatin.counterspro.database;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

import com.blogspot.dibargatin.counterspro.database.Counter.RateType;
import com.blogspot.dibargatin.counterspro.util.FormulaEvaluator;

public class Indication {
    // ===========================================================
    // Constants
    // ===========================================================
    public final static long EMPTY_ID = -1; 
    
    public final static int COST_PRECISION = 2;

    // ===========================================================
    // Fields
    // ===========================================================
    long mId;

    Counter mCounter;

    Timestamp mDate;

    double mValue;

    double mRateValue;

    double mTotal;
    
    String mNote;
    
    // ===========================================================
    // Constructors
    // ===========================================================
    public Indication(Counter counter) {
        mId = EMPTY_ID;
        mCounter = counter;
    }
    
    public Indication(Indication indication, Counter counter) {
        mId = EMPTY_ID;
        mCounter = counter;
        mDate = indication.getDate();
        mValue = indication.getValue();
        mRateValue = indication.getRateValue();
        mTotal = indication.getTotal();
        mNote = indication.getNote();
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public long getId() {
        return mId;
    }

    public Counter getCounter() {
        return mCounter;
    }

    public Timestamp getDate() {
        return mDate;
    }

    public double getValue() {
        return mValue;
    }

    public double getRateValue() {
        return mRateValue;
    }

    public double getTotal() {
        return mTotal;
    }

    public double getPreviousTotal() {
        return mTotal - mValue;
    }
    
    public String getNote() {
        return mNote;
    }
    
    public void setId(long id) {
        this.mId = id;
    }

    public void setDate(Timestamp date) {
        this.mDate = date;
    }

    public void setValue(double value) {
        this.mValue = value;
    }

    public void setRateValue(double rateValue) {
        this.mRateValue = rateValue;
    }

    public void setTotal(double total) {
        this.mTotal = total;
    }
    
    public void setNote(String note) {
        this.mNote = note;
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
    public double calcCost(int precision, String[] totalAliases, String[] valueAliases, String[] rateAliases) {
        double result = 0.0;

        if (mCounter != null) {
            if (mCounter.getRateType() == RateType.SIMPLE) {
                result = mRateValue * mValue;

            } else if (mCounter.getRateType() == RateType.FORMULA) {

                final FormulaEvaluator eval = new FormulaEvaluator(totalAliases, mTotal,
                        valueAliases, mValue, rateAliases, mRateValue);

                result = eval.evaluate(mCounter.getFormula());
            }
        }
        
        if (!Double.isInfinite(result) && !Double.isNaN(result)) {
            result = new BigDecimal(result).setScale(precision, RoundingMode.HALF_UP).doubleValue();
        }

        return result;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
