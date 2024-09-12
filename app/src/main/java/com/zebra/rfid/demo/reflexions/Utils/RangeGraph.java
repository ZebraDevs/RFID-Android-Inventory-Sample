package com.zebra.rfid.demo.reflexions.Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.zebra.rfid.demo.reflexions.R;

/**
 * Draw a Range Graph to show the current value as a percent in a vertical bar
 */
public class RangeGraph extends View {
    private static final String TAG = "RangeGraph";
    private static final int PADDING_5 = 5;
    private static final int WIDTH_OF_LINE = 25;
    private static final int FONT_COLOR = Color.BLACK;
    private static final int LINE_COLOR = Color.parseColor("#008080");
    // Graphics
    protected int colorNeutral = Color.parseColor("#ededed");

    //IN RANGE COLOR
    protected int colorInRange = Color.parseColor("#0178AE");
    // State
    private int mMin = 0, mMax = 100;
    private int mValue = 0;
    private int mFontSize = 10;
    private Paint mPaint;

    // Layout
    private int mWidth = 250;
    private int mHeight = 600;

    /**
     * Initialize the class
     *
     * @param context - Context to be used
     */
    public RangeGraph(Context context) {
        super(context);
        commonSetup();
    }

    /**
     * Initialize the class
     *
     * @param context - context to be used
     * @param attrs   - Display attributes to be used
     */
    public RangeGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Then allow overrides from XML
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RangeGraph,
                0, 0);
        try {
            mMin = a.getInteger(R.styleable.RangeGraph_minimum, 0);
            mMax = a.getInteger(R.styleable.RangeGraph_maximum, 100);
        } finally {
            a.recycle();
        }
        commonSetup();
    }

    private void commonSetup() {
        mPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(colorNeutral);
        // Scale the desired text size to match screen density
        mPaint.setTextSize(mFontSize * getResources().getDisplayMetrics().density);
        //mPaint.setStrokeWidth(2f);
        setPadding(PADDING_5, PADDING_5, PADDING_5, PADDING_5);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //setMeasuredDimension(mWidth, mHeight);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        mWidth = parentWidth / 2;
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        mHeight = parentHeight;
        this.setMeasuredDimension(parentWidth, parentHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String stringMax = Integer.toString(mMax);
        String stringMin = Integer.toString(mMin);

        // Draw the bar outline
        int top = getPaddingTop();
        int bot = mHeight - getPaddingBottom();
        int leftSide = (int) (mWidth * 0.75f);
        int rightSide = (int) (mWidth * 1.25f);

        //Preserve the old style
        Paint.Style oldStyle = mPaint.getStyle();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(colorNeutral);
        canvas.drawRect(leftSide, top, rightSide, bot, mPaint);

        //Reset to old style
        mPaint.setStyle(oldStyle);
        // Now draw the bar graph.
        mPaint.setColor(colorInRange);
        //Value expressed as percentage of height
        int barHeight = (int) ((((float) mValue / mMax)) * mHeight);
        Constants.logAsMessage(Constants.TYPE_DEBUG, TAG, String.format("drawRect(%d %d %d %d)", leftSide, mHeight - barHeight, rightSide, bot));
        canvas.drawRect(leftSide, bot - barHeight, rightSide, bot, mPaint);

        //Reset to old style
        mPaint.setStyle(oldStyle);

        // First put in two tick marks before changing the color
        mPaint.setColor(LINE_COLOR);
        //canvas.drawLine(rightSide, bot, rightSide + WIDTH_OF_LINE, bot, mPaint);
        //canvas.drawLine(rightSide, top, rightSide + WIDTH_OF_LINE, top, mPaint);

        Paint.FontMetrics fm = mPaint.getFontMetrics();
        final float fontHeight = fm.ascent + fm.descent;

        mPaint.setColor(FONT_COLOR);
        //Draw the level values
        //canvas.drawText(stringMax, rightSide + WIDTH_OF_LINE + PADDING_5, top - fontHeight, mPaint);
        //canvas.drawText(stringMin, rightSide + WIDTH_OF_LINE + PADDING_5, bot, mPaint);
        //Reset to old style
        mPaint.setStyle(oldStyle);

        // Percentage text
        mPaint.setTextSize(25);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setFakeBoldText(true);
        mPaint.setColor(FONT_COLOR);
        canvas.drawText(mValue+"%", leftSide+(rightSide-leftSide)/2, top + (bot-top)/2, mPaint);
    }

    // Simple accessors
    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        this.mValue = value;
    }
}