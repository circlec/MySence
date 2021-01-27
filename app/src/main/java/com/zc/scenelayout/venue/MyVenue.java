package com.zc.scenelayout.venue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


import java.util.ArrayList;

import static com.zc.scenelayout.venue.VenueModelInfo.MODEL_TYPE_BOX;
import static com.zc.scenelayout.venue.VenueModelInfo.MODEL_TYPE_OVAL;
import static com.zc.scenelayout.venue.VenueModelInfo.MODEL_TYPE_RECTANGLE;

/**
 * @作者 zhouchao
 * @日期 2021/1/20
 * @描述 用于展示场景的view, 可以在场景中布置模型，并提供模型的选择、拖动等事件
 */
public class MyVenue extends View implements ActionListener {
    public static final String TAG = MyVenue.class.getSimpleName();
    private Engine engine;

    private int venueHeight = 0;//场馆高度
    private int venueWidth = 0;//场馆宽度
    //画笔
    private Paint mRectPaint;
    //场景的边界左上右下
    public static int left = 0;
    public static int top = 0;
    public static int right = 0;
    public static int bottom = 0;

    public MyVenue(Context context) {
        super(context);
    }

    public MyVenue(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVenue(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyVenue(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initPaint();
        initPosition();

    }

    /**
     * 初始化边界
     */
    private void initPosition() {
        venueHeight = getHeight() * 2 / 3;
        venueWidth = getWidth();
        left = getLeft();
        top = getTop();
        right = getRight();
        bottom = getBottom();
        VenueInfo venueInfo = new VenueInfo(venueWidth, venueHeight, left, top, right, bottom, getStatusBarHeight(getContext()));
        engine = new Engine();
        engine.addOnActionListener(this);
        engine.init(venueInfo);
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        if (mRectPaint == null)
            mRectPaint = new Paint();
        mRectPaint.setColor(Color.RED);
        mRectPaint.setStrokeWidth(3);
        mRectPaint.setStyle(Paint.Style.FILL);
        mRectPaint.setPathEffect(new DashPathEffect(new float[]{10, 0}, 0));
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawVenue(canvas);
        drawGrid(canvas);
        if (engine.getModelInfos().size() == 0) return;
        drawModel(canvas, engine.getModelInfos());
        initPaint();
    }

    private void drawGrid(Canvas canvas) {
        mRectPaint.setColor(Color.WHITE);
        //画竖线
        for (int i = left; i < right; i += 80) {
            canvas.drawLine(i, top, i, bottom, mRectPaint);
        }
        //画横线
        for (int j = top; j < bottom; j += 80) {
            canvas.drawLine(left, j, right, j, mRectPaint);
        }
    }

    private void drawVenue(Canvas canvas) {
        mRectPaint.setColor(Color.BLUE);
        Rect rect = new Rect(left, top, right, venueHeight);
        canvas.drawRect(rect, mRectPaint);
    }

    /**
     * 绘制模型
     *
     * @param canvas 画布
     */
    private void drawModel(Canvas canvas, ArrayList<VenueModelInfo> modelInfos) {
        for (VenueModelInfo modelInfo : modelInfos) {
            if (modelInfo.isSelect()) {
                mRectPaint.setColor(Color.GREEN);
            } else {
                mRectPaint.setColor(Color.RED);
            }
            mRectPaint.setStrokeWidth(3);
            if (modelInfo.getModelType() == MODEL_TYPE_OVAL) {
                drawModelOval(canvas, modelInfo);
            } else if (modelInfo.getModelType() == MODEL_TYPE_RECTANGLE) {
                drawModelRectangle(canvas, modelInfo);
            } else if (modelInfo.getModelType() == MODEL_TYPE_BOX) {
                drawModelBox(canvas, modelInfo);
            }
        }
    }

    /**
     * 绘制box类型模型 （框选虚线）
     *
     * @param canvas    画布
     * @param modelInfo box类型模型数据
     */
    private void drawModelBox(Canvas canvas, VenueModelInfo modelInfo) {
        mRectPaint.setStrokeWidth(12);
        mRectPaint.setColor(Color.GREEN);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setPathEffect(new DashPathEffect(new float[]{20, 20}, 0));
        Rect rect = new Rect(modelInfo.getLeft(), modelInfo.getTop(), modelInfo.getRight(), modelInfo.getBottom());
        canvas.drawRect(rect, mRectPaint);
    }

    /**
     * 绘制rectangle类型模型
     *
     * @param canvas    画布
     * @param modelInfo rectangle模型数据
     */
    private void drawModelRectangle(Canvas canvas, VenueModelInfo modelInfo) {
        mRectPaint.setPathEffect(new DashPathEffect(new float[]{10, 0}, 0));
        mRectPaint.setStyle(Paint.Style.FILL);
        Rect rect = new Rect(modelInfo.getLeft(), modelInfo.getTop(), modelInfo.getRight(), modelInfo.getBottom());
        int centerX = (modelInfo.getLeft() + modelInfo.getRight()) / 2;
        int centerY = (modelInfo.getTop() + modelInfo.getBottom()) / 2;
        canvas.rotate(modelInfo.getRotationAngle(), centerX, centerY);
        canvas.drawRect(rect, mRectPaint);
        canvas.rotate(-modelInfo.getRotationAngle(), centerX, centerY);
    }

    /**
     * 绘制oval类型模型
     *
     * @param canvas    画布
     * @param modelInfo oval模型数据
     */
    private void drawModelOval(Canvas canvas, VenueModelInfo modelInfo) {
        int centerX = modelInfo.getLeft() + (modelInfo.getRight() - modelInfo.getLeft()) / 2;
        int centerY = modelInfo.getTop() + (modelInfo.getBottom() - modelInfo.getTop()) / 2;
        int radius = (modelInfo.getRight() - modelInfo.getLeft()) / 2;
        mRectPaint.setPathEffect(new DashPathEffect(new float[]{10, 0}, 0));
        mRectPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, radius, mRectPaint);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        engine.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void createModel() {
        invalidate();
    }

    @Override
    public void moveModel() {
        invalidate();
    }

    @Override
    public void selectAreaModel() {
        invalidate();
    }

    @Override
    public void deleteModel() {
        invalidate();
    }


    @Override
    public void selectSingleModel() {
        invalidate();
        Toast.makeText(getContext(), "选择单个模型", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void moveToolBoxModel() {
        invalidate();
    }

    @Override
    public void moveOutBoundary() {
//        invalidate();
    }

    @Override
    public void createFail() {
        invalidate();
        Toast.makeText(getContext(), "工具栏模型未移动到场馆内", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void moveFailDouble() {
        invalidate();
        Toast.makeText(getContext(), "有重叠区域", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void rotate() {
        invalidate();
    }

    @Override
    public void clearSelectArea() {
        invalidate();
        int selectCount = 0;
        for (VenueModelInfo venueModelInfo : engine.getModelInfos()) {
            if (venueModelInfo.isSelect()) {
                selectCount++;
            }
        }
        Toast.makeText(getContext(), "框选完成，清除选择框，选中" + selectCount + "个模型", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void align() {
        invalidate();
    }

    public void deleteSelectModel() {
        engine.deleteSelectModel();
    }

    public void setSelectRotate(int rotationAngle) {
        engine.setSelectRotate(rotationAngle);
    }
}
