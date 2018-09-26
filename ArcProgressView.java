package com.example.swyww.testproject;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by swyww on 2018/9/21
 */
public class ArcProgressView extends View {

    /**
     * 圆心坐标点
     */
    private float centerX;
    private float centerY;
    /**
     * 半径
     */
    private float radius = 300;

    /**
     * 圆弧背景色
     */
    private int bgColor = Color.parseColor("#ffffff");
    /**
     * 圆弧边框色
     */
    private int bgStrokeColor = Color.parseColor("#B3999DA1");
    private String headText;
    private String bodyText;
    /**
     * px
     */
    private float headTextSize = 33;
    private float bodyTextSize = 90;
    /**
     * 文本直接的间距
     */
    private float headAndBodySpace = 27;
    private int headTextColor = Color.parseColor("#717273");
    private int bodyTextColor = Color.parseColor("#303030");
    /*
     * 圆弧背景宽度+边线宽度 比如bgWidth = 36 边框线 =2 --> bgStrokeWidth = 38
     */
    private int bgStrokeWidth = 38;
    /**
     * 圆弧背景宽度
     */
    private int bgWidth = 36;
    /**
     * 颜色圆弧宽度
     */
    private int arcWidth = 20;

    /**
     * 开始圆弧角度
     */
    private float startAngle = 135;
    /**
     * 最大圆弧角度  不是结束角度
     */
    private float sweepRange = 270;
    /**
     * 空白间距角度
     */
    private float spaceAngle = 4;

    private float spacePercent;

    /**
     * 各种Paint
     */
    private Paint bgPaint;
    private Paint bgStrokePaint;
    private Paint spacePaint;
    private Paint headPaint;
    private Paint bodyPaint;
    /**
     * 圆弧Paint
     */
    private List<Paint> arcPaints = new ArrayList<>();
    private List<Line> mLines = new ArrayList<>();

    private float mProgress = 1f;

    private Matrix mMatrix;

    public ArcProgressView(Context context) {
        super(context);
        init();
    }

