
package com.blogspot.dibargatin.counterspro.database;

import java.util.ArrayList;
import java.util.Collection;

public class IndicationsCollection extends ArrayList<Indication> {
    // ===========================================================
    // Constants
    // ===========================================================
    static final long serialVersionUID = 1L;

    // ===========================================================
    // Fields
    // ===========================================================
    private double mTotal = 0;

    private double mTotalCost = 0;

    private int mPrecision = Indication.COST_PRECISION;

    private String[] mTotalAliases;

    private String[] mValueAliases;

    private String[] mRateAliases;

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public double getTotal() {
        return mTotal;
    }

    public double getTotalCost() {
        throwCostCalculatorNotInit();
        return mTotalCost;
    }

    public long getMinTime() {
        long min = 0;

        if (size() != 0) {
            min = this.get(0).getDate().getTime();

            for (Indication i : this) {
                if (min > i.getDate().getTime()) {
                    min = i.getDate().getTime();
                }
            }
        }

        return min;
    }

    public long getMaxTime() {
        long max = 0;

        if (size() != 0) {
            max = this.get(0).getDate().getTime();

            for (Indication i : this) {
                if (max < i.getDate().getTime()) {
                    max = i.getDate().getTime();
                }
            }
        }

        return max;
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================
    @Override
    public boolean add(Indication object) {
        addToTotal(object);
        addToTotalCost(object);

        return super.add(object);
    }

    @Override
    public void add(int index, Indication object) {
        addToTotal(object);
        addToTotalCost(object);

        super.add(index, object);
    }

    @Override
    public boolean addAll(Collection<? extends Indication> collection) {
        for (Indication i : collection) {
            addToTotal(i);
            addToTotalCost(i);
        }

        return super.addAll(collection);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Indication> collection) {
        for (Indication i : collection) {
            addToTotal(i);
            addToTotalCost(i);
        }

        return super.addAll(index, collection);
    }

    @Override
    public Indication remove(int index) {
        subFromTotal(this.get(index));
        subFromTotalCost(this.get(index));

        return super.remove(index);
    }

    @Override
    public boolean remove(Object object) {
        if (this.contains(object)) {
            subFromTotal((Indication)object);
            subFromTotalCost((Indication)object);
        }

        return super.remove(object);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        for (Object i : collection) {
            if (this.contains(i)) {
                subFromTotal((Indication)i);
                subFromTotalCost((Indication)i);
            }
        }

        return super.removeAll(collection);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            subFromTotal(this.get(i));
            subFromTotalCost(this.get(i));
        }

        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        for (Indication i : this) {
            if (!collection.contains(i)) {
                subFromTotal(i);
                subFromTotalCost(i);
            }
        }

        return super.retainAll(collection);
    }

    @Override
    public void clear() {
        mTotal = 0;
        mTotalCost = 0;

        super.clear();
    }

    @Override
    public Indication set(int index, Indication object) {
        final Indication obj = this.get(index);
        if (obj != null) {
            subFromTotal(obj);
            subFromTotalCost(obj);
        }

        addToTotal(object);
        addToTotalCost(object);

        return super.set(index, object);
    }

    // ===========================================================
    // Methods
    // ===========================================================
    public void initCostCalculator(int precision, String[] totalAliases, String[] valueAliases,
            String[] rateAliases) {
        mTotalAliases = totalAliases;
        mValueAliases = valueAliases;
        mRateAliases = rateAliases;

        mPrecision = precision;
        mTotalCost = 0;

        for (Indication i : this) {
            addToTotalCost(i);
        }
    }

    public boolean checkCostCalculatorState() {
        boolean result = true;

        if (mTotalAliases == null || mValueAliases == null || mRateAliases == null) {
            result = false;
        }

        return result;
    }

    private void throwCostCalculatorNotInit() {
        if (!checkCostCalculatorState()) {
            throw new RuntimeException(
                    "Cost calculator not init. Before use it you must call initCostCalculator()");
        }
    }

    private void addToTotal(Indication object) {
        mTotal += object.getValue();
    }

    private void addToTotalCost(Indication object) {
        if (checkCostCalculatorState()) {
            mTotalCost += object.calcCost(mPrecision, mTotalAliases,
                    mValueAliases, mRateAliases);
        }
    }

    private void subFromTotal(Indication object) {
        mTotal -= object.getValue();
    }

    private void subFromTotalCost(Indication object) {
        if (checkCostCalculatorState()) {
            mTotalCost -= object.calcCost(mPrecision, mTotalAliases, mValueAliases,
                    mRateAliases);
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
