package com.yhongm.xdevframe;

import android.content.Context;
import android.os.Looper;

import com.yhongm.xdev_frame_core.custom_agera.Result;
import com.yhongm.xdev_frame_core.custom_eventbus.Subscribe;
import com.yhongm.xdev_frame_core.custom_eventbus.ThreadMode;
import com.yhongm.xdev_frame_core.mvp.presenter.XDevPresenter;

/**
 * Created by yhongm on 2017/04/11.
 */

public class TestPresenter extends XDevPresenter<TestContract.View> implements TestContract.Presenter {
    TestRepository testRepository;

    public TestPresenter(Context context) {
        super(context);

        testRepository = new TestRepository(mContext);
        addRepository(testRepository);//将数据Repository添加到presenter中
    }

    @Override
    public void clickBtn(String content) {
        testRepository.clickBtn(content);
    }

    /**
     * 订阅方法接收respository数据处理返回结果
     * @param testResponse
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(TestRepository testResponse) {
        Looper.prepare();
        Result<String> result = testResponse.getResult();
        if (result.succeeded()) {
            String strResult = result.get();
            mView.response(strResult);
        } else if (result.failed()) {
            Throwable failure = result.getFailure();
            mView.fail(failure.getMessage());
        }
        Looper.loop();
    }
}
