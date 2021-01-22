package com.zc.scenelayout.secens;

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

import com.zc.scenelayout.utils.RxBus;


import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

import static com.zc.scenelayout.secens.ModelInfo.MODEL_TYPE_BOX;
import static com.zc.scenelayout.secens.ModelInfo.MODEL_TYPE_OVAL;
import static com.zc.scenelayout.secens.ModelInfo.MODEL_TYPE_RECTANGLE;

/**
 * @作者 zhouchao
 * @日期 2021/1/20
 * @描述 用于展示场景的view, 可以在场景中布置模型，并提供模型的选择、拖动等事件
 */
public class MyScene extends View {

    public static final String TAG = MyScene.class.getSimpleName();

    //画笔
    private Paint mRectPaint;
    //场景的边界左上右下
    public static int left = 0;
    public static int top = 0;
    public static int right = 0;
    public static int bottom = 0;

    //记录上一次按下的x,y坐标
    private int clickLastX;
    private int clickLastY;

    //按下时是否按在模型上 按在模型上操作模型 按在空白处 可能执行的是框选操作
    boolean isPositionInModel = false;
    //记录点击的模型位置
    private int clickModelPosition;
    //记录上次操作模型的信息 用于拖动超出边界或者与其他模型重叠时 回到初始位置
    private ModelInfo lastModelInfo = new ModelInfo();

