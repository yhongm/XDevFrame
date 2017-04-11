package com.yhongm.xdevframe;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yhongm.xdev_frame_core.mvp.base.BaseMvpActivity;

/**
 * Created by yhongm on 2017/04/11.
 */

public class TestActivity extends BaseMvpActivity<TestContract.View, TestContract.Presenter> implements TestContract.View {
    View inflateView;
    EditText mEditText;
    Button mBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateView = View.inflate(this, R.layout.activity_test, null);
        setContentView(inflateView);
        mEditText = (EditText) findViewById(R.id.input);
        mBtn = (Button) findViewById(R.id.btn);
        mBtn.setOnClickListener(v -> mPresenter.clickBtn(mEditText.getText().toString().trim()));
    }

    @Override
    public TestContract.Presenter instancePresenter() {
        return new TestPresenter(this);
    }


    @Override
    public void requestSuccess() {

    }

    @Override
    public void requestFail(int errorCode) {

    }

    @Override
    public void response(String content) {
        Snackbar.make(inflateView, content, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void fail(String errorMsg) {
        Snackbar.make(inflateView, errorMsg, Snackbar.LENGTH_SHORT).show();
    }
}
