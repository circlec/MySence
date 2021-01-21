package com.zc.scenelayout.secens;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.zc.scenelayout.R;
import com.zc.scenelayout.utils.RxBus;

import java.util.ArrayList;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;

/**
 * @作者 zhouchao
 * @日期 2021/1/20
 * @描述
 */
public class MyModel extends View {
    public static final int MODEL_TYPE_OVAL = 0;
    public static final int MODEL_TYPE_RECTANGLE = 1;

    public static final String TAG = MyModel.class.getSimpleName();
    public static ArrayList<ModelInfo> modelInfos = new ArrayList<>();
    private Paint mRectPaint;

    private int lastY;
    private int lastX;
    private ModelInfo modelInfo;

    private int modelType = 0;

    public MyModel(Context context) {
        super(context);
        init();
    }


    public MyModel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initValue(context, attrs);
        init();
    }


    public MyModel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initValue(context, attrs);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyModel(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initValue(context, attrs);
        init();
    }

    private void initValue(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyModel);
        modelType = typedArray.getInt(R.styleable.MyModel_model_type, MODEL_TYPE_OVAL);
        typedArray.recycle();
    }

    private void init() {
        mRectPaint = new Paint();
        //设置画笔颜色
        mRectPaint.setColor(Color.RED);
        mRectPaint.setStrokeWidth(3);
        //设置画笔的样式
        mRectPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (modelType == MODEL_TYPE_OVAL) {
            canvas.drawColor(Color.GREEN);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, mRectPaint);
        } else if (modelType == MODEL_TYPE_RECTANGLE) {
            canvas.drawColor(Color.GREEN);
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                lastY = (int) event.getRawY();
                lastX = (int) event.getRawX();
                break;
            case MotionEvent.ACTION_UP:
                int left = getLeft();
                int top = getTop();
                int right = getRight();
                int bottom = getBottom();
                if (left < MyScene.left
                        || right > MyScene.right
                        || top < MyScene.top
                        || bottom > MyScene.bottom) {
                    Toast.makeText(getContext(), "超出边界", Toast.LENGTH_SHORT).show();
                } else {
                    //检测是否与之前的重叠
                    if (modelInfos.size() != 0) {
                        boolean isDouble = false;
                        for (ModelInfo modelInfo : modelInfos) {
                            isDouble = checkDouble(left, right, top, bottom, isDouble, modelInfo);
                        }
                        if (isDouble) {
                            Toast.makeText(getContext(), "有重叠区域", Toast.LENGTH_SHORT).show();
                        } else {
                            addModel();
                        }
                    } else {
                        addModel();
                    }

                }
                RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) getLayoutParams();
                layoutParams1.topMargin = 0;
                layoutParams1.leftMargin = 0;
                layoutParams1.addRule(ALIGN_PARENT_BOTTOM);
                if (modelType == MODEL_TYPE_RECTANGLE) {
                    layoutParams1.addRule(ALIGN_PARENT_RIGHT);
                }
                setLayoutParams(layoutParams1);
                lastX = 0;
                lastY = 0;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_BUTTON_PRESS:
                performClick();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                int dy = (int) event.getRawY() - lastY;
                int topMargin = getTop() + dy;
                int dx = (int) (event.getRawX() - lastX);
                int leftMargin = getLeft() + dx;
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
                layoutParams.topMargin = topMargin;
                layoutParams.leftMargin = leftMargin;
                layoutParams.removeRule(ALIGN_PARENT_BOTTOM);
                layoutParams.removeRule(ALIGN_PARENT_RIGHT);
                setLayoutParams(layoutParams);
                lastY = (int) event.getRawY();
                lastX = (int) event.getRawX();

//                Log.i(TAG, "getLeft: " + getLeft());
//                Log.i(TAG, "getTop: " + getTop());
//                Log.i(TAG, "getRight: " + getRight());
//                Log.i(TAG, "getBottom: " + getBottom());
//
//                Log.i(TAG, "MyScene.left: " + MyScene.left);
//                Log.i(TAG, "MyScene.top: " + MyScene.top);
//                Log.i(TAG, "MyScene.right: " + MyScene.right);
//                Log.i(TAG, "MyScene.bottom: " + MyScene.bottom);

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

    private void addModel() {
        Toast.makeText(getContext(), "通知场景，添加此模型", Toast.LENGTH_SHORT).show();
        modelInfo = new ModelInfo();
        modelInfo.setLeft(getLeft());
        modelInfo.setTop(getTop());
        modelInfo.setRight(getRight());
        modelInfo.setBottom(getBottom());
        // 2021/1/20 场景添加模型 更新界面
        modelInfo.setModelType(modelType);
        modelInfos.add(modelInfo);
        RxBus.getInstance().post(modelInfo);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }
}
