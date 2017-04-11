package com.yhongm.xdev_frame_core.mvp.presenter;

import android.content.Context;

import com.yhongm.xdev_frame_core.custom_eventbus.EventBus;
import com.yhongm.xdev_frame_core.mvp.base.BaseRepository;
import com.yhongm.xdev_frame_core.mvp.contract.Contract;

import java.util.ArrayList;

/**
 * Created by yhongm on 2017/03/17.
 */

public abstract class XDevPresenter<V extends Contract.BaseView> implements Contract.BasePresenter<V> {
    protected V mView;
    protected boolean isAttached = false;
    protected Context mContext;
    ArrayList<BaseRepository> baseRepositorieLists;

    public XDevPresenter(Context context) {
        baseRepositorieLists = new ArrayList<BaseRepository>();
        this.mContext = context;
    }

    /**
     * @param baseRepositories
     */
    public void addRepository(BaseRepository... baseRepositories) {
        for (BaseRepository bdr :
                baseRepositories) {
            this.baseRepositorieLists.add(bdr);
        }
    }


    @Override
    public void attachView(V view) {
        this.mView = view;
        isAttached = true;
        for (BaseRepository baseRepository :
                baseRepositorieLists) {
            baseRepository.attachView();
        }
        checkAddRepositoryIsCall();
        EventBus.getDefault().register(this);
    }


    @Override
    public void detachView() {
        this.mView = null;
        isAttached = false;
        for (BaseRepository baseRepository :
                baseRepositorieLists) {
            baseRepository.detachView();
        }
        EventBus.getDefault().unregister(this);
    }

    /**
     * 检查addRepository方法是否被调用
     */
    private void checkAddRepositoryIsCall() {
        if (baseRepositorieLists.size() == 0) {
            throw new IllegalArgumentException("the addRepository function must call");//判断addRepository方法是否被调用
        }
    }

    @Override
    public boolean isAttached() {
        return isAttached;
    }


}
