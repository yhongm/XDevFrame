package com.yhongm.xdev_frame_core.mvp.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.yhongm.xdev_frame_core.custom_agera.Result;
import com.yhongm.xdev_frame_core.mvp.base.BaseRepository;


/**
 * Created by yhongm on 2017/03/22.
 * 处理操作数据相关的业务逻辑的基类，子类继承本类实现网络操作数据的业务逻辑
 * @param <D> 需要操作的数据类型
 * */
public abstract class Repository<D> extends BaseRepository<D> {
    private int errorCode = 0;

    public Repository(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public Result<D> get() {
        Log.i("Repository", "10:07/get:");// yhongm 2017/04/05 10:07
        D result = null;
        try {
            result = getData();
            if (result != null) {
                return Result.success(result);
            } else {
                return Result.success(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure(e);

        }
    }


    protected abstract D getData() throws Exception;

    protected void headCode(int errorcode) {
        this.errorCode = errorcode;
    }

    public int getErrorCode() {
        return errorCode;
    }


}
