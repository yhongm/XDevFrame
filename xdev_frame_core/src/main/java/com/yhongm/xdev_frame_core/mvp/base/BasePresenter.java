package com.yhongm.xdev_frame_core.mvp.base;

/**
 * Created by yhongm on 2017/03/06.
 * Presenter基类
 */

public interface BasePresenter<V extends BaseView> {

    //附加到视图
    void attachView(V mView);

    //脱离视图
    void detachView();

    boolean isAttached();
}
