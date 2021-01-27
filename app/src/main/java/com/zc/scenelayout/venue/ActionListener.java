package com.zc.scenelayout.venue;

/**
 * @作者 zhouchao
 * @日期 2021/1/26
 * @描述
 */
public interface ActionListener {

    void createModel();

    void moveModel();

    void selectAreaModel();

    void deleteModel();

    void selectSingleModel();

    void moveToolBoxModel();

    void moveOutBoundary();

    void createFail();

    void moveFailDouble();

    void rotate();

    void clearSelectArea();

    void align();
}
