package com.zc.scenelayout.venue;

/**
 * @作者 zhouchao
 * @日期 2021/1/26
 * @描述
 */
public class Action {

    public static final int ACTION_DEFAULT = 0;

    public static final int ACTION_CREATE = 1;//创建模型
    public static final int ACTION_MOVE = 2;//移动模型
    public static final int ACTION_SELECT_AREA = 3;//框选
    public static final int ACTION_DELETE_MODEL = 4;//删除模型
    public static final int ACTION_SELECT_SINGLE = 5;//点击选中模型
    public static final int ACTION_MOVE_TOOLBOX_MODEL = 6;//移动工具栏中模型
    public static final int ACTION_MOVE_OUT_BOUNDARY = 7;//移动超出边界
    public static final int ACTION_CREATE_FAIL = 8;//创建失败
    public static final int ACTION_MOVE_FAIL_DOUBLE = 9;//有重叠区域，移动模型失败
    public static final int ACTION_ROTATE = 10;//旋转选中模型
    public static final int ACTION_CLEAR_SELECT_AREA = 11;//清除选中框
    public static final int ACTION_ALIGN = 12;//对齐模型

    private int actitonStatus;

    public int getActitonStatus() {
        return actitonStatus;
    }

    public void setActitonStatus(int actitonStatus) {
        this.actitonStatus = actitonStatus;
    }
}
