
package com.blogspot.dibargatin.housing.graph;

import java.util.Arrays;

public class GraphSeries {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================
    final GraphData[] mValues;

    final String mName;

    final String mXAxisName;

    final double mMaxX;

    final double mMinX;

    final double mXLength;

    final String mYAxisName;

    final double mMaxY;

    final double mMinY;

    final double mYLength;

    final GraphSeriesStyle mStyle;

    // ===========================================================
    // Constructors
    // ===========================================================
    public GraphSeries(GraphData[] values, String name, String xAxisName, String yAxisName,
            GraphSeriesStyle style) {
        mValues = values;
        mName = name;
        mXAxisName = xAxisName;
        mYAxisName = yAxisName;
        mStyle = style;

        Arrays.sort(values);

        mMinX = values[0].valueX;
        mMaxX = values[values.length - 1].valueX;
        mXLength = Math.sqrt(Math.pow(mMaxX - mMinX, 2));

        double max = values[0].valueY;
        double min = values[0].valueY;

        for (int i = 1; i < values.length; i++) {
            if (values[i].valueY > max) {
                max = values[i].valueY;
            }

            if (values[i].valueY < min) {
                min = values[i].valueY;
            }
        }

        mMaxY = max;
        mMinY = min;
        mYLength = Math.sqrt(Math.pow(mMaxY - mMinY, 2));
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public GraphData[] getValues() {
        return mValues;
    }

    public String getXAxisName() {
        return mXAxisName;
    }

    public String getYAxisName() {
        return mYAxisName;
    }

    public GraphSeriesStyle getStyle() {
        return mStyle;
    }

    public double getMaxX() {
        return mMaxX;
    }

    public double getMinX() {
        return mMinX;
    }

    public double getXLength() {
        return mXLength;
    }

    public double getMaxY() {
        return mMaxY;
    }

    public double getMinY() {
        return mMinY;
    }

    public double getYLength() {
        return mYLength;
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
    static public class GraphData implements Comparable<GraphData> {
        public final double valueX;

        public final double valueY;

        public GraphData(double valueX, double valueY) {
            this.valueX = valueX;
            this.valueY = valueY;
        }

        @Override
        public int compareTo(GraphData d) {
            if (valueX < d.valueX) {
                return -1;
            }

            if (valueX > d.valueX) {
                return 1;
            }

            if (valueY > d.valueY) {
                return -1;
            }

            if (valueY < d.valueY) {
                return 1;
            }

            return 0;
        }
    }

    static public class GraphSeriesStyle {
        public int graphColor = 0xff0077cc;

        public float graphThickness = 3f;

        public int axisColor = 0xff0000ff;

        public float axisThickness = 1f;

        public boolean isDrawAxis = false;
    }
}
