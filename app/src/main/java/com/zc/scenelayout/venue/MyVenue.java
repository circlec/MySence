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
import java.util.Iterator;

import static com.zc.scenelayout.venue.VenueModelInfo.MODEL_TYPE_BOX;
import static com.zc.scenelayout.venue.VenueModelInfo.MODEL_TYPE_OVAL;
import static com.zc.scenelayout.venue.VenueModelInfo.MODEL_TYPE_RECTANGLE;

/**
 * @作者 zhouchao
 * @日期 2021/1/20
 * @描述 用于展示场景的view, 可以在场景中布置模型，并提供模型的选择、拖动等事件
 */
public class MyVenue extends View {

    private int venueHeight = 0;//场馆高度
    private int venueWidth = 0;//场馆宽度

    private int modelWidth = 200;
    private int modelHeight = 200;

    public static final String TAG = MyVenue.class.getSimpleName();
    private ArrayList<VenueModelInfo> modelInfos = new ArrayList<>();
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

    boolean isPositionInDefaultModel = false;

    //记录点击的模型位置
    private int clickModelPosition = -1;
    //记录上次操作模型的信息 用于拖动超出边界或者与其他模型重叠时 回到初始位置
    private VenueModelInfo lastModelInfo = new VenueModelInfo();

    //是否是移动模型
    private boolean isMoveModel = false;

