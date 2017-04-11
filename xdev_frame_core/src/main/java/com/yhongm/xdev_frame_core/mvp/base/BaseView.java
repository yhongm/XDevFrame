package com.yhongm.xdev_frame_core.mvp.base;

/**
 * Created by yhongm on 2017/03/06.
 * 视图基类
 */

public interface BaseView {
    //请求成功
    void requestSuccess();

    //请求失败
    void requestFail(int errorCode);
}
