package com.zc.scenelayout.venue;

import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Iterator;

import static com.zc.scenelayout.venue.VenueModelInfo.MODEL_TYPE_BOX;
import static com.zc.scenelayout.venue.VenueModelInfo.MODEL_TYPE_OVAL;
import static com.zc.scenelayout.venue.VenueModelInfo.MODEL_TYPE_RECTANGLE;

/**
 * @作者 zhouchao
 * @日期 2021/1/25
 * @描述
 */
public class Engine {

    public static final String TAG = Engine.class.getSimpleName();

    private boolean isAlign = true;//是否开启对齐

    private Action action;

    private ActionListener actionListener;

    private int statusBarHeight;

    private int venueHeight = 0;//场馆高度
    private int venueWidth = 0;//场馆宽度

    private int modelWidth = 160;
    private int modelHeight = 160;

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

    //按下时是按在工具箱中模型上
    boolean isPositionInToolboxModel = false;

    //记录点击的模型位置
    private int clickModelPosition = -1;
    //记录上次操作模型的信息 用于拖动超出边界或者与其他模型重叠时 回到初始位置
    private VenueModelInfo lastModelInfo = new VenueModelInfo();

    //是否是移动模型
    private boolean isMoveModel = false;

    //选中模型以外的其他模型 用于判断拖动时是否选中模型是否与其他模型重叠
    public static ArrayList<VenueModelInfo> otherModelInfos = new ArrayList<>();


    public void init(VenueInfo venueInfo) {
        venueHeight = venueInfo.getHeight();
        venueWidth = venueInfo.getWidth();
        left = venueInfo.getLeft();
        right = venueInfo.getRight();
        top = venueInfo.getTop();
        bottom = venueInfo.getBottom();
        statusBarHeight = venueInfo.getStatusBarHight();
        initToolBox();
        bottom = venueHeight;
        action = new Action();
    }

    private void initToolBox() {
        VenueModelInfo modelInfo = new VenueModelInfo();
        modelInfo.setModelType(MODEL_TYPE_OVAL);
        modelInfo.setLeft(left);
        modelInfo.setRight(left + modelWidth);
        modelInfo.setBottom(bottom);
        modelInfo.setTop(bottom - modelHeight);
        modelInfo.setDefaultModel(true);

        VenueModelInfo modelInfo2 = new VenueModelInfo();
        modelInfo2.setModelType(MODEL_TYPE_RECTANGLE);
        modelInfo2.setLeft(right - modelWidth);
        modelInfo2.setRight(right);
        modelInfo2.setBottom(bottom);
        modelInfo2.setTop(bottom - modelHeight);
        modelInfo2.setDefaultModel(true);

        modelInfos.add(modelInfo);
        modelInfos.add(modelInfo2);
    }

