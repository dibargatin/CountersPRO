
package com.blogspot.dibargatin.housing.database;

import java.sql.Timestamp;

import com.blogspot.dibargatin.housing.database.Counter.RateType;
import com.blogspot.dibargatin.housing.util.FormulaEvaluator;

public class Indication {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================
    long mId;

    Counter mCounter;

    Timestamp mDate;

    double mValue;

    double mRateValue;

    double mTotal;

    // ===========================================================
    // Constructors
    // ===========================================================
    public Indication(Counter counter) {
        mCounter = counter;
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

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
    public double calcCost(String[] totalAliases, String[] valueAliases, String[] rateAliases) {
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

        return result;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
