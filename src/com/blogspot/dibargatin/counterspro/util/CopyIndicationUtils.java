package com.blogspot.dibargatin.counterspro.util;

import com.blogspot.dibargatin.counterspro.database.Indication;
import com.blogspot.dibargatin.counterspro.database.IndicationsCollection;

public class CopyIndicationUtils {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
    public static boolean isDestHasEqualDate(Indication source, IndicationsCollection destination) {
        boolean result = false;
        
        for (Indication ind : destination) {
            if (ind.getDate().equals(source.getDate())) {
                result = true;
                break;
            }
        }
        
        return result;
    }
    
    public static IndicationsCollection checkForEqualDate(IndicationsCollection source, IndicationsCollection destination) {
        IndicationsCollection result = new IndicationsCollection();
        
        for (Indication ind : source) {
            if (isDestHasEqualDate(ind, destination)) {
                result.add(ind);
            }
        }
        
        return result;
    }
    
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
