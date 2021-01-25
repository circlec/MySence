package com.zc.scenelayout.venue;

/**
 * @作者 zhouchao
 * @日期 2021/1/25
 * @描述
 */
public class VenueModelInfo {

    public static final int MODEL_TYPE_OVAL = 0;
    public static final int MODEL_TYPE_RECTANGLE = 1;
    public static final int MODEL_TYPE_BOX = 2;
    private int id;
    private int left;
    private int top;
    private int right;
    private int bottom;
    private boolean isSelect;

    private int modelType;

    private int rotationAngle;//旋转角度

    private boolean isDefaultModel;//是工具栏中的模型

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public int getModelType() {
        return modelType;
    }

    public void setModelType(int modelType) {
        this.modelType = modelType;
    }

    public int getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(int rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public boolean isDefaultModel() {
        return isDefaultModel;
    }

    public void setDefaultModel(boolean defaultModel) {
        isDefaultModel = defaultModel;
    }

    public VenueModelInfo copy() {
        VenueModelInfo modelInfo = new VenueModelInfo();
        modelInfo.setLeft(left);
        modelInfo.setRight(right);
        modelInfo.setTop(top);
        modelInfo.setBottom(bottom);
        modelInfo.setId(id);
        modelInfo.setSelect(isSelect);
        modelInfo.setModelType(modelType);
        modelInfo.setRotationAngle(rotationAngle);
        modelInfo.setDefaultModel(isDefaultModel);
        return modelInfo;
    }
}
