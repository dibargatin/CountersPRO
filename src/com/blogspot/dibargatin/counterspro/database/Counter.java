
package com.blogspot.dibargatin.counterspro.database;

public class Counter {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================
    long mId;

    String mName;

    String mNote;

    String mMeasure;

    int mColor;

    String mCurrency;

    RateType mRateType;

    PeriodType mPeriodType;

    String mFormula;
    
    ViewValueType mViewValueType;    

    IndicationsCollection mIndications;

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getNote() {
        return mNote;
    }

    public String getMeasure() {
        return mMeasure;
    }

    public int getColor() {
        return mColor;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public RateType getRateType() {
        return mRateType;
    }

    public PeriodType getPeriodType() {
        return mPeriodType;
    }

    public String getFormula() {
        return mFormula;
    }

    public IndicationsCollection getIndications() {
        return mIndications;
    }

    public void setIndications(IndicationsCollection indications) {
        this.mIndications = indications;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setNote(String note) {
        this.mNote = note;
    }

    public void setMeasure(String measure) {
        this.mMeasure = measure;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public void setCurrency(String currency) {
        this.mCurrency = currency;
    }

    public void setRateType(RateType rateType) {
        this.mRateType = rateType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.mPeriodType = periodType;
    }

    public void setFormula(String formula) {
        this.mFormula = formula;
    }
    
    public ViewValueType getViewValueType() {
        return mViewValueType;
    }

    public void setViewValueType(ViewValueType viewValueType) {
        this.mViewValueType = viewValueType;
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
    public enum RateType {
        WITHOUT, SIMPLE, FORMULA
    }

    public enum PeriodType {
        YEAR, MONTH, DAY, HOUR, MINUTE
    }
    
    public enum ViewValueType {
        DELTA, TOTAL, COST, TOTAL_COST
    }
}
