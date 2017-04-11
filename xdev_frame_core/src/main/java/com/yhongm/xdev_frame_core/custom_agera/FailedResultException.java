package com.yhongm.xdev_frame_core.custom_agera;

import android.support.annotation.Nullable;
import android.util.Log;

public final class FailedResultException extends IllegalStateException {

    FailedResultException(@Nullable final Throwable cause) {
        super("Cannot get() from a failed result", cause);

    }

    FailedResultException(String msg) {
        Log.i("FailedResultException", "17:44/FailedResultException:" + msg);// yhongm 2017/03/30 17:44
    }
}
