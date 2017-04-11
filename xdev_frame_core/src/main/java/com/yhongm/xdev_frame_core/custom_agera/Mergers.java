package com.yhongm.xdev_frame_core.custom_agera;

import android.support.annotation.NonNull;
import android.util.Log;

public final class Mergers {

    private static final ObjectsUnequalMerger OBJECTS_UNEQUAL_MERGER = new ObjectsUnequalMerger();

    @NonNull
    public static Merger<Object, Object, Boolean> objectsUnequal() {
        return OBJECTS_UNEQUAL_MERGER;
    }

    private static final class ObjectsUnequalMerger implements Merger<Object, Object, Boolean> {
        @NonNull
        @Override
        public Boolean merge(@NonNull final Object oldValue, @NonNull final Object newValue) {
            boolean isEqual = !oldValue.equals(newValue);
            if (oldValue instanceof Result) {
                return true;
            }
            Log.i("ObjectsUnequalMerger", "9:44/merge:" + isEqual + ",oldValue:" + oldValue.getClass().getName() + ",oldToString:" + oldValue.toString() + ",newToString:" + newValue.toString());// yhongm 2017/03/30 9:44
            return isEqual;
        }
    }

    private Mergers() {
    }
}
