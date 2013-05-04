
package com.blogspot.dibargatin.housing.graph;

import java.util.ArrayList;

import com.blogspot.dibargatin.housing.graph.GraphSeries.GraphData;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class LineGraph extends View {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================
    final ArrayList<GraphSeries> mSeries = new ArrayList<GraphSeries>();

    final ArrayList<Paint> mAxisPaint = new ArrayList<Paint>();

    final ArrayList<Paint> mGraphPaint = new ArrayList<Paint>();

    final ArrayList<Paint> mPointsPaint = new ArrayList<Paint>();

    int mWidth = 0;

    int mHeight = 0;

    // ===========================================================
    // Constructors
    // ===========================================================
    public LineGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LineGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineGraph(Context context) {
        super(context);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public void addSeries(GraphSeries series) {
        mSeries.add(series);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(series.getStyle().graphColor);
        paint.setStrokeWidth(series.getStyle().graphThickness);
        mGraphPaint.add(paint);

        if (series.getStyle().isDrawAxis) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(series.getStyle().axisColor);
            paint.setStrokeWidth(series.getStyle().axisThickness);
            mAxisPaint.add(paint);
        } else {
            mAxisPaint.add(null);
        }

        if (series.getStyle().isDrawPoints) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(series.getStyle().pointsColor);
            paint.setStrokeWidth(series.getStyle().pointsThickness);
            mPointsPaint.add(paint);
        } else {
            mPointsPaint.add(null);
        }
    }

    public GraphSeries getSeries(int index) {
        return mSeries.get(index);
    }

    public void removeSeries(int index) {
        mSeries.remove(index);
        mGraphPaint.remove(index);
        mAxisPaint.remove(index);
        mPointsPaint.remove(index);
    }

    public void clearSeries() {
        mSeries.clear();
        mGraphPaint.clear();
        mAxisPaint.clear();
        mPointsPaint.clear();
    }

    public Paint getAxisPaint(int index) {
        return mAxisPaint.get(index);
    }

    public Paint getGraphPaint(int index) {
        return mGraphPaint.get(index);
    }

    public Paint getPointsPaint(int index) {
        return mPointsPaint.get(index);
    }

    public void setAxisPaint(int index, Paint axisPaint) {
        this.mAxisPaint.set(index, axisPaint);
    }

    public void setGraphPaint(int index, Paint graphPaint) {
        this.mGraphPaint.set(index, graphPaint);
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = getPaddingLeft() + w + getPaddingRight();
        mHeight = getPaddingTop() + h + getPaddingBottom();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (GraphSeries s : mSeries) {
            drawSeries(s, canvas);
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================
    protected void drawSeries(GraphSeries series, Canvas canvas) {

        final GraphData[] v = series.getValues();

        if (v.length < 2)
            return;

        final int index = mSeries.indexOf(series);
        final Paint gp = getGraphPaint(index);
        final Paint pp = getPointsPaint(index);

        final double lx = series.getMaxX() - series.getMinX();
        final double ly = series.getMaxY() - series.getMinY();

        float[] cx = null;
        float[] cy = null;

        if (series.getStyle().isDrawPoints) {
            cx = new float[v.length];
            cy = new float[v.length];
        }

        final Path p = new Path();
        p.moveTo(0, mHeight);

        for (int i = 0; i < v.length; i++) {
            float x = mWidth * (float)((v[i].valueX - series.getMinX()) / lx);
            float y = mHeight - mHeight * (float)((v[i].valueY - series.getMinY()) / ly);

            p.lineTo(x, y);

            if (series.getStyle().isDrawPoints) {
                cx[i] = x;
                cy[i] = y;
            }
        }

        p.lineTo(mWidth, mHeight);
        p.close();
        canvas.drawPath(p, gp);

        if (series.getStyle().isDrawPoints) {
            for (int i = 1; i < v.length - 1; i++) {
                canvas.drawCircle(cx[i], cy[i], series.getStyle().pointsThickness, pp);
            }
        }

        if (series.getStyle().isDrawAxis) {
            final Paint ap = getAxisPaint(index);
            float axisY = mHeight - mHeight * (float)((0 - series.getMinY()) / ly);
            canvas.drawLine(0, axisY, mWidth, axisY, ap);
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
