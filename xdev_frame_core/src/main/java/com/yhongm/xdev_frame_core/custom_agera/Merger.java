package com.yhongm.xdev_frame_core.custom_agera;

import android.support.annotation.NonNull;

public interface Merger<TFirst, TSecond, TTo> {

  @NonNull
  TTo merge(@NonNull TFirst first, @NonNull TSecond second);
}
