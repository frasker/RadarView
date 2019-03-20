
package com.frasker.radarview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.List;

public class RadarView extends View {

    private final static String TAG = RadarView.class.getSimpleName();
    private static final int MAX_OFFSET_ANIMATION_DURATION = 600; // ms
    private static final float DECELERATE_INTERPOLATION_FACTOR = 1f;
    private List<RadarData> dataList;
    private int axisTickCount = 5;
    private int count = 5;//雷达网圈数
    private float angle;//多边形弧度
    private float radius;
    private float defaultRadius;
    private Paint radarLinePaint;//雷达区画笔
    private Paint dataPaint;//数据区画笔
    private Paint textPaint;//文本画笔
    private Paint circlePaint;//数据原点画笔
    private Paint radarBgPaint;//数据原点画笔

    //依次存下每个圈上，每个标签节点的X,Y坐标
    private float[][] mArrayDotX = null;
    private float[][] mArrayDotY = null;

    private int radarBgColor;//雷达区颜色
    private int radarLineColor;//雷达区颜色
    private int valueColor;//数据区颜色
    private int valueLineColor;//数据区线框颜色
    private int textColor;//文本颜色

    private float radarLineWidth = 0.5f;//雷达网线宽度dp
    private float valueLineWidth = 1f;//数据区边宽度dp
    private float valuePointRadius = 3f;//数据区圆点半径dp
    private float textSize;//字体大小sp

