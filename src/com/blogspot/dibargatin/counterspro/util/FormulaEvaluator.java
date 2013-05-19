package com.blogspot.dibargatin.counterspro.util;

import net.astesana.javaluator.DoubleEvaluator;
import net.astesana.javaluator.StaticVariableSet;

public class FormulaEvaluator {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================
    final DoubleEvaluator mEvaluator = new DoubleEvaluator();
    
    final StaticVariableSet<Double> mVariables = new StaticVariableSet<Double>();

    // ===========================================================
    // Constructors
    // ===========================================================
    public FormulaEvaluator(String[] valueAliases, Double value, String[] deltaAliases, Double delta, String[] tariffAliases, Double tariff) {
        
        for (String v : valueAliases) {
            mVariables.set(v, value);
        }
        
        for (String d : deltaAliases) {
            mVariables.set(d, delta);
        }
        
        for (String t : tariffAliases) {
            mVariables.set(t, tariff);
        }
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
    public Double evaluate(String expression) {
        return mEvaluator.evaluate(expression, mVariables);        
    }
    
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
