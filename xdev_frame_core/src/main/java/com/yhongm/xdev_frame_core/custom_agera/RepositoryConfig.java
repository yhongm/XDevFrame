package com.yhongm.xdev_frame_core.custom_agera;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef(flag = true, value = {
    RepositoryConfig.CONTINUE_FLOW,
    RepositoryConfig.CANCEL_FLOW,
    RepositoryConfig.RESET_TO_INITIAL_VALUE,
    RepositoryConfig.SEND_INTERRUPT,
})
public @interface RepositoryConfig {

  int CONTINUE_FLOW = 0;

  int CANCEL_FLOW = 1;

  int RESET_TO_INITIAL_VALUE = 2 | CANCEL_FLOW;

  int SEND_INTERRUPT = 4 | CANCEL_FLOW;
}
