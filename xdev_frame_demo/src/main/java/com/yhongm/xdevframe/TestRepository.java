package com.yhongm.xdevframe;

import android.content.Context;
import android.util.Log;

import com.yhongm.xdev_frame_core.mvp.data.Repository;

/**
 * Created by yhongm on 2017/04/11.
 */

public class TestRepository extends Repository<String> {
    private String content;

    public TestRepository(Context context) {
        super(context);
    }

    @Override
    protected String getData() throws Exception {
        //实现本方法实现数据相关的业务逻辑返回数据结果
        Log.i("TestRepository", "11:44/getData:time:" + System.currentTimeMillis());// yhongm 2017/04/11 11:44
        return "result:" + content;
    }

    public void clickBtn(String content) {
        this.content = content;
        dispatchUpdate();//调用此方法更新数据操作
    }
}
