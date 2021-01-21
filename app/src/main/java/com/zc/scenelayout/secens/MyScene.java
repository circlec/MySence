package com.zc.scenelayout.secens;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.zc.scenelayout.utils.RxBus;


import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

/**
 * @作者 zhouchao
 * @日期 2021/1/20
 * @描述
 */
public class MyScene extends View {

    public static final String TAG = MyScene.class.getSimpleName();


    private Paint mRectPaint;

    public static int left = 0;
    public static int top = 0;
    public static int right = 0;
    public static int bottom = 0;

    private int clickLastX;
    private int clickLastY;

    boolean isPositionInModel = false;
    private int clickModelPosition;
    private ModelInfo lastModelInfo = new ModelInfo();

    private boolean isMove = false;


    //选中模型以外的其他模型 用于判断拖动时是否选中模型是否与其他模型重叠
    public static ArrayList<ModelInfo> otherModelInfos = new ArrayList<>();


    public MyScene(Context context) {
        super(context);
        init();
    }


    public MyScene(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyScene(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyScene(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        MyModel.modelInfos.clear();
        Disposable disposable = RxBus.getInstance().register(ModelInfo.class)
                .subscribe(this::addMyModel);
    }

    public void addModel(ModelInfo modelInfo) {
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initPaint();

    }

    private void initPaint() {
        left = getLeft();
        top = getTop();
        right = getRight();
        bottom = getBottom();
        mRectPaint = new Paint();
        mRectPaint.setColor(Color.RED);
        mRectPaint.setStrokeWidth(3);
        mRectPaint.setStyle(Paint.Style.FILL);

    }

    private void addMyModel(ModelInfo modelInfo) {
        invalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLUE);
        if (MyModel.modelInfos.size() == 0) return;
        for (ModelInfo modelInfo : MyModel.modelInfos) {
            int centerX = modelInfo.getLeft() + (modelInfo.getRight() - modelInfo.getLeft()) / 2;
            int centerY = modelInfo.getTop() + (modelInfo.getBottom() - modelInfo.getTop()) / 2;
            int radius = (modelInfo.getRight() - modelInfo.getLeft()) / 2;
            if (modelInfo.isSelect()) {
                mRectPaint.setColor(Color.GREEN);
            } else {
                mRectPaint.setColor(Color.RED);
            }
            if (modelInfo.getModelType() == MyModel.MODEL_TYPE_OVAL) {
                canvas.drawCircle(centerX, centerY, radius, mRectPaint);
            } else if (modelInfo.getModelType() == MyModel.MODEL_TYPE_RECTANGLE) {
                Rect rect = new Rect(modelInfo.getLeft(), modelInfo.getTop(), modelInfo.getRight(), modelInfo.getBottom());
                canvas.drawRect(rect, mRectPaint);
            }

        }
        mRectPaint.setColor(Color.RED);

    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //先判断是否点击到模型上面 点击到模型上再处理是点击还是移动模型
        //没有点击到模型上的话 事件不处理
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < MyModel.modelInfos.size(); i++) {
                    ModelInfo modelInfo = MyModel.modelInfos.get(i);
                    if (event.getRawX() > modelInfo.getLeft()
                            && event.getRawX() < modelInfo.getRight()
                            && event.getRawY() > modelInfo.getTop()
                            && event.getRawY() < modelInfo.getBottom()) {
                        isPositionInModel = true;
                        clickModelPosition = i;
//                        lastModelInfo = modelInfo;
                        lastModelInfo = modelInfo.copy();
                    }
                    otherModelInfos.add(modelInfo.copy());
                }
//                otherModelInfos.addAll(MyModel.modelInfos);
                if (otherModelInfos.size() > clickModelPosition)
                    otherModelInfos.remove(clickModelPosition);
                if (isPositionInModel) {
                    clickLastX = (int) event.getRawX();
                    clickLastY = (int) event.getRawY();
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "ACTION_UP: ");
                if (!isPositionInModel) break;
                Log.i(TAG, "isPositionInModel: ");
//                if (Math.abs(event.getRawX() - clickLastX) < 5 && Math.abs(event.getRawY() - clickLastY) < 5 && !isMove) {
                if (!isMove) {
                    Toast.makeText(getContext(), "是点击不是移动", Toast.LENGTH_SHORT).show();
                    MyModel.modelInfos.get(clickModelPosition).setSelect(!MyModel.modelInfos.get(clickModelPosition).isSelect());
                    invalidate();
                } else {
                    Log.i(TAG, "是移动: ");
                    boolean isDouble = false;//是否重叠
                    int clickLeft = MyModel.modelInfos.get(clickModelPosition).getLeft();
                    int clickRight = MyModel.modelInfos.get(clickModelPosition).getRight();
                    int clickTop = MyModel.modelInfos.get(clickModelPosition).getTop();
                    int clickBottom = MyModel.modelInfos.get(clickModelPosition).getBottom();
//                    Log.i(TAG, "clickLeft: " + clickLeft);
//                    Log.i(TAG, "clickTop: " + clickTop);
//                    Log.i(TAG, "clickRight: " + clickRight);
//                    Log.i(TAG, "clickBottom: " + clickBottom);

                    for (ModelInfo modelInfo : otherModelInfos) {
//                        Log.i(TAG, "-------------------------------------: " + otherModelInfos.size());
//                        Log.i(TAG, "modelInfo.getLeft: " + modelInfo.getLeft());
//                        Log.i(TAG, "modelInfo.getTop: " + modelInfo.getTop());
//                        Log.i(TAG, "modelInfo.getRight: " + modelInfo.getRight());
//                        Log.i(TAG, "modelInfo.getBottom: " + modelInfo.getBottom());
//                        Log.i(TAG, "-------------------------------------: " + otherModelInfos.size());
                        isDouble = checkDouble(clickLeft, clickRight, clickTop, clickBottom, isDouble, modelInfo);

                    }

                    if (isDouble) {
                        Toast.makeText(getContext(), "有重叠区域", Toast.LENGTH_SHORT).show();
                        MyModel.modelInfos.remove(clickModelPosition);
                        MyModel.modelInfos.add(lastModelInfo.copy());
                        invalidate();
                    } else {
                        Toast.makeText(getContext(), "已经移动到改位置", Toast.LENGTH_SHORT).show();
                    }
                }
                clickLastX = 0;
                clickLastY = 0;
                isPositionInModel = false;
                clickModelPosition = 0;
                isMove = false;
                otherModelInfos.clear();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_BUTTON_PRESS:
                performClick();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isPositionInModel) break;
                int moveX = (int) (event.getRawX() - clickLastX);
                int moveY = (int) (event.getRawY() - clickLastY);

