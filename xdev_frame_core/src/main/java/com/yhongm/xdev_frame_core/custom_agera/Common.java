package com.yhongm.xdev_frame_core.custom_agera;

import android.support.annotation.NonNull;


final class Common {
    static final NullOperator NULL_OPERATOR = new NullOperator();
    private static final class NullOperator implements Receiver {
        @Override
        public void accept(@NonNull final Object value) {
        }

    }
    private Common() {
    }
}