    public void onTouchEvent(MotionEvent event) {
        //没有点击到模型上的话 事件不处理
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                actionDown(event);
                break;
            case MotionEvent.ACTION_UP:
                actionUp(event);
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(event);
                break;
        }

    }

    /**
     * 移动操作
     *
     * @param event 事件
     */
    private void actionMove(MotionEvent event) {
        if (!isPositionInModel && (clickLastX > left && clickLastX < right && clickLastY > top && clickLastY < bottom)) {//选中了空白区域 框选 画框
            updateSelectData(event);
        } else if (isPositionInToolboxModel) {//选中模型移动
            moveToolBoxtModel(event);
        } else if (clickModelPosition != -1) {
            moveModel(event);
        }
    }

    private void moveToolBoxtModel(MotionEvent event) {
        int moveX = (int) (event.getRawX() - clickLastX);
        int moveY = (int) (event.getRawY() - statusBarHeight) - clickLastY;
        int newLeft = modelInfos.get(clickModelPosition).getLeft() + moveX;
        int newRight = modelInfos.get(clickModelPosition).getRight() + moveX;
        int newTop = modelInfos.get(clickModelPosition).getTop() + moveY;
        int newBottom = modelInfos.get(clickModelPosition).getBottom() + moveY;
        setModelBoundary(modelInfos.get(clickModelPosition), newLeft, newRight, newTop, newBottom);
        clickLastX = (int) event.getRawX();
        clickLastY = (int) event.getRawY() - statusBarHeight;
        isMoveModel = true;
//        invalidate();
//        action.setActitonStatus(Action.ACTION_MOVE_TOOLBOX_MODEL);
//        setActionListener();
        handlerAction(Action.ACTION_MOVE_TOOLBOX_MODEL);
    }

    private void handlerAction(int actionStatus) {
        action.setActitonStatus(actionStatus);
        setActionListener();
    }

    private void setActionListener() {
        switch (action.getActitonStatus()) {
            case Action.ACTION_CREATE:
                actionListener.createModel();
                break;
            case Action.ACTION_MOVE:
                actionListener.moveModel();
                break;
            case Action.ACTION_SELECT_AREA:
                actionListener.selectAreaModel();
                break;
            case Action.ACTION_DELETE_MODEL:
                actionListener.deleteModel();
                break;
            case Action.ACTION_SELECT_SINGLE:
                actionListener.selectSingleModel();
                break;
            case Action.ACTION_MOVE_TOOLBOX_MODEL:
                actionListener.moveToolBoxModel();
                break;
            case Action.ACTION_MOVE_OUT_BOUNDARY:
                actionListener.moveOutBoundary();
                break;
            case Action.ACTION_CREATE_FAIL:
                actionListener.createFail();
                break;
            case Action.ACTION_MOVE_FAIL_DOUBLE:
                actionListener.moveFailDouble();
                break;
            case Action.ACTION_ROTATE:
                actionListener.rotate();
                break;
            case Action.ACTION_CLEAR_SELECT_AREA:
                actionListener.clearSelectArea();
                break;
            case Action.ACTION_ALIGN:
                actionListener.align();
                break;
            case Action.ACTION_ALIGN_FAIL_OUT_BOUNDARY:
                actionListener.alignFailOutBoundary();
                break;

        }
    }

    /**
     * 移动模型
     *
     * @param event
     */
    private void moveModel(MotionEvent event) {
        int moveX = (int) (event.getRawX() - clickLastX);
        int moveY = (int) (event.getRawY() - statusBarHeight) - clickLastY;
        int newLeft = modelInfos.get(clickModelPosition).getLeft() + moveX;
        int newRight = modelInfos.get(clickModelPosition).getRight() + moveX;
        int newTop = modelInfos.get(clickModelPosition).getTop() + moveY;
        int newBottom = modelInfos.get(clickModelPosition).getBottom() + moveY;
        if (newLeft < left
                || newRight > right
                || newTop < top
                || newBottom > bottom) {
//            Toast.makeText(getContext(), "超出边界", Toast.LENGTH_SHORT).show();
            handlerAction(Action.ACTION_MOVE_OUT_BOUNDARY);
        } else {
            setModelBoundary(modelInfos.get(clickModelPosition), newLeft, newRight, newTop, newBottom);
            clickLastX = (int) event.getRawX();
            clickLastY = (int) event.getRawY() - statusBarHeight;
            isMoveModel = true;
//            invalidate();
            handlerAction(Action.ACTION_MOVE);
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
        } else if (isPositionInToolboxModel) {//选中工具箱中模型鼠标离开处理
            VenueModelInfo venueModelInfo = modelInfos.get(clickModelPosition);
            if (venueModelInfo.getLeft() > left && venueModelInfo.getRight() < right && venueModelInfo.getTop() > top && venueModelInfo.getBottom() < bottom) {
                //移动工具栏中模型到场馆中 向场馆中添加模型
                addModel();
                handlerAction(Action.ACTION_CREATE);
            } else {
                //没有移动到场馆内部，不添加模型 模型回到原有位置
                VenueModelInfo modelInfo = modelInfos.get(clickModelPosition);
                modelInfo.setLeft(lastModelInfo.getLeft());
                modelInfo.setRight(lastModelInfo.getRight());
                modelInfo.setTop(lastModelInfo.getTop());
                modelInfo.setBottom(lastModelInfo.getBottom());
                handlerAction(Action.ACTION_CREATE_FAIL);
            }
//            invalidate();
        } else if (clickModelPosition != -1) {//选中场馆中模型鼠标离开时
            if (!isMoveModel) {//不是移动模型的 这个操作时点击操作  改变模型选中状态
//                Toast.makeText(getContext(), "是点击不是移动", Toast.LENGTH_SHORT).show();
                modelInfos.get(clickModelPosition).setSelect(!modelInfos.get(clickModelPosition).isSelect());
//                invalidate();
                handlerAction(Action.ACTION_SELECT_SINGLE);
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

    /**
     * 添加模型
     */
    private void addModel() {
        VenueModelInfo clickModel = modelInfos.get(clickModelPosition);
        boolean isDouble = false;
        for (VenueModelInfo modelInfo : otherModelInfos) {
            isDouble = checkDouble2(clickModel.getLeft(), clickModel.getRight(), clickModel.getTop(), clickModel.getBottom(), modelInfo);
            if (isDouble) break;
        }
        if (!isDouble) {//没有重叠添加模型
            VenueModelInfo venueModelInfo = lastModelInfo.copy();
            modelInfos.add(venueModelInfo);
//            VenueModelInfo venueModelInfoClick = alignLine(modelInfos.get(clickModelPosition));
            VenueModelInfo venueModelInfoClick = modelInfos.get(clickModelPosition);
            venueModelInfoClick.setDefaultModel(false);
            alignModel(venueModelInfoClick);
        } else {//有重叠 模型回到初始位置
            VenueModelInfo modelInfo = modelInfos.get(clickModelPosition);
            modelInfo.setLeft(lastModelInfo.getLeft());
            modelInfo.setRight(lastModelInfo.getRight());
            modelInfo.setTop(lastModelInfo.getTop());
            modelInfo.setBottom(lastModelInfo.getBottom());
        }
    }

    /**
     * 对齐模型
     *
     * @param clickModel 需要对齐的模型
     */
    private void alignModel(VenueModelInfo clickModel) {
        if (!isAlign) return;
        if (otherModelInfos.size() == 0) return;
        int clickModelLeft = clickModel.getLeft();
        int clickModelRight = clickModel.getRight();
        int clickModelTop = clickModel.getTop();
        int clickModelBottom = clickModel.getBottom();

        //先找出左上角最近的模型
        VenueModelInfo nearestModel = null;
        int nearestDistance = 0;
        for (VenueModelInfo modelInfo : otherModelInfos) {
            if (nearestModel == null) {
                nearestModel = modelInfo;
                nearestDistance = (int) (Math.pow(clickModelLeft - modelInfo.getLeft(), 2) + Math.pow(clickModelTop - modelInfo.getTop(), 2));
            } else {
                int distance = (int) (Math.pow(clickModelLeft - modelInfo.getLeft(), 2) + Math.pow(clickModelTop - modelInfo.getTop(), 2));
                if (distance < nearestDistance) {
                    nearestModel = modelInfo;
                    nearestDistance = (int) (Math.pow(clickModelLeft - modelInfo.getLeft(), 2) + Math.pow(clickModelTop - modelInfo.getTop(), 2));
                }
            }
        }

        //如果top或者bottom 在最近模型的 top和bottom之间的话 top对齐  (如果是在最近模型左边 则 right在最近模型left+80px处 如果是在最近模型右边 则left在最近模型right+80x处)
        //如果left或者right 在最近模型的 left和right之间的话 left对齐 (如果是在最近模型上边 则 bottom在最近模型top+80px处 如果是在最近模型下边 则top在最近模型bottom+80px处)
        if (nearestModel == null) return;

        VenueModelInfo alginModelInfo = clickModel.copy();
        if ((clickModelTop > nearestModel.getTop() && clickModelTop < nearestModel.getBottom())
                || (clickModelBottom > nearestModel.getTop() && clickModelBottom < nearestModel.getBottom())) {
            //top对齐
            int offsetY = nearestModel.getTop() - alginModelInfo.getTop();
            alginModelInfo.setTop(alginModelInfo.getTop() + offsetY);
            alginModelInfo.setBottom(alginModelInfo.getBottom() + offsetY);
            if (clickModelLeft > nearestModel.getRight()) {
                //如果是在最近模型右边 则left在最近模型right+80px处
                int offsetX = nearestModel.getRight() + 80 - alginModelInfo.getLeft();
                alginModelInfo.setLeft(alginModelInfo.getLeft() + offsetX);
                alginModelInfo.setRight(alginModelInfo.getRight() + offsetX);
            } else if (clickModelRight < nearestModel.getLeft()) {
                //如果是在最近模型左边 则 right在最近模型left+20px处
                int offsetX = nearestModel.getLeft() - 80 - alginModelInfo.getRight();
                alginModelInfo.setLeft(alginModelInfo.getLeft() + offsetX);
                alginModelInfo.setRight(alginModelInfo.getRight() + offsetX);
            }

        } else if ((clickModelLeft > nearestModel.getLeft() && clickModelLeft < nearestModel.getRight())
                || (clickModelRight > nearestModel.getLeft() && clickModelRight < nearestModel.getRight())) {
            //left 对齐
            int offsetX = nearestModel.getLeft() - alginModelInfo.getLeft();
            alginModelInfo.setLeft(alginModelInfo.getLeft() + offsetX);
            alginModelInfo.setRight(alginModelInfo.getRight() + offsetX);
            if (clickModelTop > nearestModel.getBottom()) {
                //如果是在最近模型上边 则 bottom在最近模型top+80px处
                int offsetY = nearestModel.getBottom() + 80 - alginModelInfo.getTop();
                alginModelInfo.setTop(alginModelInfo.getTop() + offsetY);
                alginModelInfo.setBottom(alginModelInfo.getBottom() + offsetY);
            } else if (clickModelBottom < nearestModel.getTop()) {
                //如果是在最近模型下边 则top在最近模型bottom+80px处
                int offsetY = nearestModel.getTop() - 80 - alginModelInfo.getBottom();
                alginModelInfo.setTop(alginModelInfo.getTop() + offsetY);
                alginModelInfo.setBottom(alginModelInfo.getBottom() + offsetY);
            }
        }

        //检测对齐后的模型是否与其他模型重叠
        boolean isDouble = false;
        for (VenueModelInfo modelInfo : otherModelInfos) {
            isDouble = checkDouble2(modelInfo.getLeft(), modelInfo.getRight(), modelInfo.getTop(), modelInfo.getBottom(), alginModelInfo);
        }
        if (!isDouble) {
//            clickModel.setLeft(alginModelInfo.getLeft());
//            clickModel.setRight(alginModelInfo.getRight());
//            clickModel.setTop(alginModelInfo.getTop());
//            clickModel.setBottom(alginModelInfo.getBottom());
//            handlerAction(Action.ACTION_ALIGN);
            //检测边界
            int newLeft = alginModelInfo.getLeft();
            int newRight = alginModelInfo.getRight();
            int newTop = alginModelInfo.getTop();
            int newBottom = alginModelInfo.getBottom();
            if (newLeft >= left
                    && newRight <= right
                    && newTop >= top
                    && newBottom <= bottom) {
//            Toast.makeText(getContext(), "未超出边界，对齐模型", Toast.LENGTH_SHORT).show();
                clickModel.setLeft(alginModelInfo.getLeft());
                clickModel.setRight(alginModelInfo.getRight());
                clickModel.setTop(alginModelInfo.getTop());
                clickModel.setBottom(alginModelInfo.getBottom());
                handlerAction(Action.ACTION_ALIGN);
            } else {
                handlerAction(Action.ACTION_ALIGN_FAIL_OUT_BOUNDARY);
            }
        }


    }

    /**
     * 移动后放开时 模型与网格线对其
     *
     * @param venueModelInfoClick 需要对其的模型
     * @return 对其后的模型
     */
    private VenueModelInfo alignLine(VenueModelInfo venueModelInfoClick) {
        int left = venueModelInfoClick.getLeft();
        int top = venueModelInfoClick.getTop();
        int newLeft = left - left % 80;
        int newRight = venueModelInfoClick.getRight() - left % 80;
        if (left % 80 != 0) {
            venueModelInfoClick.setLeft(Math.max(newLeft, 0));
            venueModelInfoClick.setRight(newRight);
        }
        int newTop = top - top % 80;
        int newBottom = venueModelInfoClick.getBottom() - top % 80;
        if (top % 80 != 0) {
            venueModelInfoClick.setTop(Math.max(newTop, 0));
            venueModelInfoClick.setBottom(newBottom);
        }

        //对齐后再检测是否重叠 如果重叠 需要调整对齐位置
        return venueModelInfoClick;
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
//            invalidate();
            handlerAction(Action.ACTION_CLEAR_SELECT_AREA);
        }
    }

    /**
     * 手指按下操作 记录按下坐标 判断按下位置是否在模型上 如果按在模型上 记录按中模型信息
     *
     * @param event 事件
     */
    private void actionDown(MotionEvent event) {
        action.setActitonStatus(Action.ACTION_DEFAULT);
        isPositionInToolboxModel = false;
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY() - statusBarHeight;//需要减去状态栏高度
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
                    isPositionInToolboxModel = true;
                }
            }
            otherModelInfos.add(modelInfo.copy());

        }
        if (otherModelInfos.size() > clickModelPosition && clickModelPosition != -1) {
            otherModelInfos.remove(clickModelPosition);
        }
        Iterator<VenueModelInfo> it = otherModelInfos.iterator();
        while (it.hasNext()) {
            VenueModelInfo modelInfo = it.next();
            if (modelInfo.isDefaultModel()) {
                it.remove();
            }
        }
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
        int newY = (int) event.getRawY() - statusBarHeight;

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
//        invalidate();
        handlerAction(Action.ACTION_SELECT_AREA);
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
        VenueModelInfo venueModelInfo = modelInfos.get(clickModelPosition);
        int clickLeft = venueModelInfo.getLeft();
        int clickRight = venueModelInfo.getRight();
        int clickTop = venueModelInfo.getTop();
        int clickBottom = venueModelInfo.getBottom();
        for (VenueModelInfo modelInfo : otherModelInfos) {
            isDouble = checkDouble2(clickLeft, clickRight, clickTop, clickBottom, modelInfo);
            if (isDouble) break;
        }
        if (isDouble) {
//            Toast.makeText(getContext(), "有重叠区域", Toast.LENGTH_SHORT).show();
            modelInfos.remove(clickModelPosition);
            modelInfos.add(lastModelInfo.copy());
//            invalidate();
            handlerAction(Action.ACTION_MOVE_FAIL_DOUBLE);
        } else {
            alignModel(venueModelInfo);
        }
//        else {
//            alignLine(venueModelInfo);
//            handlerAction(Action.ACTION_MOVE);
//        }
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
        } else if (modelLeft > left && modelRight < right && modelTop < top && modelBottom > bottom) {//四个点都不在模型内部 但是中间部分区域 重叠到一起
            return true;
        } else if (modelLeft == left && modelRight == right && modelTop == top && modelBottom == bottom) {//完全重叠
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
        if (haveSelectDelete) {
            handlerAction(Action.ACTION_DELETE_MODEL);
        }
//            invalidate();
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
        if (isHaveSelectMode) {
            handlerAction(Action.ACTION_ROTATE);
        }
//        invalidate();
    }

    public ArrayList<VenueModelInfo> getModelInfos() {
        return modelInfos;
    }

    public void addOnActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setAlign(boolean isAlign) {
        this.isAlign = isAlign;
    }
}
