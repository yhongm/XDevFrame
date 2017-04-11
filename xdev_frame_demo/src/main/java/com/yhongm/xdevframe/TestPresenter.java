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
        addRepository(testRepository);
    }

    @Override
    public void clickBtn(String content) {
        testRepository.clickBtn(content);
    }

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