    private int mWidth, mHeight;
    private float[] dataX;
    private float[] dataY;
    private boolean needReCalculate = true;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private ValueAnimator mAnimator;
    private boolean showAnimation = false;
    private float progress = 1f;

    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RadarView);
        defaultRadius = typedArray.getDimension(R.styleable.RadarView_r_radius, -1);
        if (defaultRadius > 0) {
            radius = defaultRadius;
        }
        axisTickCount = typedArray.getInt(R.styleable.RadarView_r_axisTickCount, 5);
        radarBgColor = typedArray.getColor(R.styleable.RadarView_r_radarBgColor, 0xFFFCFCFC);
        radarLineColor = typedArray.getColor(R.styleable.RadarView_r_radarLineColor, 0xFFE5E5E5);
        valueColor = typedArray.getColor(R.styleable.RadarView_r_valueColor, 0x1E36A2FF);
        valueLineColor = typedArray.getColor(R.styleable.RadarView_r_valueLineColor, 0xFF5183FF);
        textColor = typedArray.getColor(R.styleable.RadarView_r_textColor, 0xFF666666);
        textSize = typedArray.getDimensionPixelSize(R.styleable.RadarView_r_textSize, dip2px(context, 12));
        showAnimation = typedArray.getBoolean(R.styleable.RadarView_r_showAnimation, false);
        if (showAnimation) {
            progress = 0f;
        }
        setup();
    }

    private void setup() {
        radarLinePaint = new Paint();
        radarLinePaint.setAntiAlias(true);
        radarLinePaint.setColor(radarLineColor);
        radarLinePaint.setStyle(Paint.Style.STROKE);

        dataPaint = new Paint();
        dataPaint.setAntiAlias(true);
        dataPaint.setColor(valueColor);
        dataPaint.setStyle(Paint.Style.FILL);

        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(valueColor);
        circlePaint.setStyle(Paint.Style.FILL);

        radarBgPaint = new Paint();
        radarBgPaint.setAntiAlias(true);
        radarBgPaint.setColor(radarBgColor);
        radarBgPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(textColor);
    }

    public void playAnimation() {
        if (mAnimator == null) {
            mAnimator = new ValueAnimator();
            mAnimator.setInterpolator(mDecelerateInterpolator);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    progress = (float) animation.getAnimatedValue();
                    ViewCompat.postInvalidateOnAnimation(RadarView.this);
                }
            });
        } else {
            mAnimator.cancel();
        }
        mAnimator.setDuration(MAX_OFFSET_ANIMATION_DURATION);
        mAnimator.setFloatValues(0f, 1f);
        mAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (defaultRadius < 0) {
            radius = Math.min(h, w) / 2 * 0.6f;
        }
        mWidth = w;
        mHeight = h;
        needReCalculate = true;
        postInvalidate();
        super.onSizeChanged(w, h, oldw, oldh);
    }


    private void reset() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        progress = showAnimation ? 0f : 1f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(mWidth / 2, mHeight / 2);

        if (isDataListValid()) {
            drawSpiderweb(canvas);
            drawText(canvas, mArrayDotX[axisTickCount], mArrayDotY[axisTickCount], count);
            drawRegion(canvas);
        }
    }


    Path webPath = new Path();
    Path linePath = new Path();

    /**
     * 绘制蜘蛛网
     *
     * @param canvas
     */
    private void drawSpiderweb(Canvas canvas) {
        calcAllPoints();
        drawWebRegion(canvas, axisTickCount, count, mArrayDotX, mArrayDotY);
        radarLinePaint.setStrokeWidth(dip2px(getContext(), radarLineWidth));
        for (int i = 0; i < axisTickCount + 1; i++) {
            webPath.reset();
            for (int j = 0; j < count; j++) {
                float x = mArrayDotX[i][j];
                float y = mArrayDotY[i][j];
                if (j == 0) {
                    webPath.moveTo(x, y);
                } else {
                    webPath.lineTo(x, y);
                }
                if (i == axisTickCount) {//当绘制最后一环时绘制连接线
                    linePath.reset();
                    linePath.moveTo(0, 0);
                    linePath.lineTo(x, y);
                    canvas.drawPath(linePath, radarLinePaint);
                }
            }
            webPath.close();
            canvas.drawPath(webPath, radarLinePaint);
        }
    }

    private Path inPath = new Path();
    private Path outPath = new Path();

    protected void drawWebRegion(Canvas canvas, int axisTickCount, int count, float[][] arrayDotX, float[][] arrayDotY) {
        for (int i = 0; i < axisTickCount; i++) {
            outPath.reset();
            inPath.reset();
            for (int j = 0; j < count; j++) {
                if (j == 0) {
                    inPath.moveTo(mArrayDotX[i][j], mArrayDotY[i][j]);
                    outPath.moveTo(mArrayDotX[i + 1][j], mArrayDotY[i + 1][j]);
                } else {
                    inPath.lineTo(mArrayDotX[i][j], mArrayDotY[i][j]);
                    outPath.lineTo(mArrayDotX[i + 1][j], mArrayDotY[i + 1][j]);
                }

            }
            inPath.close();
            outPath.close();
            outPath.setFillType(Path.FillType.EVEN_ODD);
            outPath.addPath(inPath);
            if (i % 2 != 0) {
                canvas.drawPath(outPath, radarBgPaint);
            }
        }
    }

    protected void drawText(Canvas canvas, float[] dotX, float[] dotY, int count) {
        textPaint.setTextSize(textSize);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float fontHeight = fontMetrics.descent - fontMetrics.ascent;
        for (int i = 0; i < count; i++) {
            float x = (float) ((radius + fontHeight * 2) * Math.sin(angle / 2 + angle * i));
            float y = (float) ((radius + fontHeight * 2) * Math.cos(angle / 2 + angle * i));
            String title = dataList.get(i).getTitle();
            float dis = textPaint.measureText(title);
            canvas.drawText(title, x - dis / 2, y, textPaint);
        }
    }


    private Path path = new Path();

    /**
     * 绘制区域
     *
     * @param canvas
     */
    private void drawRegion(Canvas canvas) {
        dataPaint.setStrokeWidth(dip2px(getContext(), valueLineWidth));
        path.reset();
        for (int i = 0; i < count; i++) {
            double percent = dataList.get(i).getPercent();
            float x = (float) (radius * Math.sin(angle / 2 + angle * i) * percent) * progress;
            float y = (float) (radius * Math.cos(angle / 2 + angle * i) * percent) * progress;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
            dataX[i] = x;
            dataY[i] = y;
        }
        path.close();
        // 绘制数据区域背景
        dataPaint.setColor(valueColor);
        dataPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, dataPaint);
        // 绘制数据边框
        dataPaint.setColor(valueLineColor);
        dataPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, dataPaint);
        // 绘制数据小圆点
        for (int i = 0; i < count; i++) {
            //绘制小圆点
            drawDataCircle(canvas, dataX[i], dataY[i]);
        }
    }


    private boolean isDataListValid() {
        return dataList != null && dataList.size() >= 3;
    }


    public void setDataList(List<RadarData> dataList) {
        if (dataList == null || dataList.size() < 3) {
            throw new RuntimeException("The number of data can not be less than 3");
        } else {
            this.dataList = dataList;
            count = dataList.size();
            angle = (float) (Math.PI * 2 / count);
            dataX = new float[count];
            dataY = new float[count];
            invalidate();
        }
    }

    private void calcAllPoints() {
        if (needReCalculate) {
            mArrayDotX = null;
            mArrayDotY = null;
            mArrayDotX = new float[axisTickCount + 1][count];
            mArrayDotY = new float[axisTickCount + 1][count];
            float r = radius / axisTickCount;//蜘蛛丝之间的间距
            for (int i = 0; i < axisTickCount + 1; i++) {
                float curR = r * i;//当前半径
                for (int j = 0; j < count; j++) {
                    float x = (float) (curR * Math.sin(angle / 2 + angle * j));
                    float y = (float) (curR * Math.cos(angle / 2 + angle * j));
                    mArrayDotX[i][j] = x;
                    mArrayDotY[i][j] = y;
                }
            }
            needReCalculate = false;
        }
    }

    protected void drawDataCircle(Canvas canvas, float x, float y) {
        if (x != 0 && y != 0) {
            circlePaint.setStyle(Paint.Style.FILL);
            circlePaint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, dip2px(getContext(), valuePointRadius), circlePaint);
            circlePaint.setColor(valueLineColor);
            circlePaint.setStrokeWidth(dip2px(getContext(), valueLineWidth));
            circlePaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(x, y, valuePointRadius, circlePaint);
        }
    }


    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
    }
}