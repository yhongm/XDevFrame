package com.yhongm.xdev_frame_core.mvp.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.yhongm.xdev_frame_core.custom_agera.Result;


/**
 * Created by yhongm on 2017/03/22.
 *
 * @param <D> 数据
 */
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
