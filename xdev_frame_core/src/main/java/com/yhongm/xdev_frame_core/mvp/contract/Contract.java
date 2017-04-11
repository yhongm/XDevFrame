package com.yhongm.xdev_frame_core.mvp.contract;

/**
 * Created by yhongm on 2017/04/11.
 */

public interface Contract {
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

    /**
     * Created by yhongm on 2017/03/06.
     * Presenter基类
     */

   public interface BasePresenter<V extends  BaseView> {

        //附加到视图
        void attachView(V mView);

        //脱离视图
        void detachView();

        boolean isAttached();
    }
}
