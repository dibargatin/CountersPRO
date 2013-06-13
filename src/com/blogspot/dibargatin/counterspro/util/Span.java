
package com.blogspot.dibargatin.counterspro.util;

public class Span {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================
    private long mLeft;

    private long mRight;

    // ===========================================================
    // Constructors
    // ===========================================================
    public Span(long left, long right) {
        mLeft = left;
        mRight = right;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public long getLeft() {
        return mLeft;
    }

    public long getRight() {
        return mRight;
    }

    public void setLeft(long left) {
        this.mLeft = left;
    }

    public void setRight(long right) {
        this.mRight = right;
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
    public boolean contains(long value) {
        return mLeft <= value ? value >= mRight : false;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
