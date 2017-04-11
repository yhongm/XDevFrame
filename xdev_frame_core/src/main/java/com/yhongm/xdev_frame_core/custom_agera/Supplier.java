package com.yhongm.xdev_frame_core.custom_agera;

import android.support.annotation.NonNull;

public interface Supplier<T> {

  @NonNull
  T get();
}
