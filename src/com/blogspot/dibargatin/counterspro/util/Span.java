
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
    
    private String mCaption;
    
    // ===========================================================
    // Constructors
    // ===========================================================
    public Span(long left, long right, String caption) {
        mLeft = left;
        mRight = right;
        mCaption = caption;
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
    
    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        this.mCaption = caption;
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
    public boolean contains(long value) {
        return mLeft <= value ? value <= mRight : false;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