    //是否是移动模型
    private boolean isMoveModel = false;

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
        initPosition();

    }

    /**
     * 初始化边界
     */
    private void initPosition() {
        left = getLeft();
        top = getTop();
        right = getRight();
        bottom = getBottom();
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

    /**
     * 添加模型 重新绘制场景及模型
     *
     * @param modelInfo 模型数据
     */
    private void addMyModel(ModelInfo modelInfo) {
        invalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLUE);
        if (MyModel.modelInfos.size() == 0) return;
        drawModel(canvas);
        initPaint();
    }

    /**
     * 绘制模型
     *
     * @param canvas 画布
     */
    private void drawModel(Canvas canvas) {
        for (ModelInfo modelInfo : MyModel.modelInfos) {
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
    private void drawModelBox(Canvas canvas, ModelInfo modelInfo) {
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
    private void drawModelRectangle(Canvas canvas, ModelInfo modelInfo) {
        mRectPaint.setPathEffect(new DashPathEffect(new float[]{10, 0}, 0));
        mRectPaint.setStyle(Paint.Style.FILL);
        Rect rect = new Rect(modelInfo.getLeft(), modelInfo.getTop(), modelInfo.getRight(), modelInfo.getBottom());
        canvas.drawRect(rect, mRectPaint);
    }

    /**
     * 绘制oval类型模型
     *
     * @param canvas    画布
     * @param modelInfo oval模型数据
     */
    private void drawModelOval(Canvas canvas, ModelInfo modelInfo) {
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //先判断是否点击到模型上面 点击到模型上再处理是点击还是移动模型
        //没有点击到模型上的话 事件不处理
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                actionDown(event);
                break;
            case MotionEvent.ACTION_UP:
                actionUp(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_BUTTON_PRESS:
                performClick();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(event);
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 移动操作
     *
     * @param event 事件
     */
    private void actionMove(MotionEvent event) {
        if (!isPositionInModel) {//选中了空白区域 框选 画框
            updateSelectData(event);
        } else {
            moveModel(event);
        }
    }

    /**
     * 移动模型
     *
     * @param event
     */
    private void moveModel(MotionEvent event) {
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
            setModelBoundary(MyModel.modelInfos.get(clickModelPosition), newLeft, newRight, newTop, newBottom);
            clickLastX = (int) event.getRawX();
            clickLastY = (int) event.getRawY();
            isMoveModel = true;
            invalidate();
        }
    }

    /**
     * 手指抬起操作
     *
     * @param event 事件
     */
    private void actionUp(MotionEvent event) {
        if (!isPositionInModel) {//框选
            boxUp();
        } else {
            if (!isMoveModel) {//不是移动模型的 这个操作时点击操作  改变模型选中状态
                Toast.makeText(getContext(), "是点击不是移动", Toast.LENGTH_SHORT).show();
                MyModel.modelInfos.get(clickModelPosition).setSelect(!MyModel.modelInfos.get(clickModelPosition).isSelect());
                invalidate();
            } else {//移动模型操作
                moveUp();
            }
        }
        clickLastX = 0;
        clickLastY = 0;
        isPositionInModel = false;
        clickModelPosition = 0;
        isMoveModel = false;
        otherModelInfos.clear();
    }

    /**
     * 框选手指抬起操作
     * 清除选择框
     */
    private void boxUp() {
        int framePosition = -1;
        for (int i = 0; i < MyModel.modelInfos.size(); i++) {
            ModelInfo modelInfo = MyModel.modelInfos.get(i);
            if (modelInfo.getModelType() == MODEL_TYPE_BOX) {
                framePosition = i;
            }
        }
        //清楚选择框
        if (framePosition != -1) {
            MyModel.modelInfos.remove(framePosition);
            invalidate();
        }
    }

    /**
     * 手指按下操作 记录按下坐标 判断按下位置是否在模型上 如果按在模型上 记录按中模型信息
     *
     * @param event 事件
     */
    private void actionDown(MotionEvent event) {
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();
        for (int i = 0; i < MyModel.modelInfos.size(); i++) {
            ModelInfo modelInfo = MyModel.modelInfos.get(i);
            if (rawX > modelInfo.getLeft()
                    && rawX < modelInfo.getRight()
                    && rawY > modelInfo.getTop()
                    && rawY < modelInfo.getBottom()) {
                isPositionInModel = true;
                clickModelPosition = i;
                lastModelInfo = modelInfo.copy();
            }
            otherModelInfos.add(modelInfo.copy());
        }
        if (otherModelInfos.size() > clickModelPosition)
            otherModelInfos.remove(clickModelPosition);
        clickLastX = (int) event.getRawX();
        clickLastY = (int) event.getRawY();
    }

    /**
     * 设置框选选中的模型 选中的模型状态设置选中
     *
     * @param selectLeft   框选左边界
     * @param selectTop    框选右边界
     * @param selectRight  框选上边界
     * @param selectBottom 框选下边界
     * @param modelInfo    模型数据
     */
    private void setSelectModel(int selectLeft, int selectTop, int selectRight, int selectBottom, ModelInfo modelInfo) {
        if (selectLeft < modelInfo.getLeft()
                && selectRight > modelInfo.getRight()
                && selectTop < modelInfo.getTop()
                && selectBottom > modelInfo.getBottom()) {
            modelInfo.setSelect(true);
        } else {
            modelInfo.setSelect(false);
        }
    }

    /**
     * 框选操作 改变选中模型状态 未选中模型状态置为初始状态
     *
     * @param event 事件
     */
    private void updateSelectData(MotionEvent event) {
        int newX = (int) event.getRawX();
        int newY = (int) event.getRawY();

        int framePosition = -1;
        int selectLeft = (int) Math.min(event.getRawX(), clickLastX);
        int selectRight = (int) Math.max(event.getRawX(), clickLastX);
        int selectTop = (int) Math.min(event.getRawY(), clickLastY);
        int selectBottom = (int) Math.max(event.getRawY(), clickLastY);
        for (int i = 0; i < MyModel.modelInfos.size(); i++) {
            if (MyModel.modelInfos.get(i).getModelType() == MODEL_TYPE_BOX) {
                framePosition = i;
            } else {
                setSelectModel(selectLeft, selectTop, selectRight, selectBottom, MyModel.modelInfos.get(i));
            }
        }
        if (framePosition != -1) {
            ModelInfo modelInfo = MyModel.modelInfos.get(framePosition);
            setModelBoundary(modelInfo, Math.min(newX, clickLastX), Math.max(newX, clickLastX), Math.min(newY, clickLastY), Math.max(newY, clickLastY));
        } else {
            ModelInfo modelInfo = new ModelInfo();
            setModelBoundary(modelInfo, Math.min(newX, clickLastX), Math.max(newX, clickLastX), Math.min(newY, clickLastY), Math.max(newY, clickLastY));
            modelInfo.setModelType(ModelInfo.MODEL_TYPE_BOX);
            MyModel.modelInfos.add(modelInfo);
        }
        invalidate();
    }

    /**
     * 设置模型边界 左上右下
     *
     * @param modelInfo 需要设置的模型
     * @param left      左边界
     * @param right     右边界
     * @param top       上边界
     * @param bottom    下边界
     */
    private void setModelBoundary(ModelInfo modelInfo, int left, int right, int top, int bottom) {
        modelInfo.setLeft(left);
        modelInfo.setRight(right);
        modelInfo.setTop(top);
        modelInfo.setBottom(bottom);
    }

    /**
     * 移动模型手指抬起操作 判断是否重叠 返回到原来位置
     */
    private void moveUp() {
        boolean isDouble = false;//是否重叠
        int clickLeft = MyModel.modelInfos.get(clickModelPosition).getLeft();
        int clickRight = MyModel.modelInfos.get(clickModelPosition).getRight();
        int clickTop = MyModel.modelInfos.get(clickModelPosition).getTop();
        int clickBottom = MyModel.modelInfos.get(clickModelPosition).getBottom();
        for (ModelInfo modelInfo : otherModelInfos) {
            isDouble = checkDouble2(clickLeft, clickRight, clickTop, clickBottom, modelInfo);
            if (isDouble) break;
        }
        if (isDouble) {
            Toast.makeText(getContext(), "有重叠区域", Toast.LENGTH_SHORT).show();
            MyModel.modelInfos.remove(clickModelPosition);
            MyModel.modelInfos.add(lastModelInfo.copy());
            invalidate();
        } else {
            Toast.makeText(getContext(), "已经移动到该位置", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 判断模型移动时是否与其他模型存在重叠
     *
     * @param left      移动模型的左边界
     * @param right     移动模型的右边界
     * @param top       移动模型的上边界
     * @param bottom    移动模型的下边界
     * @param modelInfo 其他模型的数据
     * @return 重叠返回true 未重叠返回false
     */
    private boolean checkDouble2(int left, int right, int top, int bottom, ModelInfo modelInfo) {
        int modelLeft = modelInfo.getLeft();
        int modelRight = modelInfo.getRight();
        int modelTop = modelInfo.getTop();
        int modelBottom = modelInfo.getBottom();
        if ((left > modelLeft && left < modelRight || right > modelLeft && right < modelRight)
                && (top > modelTop && top < modelBottom || bottom > modelTop && bottom < modelBottom)) {
            return true;
        } else if ((modelLeft > left && modelLeft < right || modelRight > left && modelRight < right)
                && (modelTop > top && modelTop < bottom || modelBottom > top && modelBottom < bottom)) {
            //覆盖
            return true;
        }
        return false;
    }

}
