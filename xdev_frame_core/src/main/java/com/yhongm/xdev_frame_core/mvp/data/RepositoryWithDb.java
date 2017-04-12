package com.yhongm.xdev_frame_core.mvp.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.yhongm.xdev_frame_core.custom_agera.Result;
import com.yhongm.xdev_frame_core.mvp.base.BaseRepository;

import java.util.ArrayList;

/**
 * Created by yhongm on 2017/03/31.
 *  处理操作数据相关的业务逻辑的基类，子类继承本类实现包含网络和数据库操作数据的业务逻辑
 * @param <ResultItemBean> 数据从网络请求返回的结果类型
 * @param <DbEntity>       数据从数据库返回的结果类型
 */
public abstract class RepositoryWithDb<ResultItemBean, DbEntity> extends BaseRepository<ArrayList<DbEntity>> {
    public RepositoryWithDb(Context context) {
        super(context);
    }

    /**
     * 将服务器需要获取的bean转为插入数据库需要的entity
     * 也可以插入将额外数据插入到关联表
     *
     * @param resultItemBean 需要转换的bean
     * @return 插入数据库的entrity
     */
    protected abstract DbEntity changeAndInsertOther(ResultItemBean resultItemBean);

    @NonNull
    @Override
    public Result<ArrayList<DbEntity>> get() {
        Log.i("RepositoryWithDb", "10:07/get:");// yhongm 2017/04/05 10:07
        ArrayList<DbEntity> result = null;
        handleData();
        try {
            result = getDataFromDb();
            return Result.success(result);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }


    private void handleData() {
        //数据持久化不在这里处理，暂时放在这里
        try {
            ArrayList<ResultItemBean> resultItems = getDataFromNet();
            ArrayList<DbEntity> entities = new ArrayList<DbEntity>();
            for (ResultItemBean resultItem :
                    resultItems) {

                DbEntity dbEntity = changeAndInsertOther(resultItem);
                entities.add(dbEntity);
            }
            insertDataToDb(entities);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * 插入数据库
     *
     * @param dbEntitys
     * @return
     */
    protected abstract boolean insertDataToDb(ArrayList<DbEntity> dbEntitys);

    /**
     * 从网络获取数据
     *
     * @return
     * @throws Exception
     */
    protected abstract ArrayList<ResultItemBean> getDataFromNet() throws Exception;

    /**
     * 从数据库获取数据
     *
     * @return
     * @throws Exception
     */
    protected abstract ArrayList<DbEntity> getDataFromDb() throws Exception;
}
