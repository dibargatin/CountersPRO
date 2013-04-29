
package com.blogspot.dibargatin.housing.util;

import java.text.DecimalFormatSymbols;

import android.content.Context;
import android.text.InputType;
import android.text.method.DigitsKeyListener;

public class DecimalKeyListener extends DigitsKeyListener {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================
    private final char[] acceptedCharacters;

    // ===========================================================
    // Constructors
    // ===========================================================
    public DecimalKeyListener(Context c) {
        acceptedCharacters = new char[] {
                '0',
                '1',
                '2',
                '3',
                '4',
                '5',
                '6',
                '7',
                '8',
                '9',
                '-',
                new DecimalFormatSymbols(c.getResources().getConfiguration().locale)
                        .getDecimalSeparator()
        };
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public int getInputType() {
        return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT;
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================
    @Override
    protected char[] getAcceptedChars() {
        return acceptedCharacters;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
