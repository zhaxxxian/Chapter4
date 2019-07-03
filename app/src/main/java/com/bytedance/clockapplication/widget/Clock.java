package com.bytedance.clockapplication.widget;

import android.app.Notification;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class Clock extends View {

    private final static String TAG = Clock.class.getSimpleName();

    private static final int FULL_ANGLE = 360;

    private static final int CUSTOM_ALPHA = 140;
    private static final int FULL_ALPHA = 255;

    private static final int DEFAULT_PRIMARY_COLOR = Color.WHITE;
    private static final int DEFAULT_SECONDARY_COLOR = Color.LTGRAY;

    private static final float DEFAULT_DEGREE_STROKE_WIDTH = 0.010f;

    public final static int AM = 0;

    private static final int RIGHT_ANGLE = 90;

    private int mWidth, mCenterX, mCenterY, mRadius;

    /**
     * properties
     */
    private int centerInnerColor;
    private int centerOuterColor;

    private int secondsNeedleColor;
    private int hoursNeedleColor;
    private int minutesNeedleColor;

    private int degreesColor;

    private int hoursValuesColor;

    private int numbersColor;

    private boolean mShowAnalog = true;

    public Clock(Context context) {
        super(context);
        init(context, null);
    }

    public Clock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Clock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    private void init(Context context, AttributeSet attrs) {

        this.centerInnerColor = Color.LTGRAY;
        this.centerOuterColor = DEFAULT_PRIMARY_COLOR;

        this.secondsNeedleColor = DEFAULT_SECONDARY_COLOR;
        this.hoursNeedleColor = DEFAULT_PRIMARY_COLOR;
        this.minutesNeedleColor = DEFAULT_PRIMARY_COLOR;

        this.degreesColor = DEFAULT_PRIMARY_COLOR;

        this.hoursValuesColor = DEFAULT_PRIMARY_COLOR;

        numbersColor = Color.WHITE;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getHeight() > getWidth() ? getWidth() : getHeight();

        int halfWidth = mWidth / 2;
        mCenterX = halfWidth;
        mCenterY = halfWidth;
        mRadius = halfWidth;
        if (mShowAnalog) {
            drawDegrees(canvas);
            drawHoursValues(canvas);
            drawNeedles(canvas);
            postInvalidateDelayed(1000);
            drawCenter(canvas);
        } else {
            drawNumbers(canvas);
            postInvalidateDelayed(1000);
        }

    }
    private void drawDegrees(Canvas canvas) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paint.setColor(degreesColor);

        int rPadded = mCenterX - (int) (mWidth * 0.01f);
        int rEnd = mCenterX - (int) (mWidth * 0.05f);

        for (int i = 0; i < FULL_ANGLE; i += 6 /* Step */) {

            if ((i % RIGHT_ANGLE) != 0 && (i % 15) != 0)
                paint.setAlpha(CUSTOM_ALPHA);
            else {
                paint.setAlpha(FULL_ALPHA);
            }

            int startX = (int) (mCenterX + rPadded * Math.cos(Math.toRadians(i)));
            int startY = (int) (mCenterX - rPadded * Math.sin(Math.toRadians(i)));

            int stopX = (int) (mCenterX + rEnd * Math.cos(Math.toRadians(i)));
            int stopY = (int) (mCenterX - rEnd * Math.sin(Math.toRadians(i)));

            canvas.drawLine(startX, startY, stopX, stopY, paint);

        }
    }

    /**
     * @param canvas
     */
    private void drawNumbers(Canvas canvas) {

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(mWidth * 0.2f);
        textPaint.setColor(numbersColor);
        textPaint.setColor(numbersColor);
        textPaint.setAntiAlias(true);

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int amPm = calendar.get(Calendar.AM_PM);

        String time = String.format("%s:%s:%s%s",
                String.format(Locale.getDefault(), "%02d", hour),
                String.format(Locale.getDefault(), "%02d", minute),
                String.format(Locale.getDefault(), "%02d", second),
                amPm == AM ? "AM" : "PM");

        SpannableStringBuilder spannableString = new SpannableStringBuilder(time);
        spannableString.setSpan(new RelativeSizeSpan(0.3f), spannableString.toString().length() - 2, spannableString.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // se superscript percent

        StaticLayout layout = new StaticLayout(spannableString, textPaint, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1, 1, true);
        canvas.translate(mCenterX - layout.getWidth() / 2f, mCenterY - layout.getHeight() / 2f);
        layout.draw(canvas);
    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */
    private void drawHoursValues(Canvas canvas) {
        // Default Color:
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(mWidth*0.04f);
        textPaint.setColor(numbersColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        int distance = mCenterX - (int) (mWidth * 0.1f);
        // - hoursValuesColor
        for (int i = 0; i < 12; i++) {
            int a = (int ) (distance * Math.sin(i * 30 * Math.PI / 180)+mCenterX);
            int b = (int) (mCenterY - distance * Math.cos(i * 30 * Math.PI / 180));
            if (i == 0) {
                canvas.drawText("12", a, b + mWidth*0.04f / 3,textPaint);
            } else {
                canvas.drawText(String.valueOf(i), a, b + mWidth*0.04f / 3, textPaint);
            }
        }



    }

    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition.
     *
     * @param canvas
     */
    private void drawNeedles(final Canvas canvas) {
        // Default Color:
        // - secondsNeedleColor
        Paint paintHour = new Paint();
        paintHour.setColor(Color.WHITE);
        paintHour.setAntiAlias(true);
        paintHour.setStrokeWidth(12);
        paintHour.setStyle(Paint.Style.FILL);
        // - hoursNeedleColor
        Paint paintMinute = new Paint();
        paintMinute.setColor(Color.WHITE);
        paintMinute.setAntiAlias(true);
        paintMinute.setStrokeWidth(8);
        paintMinute.setStyle(Paint.Style.FILL);
        // - minutesNeedleColor
        Paint paintSecond = new Paint();
        paintSecond.setColor(Color.LTGRAY);
        paintSecond.setAntiAlias(true);
        paintSecond.setStrokeWidth(4);
        paintSecond.setStyle(Paint.Style.FILL);

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        int hourAngle = hour * 30 + minute / 2;
        int minuteAngle = minute * 6 + second / 2;
        int secondAngle = second * 6;

        canvas.rotate(hourAngle, mCenterX, mCenterY);
        canvas.drawLine(mCenterX, mCenterY, mCenterX, mCenterY - mRadius + 300, paintHour);
        canvas.save();
        canvas.restore();
        canvas.rotate(-hourAngle, mCenterX, mCenterY);

        canvas.rotate(minuteAngle, mCenterX, mCenterY);
        canvas.drawLine(mCenterX, mCenterY, mCenterX, mCenterY - mRadius + 200, paintMinute);
        canvas.save();
        canvas.restore();
        canvas.rotate(-minuteAngle, mCenterX, mCenterY);

        canvas.rotate(secondAngle, mCenterX, mCenterY);
        canvas.drawLine(mCenterX, mCenterY, mCenterX, mCenterY - mRadius + 150, paintSecond);
        canvas.save();
        canvas.restore();
        canvas.rotate(-secondAngle, mCenterX, mCenterY);


    }

    /**
     * Draw Center Dot
     *
     * @param canvas
     */
    private void drawCenter(Canvas canvas) {
        // Default Color:
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(mCenterX,mCenterY,10,paint);
        // - centerInnerColor
        // - centerOuterColor

    }

    public void setShowAnalog(boolean showAnalog) {
        mShowAnalog = showAnalog;
        invalidate();
    }

    public boolean isShowAnalog() {
        return mShowAnalog;
    }

}