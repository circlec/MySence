package com.zc.scenelayout.venue;

/**
 * @作者 zhouchao
 * @日期 2021/1/26
 * @描述
 */
public class VenueInfo {


    private int width;
    private int height;

    private int left;
    private int top;
    private int right;
    private int bottom;
    private int statusBarHight;

    public VenueInfo(int width, int height, int left, int top, int right, int bottom,int statusBarHight) {
        this.width = width;
        this.height = height;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.statusBarHight = statusBarHight;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
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

    public int getStatusBarHight() {
        return statusBarHight;
    }

    public void setStatusBarHight(int statusBarHight) {
        this.statusBarHight = statusBarHight;
    }
}
