package com.yhongm.xdev_frame_core.custom_eventbus.meta;


import com.yhongm.xdev_frame_core.custom_eventbus.SubscriberMethod;

/** Base class for generated index classes created by annotation processing. */
public interface SubscriberInfo {
    Class<?> getSubscriberClass();

    SubscriberMethod[] getSubscriberMethods();

    SubscriberInfo getSuperSubscriberInfo();

    boolean shouldCheckSuperclass();
}