    public ArcProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArcProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ArcProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        spacePercent = spaceAngle / sweepRange;
        mMatrix = new Matrix();
        initPaint();
    }

    private void initPaint() {
        if (bgPaint == null) {
            bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(bgWidth);
        bgPaint.setColor(bgColor);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);
        bgPaint.setStrokeJoin(Paint.Join.ROUND);
        if (bgStrokePaint == null) {
            bgStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        bgStrokePaint.setStyle(Paint.Style.STROKE);
        bgStrokePaint.setStrokeWidth(bgStrokeWidth);
        bgStrokePaint.setColor(bgStrokeColor);
        bgStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        bgStrokePaint.setStrokeJoin(Paint.Join.ROUND);
        if (spacePaint == null) {
            spacePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        spacePaint.setStyle(Paint.Style.STROKE);
        spacePaint.setStrokeWidth(arcWidth);
        spacePaint.setColor(bgColor);
        spacePaint.setStrokeCap(Paint.Cap.BUTT);
        spacePaint.setStrokeJoin(Paint.Join.ROUND);
        if (headPaint == null) {
            headPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        headPaint.setTextSize(headTextSize);
        headPaint.setColor(headTextColor);
        if (bodyPaint == null) {
            bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        bodyPaint.setTextSize(bodyTextSize);
        bodyPaint.setColor(bodyTextColor);
    }

    /**
     * 确保所以percen加起来不超过1 超过1的将被抛弃
     *
     * @param lines
     */
    public void setLine(List<Line> lines) {
        if (lines == null) {
            return;
        }
        //安全起见 先剔除空数据
        for (Iterator<Line> itr = lines.iterator(); itr.hasNext(); ) {
            if (itr.next() == null) {
                itr.remove();
            }
        }
        if (lines.size() <= 0) {
            return;
        }

        boolean needSpace = lines.size() > 1;

        lines.get(0).setType(LineType.ROUND_BUTT);

        if (needSpace) {
            //特殊增加两个line
            lines.get(lines.size() - 1).setType(LineType.BUTT_ROUND);
            lines.add(1, new Line(lines.get(0).color, lines.get(0).colors, LineType.PREVIOUS_BUTT));
            lines.add(lines.size() - 1, new Line(lines.get(lines.size() - 1).color, lines.get(lines.size() - 1).colors, LineType.NEX_BUTT));
        }


        float percentTotal = 0.0f;
        float firstPercent = lines.get(0).percent;
        float lastPercent = lines.get(lines.size() - 1).percent;

        arcPaints.clear();

        for (int i = 0; i < lines.size(); i++) {

            if (percentTotal > mProgress) {
                break;
            }

            Line line = lines.get(i);
            float start;
            float sweep;

            if (percentTotal + line.percent >= mProgress) {
                line.percent = mProgress - percentTotal;
            }

            if (needSpace) {
                if (line.getType() == LineType.ROUND_BUTT) {
                    start = startAngle + sweepRange * percentTotal;
                    sweep = sweepRange * line.percent / 2;
                } else if (line.getType() == LineType.PREVIOUS_BUTT) {
                    start = startAngle + sweepRange * (percentTotal - firstPercent / 2);
                    sweep = sweepRange * firstPercent / 2;
                } else if (line.getType() == LineType.NEX_BUTT) {
                    start = startAngle + sweepRange * (percentTotal + spacePercent / 2);
                    sweep = sweepRange * lastPercent / 2;
                } else if (line.getType() == LineType.BUTT_ROUND) {
                    start = startAngle + sweepRange * (percentTotal + line.percent / 2);
                    sweep = sweepRange * line.percent / 2;
                } else {
                    start = startAngle + sweepRange * (percentTotal + spacePercent / 2);
                    sweep = sweepRange * (line.percent - spacePercent / 2);
                }
            } else {
                start = startAngle;
                sweep = sweepRange * line.percent;
            }

            percentTotal += line.percent;

            line.setStart(start);
            line.setSweep(sweep);


            Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            arcPaint.setStyle(Paint.Style.STROKE);
            arcPaint.setStrokeWidth(arcWidth);
            arcPaint.setColor(line.color);
            if (line.getType() == LineType.ROUND_BUTT || line.getType() == LineType.BUTT_ROUND) {
                arcPaint.setStrokeCap(Paint.Cap.ROUND);
            } else {
                arcPaint.setStrokeCap(Paint.Cap.BUTT);
            }
            if (line.colors != null) {
                mMatrix.reset();
                //将扫描起始的地方旋转至当前线段开始处
                mMatrix.setRotate(startAngle, centerX, centerY);
                Shader shader = new SweepGradient(centerX, centerY, line.colors, null);
                shader.setLocalMatrix(mMatrix);
                arcPaint.setShader(shader);
            }
            arcPaint.setStrokeJoin(Paint.Join.ROUND);
            arcPaints.add(arcPaint);
        }

        mLines.clear();
        mLines.addAll(lines);
    }

    public void animator(int durationMillis) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(durationMillis);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgress = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        animator.start();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int size = (int) (radius * 2);


        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(size, size);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(size, heightSize);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, size);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        radius = Math.min(Math.min(getWidth(), getHeight()) / 2 - bgStrokeWidth, radius);

        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        @SuppressLint("DrawAllocation") RectF rectF = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        drawContent(canvas, rectF);
        drawText(canvas);
    }

    /**
     * 顺序不能打乱
     * @param canvas
     * @param rectF
     */
    private void drawContent(Canvas canvas, RectF rectF) {
        drawBgArc(canvas, rectF);
        drawArc(canvas, rectF);
    }

    private void drawBgArc(Canvas canvas, RectF rectF) {
        canvas.drawArc(rectF, startAngle, sweepRange, false, bgStrokePaint);
        canvas.drawArc(rectF, startAngle, sweepRange, false, bgPaint);
    }

    private void drawArc(Canvas canvas, RectF rectF) {
        if (mLines.size() <= 0) {
            return;
        }
        float hasDraw = 0f;
        for (int i = 0; i < mLines.size(); i++) {
            Line line = mLines.get(i);
            float range = line.sweep;
            if (i >= arcPaints.size()) {
                continue;
            }
            if (hasDraw >= sweepRange * mProgress) {
                break;
            }

            if (hasDraw + range >= sweepRange * mProgress) {
                range = sweepRange * mProgress - hasDraw;
            }
            canvas.drawArc(rectF, line.start, range, false, arcPaints.get(i));
            hasDraw += range;
        }
    }

    private void drawText(Canvas canvas) {
        boolean noHeadText = TextUtils.isEmpty(headText);
        boolean noBodyText = TextUtils.isEmpty(bodyText);

        if (noBodyText && noHeadText) {
            return;
        }

        String currentBody = "";
        //这里假设需求场景是数字文本的变化
        if (!noBodyText) {
            float currentValue = Float.parseFloat(bodyText) * mProgress;
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            currentBody = decimalFormat.format(currentValue);
        }

        float x = getWidth() / 2;
        float y = getHeight() / 2;

        float headTextWidth = 0;
        float headTextHeight = 0;
        if (!noHeadText) {
            Rect rect = new Rect();
            headPaint.getTextBounds(headText, 0, headText.length(), rect);
            headTextWidth = rect.width();
            headTextHeight = rect.height();
        }

        float bodyTextWidth = 0;
        float bodyTextHeight = 0;
        if (!noBodyText) {
            Rect rect = new Rect();
            bodyPaint.getTextBounds(currentBody, 0, currentBody.length(), rect);
            bodyTextWidth = rect.width();
            bodyTextHeight = rect.height();
        }

        if (noBodyText) {
            canvas.drawText(headText, x - headTextWidth / 2, y + headTextHeight / 2, headPaint);
        } else if (noHeadText) {
            canvas.drawText(currentBody, x - bodyTextWidth / 2, y + bodyTextHeight / 2, bodyPaint);
        } else {
            //设计图上好像body的在中间  并不是居中分开的？？
            canvas.drawText(headText, x - headTextWidth / 2, y - headAndBodySpace, headPaint);
            canvas.drawText(currentBody, x - bodyTextWidth / 2, y + bodyTextHeight, bodyPaint);
        }
    }


    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public void setBgStrokeColor(int bgStrokeColor) {
        this.bgStrokeColor = bgStrokeColor;
    }

    public void setHeadText(String headText) {
        this.headText = headText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }


    public void setHeadTextSize(float headTextSize) {
        this.headTextSize = headTextSize;
    }

    public void setBodyTextSize(float bodyTextSize) {
        this.bodyTextSize = bodyTextSize;
    }

    public void setHeadAndBodySpace(float headAndBodySpace) {
        this.headAndBodySpace = headAndBodySpace;
    }

    public void setHeadTextColor(int headTextColor) {
        this.headTextColor = headTextColor;
    }

    public void setBodyTextColor(int bodyTextColor) {
        this.bodyTextColor = bodyTextColor;
    }

    public void setBgWidth(int bgWidth) {
        this.bgWidth = bgWidth;
    }

    public void setBgStrokeWidth(int bgStrokeWidth) {
        this.bgStrokeWidth = bgStrokeWidth;
    }

    public void setArcWidth(int arcWidth) {
        this.arcWidth = arcWidth;
    }


    private enum LineType {
        /**
         * 开头线段 开头辅助线段 中间下端 结尾辅助线段  结尾线段
         */
        ROUND_BUTT,
        PREVIOUS_BUTT,
        BUTT,
        NEX_BUTT,
        BUTT_ROUND
    }


    public static class Line {

        /**
         * 线段颜色
         */
        private final int color;
        /**
         * 该段所占角度比例
         */
        private float percent;
        /**
         * 线段类型
         */
        private LineType type = LineType.BUTT;
        /**
         * 渐变颜色
         */
        private int[] colors;
        private float start;
        private float sweep;

        public Line(int color, float percent) {
            this.color = color;
            this.percent = percent;
        }

        private Line(int color, int[] colors, LineType type) {
            this.color = color;
            this.type = type;
            this.percent = 0;
            this.colors = colors;
        }

        public float getStart() {
            return start;
        }

        public void setStart(float start) {
            this.start = start;
        }

        public float getSweep() {
            return sweep;
        }

        public void setSweep(float sweep) {
            this.sweep = sweep;
        }

        public LineType getType() {
            return type;
        }

        public void setColors(int[] colors) {
            this.colors = colors;
        }

        public void setType(LineType type) {
            this.type = type;
        }
    }

}