    //选中模型以外的其他模型 用于判断拖动时是否选中模型是否与其他模型重叠
    public static ArrayList<VenueModelInfo> otherModelInfos = new ArrayList<>();

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
        initToolBox();

    }

    private void initToolBox() {
        VenueModelInfo modelInfo = new VenueModelInfo();
        modelInfo.setModelType(MODEL_TYPE_OVAL);
        modelInfo.setLeft(left);
        modelInfo.setRight(left + modelWidth);
        modelInfo.setBottom(getBottom());
        modelInfo.setTop(getBottom() - modelHeight);
        modelInfo.setDefaultModel(true);

        VenueModelInfo modelInfo2 = new VenueModelInfo();
        modelInfo2.setModelType(MODEL_TYPE_RECTANGLE);
        modelInfo2.setLeft(right - modelWidth);
        modelInfo2.setRight(right);
        modelInfo2.setBottom(getBottom());
        modelInfo2.setTop(getBottom() - modelHeight);
        modelInfo2.setDefaultModel(true);

        modelInfos.add(modelInfo);
        modelInfos.add(modelInfo2);
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

        bottom = venueHeight;

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
        if (modelInfos.size() == 0) return;
        drawModel(canvas);
        initPaint();
    }

    private void drawVenue(Canvas canvas) {
        mRectPaint.setColor(Color.BLUE);
        Rect rect = new Rect(left, top, right, bottom);
        canvas.drawRect(rect, mRectPaint);
    }

    /**
     * 绘制模型
     *
     * @param canvas 画布
     */
    private void drawModel(Canvas canvas) {
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
            case MotionEvent.ACTION_BUTTON_PRESS:
                performClick();
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
        if (!isPositionInModel && (clickLastX > left && clickLastX < right && clickLastY > top && clickLastY < bottom)) {//选中了空白区域 框选 画框
            updateSelectData(event);
        } else if (isPositionInDefaultModel) {//选中模型移动
            moveDefaultModel(event);
        } else if (clickModelPosition != -1) {
            moveModel(event);
        }
    }

    private void moveDefaultModel(MotionEvent event) {
        int moveX = (int) (event.getRawX() - clickLastX);
        int moveY = (int) (event.getRawY() - getStatusBarHeight(getContext()) - clickLastY);
        int newLeft = modelInfos.get(clickModelPosition).getLeft() + moveX;
        int newRight = modelInfos.get(clickModelPosition).getRight() + moveX;
        int newTop = modelInfos.get(clickModelPosition).getTop() + moveY;
        int newBottom = modelInfos.get(clickModelPosition).getBottom() + moveY;
        setModelBoundary(modelInfos.get(clickModelPosition), newLeft, newRight, newTop, newBottom);
        clickLastX = (int) event.getRawX();
        clickLastY = (int) event.getRawY() - getStatusBarHeight(getContext());
        isMoveModel = true;
        invalidate();
    }

    /**
     * 移动模型
     *
     * @param event
     */
    private void moveModel(MotionEvent event) {
        int moveX = (int) (event.getRawX() - clickLastX);
        int moveY = (int) (event.getRawY() - getStatusBarHeight(getContext()) - clickLastY);
        int newLeft = modelInfos.get(clickModelPosition).getLeft() + moveX;
        int newRight = modelInfos.get(clickModelPosition).getRight() + moveX;
        int newTop = modelInfos.get(clickModelPosition).getTop() + moveY;
        int newBottom = modelInfos.get(clickModelPosition).getBottom() + moveY;
        if (newLeft < left
                || newRight > right
                || newTop < top
                || newBottom > bottom) {
            Toast.makeText(getContext(), "超出边界", Toast.LENGTH_SHORT).show();
        } else {
            setModelBoundary(modelInfos.get(clickModelPosition), newLeft, newRight, newTop, newBottom);
            clickLastX = (int) event.getRawX();
            clickLastY = (int) event.getRawY() - getStatusBarHeight(getContext());
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
        } else if (isPositionInDefaultModel) {//选中工具箱中模型鼠标离开处理
            VenueModelInfo venueModelInfo = modelInfos.get(clickModelPosition);
            if (venueModelInfo.getLeft() > left && venueModelInfo.getRight() < right && venueModelInfo.getTop() > top && venueModelInfo.getBottom() < bottom) {
                //移动工具栏中模型到场馆中 向场馆中添加模型
                addModel();
            } else {
                //没有移动到场馆内部，不添加模型 模型回到原有位置
                VenueModelInfo modelInfo = modelInfos.get(clickModelPosition);
                modelInfo.setLeft(lastModelInfo.getLeft());
                modelInfo.setRight(lastModelInfo.getRight());
                modelInfo.setTop(lastModelInfo.getTop());
                modelInfo.setBottom(lastModelInfo.getBottom());
            }
            invalidate();
        } else if (clickModelPosition != -1) {//选中场馆中模型鼠标离开时
            if (!isMoveModel) {//不是移动模型的 这个操作时点击操作  改变模型选中状态
                Toast.makeText(getContext(), "是点击不是移动", Toast.LENGTH_SHORT).show();
                modelInfos.get(clickModelPosition).setSelect(!modelInfos.get(clickModelPosition).isSelect());
                invalidate();
            } else {//移动模型操作
                moveVenveModelUp();
            }
        }
        clickLastX = 0;
        clickLastY = 0;
        isPositionInModel = false;
        clickModelPosition = -1;
        isMoveModel = false;
        otherModelInfos.clear();
    }

    private void addModel() {
        VenueModelInfo clickModel = modelInfos.get(clickModelPosition);
        boolean isDouble = false;
        for (VenueModelInfo modelInfo : otherModelInfos) {
            isDouble = checkDouble2(clickModel.getLeft(), clickModel.getRight(), clickModel.getTop(), clickModel.getBottom(), modelInfo);
            if (isDouble) break;
        }
        if (!isDouble) {
            VenueModelInfo venueModelInfo = lastModelInfo.copy();
            modelInfos.add(venueModelInfo);
            modelInfos.get(clickModelPosition).setDefaultModel(false);
        } else {
            VenueModelInfo modelInfo = modelInfos.get(clickModelPosition);
            modelInfo.setLeft(lastModelInfo.getLeft());
            modelInfo.setRight(lastModelInfo.getRight());
            modelInfo.setTop(lastModelInfo.getTop());
            modelInfo.setBottom(lastModelInfo.getBottom());
            modelInfos.add(modelInfo);
        }


    }

    /**
     * 框选手指抬起操作
     * 清除选择框
     */
    private void boxUp() {
        int framePosition = -1;
        for (int i = 0; i < modelInfos.size(); i++) {
            VenueModelInfo modelInfo = modelInfos.get(i);
            if (modelInfo.getModelType() == MODEL_TYPE_BOX) {
                framePosition = i;
            }
        }
        //清楚选择框
        if (framePosition != -1) {
            modelInfos.remove(framePosition);
            invalidate();
        }
    }

    /**
     * 手指按下操作 记录按下坐标 判断按下位置是否在模型上 如果按在模型上 记录按中模型信息
     *
     * @param event 事件
     */
    private void actionDown(MotionEvent event) {
        isPositionInDefaultModel = false;
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY() - getStatusBarHeight(getContext());//需要减去状态栏高度
        for (int i = 0; i < modelInfos.size(); i++) {
            VenueModelInfo modelInfo = modelInfos.get(i);
            if (rawX > modelInfo.getLeft()
                    && rawX < modelInfo.getRight()
                    && rawY > modelInfo.getTop()
                    && rawY < modelInfo.getBottom()) {
                isPositionInModel = true;
                clickModelPosition = i;
                lastModelInfo = modelInfo.copy();
                if (modelInfo.isDefaultModel()) {
                    isPositionInDefaultModel = true;
                }
            }
            otherModelInfos.add(modelInfo.copy());
        }
        if (otherModelInfos.size() > clickModelPosition && clickModelPosition != -1)
            otherModelInfos.remove(clickModelPosition);
        clickLastX = rawX;
        clickLastY = rawY;
    }

    /**
     * 设置框选选中的模型 选中的模型状态设置选中
     *
     * @param left      框选左边界
     * @param top       框选右边界
     * @param right     框选上边界
     * @param bottom    框选下边界
     * @param modelInfo 模型数据
     */
    private void setSelectModel(int left, int top, int right, int bottom, VenueModelInfo modelInfo) {
        //只有全部选中才判断是选中 暂时不用 用下面的只要有部分选中就算选中
//        if (selectLeft < modelInfo.getLeft()
//                && selectRight > modelInfo.getRight()
//                && selectTop < modelInfo.getTop()
//                && selectBottom > modelInfo.getBottom()) {
//            modelInfo.setSelect(true);
//        } else {
//            modelInfo.setSelect(false);
//        }

        int modelLeft = modelInfo.getLeft();
        int modelRight = modelInfo.getRight();
        int modelTop = modelInfo.getTop();
        int modelBottom = modelInfo.getBottom();
        if ((left > modelLeft && left < modelRight || right > modelLeft && right < modelRight)
                && (top > modelTop && top < modelBottom || bottom > modelTop && bottom < modelBottom)) {//四个点坐标有一个点在模型内部就是重叠
            modelInfo.setSelect(true);
        } else if ((modelLeft > left && modelLeft < right || modelRight > left && modelRight < right)
                && (modelTop > top && modelTop < bottom || modelBottom > top && modelBottom < bottom)) {//覆盖模型或者被模型覆盖的情况
            modelInfo.setSelect(true);
        } else if (modelLeft < left && modelRight > right && modelTop > top && modelBottom < bottom) {//四个点都不在模型内部 但是中间部分区域 重叠到一起
            modelInfo.setSelect(true);
        } else if (modelLeft > left && modelRight < right && modelTop < top && modelBottom > bottom) {//四个点都不在模型内部 但是中间部分区域 重叠到一起
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
        int newY = (int) event.getRawY() - getStatusBarHeight(getContext());

        int framePosition = -1;
        int selectLeft = Math.min(newX, clickLastX);
        int selectRight = Math.max(newX, clickLastX);
        int selectTop = Math.min(newY, clickLastY);
        int selectBottom = Math.max(newY, clickLastY);
        for (int i = 0; i < modelInfos.size(); i++) {
            if (modelInfos.get(i).getModelType() == MODEL_TYPE_BOX) {
                framePosition = i;
            } else {
                setSelectModel(selectLeft, selectTop, selectRight, selectBottom, modelInfos.get(i));
            }
        }
        if (framePosition != -1) {
            VenueModelInfo modelInfo = modelInfos.get(framePosition);
            setModelBoundary(modelInfo, Math.min(newX, clickLastX), Math.max(newX, clickLastX), Math.min(newY, clickLastY), Math.max(newY, clickLastY));
        } else {
            VenueModelInfo modelInfo = new VenueModelInfo();
            setModelBoundary(modelInfo, Math.min(newX, clickLastX), Math.max(newX, clickLastX), Math.min(newY, clickLastY), Math.max(newY, clickLastY));
            modelInfo.setModelType(MODEL_TYPE_BOX);
            modelInfos.add(modelInfo);
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
    private void setModelBoundary(VenueModelInfo modelInfo, int left, int right, int top, int bottom) {
        modelInfo.setLeft(left);
        modelInfo.setRight(right);
        modelInfo.setTop(top);
        modelInfo.setBottom(bottom);
    }

    /**
     * 移动场馆内模型手指抬起操作 判断是否重叠 返回到原来位置
     */
    private void moveVenveModelUp() {
        boolean isDouble = false;//是否重叠
        int clickLeft = modelInfos.get(clickModelPosition).getLeft();
        int clickRight = modelInfos.get(clickModelPosition).getRight();
        int clickTop = modelInfos.get(clickModelPosition).getTop();
        int clickBottom = modelInfos.get(clickModelPosition).getBottom();
        for (VenueModelInfo modelInfo : otherModelInfos) {
            isDouble = checkDouble2(clickLeft, clickRight, clickTop, clickBottom, modelInfo);
            if (isDouble) break;
        }
        if (isDouble) {
            Toast.makeText(getContext(), "有重叠区域", Toast.LENGTH_SHORT).show();
            modelInfos.remove(clickModelPosition);
            modelInfos.add(lastModelInfo.copy());
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
    private boolean checkDouble2(int left, int right, int top, int bottom, VenueModelInfo modelInfo) {
        int modelLeft = modelInfo.getLeft();
        int modelRight = modelInfo.getRight();
        int modelTop = modelInfo.getTop();
        int modelBottom = modelInfo.getBottom();
        if ((left > modelLeft && left < modelRight || right > modelLeft && right < modelRight)
                && (top > modelTop && top < modelBottom || bottom > modelTop && bottom < modelBottom)) {//四个点坐标有一个点在模型内部就是重叠
            return true;
        } else if ((modelLeft > left && modelLeft < right || modelRight > left && modelRight < right)
                && (modelTop > top && modelTop < bottom || modelBottom > top && modelBottom < bottom)) {//覆盖模型或者被模型覆盖的情况
            return true;
        } else if (modelLeft < left && modelRight > right && modelTop > top && modelBottom < bottom) {//四个点都不在模型内部 但是中间部分区域 重叠到一起
            return true;
        } else if (modelLeft > left && modelRight < right && modelTop < top && modelBottom > bottom) {
            return true;
        }
        return false;
    }

    /**
     * 删除选中模型
     */
    public void deleteSelectModel() {
        boolean haveSelectDelete = false;
        Iterator<VenueModelInfo> it = modelInfos.iterator();
        while (it.hasNext()) {
            VenueModelInfo modelInfo = it.next();
            if (modelInfo.isSelect()) {
                it.remove();
                haveSelectDelete = true;
            }
        }
        if (haveSelectDelete)
            invalidate();
    }

    /**
     * 设置选中模型旋转角度
     *
     * @param rotationAngle 旋转角度
     */
    public void setSelectRotate(int rotationAngle) {
        boolean isHaveSelectMode = false;
        for (VenueModelInfo modelInfo : modelInfos) {
            if (modelInfo.isSelect()) {
                modelInfo.setRotationAngle(modelInfo.getRotationAngle() + rotationAngle);
                isHaveSelectMode = true;
            }
        }
        if (isHaveSelectMode) invalidate();
    }

    private int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
