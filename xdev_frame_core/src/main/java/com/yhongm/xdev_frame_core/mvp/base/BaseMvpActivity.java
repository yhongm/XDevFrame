package com.yhongm.xdev_frame_core.mvp.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.yhongm.xdev_frame_core.mvp.contract.Contract;

/**
 * Created by yhongm on 2017/03/06.
 * mvp activity基类
 */
public abstract class BaseMvpActivity<V extends Contract.BaseView, T extends Contract.BasePresenter<V>> extends FragmentActivity {
    public T mPresenter;
    V mView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = instancePresenter();

        if (mPresenter != null) {
            mPresenter.attachView((V) this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter.isAttached()) {
            mPresenter.detachView();
        }
    }

    /**
     * 实例一个Presenter
     *
     * @return
     */
    public abstract T instancePresenter();
}
