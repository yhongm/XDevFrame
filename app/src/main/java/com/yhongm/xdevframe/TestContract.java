package com.yhongm.xdevframe;

import com.yhongm.xdev_frame_core.mvp.contract.Contract;

/**
 * Created by yhongm on 2017/04/11.
 */

public interface TestContract extends Contract {
    public interface View extends BaseView {
        void response(String content);

        void fail(String errorMsg);
    }

    public interface Presenter extends BasePresenter<View> {

        void clickBtn(String content);
    }
}