                int newLeft = MyModel.modelInfos.get(clickModelPosition).getLeft() + moveX;
                int newRight = MyModel.modelInfos.get(clickModelPosition).getRight() + moveX;
                int newTop = MyModel.modelInfos.get(clickModelPosition).getTop() + moveY;
                int newBottom = MyModel.modelInfos.get(clickModelPosition).getBottom() + moveY;
                if (newLeft < left
                        || newRight > right
                        || newTop < top
                        || newBottom > bottom) {
                    Toast.makeText(getContext(), "超出边界", Toast.LENGTH_SHORT).show();
                } else {
                    MyModel.modelInfos.get(clickModelPosition).setLeft(newLeft);
                    MyModel.modelInfos.get(clickModelPosition).setRight(newRight);
                    MyModel.modelInfos.get(clickModelPosition).setTop(newTop);
                    MyModel.modelInfos.get(clickModelPosition).setBottom(newBottom);
                    clickLastX = (int) event.getRawX();
                    clickLastY = (int) event.getRawY();
                    isMove = true;
                    invalidate();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean checkDouble(int left, int right, int top, int bottom, boolean isDouble, ModelInfo modelInfo) {
        if ((left > modelInfo.getLeft() && left < modelInfo.getRight() || right > modelInfo.getLeft() && right < modelInfo.getRight())
                && (top > modelInfo.getTop() && top < modelInfo.getBottom() || bottom > modelInfo.getTop() && bottom < modelInfo.getBottom())) {
            isDouble = true;
        } else if (left <= modelInfo.getLeft() && right >= modelInfo.getRight() && top <= modelInfo.getTop() && bottom >= modelInfo.getBottom()) {
            //覆盖
            isDouble = true;
        } else if ((left <= modelInfo.getLeft() && right >= modelInfo.getRight()) && (top > modelInfo.getTop() && top < modelInfo.getBottom())) {
            isDouble = true;
        } else if ((left <= modelInfo.getLeft() && right >= modelInfo.getRight()) && (bottom > modelInfo.getTop() && bottom < modelInfo.getBottom())) {
            isDouble = true;
        } else if ((top <= modelInfo.getTop() && bottom >= modelInfo.getBottom()) && (left > modelInfo.getLeft() && left < modelInfo.getRight())) {
            isDouble = true;
        } else if ((top <= modelInfo.getTop() && bottom >= modelInfo.getBottom()) && (right > modelInfo.getLeft() && right < modelInfo.getRight())) {
            isDouble = true;
        }
        return isDouble;
    }

}
