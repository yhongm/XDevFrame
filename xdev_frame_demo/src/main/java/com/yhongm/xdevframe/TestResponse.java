package com.yhongm.xdevframe;

import android.content.Context;
import android.util.Log;

import com.yhongm.xdev_frame_core.mvp.base.Repository;

/**
 * Created by yhongm on 2017/04/11.
 */

public class TestResponse extends Repository<String> {
    private String content;

    public TestResponse(Context context) {
        super(context);
    }

    @Override
    protected String getData() throws Exception {
        Log.i("TestResponse", "11:44/getData:time:" + System.currentTimeMillis());// yhongm 2017/04/11 11:44
        return "result:" + content;
    }

    @Override
    protected String initEntity() {
        return null;
    }

    public void clickBtn(String content) {
        this.content = content;
        dispatchUpdate();
    }
}
