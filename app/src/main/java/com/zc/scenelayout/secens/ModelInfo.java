package com.zc.scenelayout.secens;

/**
 * @作者 zhouchao
 * @日期 2021/1/20
 * @描述
 */
public class ModelInfo {
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

    public ModelInfo copy() {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setLeft(left);
        modelInfo.setRight(right);
        modelInfo.setTop(top);
        modelInfo.setBottom(bottom);
        modelInfo.setId(id);
        modelInfo.setSelect(isSelect);
        modelInfo.setModelType(modelType);
        modelInfo.setRotationAngle(rotationAngle);
        return modelInfo;
    }
}
